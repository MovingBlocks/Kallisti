// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.oc;

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

    private int viewportWidth, viewportHeight;
    private int width, height, bitDepthUsed;

    public OCGPURenderer(OCTextRenderer textRenderer) {
        this.textRenderer = textRenderer;
    }

    protected OCGPURenderer(OCTextRenderer textRenderer, int[] palette, int bitDepthUsed) {
        this.textRenderer = textRenderer;
        this.palette = palette;
        this.bitDepthUsed = bitDepthUsed;
    }

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

    public int getViewportWidth() {
        return viewportWidth;
    }

    public int getViewportHeight() {
        return viewportHeight;
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

    public void setResolution(int width, int height, int viewportWidth, int viewportHeight) {
        this.chars = rescale(this.chars, this.width, this.height, width, height);
        this.bgs = rescale(this.bgs, this.width, this.height, width, height);
        this.fgs = rescale(this.fgs, this.width, this.height, width, height);
        this.width = width;
        this.height = height;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
    }

    @Override
    public void update(InputStream stream) throws IOException {
        DataInputStream dataStream = new DataInputStream(stream);
        int type = dataStream.readUnsignedByte();

        if (type == 0x01) { // INITIAL packet
            readInitialPacket(dataStream);
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
                if (ix >= 1 && iy >= 1 && ix <= this.width && iy <= this.height) {
                    int idx = (iy - 1) * this.width + (ix - 1);
                    chars[idx] = codepoint;
                    bgs[idx] = bg;
                    fgs[idx] = fg;
                }
            }
        }
    }

    public void copy(int x, int y, int width, int height, int tx, int ty) {
        int[] charB = new int[width * height];
        int[] bgB = new int[width * height];
        int[] fgB = new int[width * height];
        int i;

        i = 0;
        for (int iy = y; iy < y + height; iy++) {
            for (int ix = x; ix < x + width; ix++, i++) {
                if (ix >= 1 && iy >= 1 && ix <= this.width && iy <= this.height) {
                    int idxSrc = (iy - 1) * this.width + (ix - 1);
                    charB[i] = chars[idxSrc];
                    bgB[i] = bgs[idxSrc];
                    fgB[i] = fgs[idxSrc];
                }
            }
        }

        i = 0;
        for (int oy = y + ty; oy < y + ty + height; oy++) {
            for (int ox = x + tx; ox < x + tx + width; ox++, i++) {
                int ix = ox - tx;
                int iy = oy - ty;
                if (ix >= 1 && iy >= 1 && ix <= this.width && iy <= this.height) {
                    if (ox >= 1 && oy >= 1 && ox <= this.width && oy <= this.height) {
                        int idxDst = (oy - 1) * this.width + (ox - 1);
                        chars[idxDst] = charB[i];
                        bgs[idxDst] = bgB[i];
                        fgs[idxDst] = fgB[i];
                    }
                }
            }
        }
    }

    public void set(int bg, int fg, int x, int y, boolean vertical, String value) {
        int pos = (y - 1) * width + (x - 1);
        int maxPos = vertical ? (width * height) : (y * width);

        for (int i = 0; i < value.length(); i++) {
            if (pos >= maxPos) break;
            int code = value.codePointAt(i);
            chars[pos] = code;
            bgs[pos] = bg;
            fgs[pos] = fg;
            if (vertical) {
                pos += width;
            } else {
                pos += textRenderer.getFont().getCharWidth(code);
            }
        }
    }

    protected void readInitialPacket(DataInputStream dataStream) throws IOException {
        int nwidth = dataStream.readUnsignedShort();
        int nheight = dataStream.readUnsignedShort();
        int nviewportw = dataStream.readUnsignedShort();
        int nviewporth = dataStream.readUnsignedShort();
        setResolution(nwidth, nheight, nviewportw, nviewporth);

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
    }

    protected void writeInitialPacket(DataOutputStream dataStream) throws IOException {
        dataStream.writeShort(getWidth());
        dataStream.writeShort(getHeight());
        dataStream.writeShort(getViewportWidth());
        dataStream.writeShort(getViewportHeight());
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

    public int[] getPaletteArray() {
        int[] paletteCopy = new int[palette.length];
        System.arraycopy(palette, 0, paletteCopy, 0, palette.length);
        return paletteCopy;
    }
}
