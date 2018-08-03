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
import org.terasology.kallisti.base.interfaces.Persistable;
import org.terasology.kallisti.base.interfaces.Synchronizable;
import org.terasology.kallisti.base.util.KallistiColor;
import org.terasology.kallisti.base.util.KallistiMath;
import org.terasology.kallisti.base.util.PersistenceException;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class PeripheralOCGPU implements Synchronizable, Peripheral, Persistable {
    private final OCGPURenderer renderer;
    private final MachineOpenComputers machine;
    private final int maxWidth, maxHeight, bitDepth;
    private List<OCGPUCommand> commands;

    private String screenAddr;
    private int bgColor, fgColor;

    public PeripheralOCGPU(MachineOpenComputers machine, int maxWidth, int maxHeight, int[] palette) {
        this.machine = machine;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;

        int[] resizedPalette = new int[KallistiMath.smallestContainingPowerTwo(palette.length)];
        System.arraycopy(palette, 0, resizedPalette, 0, palette.length);

        this.bgColor = 0;
        this.fgColor = palette.length - 1;

        this.bitDepth = KallistiMath.log2up(resizedPalette.length);
        this.commands = new LinkedList<>();

        this.renderer = new OCGPURenderer(new OCTextRenderer(machine.font), resizedPalette, bitDepth);
        setResolution(maxWidth, maxHeight);
    }

	/**
	 * Apply a command on the server side.
	 */
	protected void apply(OCGPUCommand command) {
    	commands.add(command);
    	command.apply(renderer);
    }

    @ComponentMethod
    public boolean bind(String address) {
        for (FrameBuffer b : machine.getComponentsByClass(FrameBuffer.class)) {
            if (machine.getComponentAddress(b).equals(address)) {
                b.bind(this, new OCGPURenderer(renderer.getTextRenderer(), renderer.getPaletteArray(), bitDepth));
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
        return i >= 0 && i < renderer.getPaletteSize() ? renderer.getPaletteColor(i) : 0;
    }

    @ComponentMethod
    public void setPaletteColor(Number index, Number value) {
        int i = index.intValue();
        if (i >= 0 && i < renderer.getAccessiblePaletteSize()) {
            renderer.setPaletteColor(i, value.intValue() & 0xFFFFFF);
        }
    }

    @ComponentMethod(returnsMultipleArguments = true)
    public int[] getResolution() {
	    return new int[] { renderer.getWidth(), renderer.getHeight() };
    }

    // TODO: plan9k???
    @ComponentMethod(returnsMultipleArguments = true)
    public int[] getResolution(Object o) {
	    return new int[] { renderer.getWidth(), renderer.getHeight() };
    }

    @ComponentMethod(returnsMultipleArguments = true)
    public int[] getViewport() {
        return new int[] { renderer.getViewportWidth(), renderer.getViewportHeight() };
    }

    @ComponentMethod(returnsMultipleArguments = true)
    public int[] maxResolution() {
        return new int[] { maxWidth, maxHeight };
    }

    @ComponentMethod
    public boolean setViewport(Number widthN, Number heightN) {
        int width = widthN.intValue();
        int height = heightN.intValue();
        if (width <= renderer.getWidth() && height <= renderer.getHeight() && width >= 1 && height >= 1) {
            apply(new OCGPUCommand.SetResolution(renderer.getWidth(), renderer.getHeight(), width, height));
            return true;
        } else {
            return false;
        }
    }

    @ComponentMethod
    public boolean setResolution(Number widthN, Number heightN) {
        int width = widthN.intValue();
        int height = heightN.intValue();
        if (width <= maxWidth && height <= maxHeight && width >= 1 && height >= 1) {
            apply(new OCGPUCommand.SetResolution(width, height, width, height));
            return true;
        } else {
            return false;
        }
    }

    private int findNearest(int color, int colorCount) {
        double distance = Double.MAX_VALUE;
        int v = 0;

        // Special case (as per OC documentation): If only 1-bit and color 0 is zeroes,
        // all non-0 values round to color 1.
        if (renderer.getAccessiblePaletteSize() == 2 && renderer.getPaletteColor(0) == 0x000000) {
            return (color & 0xFFFFFF) == 0 ? 1 : 0;
        }

        for (int i = 0; i < colorCount; i++) {
            int pcolor = renderer.getPaletteColor(i);
            if (pcolor == color) {
                return i;
            } else {
                double diff = KallistiColor.distance(color, pcolor);
                if (diff < distance) {
                    distance = diff;
                    v = i;
                }
            }
        }

        return v;
    }

    private int[] getOldColorReturn(int palIdx) {
    	return new int[] { renderer.getPaletteColor(palIdx), palIdx };
    }

    @ComponentMethod
    public int getBackground() {
        return getPaletteColor(bgColor);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @ComponentMethod(returnsMultipleArguments = true)
    public int[] setBackground(Number color, Optional<Boolean> isPaletteIndex) {
        int[] oldColorReturn = getOldColorReturn(bgColor);
        int colorCount = renderer.getAccessiblePaletteSize();

        if (isPaletteIndex.orElse(false) && color.intValue() >= 0 && color.intValue() < colorCount) {
            bgColor = color.intValue();
        } else {
            bgColor = findNearest(color.intValue(), colorCount);
        }

        return oldColorReturn;
    }

    @ComponentMethod
    public int getForeground() {
        return getPaletteColor(fgColor);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @ComponentMethod(returnsMultipleArguments = true)
    public int[] setForeground(Number color, Optional<Boolean> isPaletteIndex) {
        int[] oldColorReturn = getOldColorReturn(fgColor);
        int colorCount = renderer.getAccessiblePaletteSize();

        if (isPaletteIndex.orElse(false) && color.intValue() >= 0 && color.intValue() < colorCount) {
            fgColor = color.intValue();
        } else {
            fgColor = findNearest(color.intValue(), colorCount);
        }

        return oldColorReturn;
    }

    @ComponentMethod
    public int getDepth() {
        return renderer.getBitDepthUsed();
    }

    @ComponentMethod
    public int maxDepth() {
        return bitDepth;
    }

    @ComponentMethod
    public boolean setDepth(int depth) {
        if (depth >= 1 && depth <= bitDepth) {
            renderer.setBitDepthUsed(depth);
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

        if (xi >= 1 && yi >= 1 && xi <= renderer.getWidth() && yi <= renderer.getHeight()) {
            int chr = renderer.getChar(xi - 1, yi - 1);
            String s = new StringBuilder().appendCodePoint(chr).toString();
            int[] bgColor = getOldColorReturn(renderer.getBG(xi - 1, yi - 1));
            int[] fgColor = getOldColorReturn(renderer.getFG(xi - 1, yi - 1));

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
        if (c.length() >= 1) {
            int codePoint = c.codePointAt(0);
            apply(new OCGPUCommand.Fill(bgColor, fgColor, x.intValue(), y.intValue(), width.intValue(), height.intValue(), codePoint));
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

        apply(new OCGPUCommand.Copy(x.intValue(), y.intValue(), width.intValue(), height.intValue(), tx.intValue(), ty.intValue()));
        return true;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @ComponentMethod
    public boolean set(Number x, Number y, String value, Optional<Boolean> vertical) {
		int maxLen = vertical.orElse(false) ? (maxHeight - (y.intValue() - 1)) : (maxWidth - (x.intValue() - 1));
		apply(new OCGPUCommand.Set(bgColor, fgColor, x.intValue(), y.intValue(), vertical.orElse(false), value.length() > maxLen ? value.substring(0, maxLen) : value));
        return true;
    }

    @Override
    public String type() {
        return "gpu";
    }

    @Override
    public boolean hasSyncPacket(Type type) {
        return type == Type.INITIAL || !commands.isEmpty();
    }

    @Override
	public void writeSyncPacket(Type type, OutputStream stream) throws IOException {
		DataOutputStream dataStream = new DataOutputStream(stream);
		if (type == Type.DELTA && commands.size() > (maxHeight * maxWidth / 9)) {
			type = Type.INITIAL;
		}

		if (type == Type.INITIAL) {
			dataStream.writeByte(0x01); // header byte

			renderer.writeInitialPacket(dataStream);
		} else if (type == Type.DELTA) {
			dataStream.writeByte(0x02); // header byte

			dataStream.writeInt(commands.size());
			for (OCGPUCommand command : commands) {
				OCGPUCommand.write(command, dataStream);
			}
		}

		commands.clear();
		dataStream.close();
	}

	private static final int PERSISTENCE_VERSION = 0x01;

    @Override
    public void persist(OutputStream data) throws IOException, PersistenceException {
        DataOutputStream stream = new DataOutputStream(data);
        stream.writeShort(PERSISTENCE_VERSION);
        if (screenAddr != null) {
            stream.writeBoolean(true);
            stream.writeUTF(screenAddr);
        } else {
            stream.writeBoolean(false);
        }
        stream.writeInt(bgColor);
        stream.writeInt(fgColor);
        renderer.writeInitialPacket(stream);
        stream.close();
    }

    @Override
    public void unpersist(InputStream data) throws IOException, PersistenceException {
        DataInputStream stream = new DataInputStream(data);
        int v = stream.readUnsignedShort();
        if (v > PERSISTENCE_VERSION) {
            throw new PersistenceException("Version too new!");
        }
        if (stream.readBoolean()) {
            bind(stream.readUTF());
        }
        bgColor = stream.readInt();
        fgColor = stream.readInt();
        renderer.readInitialPacket(stream);
        stream.close();
    }
}
