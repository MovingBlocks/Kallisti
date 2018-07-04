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

import org.terasology.jnlua.LuaState53;
import org.terasology.kallisti.base.component.ComponentContext;
import org.terasology.kallisti.base.component.Machine;
import org.terasology.kallisti.base.component.Peripheral;
import org.terasology.kallisti.base.interfaces.Persistable;
import org.terasology.kallisti.base.util.PersistenceException;
import org.terasology.kallisti.jnlua.KallistiConverter;
import org.terasology.kallisti.jnlua.KallistiGlobalRegistry;
import org.terasology.jnlua.LuaState;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.*;

public class MachineOpenComputers extends Machine {
    // TODO: This shouldn't be here, but ShimUnicode...
    protected final OCFont font;

    private final Map<String, Object> peripheralAddressMap = new HashMap<>();
    private final LinkedList<Object[]> signalQueue = new LinkedList<>();

    private final String machineJson;
    private final LuaState state;
    private final ShimUserdata shimUserdata;

    private PeripheralOCComputer peripheralComputer;
    private final PersistenceAPI persistenceAPI;
    private final int memorySize;

    public MachineOpenComputers(String machineJson, ComponentContext selfContext, OCFont font, int memorySize, Class<? extends LuaState> luaClass, boolean enablePersistence) {
        this(machineJson, selfContext, font, memorySize, luaClass, enablePersistence ? "__persist_" + UUID.randomUUID().toString() : null);
    }

