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

import org.terasology.kallisti.base.interfaces.StaticByteStorage;

import java.io.*;

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
