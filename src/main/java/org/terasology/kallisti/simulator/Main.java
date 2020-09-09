// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.simulator;

import org.terasology.jnlua.LuaState52;
import org.terasology.jnlua.LuaState53;
import org.terasology.kallisti.base.util.KallistiFileUtils;
import org.terasology.kallisti.oc.MachineOpenComputers;
import org.terasology.kallisti.oc.OCFont;
import org.terasology.kallisti.oc.OCGPURenderer;
import org.terasology.kallisti.oc.PeripheralOCGPU;

import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        SimulatorInstantiationManager manager = new SimulatorInstantiationManager();
        manager.register("MachineOpenComputers", (owner, context, json) -> {
            MachineOpenComputers machine = new MachineOpenComputers(
                    KallistiFileUtils.readString(new File(json.get("machineFile").getAsString())),
                    context,
                    new OCFont(
                            KallistiFileUtils.readString(new File(json.get("font").getAsString())),
                            json.get("fontHeight").getAsInt()
                    ),
                    json.has("memory") ? json.get("memory").getAsInt() : 0,
                    true,
                    json.has("luaVersion") && "5.2".equals(json.get("luaVersion").getAsString()) ? LuaState52.class :
                            LuaState53.class,
                    json.has("persistence") && json.get("persistence").getAsBoolean()
            );

            if (json.has("timeout")) {
                machine = machine.setTimeout(json.get("timeout").getAsDouble());
            }

            return machine;
        });

        manager.register("InMemoryStaticByteStorage", (owner, context, json) -> new InMemoryStaticByteStorage(
                json.get("file").getAsString(),
                json.get("size").getAsInt()
        ));

        manager.register("SimulatorFileSystem", (owner, context, json) -> new SimulatorFileSystem(
                json.get("base").getAsString()
        ));

        manager.register("SimulatorFrameBufferWindow", (owner, context, json) -> new SimulatorFrameBufferWindow(
                json.get("windowName").getAsString()
        ));

        manager.register("SimulatorKeyboardInputWindow", (owner, context, json) -> new SimulatorKeyboardInputWindow(
                json.get("windowName").getAsString()
        ));

        manager.register("PeripheralOCGPU", (owner, context, json) -> new PeripheralOCGPU(
                (MachineOpenComputers) owner,
                json.get("maxWidth").getAsInt(),
                json.get("maxHeight").getAsInt(),
                OCGPURenderer.genThirdTierPalette()
        ));

        Simulator simulator = new Simulator(manager, new File(args[0]));
        simulator.start();

        boolean tick = true;

        while (tick) {
            long t = System.currentTimeMillis();
            tick = simulator.tick();
            int l = (int) (simulator.getTickDuration() * 1000) - ((int) (System.currentTimeMillis() - t));
            if (l > 0) {
                Thread.sleep(l);
            }
        }
    }
}
