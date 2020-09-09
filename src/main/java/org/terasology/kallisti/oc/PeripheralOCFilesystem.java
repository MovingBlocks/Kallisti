// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.oc;

import org.terasology.kallisti.base.component.ComponentContext;
import org.terasology.kallisti.base.component.ComponentMethod;
import org.terasology.kallisti.base.component.ComponentRule;
import org.terasology.kallisti.base.component.Peripheral;
import org.terasology.kallisti.base.interfaces.FileSystem;
import org.terasology.kallisti.base.interfaces.Labelable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PeripheralOCFilesystem implements Peripheral {
    private final FileSystem fileSystem;
    private final MachineOpenComputers machine;

    @ComponentRule
    public PeripheralOCFilesystem(MachineOpenComputers machine, FileSystem fileSystem) {
        this.fileSystem = fileSystem;
        this.machine = machine;
    }

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
            return fileSystem.createDirectory(path) ? new Object[]{true} : new Object[]{null, "failure"};
        } catch (Exception e) {
            return new Object[]{null, e.getMessage()};
        }
    }

    @ComponentMethod(returnsMultipleArguments = true)
    public Object[] remove(String path) {
        try {
            return fileSystem.delete(path) ? new Object[]{true} : new Object[]{null, "failure"};
        } catch (Exception e) {
            return new Object[]{null, e.getMessage()};
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

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @ComponentMethod
    public Object open(String path, Optional<String> omode) throws IOException {
        String mode = omode.orElse("r");
        if ("r".equals(mode) || "rb".equals(mode)) return new OCFile(fileSystem.open(path, FileSystem.OpenMode.READ));
        else if ("w".equals(mode) || "wb".equals(mode))
            return new OCFile(fileSystem.open(path, FileSystem.OpenMode.WRITE));
        else if ("a".equals(mode) || "ab".equals(mode))
            return new OCFile(fileSystem.open(path, FileSystem.OpenMode.APPEND));
        else return null;
    }

    @ComponentMethod
    public void close(OCFile o) throws Exception {
        o.close();
    }

    @ComponentMethod
    public Object read(OCFile o, Number bytes) throws IOException {
        return o.read(bytes.intValue());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @ComponentMethod
    public long seek(OCFile o, String whence, Optional<Number> offset) throws IOException {
        return o.seek(whence, offset);
    }

    @ComponentMethod
    public boolean write(OCFile o, Object value) throws IOException {
        byte[] valueArr;

        if (value instanceof String) {
            valueArr = ((String) value).getBytes(machine.getCharset());
        } else if (value instanceof byte[]) {
            valueArr = (byte[]) value;
        } else {
            return false;
        }

        return o.write(valueArr);
    }

    public class OCFile {
        private final FileSystem.File file;

        public OCFile(FileSystem.File file) {
            this.file = file;
        }

        @ComponentMethod
        public void close() throws Exception {
            file.close();
        }

        @ComponentMethod
        public byte[] read(int bytes) throws IOException {
            byte[] data = file.read(bytes);
            // OpenComputers' reading relies on returning nil if the file
            // has been read fully.
            if (data != null && data.length == 0) {
                return null;
            } else {
                return data;
            }
        }

        @ComponentMethod
        public boolean write(byte[] value) throws IOException {
            return file.write(value);
        }

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        @ComponentMethod
        public long seek(String whence, Optional<Number> offset) throws IOException {
            return file.seek(stringToWhence(whence), offset.orElse(0).intValue());
        }
    }
}
