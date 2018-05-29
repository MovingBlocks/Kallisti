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

import java.util.Locale;

public class ShimUnicode {
    private final OCFont font;

    public ShimUnicode(OCFont font) {
        this.font = font;
    }

    private int getCharWidth(int i) {
        if (font != null) {
            int w = font.getFontWidth();
            return (font.getCharWidth(i) + (w - 1)) / w;
        } else {
            return 1;
        }
    }

    public int len(String s) {
        return s.length();
    }

    public int wlen(String s) {
        int len = 0;
        for (int i = 0; i < s.length(); i++) {
            len += getCharWidth(s.codePointAt(i));
        }
        return len;
    }

    public int charWidth(String s) {
        if (s.length() == 0) {
            return 0;
        }

        return getCharWidth(s.codePointAt(0));
    }

    public boolean isWide(String s) {
        return charWidth(s) > 1;
    }

    public String wtrunc(String s, int len) {
        if (len <= 1) {
            return "";
        } else if (wlen(s) > (len - 1)) {
            while (wlen(s) > (len - 1)) {
                s = s.substring(0, s.length() - 1);
            }
            return s;
        } else {
            throw new RuntimeException("not enough characters");
        }
    }

    public String lower(String s) {
        return s.toLowerCase(Locale.ENGLISH);
    }

    public String upper(String s) {
        return s.toUpperCase(Locale.ENGLISH);
    }

    public String reverse(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = s.length() - 1; i >= 0; i--) {
            b.appendCodePoint(s.codePointAt(i));
        }
        return b.toString();
    }

    public String __char(Object... values) {
        StringBuilder builder = new StringBuilder();
        for (Object n : values) {
            if (n instanceof Number && ((Number) n).intValue() > 0) {
                builder.appendCodePoint(((Number) n).intValue());
            } else if (n instanceof Character) {
                builder.appendCodePoint((Character) n);
            }
        }
        return builder.toString();
    }

    public String sub(String s, int i) {
        return sub(s, i, -1);
    }

    public String sub(String s, int i, int j) {
        if (i < 0) i = s.length() + (i + 1);
        if (j < 0) j = s.length() + (j + 1);
        if (j > s.length()) j = s.length();
        return s.substring(i - 1, j);
    }
}
