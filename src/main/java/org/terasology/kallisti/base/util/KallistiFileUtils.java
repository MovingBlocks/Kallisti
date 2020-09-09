// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.base.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class KallistiFileUtils {
    private KallistiFileUtils() {
    }

    /**
     * Reads the contents of a file into a byte array.
     *
     * @param inFile The file to read.
     * @return The read byte array.
     * @throws IOException
     */
    public static byte[] read(File inFile) throws IOException {
        FileInputStream in = new FileInputStream(inFile);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }

        in.close();
        out.close();

        return out.toByteArray();
    }

    /**
     * Reads the contents of a file into a string using the UTF-8 character set.
     *
     * @param inFile The file to read.
     * @return The read string.
     * @throws IOException
     */
    public static String readString(File inFile) throws IOException {
        return readString(inFile, StandardCharsets.UTF_8);
    }

    /**
     * Reads the contents of a file into a string using the provided character set.
     *
     * @param inFile The file to read.
     * @param charset The character set to use.
     * @return The read string.
     * @throws IOException
     */
    public static String readString(File inFile, Charset charset) throws IOException {
        return new String(read(inFile), charset);
    }

}
