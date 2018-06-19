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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class OCFont {
    private final Map<Integer, byte[]> fontArray = new HashMap<>();
    private final int fontHeight;

    public OCFont(String font, int fontHeight) {
        this.fontHeight = fontHeight;
        Arrays.asList(font.split("\\n")).forEach((s) -> {
            String[] data = s.trim().split(":");
            int key = Integer.parseInt(data[0], 16);
            byte[] bytes = new byte[data[1].length() / 2];
            for (int i = 0; i < data[1].length() / 2; i++) {
                bytes[i] = (byte) Integer.parseInt(data[1].substring(i * 2, i * 2 + 2), 16);
            }
            fontArray.put(key, bytes);
        });
    }

    public byte[] getData(int v) {
        byte[] data = fontArray.get(v);
        return data != null ? data : fontArray.get(0x20);
    }

    public int getFontWidth() {
        return 8;
    }

    public int getFontHeight() {
        return fontHeight;
    }

    public int getCharWidth(int codePoint) {
        byte[] data = getData(codePoint);
        return data.length / fontHeight;
    }
}
