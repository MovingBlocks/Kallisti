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

import org.terasology.kallisti.base.component.ComponentMethod;
import org.terasology.kallisti.base.component.Peripheral;
import org.terasology.kallisti.base.interfaces.FrameBuffer;
import org.terasology.kallisti.base.util.KallistiColor;
import org.terasology.kallisti.base.util.KallistiMath;

public class PeripheralOCGPU implements FrameBuffer.Renderer, Peripheral {
    private final OCTextRenderer renderer;
    private final MachineOpenComputers machine;
    private final int maxWidth, maxHeight, bitDepth;
    private final int[] palette;

    private int[] chars = new int[0];
    private int[] bgs = new int[0];
    private int[] fgs = new int[0];

    private String screenAddr;
    private int width, height, bitDepthUsed;
    private int bgColor, fgColor;

    public static int[] genThirdTierPalette() {
        int[] pal = new int[256];
        for (int i = 0; i < 16; i++) {
            pal[i] = ((i + 1) * 255 / 17) * 0x10101;
        }
        for (int i = 0; i < 240; i++) {
            int b = (i % 5) * 255 / 4;
            int g = ((i / 5) % 8) * 255 / 7;
            int r = ((i / 40) % 6) * 255 / 5;
            pal[i + 16] = (r << 16) | (g << 8) | b;
        }
        return pal;
    }

