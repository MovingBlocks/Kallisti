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
import org.terasology.kallisti.base.component.ComponentTickEvent;
import org.terasology.kallisti.base.component.Machine;
import org.terasology.kallisti.base.component.Peripheral;
import org.terasology.kallisti.jnlua.KallistiConverter;
import org.terasology.kallisti.jnlua.KallistiGlobalRegistry;
import org.terasology.jnlua.LuaState;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.*;

public class MachineOpenComputers extends Machine {
    // TODO: This shouldn't be here, but ShimUnicode...
    protected final OCFont font;

    private final Map<String, Object> peripheralAddressMap = new HashMap<>();
    private final LinkedList<Object[]> signalQueue = new LinkedList<>();

    private final File machineJson;
    private final LuaState state;
    private final ShimUserdata shimUserdata;

    private PeripheralOCComputer peripheralComputer;

    public MachineOpenComputers(File root, ComponentContext selfContext, OCFont font) {
        this.machineJson = root;
        this.font = font;
        this.state = new LuaState();

        KallistiConverter converter = new KallistiConverter(state.getConverter());

        state.openLib(LuaState.Library.BASE);
        state.openLib(LuaState.Library.MATH);
        state.openLib(LuaState.Library.STRING);
        state.openLib(LuaState.Library.COROUTINE);
        state.openLib(LuaState.Library.TABLE);
        state.openLib(LuaState.Library.DEBUG);
        state.openLib(LuaState.Library.UTF8);
        state.openLib(LuaState.Library.ERIS);
        state.pop(8);

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

        addLuaProxy(List.class, new LuaProxyList());
        addLuaProxy(Map.class, new LuaProxyMap());

        addComponent(selfContext, peripheralComputer = new PeripheralOCComputer());

        register(PeripheralOCEEPROM.class);
        register(PeripheralOCFilesystem.class);
        register(PeripheralOCKeyboard.class);
        register(PeripheralOCScreen.class);
    }

    @Override
    public void initialize() {
        super.initialize();

        for (Peripheral o : getComponentsByClass(Peripheral.class)) {
            peripheralAddressMap.put(getComponentAddress(o), o);
        }
    }

    public void pushSignal(Object... args) {
        signalQueue.add(args);
    }

    public void addLuaProxy(Class c, LuaProxy proxy) {
        shimUserdata.proxyMap.put(c, proxy);
    }

    public String getComputerAddress() {
        return getComponentAddress(peripheralComputer);
    }

    public LuaState getLuaState() {
        return state;
    }

    public String getComponentType(Object o) {
        if (!(o instanceof Peripheral)) {
            return "none";
        } else {
            return ((Peripheral) o).type();
        }
    }

    public String getComponentAddress(Object o) {
        ComponentContext context = o instanceof ComponentContext ? (ComponentContext) o : getContext(o);
        return context != null ? UUID.nameUUIDFromBytes(context.identifier().getBytes(Charset.forName("UTF-8"))).toString() : null;
    }

    public Object getPeripheral(String address) {
        return peripheralAddressMap.get(address);
    }

    public void start() throws Exception {
        state.load(new FileInputStream(machineJson), "=machine", "t");
        state.newThread();
    }

    private Object[] lastReturned;
    private double lastReturnedTime, time = 0, cpuTime = 0;

    public double getCpuTime() {
        return cpuTime;
    }

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

        // TODO: I think this may need microsecond accuracy, if possible?
        long cpuTimeStart = System.currentTimeMillis();
        int ret = state.resume(1, inArgs);
        cpuTime += (System.currentTimeMillis() - cpuTimeStart) / 1000.0;

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
}
