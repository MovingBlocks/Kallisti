// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.oc;

import org.terasology.kallisti.base.component.ComponentContext;
import org.terasology.kallisti.base.component.ComponentMethod;
import org.terasology.kallisti.base.component.ComponentRule;
import org.terasology.kallisti.base.component.Peripheral;
import org.terasology.kallisti.base.interfaces.ConnectedContext;
import org.terasology.kallisti.base.interfaces.FrameBuffer;
import org.terasology.kallisti.base.interfaces.Persistable;
import org.terasology.kallisti.base.util.PersistenceException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PeripheralOCScreen implements Peripheral, Persistable {
    private static final int PERSISTENCE_VERSION = 0x01;
    private final MachineOpenComputers machine;
    private final FrameBuffer buffer;
    private boolean on = true;

    @ComponentRule
    public PeripheralOCScreen(MachineOpenComputers machine, FrameBuffer buffer) {
        this.machine = machine;
        this.buffer = buffer;
    }

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
        return new double[]{
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
