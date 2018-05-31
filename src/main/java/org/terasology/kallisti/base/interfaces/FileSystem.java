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

package org.terasology.kallisti.base.interfaces;

import org.terasology.kallisti.base.component.ComponentInterface;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.util.Date;
import java.util.List;

@ComponentInterface
public interface FileSystem {
    interface Metadata {
        String name();
        boolean isDirectory();
        boolean canRead();
        boolean canWrite();
        Date creationTime();
        Date modificationTime();
        long size();
    }

    enum OpenMode {
        READ,
        WRITE,
        APPEND
    }

    enum Whence {
        BEGINNING,
        CURRENT,
        END
    }

    interface File {
        boolean isSeekable();
        boolean isReadable();
        boolean isWritable();

        long seek(Whence whence, int offset) throws IOException;
        byte[] read(int bytes) throws IOException;
        boolean write(byte[] value, int offset, int len) throws IOException;
        void close() throws IOException;

        default boolean write(byte[] value) throws IOException {
            return write(value, 0, value.length);
        }

        default long seek(Whence whence) throws IOException {
            return seek(whence, 0);
        }

        default long position() throws IOException {
            return isSeekable() ? seek(Whence.CURRENT, 0) : -1;
        }
    }

    List<Metadata> list(String path);
    File open(String path, OpenMode mode) throws IOException;
    Metadata metadata(String path) throws FileNotFoundException;

    boolean createDirectory(String path) throws IOException;
    boolean delete(String path) throws IOException;

    long getTotalAreaBytes();
    long getUsedAreaBytes();

    default long getFreeAreaBytes() {
        return Math.max(0, getTotalAreaBytes() - getUsedAreaBytes());
    }
}
