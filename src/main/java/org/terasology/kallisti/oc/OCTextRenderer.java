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

import org.terasology.kallisti.base.interfaces.FrameBuffer;
import org.terasology.kallisti.base.util.SimpleFrameBufferImage;

public class OCTextRenderer {
    private final OCFont font;

    public OCTextRenderer(OCFont font) {
        this.font = font;
    }

    public FrameBuffer.Image drawImage(OCGPURenderer gpu) {
        if (gpu.getViewportWidth() <= 0 || gpu.getViewportHeight() <= 0) {
            return new SimpleFrameBufferImage(1, 1);
        }

        FrameBuffer.Image image = new SimpleFrameBufferImage(gpu.getViewportWidth()*font.getFontWidth(), gpu.getViewportHeight()*font.getFontHeight());
        for (int y = 0; y < gpu.getViewportHeight(); y++) {
            for (int x = 0; x < gpu.getViewportWidth(); x++) {
                int w = drawChar(image, x*font.getFontWidth(), y*font.getFontHeight(), gpu.getChar(x, y), gpu.getPaletteColor(gpu.getBG(x, y)), gpu.getPaletteColor(gpu.getFG(x, y)));
                x += (w / font.getFontWidth()) - 1;
            }
        }
        return image;
    }

    protected int drawChar(FrameBuffer.Image image, int px, int py, int chr, int bg, int fg) {
        int[] imgData = image.data();
        byte[] data = font.getData(chr);

        int fontWidth = data.length * 8 / font.getFontHeight();
        int p = 0;
        for (int iy = 0; iy < font.getFontHeight(); iy++) {
            int dp = (py + iy) * image.size().getX() + px;
            for (int ix = 0; ix < fontWidth; ix++, dp++, p++) {
                int v = data[p >> 3] & (1 << ((p ^ 7) & 7));
                imgData[dp] = 0xFF000000 | (v != 0 ? fg : bg);
            }
        }

        return fontWidth;
    }

    public OCFont getFont() {
        return font;
    }
}
