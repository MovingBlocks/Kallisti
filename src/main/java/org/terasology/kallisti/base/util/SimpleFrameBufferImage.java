// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.base.util;

import org.terasology.kallisti.base.interfaces.FrameBuffer;

public class SimpleFrameBufferImage implements FrameBuffer.Image {
    private final PixelDimension size;
    private final int[] data;

    public SimpleFrameBufferImage(int w, int h) {
        this(w, h, new int[w * h]);
    }


    public SimpleFrameBufferImage(int w, int h, int[] data) {
        assert w * h == data.length;
        this.size = new PixelDimension(w, h);
        this.data = data;
    }

    @Override
    public PixelDimension size() {
        return size;
    }

    @Override
    public int[] data() {
        return data;
    }
}