    public PeripheralOCGPU(MachineOpenComputers machine, int maxWidth, int maxHeight, int[] palette) {
        this.machine = machine;
        this.renderer = new OCTextRenderer(machine.font);
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.palette = new int[KallistiMath.smallestContainingPowerTwo(palette.length)];
        System.arraycopy(palette, 0, this.palette, 0, palette.length);

        this.bgColor = 0;
        this.fgColor = palette.length - 1;

        this.bitDepth = KallistiMath.log2up(this.palette.length);
        this.bitDepthUsed = this.bitDepth;

        setResolution(maxWidth, maxHeight);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getCharAt(int x, int y) {
        return chars[y * width + x];
    }

    public int getBGColorAt(int x, int y) {
        return bgs[y * width + x];
    }

    public int getFGColorAt(int x, int y) {
        return fgs[y * width + x];
    }

    private static int[] rescale(int[] array, int oldWidth, int oldHeight, int newWidth, int newHeight) {
        int[] newArray = new int[newWidth * newHeight];
        int xSize = Math.min(oldWidth, newWidth);
        for (int iy = 0; iy < Math.min(oldHeight, newHeight); iy++) {
            System.arraycopy(array, iy * oldWidth, newArray, iy * newWidth, xSize);
        }
        return newArray;
    }

    @ComponentMethod
    public boolean bind(String address) {
        for (FrameBuffer b : machine.getComponentsByClass(FrameBuffer.class)) {
            if (machine.getComponentAddress(b).equals(address)) {
                b.bind(this);
                this.screenAddr = address;
                return true;
            }
        }

        return false;
    }

    @ComponentMethod
    public String getScreen() {
        return screenAddr;
    }

    @ComponentMethod
    public int getPaletteColor(Number index) {
        int i = index.intValue();
        return i >= 0 && i < palette.length ? palette[i] : 0;
    }

    @ComponentMethod
    public void setPaletteColor(Number index, Number value) {
        int i = index.intValue();
        if (i >= 0 && i < palette.length) {
            palette[i] = value.intValue() & 0xFFFFFF;
        }
    }

    @ComponentMethod(returnsMultipleArguments = true)
    public int[] getResolution() {
        return new int[] { width, height };
    }

    // TODO: plan9k???
    @ComponentMethod(returnsMultipleArguments = true)
    public int[] getResolution(Object o) {
        return new int[] { width, height };
    }

    @ComponentMethod(returnsMultipleArguments = true)
    public int[] getViewport() {
        return new int[] { width, height };
    }

    @ComponentMethod(returnsMultipleArguments = true)
    public int[] maxResolution() {
        return new int[] { maxWidth, maxHeight };
    }

    @ComponentMethod
    public boolean setResolution(Number widthN, Number heightN) {
        int width = widthN.intValue();
        int height = heightN.intValue();
        if (width <= maxWidth && height <= maxHeight && width > 0 && height > 0) {
            this.chars = rescale(this.chars, this.width, this.height, width, height);
            this.bgs = rescale(this.bgs, this.width, this.height, width, height);
            this.fgs = rescale(this.fgs, this.width, this.height, width, height);
            this.width = width;
            this.height = height;
            return true;
        } else {
            return false;
        }
    }

    private int findNearest(int color, int colorCount) {
        double distance = Double.MAX_VALUE;
        int v = 0;

        for (int i = 0; i < colorCount; i++) {
            int pcolor = palette[i];
            if (pcolor == color) {
                return pcolor;
            } else {
                double diff = KallistiColor.distance(color, pcolor);
                if (diff < distance) {
                    distance = diff;
                    v = i;
                }
            }
        }

        return palette[v];
    }

    private int[] getOldColorReturn(int oldColor) {
        for (int i = 0; i < palette.length; i++) {
            if (palette[i] == oldColor) {
                return new int[] { oldColor, i };
            }
        }

        return new int[] { oldColor };
    }

    @ComponentMethod
    public int getBackground() {
        return bgColor;
    }

    @ComponentMethod(returnsMultipleArguments = true)
    public int[] setBackground(Number color) {
        return setBackground(color, false);
    }

    @ComponentMethod(returnsMultipleArguments = true)
    public int[] setBackground(Number color, boolean isPaletteIndex) {
        int[] oldColorReturn = getOldColorReturn(bgColor);
        int colorCount = 1 << bitDepthUsed;

        if (isPaletteIndex && color.intValue() >= 0 && color.intValue() < colorCount) {
            bgColor = palette[color.intValue()];
        } else {
            bgColor = findNearest(color.intValue(), colorCount);
        }

        return oldColorReturn;
    }

    @ComponentMethod
    public int getForeground() {
        return fgColor;
    }

    @ComponentMethod(returnsMultipleArguments = true)
    public int[] setForeground(Number color) {
        return setForeground(color, false);
    }

    @ComponentMethod(returnsMultipleArguments = true)
    public int[] setForeground(Number color, boolean isPaletteIndex) {
        int[] oldColorReturn = getOldColorReturn(fgColor);
        int colorCount = 1 << bitDepthUsed;

        if (isPaletteIndex && color.intValue() >= 0 && color.intValue() < colorCount) {
            fgColor = palette[color.intValue()];
        } else {
            fgColor = findNearest(color.intValue(), colorCount);
        }

        return oldColorReturn;
    }

    @ComponentMethod
    public int getDepth() {
        return bitDepthUsed;
    }

    @ComponentMethod
    public int maxDepth() {
        return bitDepth;
    }

    @ComponentMethod
    public boolean setDepth(int depth) {
        if (depth >= 1 && depth <= bitDepth) {
            bitDepthUsed = depth;
            return true;
        } else {
            return false;
        }
    }

    // Drawing commands
    @ComponentMethod(returnsMultipleArguments = true)
    public Object[] get(Number x, Number y) {
        int xi = x.intValue();
        int yi = y.intValue();

        if (xi >= 1 && yi >= 1 && xi <= width && yi <= height) {
            int idx = (yi - 1) * width + (xi - 1);
            int chr = chars[idx];
            String s = new StringBuilder().appendCodePoint(chr).toString();
            int[] bgColor = getOldColorReturn(bgs[idx]);
            int[] fgColor = getOldColorReturn(fgs[idx]);

            if (bgColor.length == 2 && fgColor.length == 2) {
                return new Object[] { s, fgColor[0], bgColor[0], fgColor[1], bgColor[1] };
            } else {
                return new Object[] { s, fgColor[0], bgColor[0] };
            }
        } else {
            return new Object[] { " ", 0, 0, 0, 0 };
        }
    }

    @ComponentMethod
    public boolean fill(Number x, Number y, Number width, Number height, String c) {
        if (c.length() == 1) {
            int codePoint = c.codePointAt(0);
            for (int iy = y.intValue(); iy < y.intValue() + height.intValue(); iy++) {
                for (int ix = x.intValue(); ix < x.intValue() + width.intValue(); ix++) {
                    if (ix >= 1 && iy >= 1 && ix <= this.width && iy <= this.height) {
                        int idx = (iy - 1) * this.width + (ix - 1);
                        chars[idx] = codePoint;
                        bgs[idx] = bgColor;
                        fgs[idx] = fgColor;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @ComponentMethod
    public boolean copy(Number x, Number y, Number width, Number height, Number tx, Number ty) {
        if (tx.intValue() == 0 && ty.intValue() == 0) {
            return true;
        }

        for (int iy = y.intValue(); iy < y.intValue() + height.intValue(); iy++) {
            for (int ix = x.intValue(); ix < x.intValue() + width.intValue(); ix++) {
                if (ix >= 1 && iy >= 1 && ix <= this.width && iy <= this.height) {
                    int ox = ix + tx.intValue();
                    int oy = iy + ty.intValue();
                    if (ox >= 1 && oy >= 1 && ox <= this.width && oy <= this.height) {
                        int idxSrc = (iy - 1) * this.width + (ix - 1);
                        int idxDst = (oy - 1) * this.width + (ox - 1);
                        chars[idxDst] = chars[idxSrc];
                        bgs[idxDst] = bgs[idxSrc];
                        fgs[idxDst] = fgs[idxSrc];
                    }
                }
            }
        }
        return true;
    }

    @ComponentMethod
    public boolean set(Number x, Number y, String value) {
        return set(x, y, value, false);
    }

    @ComponentMethod
    public boolean set(Number x, Number y, String value, boolean vertical) {
        int pos = (y.intValue() - 1) * width + (x.intValue() - 1);
        int add = vertical ? width : 1;
        int maxPos = vertical ? (width * height) : (pos - (pos % width)) + width;

        for (int i = 0; i < value.length(); i++) {
            if (pos >= maxPos) break;
            chars[pos] = value.codePointAt(i);
            bgs[pos] = bgColor;
            fgs[pos] = fgColor;
            pos += add;
        }

        return true;
    }

    @Override
    public void render(FrameBuffer buffer) {
        buffer.blit(renderer.drawImage(this));
    }

    @Override
    public String type() {
        return "gpu";
    }
}
