/*
 * Copyright 2018 Adrian Siekierka, MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.kallisti.oc;

import org.terasology.jnlua.LuaState;
import org.terasology.kallisti.base.interfaces.Persistable;
import org.terasology.kallisti.base.util.PersistenceException;

import java.io.*;
import java.util.*;

public class OCPersistenceAPI implements Persistable {
	private static final int CURRENT_VERSION = 0x01;

	private final MachineOpenComputers machine;
	private String persistenceKey;

	OCPersistenceAPI(MachineOpenComputers machine, String persistenceKey) {
		this.machine = machine;
		this.persistenceKey = persistenceKey;
	}

	void initialize() {
		LuaState state = machine.getLuaState();
		state.newTable();
		state.newTable();

		int perms = state.getTop() - 1;
		int uperms = state.getTop();

		state.pushString("_G");
		state.getGlobal("_G");
		try {
			flattenAndStore(state, perms, uperms);
		} catch (PersistenceException e) {
			throw new RuntimeException(e);
		}
		state.setField(state.REGISTRYINDEX, "uperms");
		state.setField(state.REGISTRYINDEX, "perms");

		configure();
	}

	// perms = lua.getTop() - 1, uperms = lua.getTop()
	private static void flattenAndStore(LuaState state, int perms, int uperms) throws PersistenceException {
		/* ... k v */
		// We only care for tables and functions, any value types are safe.
		if (state.isFunction(-1) || state.isTable(-1)) {
			state.pushValue(-2); /* ... k v k */
			state.getTable(uperms); /* ... k v uperms[k] */
			if (!state.isNil(-1)) {
				throw new PersistenceException("duplicate permanent value named " + state.toString(-3));
			}
			state.pop(1); /* ... k v */
			// If we have aliases its enough to store the value once.
			state.pushValue(-1); /* ... k v v */
			state.getTable(perms); /* ... k v perms[v] */
			boolean isNew = state.isNil(-1);
			state.pop(1); /* ... k v */
			if (isNew) {
				state.pushValue(-1); /* ... k v v */
				state.pushValue(-3); /* ... k v v k */
				state.rawSet(perms); /* ... k v ; perms[v] = k */
				state.pushValue(-2); /* ... k v k */
				state.pushValue(-2); /* ... k v k v */
				state.rawSet(uperms); /* ... k v ; uperms[k] = v */
				// Recurse into tables.
				if (state.isTable(-1)) {
					// Enforce a deterministic order when determining the keys, to ensure
					// the keys are the same when unpersisting again.
					String key = state.toString(-2);
					List<String> childKeys = new ArrayList<>();
					state.pushNil(); /* ... k v nil */
					while (state.next(-2)) {
						/* ... k v ck cv */
						state.pop(1); /* ... k v ck */
						childKeys.add(state.toString(-1));
					}
					/* ... k v */
					childKeys.sort(Comparator.reverseOrder());
					for (String childKey : childKeys) {
						state.pushString(key + "." + childKey); /* ... k v ck */
						state.getField(-2, childKey); /* ... k v ck cv */
						flattenAndStore(state, perms, uperms); /* ... k v */
					}
					/* ... k v */
				}
				/* ... k v */
			}
			/* ... k v */
		}
		state.pop(2); /* ... */
	}

	void configure() {
		LuaState state = machine.getLuaState();

		// Configure Eris
		state.getGlobal("eris");
		state.getField(-1, "settings");
		state.pushString("spkey");
		state.pushString(persistenceKey);
		state.call(2, 0);
		state.pop(1);

		// Add persistKey()
		state.pushJavaFunction((s) -> {
			s.pushString(this.persistenceKey);
			return 1;
		});
		state.setGlobal("persistKey");
	}

	private byte[] persistState(int index) throws PersistenceException {
		LuaState state = machine.getLuaState();
		try {
			state.gc(LuaState.GcAction.STOP, 0);
			state.getGlobal("eris");
			state.getField(-1, "persist");
			if (state.isFunction(-1)) {
				state.getField(state.REGISTRYINDEX, "perms");
				state.pushValue(index);
				try {
					state.call(2, 1);
				} catch (Throwable e) {
					throw new PersistenceException(e);
				}
				if (state.isString(-1)) {
					byte[] result = state.toByteArray(-1);
					state.pop(2);
					return result;
				}
			}
			state.pop(2);
		} finally {
			state.gc(LuaState.GcAction.RESTART, 0);
		}
		return null;
	}

	private boolean unpersistState(byte[] data) throws PersistenceException {
		LuaState state = machine.getLuaState();
		try {
			state.gc(LuaState.GcAction.STOP, 0);
			state.getGlobal("eris");
			state.getField(-1, "unpersist");
			if (state.isFunction(-1)) {
				state.getField(state.REGISTRYINDEX, "uperms");
				state.pushByteArray(data);
				try {
					state.call(2, 1);
				} catch (Throwable e) {
					throw new PersistenceException(e);
				}
				state.insert(-2);
				state.pop(1);
				return true;
			}
			state.pop(1);
		} finally {
			state.gc(LuaState.GcAction.RESTART, 0);
		}

		return false;
	}

	private Map<String, Persistable> assemblePersistableMap() {
		Map<String, Persistable> m = new HashMap<>();

		for (Object o : machine.getAllComponents()) {
			if (o instanceof Persistable) {
				m.put(machine.getComponentAddress(o), (Persistable) o);
			}
		}

		return m;
	}

	@Override
	public void persist(OutputStream data) throws IOException, PersistenceException {
		DataOutputStream stream = new DataOutputStream(data);
		stream.writeShort(CURRENT_VERSION); // version
		stream.writeUTF(persistenceKey);

		// persist Lua side
		machine.setLimitMemorySize(false);

		byte[] kernel = persistState(1);
		byte[] stack = new byte[0];
		if (machine.getLuaState().isFunction(2) || machine.getLuaState().isTable(2)) {
			stack = persistState(2);
		}

		if (kernel != null) {
			stream.writeInt(kernel.length);
			stream.write(kernel);

			if (stack != null) {
				stream.writeInt(stack.length);
				stream.write(stack);
			} else {
				stream.writeInt(0);
			}
		} else {
			stream.writeInt(0);
		}

		machine.setLimitMemorySize(true);

		// persist components
		Map<String, Persistable> m = assemblePersistableMap();
		stream.writeInt(m.size());
		for (Map.Entry<String, Persistable> entry : m.entrySet()) {
			stream.writeUTF(entry.getKey());
			ByteArrayOutputStream s = new ByteArrayOutputStream();
			entry.getValue().persist(s);
			stream.writeInt(s.size());
			stream.write(s.toByteArray());
			s.close();
		}

		stream.close();
	}

	@Override
	public void unpersist(InputStream data) throws IOException, PersistenceException {
		DataInputStream stream = new DataInputStream(data);
		int ver = stream.readShort();
		if (ver > CURRENT_VERSION) {
			throw new PersistenceException("Data too new!");
		}

		persistenceKey = stream.readUTF();
		configure();

		// unpersist Lua side
		machine.setLimitMemorySize(false);
		machine.getLuaState().setTop(0); // clear stack

		int kernelLength = stream.readInt();
		if (kernelLength > 0) {
			byte[] kernel = new byte[kernelLength];
			stream.read(kernel);
			unpersistState(kernel);

			int stackLength = stream.readInt();
			if (stackLength > 0) {
				byte[] stack = new byte[stackLength];
				stream.read(stack);
				unpersistState(stack);
				if (!machine.getLuaState().isTable(2) && !machine.getLuaState().isFunction(2)) {
					throw new PersistenceException("invalid stack type");
				}
			}
		}

		machine.setLimitMemorySize(true);

		// unpersist components
		Map<String, Persistable> m = assemblePersistableMap();
		int msize = stream.readInt();
		for (int i = 0; i < msize; i++) {
			String addr = stream.readUTF();
			if (m.containsKey(addr)) {
				byte[] cData = new byte[stream.readInt()];
				if (stream.read(cData) == cData.length) {
					ByteArrayInputStream s = new ByteArrayInputStream(cData);
					m.get(addr).unpersist(s);
				}
			} else {
				// No component found
				int v = stream.readInt();
				stream.skipBytes(v);
			}
		}
	}
}
