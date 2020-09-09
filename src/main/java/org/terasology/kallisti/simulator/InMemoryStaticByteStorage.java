// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.simulator;

import org.terasology.kallisti.base.interfaces.StaticByteStorage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class InMemoryStaticByteStorage implements StaticByteStorage {
    private final File file;
    private final byte[] data;

    public InMemoryStaticByteStorage(String file, int size) throws IOException {
        this.file = new File(file);

        FileInputStream in = new FileInputStream(this.file);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }

        in.close();
        out.close();

        byte[] result = out.toByteArray();
        data = new byte[Math.max(result.length, size)];
        System.arraycopy(result, 0, data, 0, result.length);
    }

    @Override
    public byte[] get() {
        return data;
    }

    @Override
    public boolean canModify() {
        return true;
    }

    @Override
    public void markModified() {
        // TODO
        /* try {
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(data);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } */
    }
}
