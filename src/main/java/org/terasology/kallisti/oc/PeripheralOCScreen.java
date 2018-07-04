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

import org.terasology.kallisti.base.interfaces.ConnectedContext;
import org.terasology.kallisti.base.interfaces.FrameBuffer;
import org.terasology.kallisti.base.component.ComponentContext;
import org.terasology.kallisti.base.component.ComponentMethod;
import org.terasology.kallisti.base.component.ComponentRule;
import org.terasology.kallisti.base.component.Peripheral;
import org.terasology.kallisti.base.interfaces.Persistable;
import org.terasology.kallisti.base.util.PersistenceException;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PeripheralOCScreen implements Peripheral, Persistable {
    private final MachineOpenComputers machine;
    private final FrameBuffer buffer;

    @ComponentRule
    public PeripheralOCScreen(MachineOpenComputers machine, FrameBuffer buffer) {
        this.machine = machine;
        this.buffer = buffer;
    }

    private boolean on = true;

    @ComponentMethod
    public boolean isOn() {
        return on;
    }

    private boolean turn(boolean val) {
        if (on != val) {
            on = val;
            return true;
        } else {
            return false;
        }
    }

    @ComponentMethod
    public boolean turnOn() {
        return turn(true);
    }

    @ComponentMethod
    public boolean turnOff() {
        return turn(false);
    }

    @ComponentMethod(returnsMultipleArguments = true)
    public double[] getAspectRatio() {
        return new double[] {
                buffer.aspectRatio().getX(),
                buffer.aspectRatio().getY()
        };
    }

    @ComponentMethod
    public List<String> getKeyboards() {
        ComponentContext ctx = machine.getContext(this);
        if (ctx instanceof ConnectedContext) {
            return ((ConnectedContext) ctx).getNeighbors().stream()
                    .filter((c) -> machine.getComponent(ctx, PeripheralOCKeyboard.class) != null)
                    .map(machine::getComponentAddress)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String type() {
        return "screen";
    }

    private static final int PERSISTENCE_VERSION = 0x01;

    @Override
    public void persist(OutputStream data) throws IOException, PersistenceException {
        DataOutputStream stream = new DataOutputStream(data);
        stream.writeShort(PERSISTENCE_VERSION);
        stream.writeBoolean(on);
    }

    @Override
    public void unpersist(InputStream data) throws IOException, PersistenceException {
        DataInputStream stream = new DataInputStream(data);
        int v = stream.readUnsignedShort();
        if (v > PERSISTENCE_VERSION) {
            throw new PersistenceException("Version too new!");
        }
        on = stream.readBoolean();
    }
}