    public MachineOpenComputers(String machineJson, ComponentContext selfContext, OCFont font, int memorySize, Class<? extends LuaState> luaClass, String persistenceKey) {
        this.machineJson = machineJson;
        this.memorySize = memorySize;
        this.font = font;
        try {
            if (memorySize > 0) {
                this.state = luaClass.getConstructor(int.class).newInstance(memorySize);
            } else {
                this.state = luaClass.newInstance();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        KallistiConverter converter = new KallistiConverter(state.getConverter());

        state.openLib(LuaState.Library.BASE);
        state.openLib(LuaState.Library.MATH);
        state.openLib(LuaState.Library.STRING);
        state.openLib(LuaState.Library.COROUTINE);
        state.openLib(LuaState.Library.TABLE);
        state.openLib(LuaState.Library.DEBUG);
        state.pop(6);

        if (state instanceof LuaState53) {
            state.openLib(LuaState.Library.UTF8);
            state.pop(1);
        } else {
            state.openLib(LuaState.Library.BIT32);
            state.pop(1);
        }

        try {
            state.openLib(LuaState.Library.ERIS);
            state.pop(1);
        } catch (IllegalArgumentException e) {
            persistenceKey = null;
        }

        if (persistenceKey != null) {
            persistenceAPI = new PersistenceAPI(this, persistenceKey);
        } else {
            persistenceAPI = null;
        }

        state.setConverter(converter);

        KallistiGlobalRegistry.registerGlobal(new ShimComputer(this), "computer", state);
        KallistiGlobalRegistry.registerGlobal(new ShimSystem(this), "system", state);
        KallistiGlobalRegistry.registerGlobal(new ShimComponent(this), "component", state);
        KallistiGlobalRegistry.registerGlobal(shimUserdata = new ShimUserdata(this), "userdata", state);
        KallistiGlobalRegistry.registerGlobal(new ShimUnicode(font), "unicode", state);
        state.pop(5);

        // TODO: ShimOS crashes in os.date() (ls -l)
        //KallistiGlobalRegistry.registerGlobal(new ShimOS(this), "os", state);
        state.openLib(LuaState.Library.OS);
        state.pop(1);

        addLuaProxy(List.class, new OCLuaProxyList());
        addLuaProxy(Map.class, new OCLuaProxyMap());

        addComponent(selfContext, peripheralComputer = new PeripheralOCComputer());

        registerRules(PeripheralOCEEPROM.class);
        registerRules(PeripheralOCFilesystem.class);
        registerRules(PeripheralOCKeyboard.class);
        registerRules(PeripheralOCScreen.class);
    }

    void setLimitMemorySize(boolean v) {
        if (memorySize > 0) {
            state.setTotalMemory(v ? memorySize : Integer.MAX_VALUE);
        }
    }

    @Override
    public void initialize() {
        super.initialize();

        for (Peripheral o : getComponentsByClass(Peripheral.class)) {
            peripheralAddressMap.put(getComponentAddress(o), o);
        }

        getPersistenceHandler().ifPresent((h) -> ((PersistenceAPI) h).initialize());
    }

    /**
     * Push a new signal to the OpenComputers signal queue. The first
     * parameter is expected to be of type String by most OpenComputers
     * software.
     * @param args The signal, as an array of arguments.
     */
    public void pushSignal(Object... args) {
        signalQueue.add(args);
    }

    /**
     * Register a OCLuaProxy for a given class.
     * @param c The class.
     * @param proxy The OCLuaProxy instance.
     */
    public void addLuaProxy(Class c, OCLuaProxy proxy) {
        shimUserdata.proxyMap.put(c, proxy);
    }

    /**
     * @return The OpenComputers-style address of the PeripheralOCComputer instance.
     */
    public String getComputerAddress() {
        return getComponentAddress(peripheralComputer);
    }

    protected LuaState getLuaState() {
        return state;
    }

    /**
     * Retrieve the OpenComputers-defined "type" of a component.
     * @param o The component object.
     * @return The type, as a string.
     */
    public String getComponentType(Object o) {
        if (!(o instanceof Peripheral)) {
            return "none";
        } else {
            return ((Peripheral) o).type();
        }
    }

    /**
     * Retrieve the OpenComputers-defined "address" of a component.
     * @param o The component object.
     * @return The address UUID, as a string.
     */
    public String getComponentAddress(Object o) {
        ComponentContext context = o instanceof ComponentContext ? (ComponentContext) o : getContext(o);
        return context != null ? UUID.nameUUIDFromBytes(context.identifier().getBytes(Charset.forName("UTF-8"))).toString() : null;
    }

    /**
     * Retrieve a component implementing Peripheral with a given
     * OpenComputers-defined "address".
     * @param address The OC-style address.
     * @return The peripheral, or null if none found.
     */
    public Object getPeripheral(String address) {
        return peripheralAddressMap.get(address);
    }

    public void start() throws Exception {
        state.load(machineJson, "=machine");
        state.newThread();
    }

    private Object[] lastReturned;
    private double lastReturnedTime, time = 0, cpuTime = 0;

    /**
     * @return The virtual machine CPU time, in seconds.
     */
    public double getCpuTime() {
        return cpuTime;
    }

    /**
     * @return The virtual machine uptime, in seconds.
     */
    public double getTime() {
        return time;
    }

    @Override
    public boolean tickInternal(double tickTime) throws Exception {
        int inArgs = 0;
        if (lastReturned != null && lastReturned.length > 0) {
            if (lastReturned[0] instanceof Number) {
                double waitUntil = ((Number) lastReturned[0]).doubleValue();
                if (!signalQueue.isEmpty()) {
                    Object[] in = signalQueue.removeFirst();
                    inArgs = in.length;
                    for (int i = 0; i < inArgs; i++) {
                        state.pushJavaObject(in[i]);
                    }
                } else if (lastReturnedTime + waitUntil > time + tickTime) {
                    time += tickTime;
                    return true;
                }
            } else if (lastReturned[0] instanceof Boolean) {
                boolean reboot = (boolean) lastReturned[0];
                if (reboot) {
                    state.pop(state.getTop());
                    lastReturned = null;

                    // TODO: Should this create a new LuaState?

                    start();
                    return true;
                } else {
                    // Shutdown
                    return false;
                }
            }
        }

        long cpuTimeStart = System.nanoTime();
        int ret = state.resume(1, inArgs);
        cpuTime += (System.nanoTime() - cpuTimeStart) / 1_000_000_000.0;

        if (ret == LuaState.YIELD) {
            int count = Math.min(state.getTop() - 1, ret);
            if (count > 0) {
                Object[] args = new Object[count];
                for (int i = 0; i < count; i++) {
                    args[i] = state.toJavaObject(-(i + 1), Object.class);
                }
                state.pop(count);
                lastReturned = args;
            } else {
                lastReturned = null;
            }
            time += tickTime;
            lastReturnedTime = time;
        }

        return true;
    }

    @Override
    public Optional<Persistable> getPersistenceHandler() {
        return Optional.ofNullable(persistenceAPI);
    }

    /**
     * @return The character set used by the Lua virtual machine.
     */
    public Charset getCharset() {
        return getLuaState().getCharset();
    }
}
