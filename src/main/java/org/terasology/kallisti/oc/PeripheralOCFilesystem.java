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
import org.terasology.kallisti.base.component.ComponentRule;
import org.terasology.kallisti.base.component.Peripheral;
import org.terasology.kallisti.base.interfaces.FileSystem;
import org.terasology.kallisti.base.interfaces.Labelable;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PeripheralOCFilesystem implements Peripheral {
    private static FileSystem.Whence stringToWhence(String s) {
        if ("set".equals(s)) return FileSystem.Whence.BEGINNING;
        else if ("cur".equals(s)) return FileSystem.Whence.CURRENT;
        else if ("end".equals(s)) return FileSystem.Whence.END;
        else throw new RuntimeException("Unknown whence '" + s + "'");
    }

    @Override
    public String type() {
        return "filesystem";
    }

    public class OCFileString {
        private final FileSystem.File file;

        public OCFileString(FileSystem.File file) {
            this.file = file;
        }

        @ComponentMethod
        public void close() throws IOException {
            file.close();
        }

        @ComponentMethod
        public String read(int bytes) throws IOException {
            byte[] data = file.read(bytes);
            return data != null ? new String(data, Charset.forName("UTF-8")) : null;
        }

        @ComponentMethod
        public boolean write(String value) throws IOException {
            return file.write(value.getBytes(Charset.forName("UTF-8")));
        }

        @ComponentMethod
        public long seek(String whence, int offset) throws IOException {
            return file.seek(stringToWhence(whence), offset);
        }

        @ComponentMethod
        public long seek(String whence) throws IOException {
            return seek(whence, 0);
        }
    }

    public class OCFileByteArray {
        private final FileSystem.File file;

        public OCFileByteArray(FileSystem.File file) {
            this.file = file;
        }

        @ComponentMethod
        public void close() throws IOException {
            file.close();
        }

        @ComponentMethod
        public byte[] read(int bytes) throws IOException {
            return file.read(bytes);
        }

        @ComponentMethod
        public boolean write(byte[] value) throws IOException {
            return file.write(value);
        }

        @ComponentMethod
        public long seek(String whence, int offset) throws IOException {
            return file.seek(stringToWhence(whence), offset);
        }

        @ComponentMethod
        public long seek(String whence) throws IOException {
            return seek(whence, 0);
        }
    }

    private final FileSystem fileSystem;
    private final MachineOpenComputers machine;

    @ComponentRule
    public PeripheralOCFilesystem(MachineOpenComputers machine, FileSystem fileSystem) {
        this.fileSystem = fileSystem;
        this.machine = machine;
    }

    @ComponentMethod
    public String getLabel() {
        ComponentContext context = machine.getContext(this);
        if (context instanceof Labelable) {
            return ((Labelable) context).getLabel();
        }
        return "";
    }

    @ComponentMethod
    public String setLabel(String l) {
        ComponentContext context = machine.getContext(this);
        if (context instanceof Labelable) {
            ((Labelable) context).setLabel(l);
        }
        return getLabel();
    }

    @ComponentMethod
    public long spaceUsed() {
        return fileSystem.getUsedAreaBytes();
    }

    @ComponentMethod
    public long spaceTotal() {
        return fileSystem.getTotalAreaBytes();
    }

    @ComponentMethod
    public boolean isReadOnly() throws FileNotFoundException {
        return !fileSystem.metadata("/").canWrite();
    }

    @ComponentMethod
    public long size(String path) throws FileNotFoundException {
        return fileSystem.metadata(path).size();
    }

    @ComponentMethod
    public long lastModified(String path) throws FileNotFoundException {
        return fileSystem.metadata(path).modificationTime().getTime() / 1000L;
    }

    @ComponentMethod(returnsMultipleArguments = true)
    public Object[] makeDirectory(String path) {
        try {
            return fileSystem.createDirectory(path) ? new Object[] {true} : new Object[] {null, "failure"};
        } catch (Exception e) {
            return new Object[] {null, e.getMessage()};
        }
    }

    @ComponentMethod(returnsMultipleArguments = true)
    public Object[] remove(String path) {
        try {
            return fileSystem.delete(path) ? new Object[] {true} : new Object[] {null, "failure"};
        } catch (Exception e) {
            return new Object[] {null, e.getMessage()};
        }
    }

    @ComponentMethod
    public List<String> list(String path) {
        try {
            return fileSystem.list(path).stream().map((m) -> m.isDirectory() ? m.name() + "/" : m.name()).collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    @ComponentMethod
    public boolean exists(String path) {
        try {
            fileSystem.metadata(path);
            return true;
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    @ComponentMethod
    public boolean isDirectory(String path) {
        try {
            return fileSystem.metadata(path).isDirectory();
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    @ComponentMethod
    public Object open(String path) throws IOException {
        return open(path, "r");
    }

    @ComponentMethod
    public Object open(String path, String mode) throws IOException {
        if      ("r".equals(mode)) return new OCFileString(fileSystem.open(path, FileSystem.OpenMode.READ));
        else if ("rb".equals(mode)) return new OCFileByteArray(fileSystem.open(path, FileSystem.OpenMode.READ));
        else if ("w".equals(mode)) return new OCFileString(fileSystem.open(path, FileSystem.OpenMode.WRITE));
        else if ("wb".equals(mode)) return new OCFileByteArray(fileSystem.open(path, FileSystem.OpenMode.WRITE));
        else if ("a".equals(mode)) return new OCFileString(fileSystem.open(path, FileSystem.OpenMode.APPEND));
        else if ("ab".equals(mode)) return new OCFileByteArray(fileSystem.open(path, FileSystem.OpenMode.APPEND));
        else return null;
    }

    @ComponentMethod
    public void close(Object o) throws IOException {
        if (o instanceof OCFileString) ((OCFileString) o).close();
        else if (o instanceof OCFileByteArray) ((OCFileByteArray) o).close();
    }

    @ComponentMethod
    public Object read(Object o, Number bytes) throws IOException {
        if (o instanceof OCFileString) return ((OCFileString) o).read(bytes.intValue());
        else if (o instanceof OCFileByteArray) return ((OCFileByteArray) o).read(bytes.intValue());
        else return null;
    }

    @ComponentMethod
    public long seek(Object o, String whence) throws IOException {
        return seek(o, whence, 0);
    }

    @ComponentMethod
    public long seek(Object o, String whence, int offset) throws IOException {
        if (o instanceof OCFileString) return ((OCFileString) o).seek(whence, offset);
        else if (o instanceof OCFileByteArray) return ((OCFileByteArray) o).seek(whence, offset);
        else return -1;
    }

    @ComponentMethod
    public boolean write(Object o, Object value) throws IOException {
        String valueStr;
        byte[] valueArr;

        if (value instanceof String) {
            valueStr = (String) value;
            valueArr = valueStr.getBytes(Charset.forName("UTF-8"));
        } else if (value instanceof byte[]) {
            valueArr = (byte[]) value;
            valueStr = new String(valueArr, Charset.forName("UTF-8"));
        } else {
            return false;
        }

        if (o instanceof OCFileString) return ((OCFileString) o).write(valueStr);
        else if (o instanceof OCFileByteArray) return ((OCFileByteArray) o).write(valueArr);
        else return false;
    }
}
