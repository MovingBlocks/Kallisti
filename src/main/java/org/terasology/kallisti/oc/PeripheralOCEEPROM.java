// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.oc;

import org.terasology.kallisti.base.component.ComponentContext;
import org.terasology.kallisti.base.component.ComponentMethod;
import org.terasology.kallisti.base.component.ComponentRule;
import org.terasology.kallisti.base.component.Peripheral;
import org.terasology.kallisti.base.interfaces.Labelable;
import org.terasology.kallisti.base.interfaces.StaticByteStorage;

public class PeripheralOCEEPROM implements Peripheral {
    private final MachineOpenComputers machine;
    private final StaticByteStorage backing;
    private final int codeSize, dataSize;

    @ComponentRule
    public PeripheralOCEEPROM(MachineOpenComputers machine, StaticByteStorage storage) {
        this.machine = machine;
        this.backing = storage;

        this.dataSize = 256;
        this.codeSize = this.backing.get().length - this.dataSize;
    }

    private String getString(int pos, int len) {
        byte[] data = backing.get();
        int i;
        for (i = 0; i < len; i++) {
            if (data[pos + i] == 0) {
                break;
            }
        }

        return new String(backing.get(), pos, i, machine.getCharset());
    }

    private boolean setString(int pos, int len, String s) {
        if (!backing.canModify()) {
            return false;
        }

        byte[] data = s.getBytes(machine.getCharset());
        byte[] to = backing.get();

        // If the data is of size len + 2 or more, quit.
        // If the data is of size len + 1 *and* the last byte is not \x00, quit.
        if (data.length >= len + 2) {
            return false;
        } else if (data.length == len + 1 && data[len] != 0) {
            return false;
        } else if (data.length >= len) {
            System.arraycopy(data, 0, to, pos, len);
            backing.markModified();
            return true;
        } else {
            System.arraycopy(data, 0, to, pos, data.length);
            for (int i = data.length; i < len; i++) {
                to[pos + i] = 0;
            }
            backing.markModified();
            return true;
        }
    }

    @ComponentMethod
    public int getSize() {
        return codeSize;
    }

    @ComponentMethod
    public int getDataSize() {
        return dataSize;
    }

    @ComponentMethod
    public String getData() {
        return getString(codeSize, dataSize);
    }

    @ComponentMethod
    public void setData(String data) {
        setString(codeSize, dataSize, data);
    }

    @ComponentMethod
    public String get() {
        return getString(0, codeSize);
    }

    @ComponentMethod
    public void set(String data) {
        setString(0, codeSize, data);
    }

    @ComponentMethod
    public String getLabel() {
        ComponentContext ctx = machine.getContext(this);
        if (ctx instanceof Labelable) {
            return ((Labelable) ctx).getLabel();
        } else {
            return "";
        }
    }

    @ComponentMethod
    public void setLabel(String label) {
        ComponentContext ctx = machine.getContext(this);
        if (ctx instanceof Labelable) {
            ((Labelable) ctx).setLabel(label);
        }
    }

    @ComponentMethod
    public String getChecksum() {
        // TODO
        return "00000000";
    }

    @ComponentMethod
    public boolean makeReadonly(String checksum) {
        if (!checksum.equals(getChecksum())) {
            return false;
        }

        // TODO
        return false;
    }

    @Override
    public String type() {
        return "eeprom";
    }
}
