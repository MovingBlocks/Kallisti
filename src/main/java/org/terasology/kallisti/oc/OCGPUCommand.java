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

import java.io.*;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public abstract class OCGPUCommand {
    private static final Map<Integer, Class> idToClass;
    private static final Map<Class, Integer> classToId;

    private static void register(int id, Class<? extends OCGPUCommand> c) {
        idToClass.put(id, c);
        classToId.put(c, id);
    }

    static {
        idToClass = new HashMap<>();
        classToId = new IdentityHashMap<>();

        register(0x01, SetPaletteColor.class);
        register(0x02, Fill.class);
        register(0x03, Copy.class);
        register(0x04, Set.class);
        register(0x05, SetResolution.class);
    }

    public static void write(OCGPUCommand command, DataOutputStream dataStream) throws IOException {
        dataStream.writeByte(classToId.get(command.getClass()));
        command.write(dataStream);
    }

    public static OCGPUCommand read(DataInputStream dataStream) throws IOException {
        int id = dataStream.readUnsignedByte();
        Class c = idToClass.get(id);
        try {
            return (OCGPUCommand) c.getConstructor(DataInputStream.class).newInstance(dataStream);
        } catch (Exception e) {
            throw new RuntimeException(e); // Should not happen!
        }
    }

    abstract static class WithColors extends OCGPUCommand {
        final int bg, fg;

        public WithColors(int bg, int fg) {
            this.bg = bg;
            this.fg = fg;
        }

        public WithColors(DataInputStream stream) throws IOException {
            this.bg = stream.readInt();
            this.fg = stream.readInt();
        }

        @Override
        public void write(DataOutputStream stream) throws IOException {
            stream.writeInt(bg);
            stream.writeInt(fg);
        }
    }

    public static class SetPaletteColor extends OCGPUCommand {
        final int palId, palColor;

        public SetPaletteColor(int palId, int palColor) {
            this.palId = palId;
            this.palColor = palColor;
        }

        public SetPaletteColor(DataInputStream stream) throws IOException {
            palId = stream.readInt();
            palColor = stream.readInt();
        }

        @Override
        public void write(DataOutputStream stream) throws IOException {
            stream.writeInt(palId);
            stream.writeInt(palColor);
        }

        @Override
        public void apply(OCGPURenderer renderer) {
            renderer.setPaletteColor(palId, palColor);
        }
    }

    public static class SetResolution extends OCGPUCommand {
        final int newWidth, newHeight, newViewportWidth, newViewportHeight;

        public SetResolution(int newWidth, int newHeight, int newViewportWidth, int newViewportHeight) {
            this.newWidth = newWidth;
            this.newHeight = newHeight;
            this.newViewportWidth = newViewportWidth;
            this.newViewportHeight = newViewportHeight;
        }

        public SetResolution(DataInputStream stream) throws IOException {
            newWidth = stream.readUnsignedShort();
            newHeight = stream.readUnsignedShort();
            newViewportWidth = stream.readUnsignedShort();
            newViewportHeight = stream.readUnsignedShort();
        }

        @Override
        public void write(DataOutputStream stream) throws IOException {
            stream.writeShort(newWidth);
            stream.writeShort(newHeight);
            stream.writeShort(newViewportWidth);
            stream.writeShort(newViewportHeight);
        }

        @Override
        public void apply(OCGPURenderer renderer) {
            renderer.setResolution(newWidth, newHeight, newViewportWidth, newViewportHeight);
        }
    }

    public static class Fill extends WithColors {
        final int x, y, width, height, codePoint;

        public Fill(int bg, int fg, int x, int y, int width, int height, int codePoint) {
            super(bg, fg);
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.codePoint = codePoint;
        }

        public Fill(DataInputStream stream) throws IOException {
            super(stream);
            x = stream.readUnsignedShort();
            y = stream.readUnsignedShort();
            width = stream.readUnsignedShort();
            height = stream.readUnsignedShort();
            codePoint = stream.readInt();
        }

        @Override
        public void write(DataOutputStream stream) throws IOException {
            super.write(stream);
            stream.writeShort(x);
            stream.writeShort(y);
            stream.writeShort(width);
            stream.writeShort(height);
            stream.writeInt(codePoint);
        }

        @Override
        public void apply(OCGPURenderer renderer) {
            renderer.fill(bg, fg, x, y, width, height, codePoint);
        }
    }

    public static class Copy extends OCGPUCommand {
        final int x, y, width, height, tx, ty;

        public Copy(int x, int y, int width, int height, int tx, int ty) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.tx = tx;
            this.ty = ty;
        }

        public Copy(DataInputStream stream) throws IOException {
            x = stream.readUnsignedShort();
            y = stream.readUnsignedShort();
            width = stream.readUnsignedShort();
            height = stream.readUnsignedShort();
            tx = stream.readShort();
            ty = stream.readShort();
        }

        @Override
        public void write(DataOutputStream stream) throws IOException {
            stream.writeShort(x);
            stream.writeShort(y);
            stream.writeShort(width);
            stream.writeShort(height);
            stream.writeShort(tx);
            stream.writeShort(ty);
        }

        @Override
        public void apply(OCGPURenderer renderer) {
            renderer.copy(x, y, width, height, tx, ty);
        }
    }

    public static class Set extends WithColors {
        final int x, y;
        final boolean vertical;
        final String value;

        public Set(int bg, int fg, int x, int y, boolean vertical, String value) {
            super(bg, fg);
            this.x = x;
            this.y = y;
            this.vertical = vertical;
            this.value = value;
        }

        public Set(DataInputStream stream) throws IOException {
            super(stream);
            x = stream.readUnsignedShort();
            y = stream.readUnsignedShort();
            vertical = stream.readBoolean();
            value = stream.readUTF();
        }

        @Override
        public void write(DataOutputStream stream) throws IOException {
            super.write(stream);
            stream.writeShort(x);
            stream.writeShort(y);
            stream.writeBoolean(vertical);
            stream.writeUTF(value);
        }

        @Override
        public void apply(OCGPURenderer renderer) {
            renderer.set(bg, fg, x, y, vertical, value);
        }
    }

    protected abstract void write(DataOutputStream stream) throws IOException;
    public abstract void apply(OCGPURenderer renderer);
}
