// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.simulator;

import org.terasology.kallisti.base.interfaces.FileSystem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FSFileJavaIO {
    private static abstract class Base implements FileSystem.File {
        protected final File file;
        protected final RandomAccessFile stream;

        public Base(File file, String mode) throws FileNotFoundException {
            this.file = file;
            this.stream = new RandomAccessFile(file, mode);
        }

        @Override
        public boolean isSeekable() {
            return true;
        }

        @Override
        public long seek(FileSystem.Whence whence, int offset) throws IOException {
            switch (whence) {
                case BEGINNING:
                    stream.seek(offset);
                    break;
                case CURRENT:
                    if (offset != 0) {
                        stream.seek(stream.getFilePointer() + offset);
                    }
                    break;
                case END:
                    stream.seek(stream.length() + offset);
                    break;
            }

            return stream.getFilePointer();
        }

        @Override
        public byte[] read(int bytes) throws IOException {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int len;
            boolean read = false;
            while (bytes > 0 && (len = stream.read(buffer, 0, Math.min(bytes, buffer.length))) != -1) {
                byteStream.write(buffer, 0, len);
                bytes -= len;
                read = true;
            }

            if (!read) {
                return null;
            } else {
                return byteStream.toByteArray();
            }
        }

        @Override
        public void close() throws IOException {
            stream.close();
        }
    }

    public static class Reader extends Base {
        public Reader(File file) throws FileNotFoundException {
            super(file, "r");
        }

        @Override
        public boolean isReadable() {
            return true;
        }

        @Override
        public boolean isWritable() {
            return false;
        }

        @Override
        public boolean write(byte[] value, int offset, int len) throws IOException {
            return false;
        }
    }

    public static class Writer extends Base {
        public Writer(File file, boolean append) throws IOException {
            super(file, "rw");
            if (!append) {
                stream.seek(0);
                stream.setLength(0);
            }
        }

        @Override
        public boolean isReadable() {
            return true;
        }

        @Override
        public boolean isWritable() {
            return true;
        }

        @Override
        public boolean write(byte[] value, int offset, int len) throws IOException {
            stream.write(value, offset, len);
            return true;
        }
    }
}
