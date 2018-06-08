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

import org.terasology.kallisti.base.component.*;
import org.terasology.kallisti.base.interfaces.Labelable;
import org.terasology.kallisti.base.interfaces.StaticByteStorage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

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

    private static final String readFile(File inf) throws IOException {
        FileInputStream in = new FileInputStream(inf);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }

        in.close();
        out.close();

        return new String(out.toByteArray(), Charset.forName("UTF-8"));
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
