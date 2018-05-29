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

package org.terasology.kallisti.base.util;

import org.terasology.kallisti.base.interfaces.FrameBuffer;

public class SimpleFrameBufferImage implements FrameBuffer.Image {
    private final PixelDimension size;
    private final int[] data;

    public SimpleFrameBufferImage(int w, int h) {
        this(w, h, new int[w * h]);
    }


    public SimpleFrameBufferImage(int w, int h, int[] data) {
        assert w*h == data.length;
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
