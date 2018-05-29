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

package org.terasology.kallisti.simulator;

import org.terasology.kallisti.base.interfaces.FileSystem;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SimulatorFileSystem implements FileSystem {
    private final java.io.File base;

    public SimulatorFileSystem(String base) {
        this(new java.io.File(base));
    }

    public SimulatorFileSystem(java.io.File base) {
        this.base = base;
    }

    private java.io.File toFile(String s) {
        return new java.io.File(base, s);
    }

    @Override
    public List<Metadata> list(String path) {
        List<Metadata> l = new ArrayList<>();
        try {
            for (java.io.File f : toFile(path).listFiles()) {
                l.add(new FSMetadataJavaIO(f));
            }
            return l;
        } catch (NullPointerException e) {
            return l;
        }
    }

    @Override
    public File open(String path, OpenMode mode) throws IOException {
        java.io.File f = toFile(path);
        if (mode == OpenMode.READ && !f.exists()) {
            throw new FileNotFoundException(path);
        }
        switch (mode) {
            case READ:
            default:
                return new FSFileJavaIO.Reader(f);
            case WRITE:
                return new FSFileJavaIO.Writer(f, false);
            case APPEND:
                return new FSFileJavaIO.Writer(f, true);
        }
    }

    @Override
    public Metadata metadata(String path) throws FileNotFoundException {
        java.io.File f = toFile(path);
        if (!f.exists()) {
            throw new FileNotFoundException(path);
        }
        return new FSMetadataJavaIO(f);
    }

    @Override
    public boolean createDirectory(String path) throws IOException {
        return toFile(path).mkdir();
    }

    @Override
    public boolean delete(String path) throws IOException {
        return toFile(path).delete();
    }

    @Override
    public long getTotalAreaBytes() {
        return base.getTotalSpace();
    }

    @Override
    public long getUsedAreaBytes() {
        return base.getTotalSpace() - base.getFreeSpace();
    }

    @Override
    public long getFreeAreaBytes() {
        return base.getUsableSpace();
    }

}
