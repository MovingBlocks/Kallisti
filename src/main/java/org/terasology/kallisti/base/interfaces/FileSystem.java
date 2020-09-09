// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.base.interfaces;

import org.terasology.kallisti.base.component.ComponentInterface;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;

/**
 * A component interface for exposing file-based filesystems to computers.
 */
@ComponentInterface
public interface FileSystem {
    /**
     * List the contents of a directory.
     *
     * @param path The directory.
     * @return A collections of all contents in the directory.
     * @throws IOException
     */
    Collection<Metadata> list(String path) throws IOException;

    /**
     * Open a file.
     *
     * @param path The path to the file.
     * @param mode The opening mode.
     * @return The opened File.
     * @throws IOException
     */
    File open(String path, OpenMode mode) throws IOException;

    /**
     * Retrieve metadata pertaining to a file or directory.
     *
     * @param path The path to the file or directory.
     * @return The metadata of the file or directory.
     * @throws FileNotFoundException
     */
    Metadata metadata(String path) throws FileNotFoundException;

    /**
     * Attempt creating a directory.
     *
     * @param path The new directory path.
     * @return Whether or not the deletion was successful.
     * @throws IOException
     */
    boolean createDirectory(String path) throws IOException;

    /**
     * Attempt deleting a file.
     *
     * @param path The file path.
     * @return Whether or not the deletion was successful.
     * @throws IOException
     */
    boolean delete(String path) throws IOException;

    /**
     * @return The total size of this filesystem, in bytes.
     */
    long getTotalAreaBytes();

    /**
     * @return The amount of bytes used by data on this filesystem.
     */
    long getUsedAreaBytes();

    /**
     * @return The amount of bytes free for data on this filesystem.
     */
    default long getFreeAreaBytes() {
        return Math.max(0, getTotalAreaBytes() - getUsedAreaBytes());
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

    interface Metadata {
        /**
         * @return The name of the file.
         */
        String name();

        /**
         * @return The full path of the file.
         */
        String path();

        /**
         * @return Whether or not the file is a directory.
         */
        boolean isDirectory();

        /**
         * @return Whether or not the file can be read.
         */
        boolean canRead();

        /**
         * @return Whether or not the file can be written to.
         */
        boolean canWrite();

        /**
         * @return The file's creation time.
         */
        Date creationTime();

        /**
         * @return The file's time of last modification.
         */
        Date modificationTime();

        /**
         * @return The size of the file, in bytes.
         */
        long size();
    }

    interface File extends AutoCloseable {
        boolean isSeekable();

        boolean isReadable();

        boolean isWritable();

        long seek(Whence whence, int offset) throws IOException;

        byte[] read(int bytes) throws IOException;

        boolean write(byte[] value, int offset, int len) throws IOException;

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
}
