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
import org.terasology.kallisti.base.interfaces.FrameBuffer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class OCGPURenderer implements FrameBuffer.Renderer {
    private final OCTextRenderer textRenderer;
    private int[] palette;

    private int[] chars = new int[0];
    private int[] bgs = new int[0];
    private int[] fgs = new int[0];

    private int width, height, bitDepthUsed;

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

    public OCGPURenderer(OCTextRenderer textRenderer) {
        this.textRenderer = textRenderer;
    }

    protected OCGPURenderer(OCTextRenderer textRenderer, int[] palette, int bitDepthUsed) {
        this.textRenderer = textRenderer;
        this.palette = palette;
        this.bitDepthUsed = bitDepthUsed;
    }

    public int getChar(int x, int y) {
        return chars[(y * width) + x];
    }

    public int getBG(int x, int y) {
        return bgs[(y * width) + x];
    }

    public int getFG(int x, int y) {
        return fgs[(y * width) + x];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getBitDepthUsed() {
        return bitDepthUsed;
    }

    public void setBitDepthUsed(int depth) {
        this.bitDepthUsed = depth;
    }

    public int getAccessiblePaletteSize() {
        return Math.min(1 << bitDepthUsed, palette.length);
    }

    public int getPaletteSize() {
        return palette.length;
    }

    public int getPaletteColor(int i) {
        return i >= 0 && i < palette.length ? palette[i] : 0;
    }

    public void setPaletteColor(int i, int v) {
        palette[i] = v;
    }

    private static int[] rescale(int[] array, int oldWidth, int oldHeight, int newWidth, int newHeight) {
        if (oldWidth != newWidth || oldHeight != newHeight) {
            int[] newArray = new int[newWidth * newHeight];
            int xSize = Math.min(oldWidth, newWidth);
            for (int iy = 0; iy < Math.min(oldHeight, newHeight); iy++) {
                System.arraycopy(array, iy * oldWidth, newArray, iy * newWidth, xSize);
            }
            return newArray;
        } else {
            return array;
        }
    }

    public void setResolution(int width, int height) {
        this.chars = rescale(this.chars, this.width, this.height, width, height);
        this.bgs = rescale(this.bgs, this.width, this.height, width, height);
        this.fgs = rescale(this.fgs, this.width, this.height, width, height);
        this.width = width;
        this.height = height;
    }

    @Override
    public void update(InputStream stream) throws IOException {
        DataInputStream dataStream = new DataInputStream(stream);
        int type = dataStream.readUnsignedByte();

        if (type == 0x01) { // INITIAL packet
            int nwidth = dataStream.readUnsignedShort();
            int nheight = dataStream.readUnsignedShort();
            setResolution(nwidth, nheight);

            bitDepthUsed = dataStream.readUnsignedByte();
            palette = new int[dataStream.readInt()];
            for (int i = 0; i < palette.length; i++) {
                palette[i] = dataStream.readInt();
            }

            for (int i = 0; i < getWidth() * getHeight(); i++)
                chars[i] = dataStream.readInt();
            for (int i = 0; i < getWidth() * getHeight(); i++)
                bgs[i] = dataStream.readInt();
            for (int i = 0; i < getWidth() * getHeight(); i++)
                fgs[i] = dataStream.readInt();
        } else if (type == 0x02) { // DELTA packet
            int size = dataStream.readInt();
            for (int i = 0; i < size; i++) {
                OCGPUCommand command = OCGPUCommand.read(dataStream);
                command.apply(this);
            }
        }

        dataStream.close();
    }

    @Override
    public void render(FrameBuffer buffer) {
        if (palette != null /* initialized by constructor or update(...) */) {
            buffer.blit(textRenderer.drawImage(this));
        }
    }

    public void fill(int bg, int fg, int x, int y, int width, int height, int codepoint) {
        for (int iy = y; iy < y + height; iy++) {
            for (int ix = x; ix < x + width; ix++) {
                if (ix >= 1 && iy >= 1 && ix <= width && iy <= height) {
                    int idx = (iy - 1) * this.width + (ix - 1);
                    chars[idx] = codepoint;
                    bgs[idx] = bg;
                    fgs[idx] = fg;
                }
            }
        }
    }

    public void copy(int x, int y, int width, int height, int tx, int ty) {
        for (int iy = y; iy < y + height; iy++) {
            for (int ix = x; ix < x + width; ix++) {
                if (ix >= 1 && iy >= 1 && ix <= this.width && iy <= this.height) {
                    int ox = ix + tx;
                    int oy = iy + ty;
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
    }

    public void set(int bg, int fg, int x, int y, boolean vertical, String value) {
        int pos = (y - 1) * width + (x - 1);
        int add = vertical ? width : 1;
        int maxPos = vertical ? (width * height) : (pos - (pos % width)) + width;

        for (int i = 0; i < value.length(); i++) {
            if (pos >= maxPos) break;
            chars[pos] = value.codePointAt(i);
            bgs[pos] = bg;
            fgs[pos] = fg;
            pos += add;
        }
    }

    protected void writeInitialPacket(DataOutputStream dataStream) throws IOException {
        dataStream.writeShort(getWidth());
        dataStream.writeShort(getHeight());
        dataStream.writeByte(getBitDepthUsed());
        dataStream.writeInt(getPaletteSize());

        for (int i = 0; i < getPaletteSize(); i++)
            dataStream.writeInt(getPaletteColor(i));

        for (int i = 0; i < getWidth() * getHeight(); i++)
            dataStream.writeInt(chars[i]);
        for (int i = 0; i < getWidth() * getHeight(); i++)
            dataStream.writeInt(bgs[i]);
        for (int i = 0; i < getWidth() * getHeight(); i++)
            dataStream.writeInt(fgs[i]);
    }

    public OCTextRenderer getTextRenderer() {
        return textRenderer;
    }
}
