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

import org.terasology.kallisti.base.component.ComponentContext;
import org.terasology.kallisti.base.component.ComponentMethod;
import org.terasology.kallisti.base.component.Peripheral;
import org.terasology.jnlua.LuaState;
import org.terasology.jnlua.LuaValueProxy;

public class ShimComponent extends ShimInvoker<Object> {
	public ShimComponent(MachineOpenComputers machine) {
		super(machine);
	}

	@Override
	protected Object get(Object value) {
		return value instanceof String ? machine.getPeripheral((String) value) : null;
	}

	@ComponentMethod
	public String doc(String address, String method) {
		return "";
	}

	@ComponentMethod
	public String type(String address) {
		Object o = get(address);
		return machine.getComponentType(o);
	}

	@ComponentMethod
	public String slot(String address) {
		// TODO
		return "unknown";
	}

	@ComponentMethod
	public LuaValueProxy list() {
		return list(null, true);
	}

	@ComponentMethod
	public LuaValueProxy list(String filter) {
		return list(filter, false);
	}

	@ComponentMethod
	public LuaValueProxy list(String filter, boolean exact) {
		LuaState state = machine.getLuaState();
		state.newTable();
		for (ComponentContext ctx : machine.getContextsByClass(Peripheral.class)) {
			Object value = machine.getComponent(ctx, Peripheral.class);
			String type = machine.getComponentType(value);

			if (filter == null || filter.length() == 0 || (exact && filter.equals(type)) || (!exact && type.contains(filter))) {
				state.pushString(type);
				state.setField(-2, machine.getComponentAddress(value));
			}
		}

		LuaValueProxy proxy = state.getProxy(-1);
		state.pop(1);
		return proxy;
	}
}
