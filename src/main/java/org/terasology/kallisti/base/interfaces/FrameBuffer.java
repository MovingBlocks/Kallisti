// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.base.interfaces;

import org.terasology.kallisti.base.component.ComponentInterface;
import org.terasology.kallisti.base.util.Dimension;
import org.terasology.kallisti.base.util.PixelDimension;

import java.io.IOException;
import java.io.InputStream;

/**
 * TODO: Is this temporary?
 */
@ComponentInterface
public interface FrameBuffer {
    void bind(Synchronizable source, Renderer renderer);

    Dimension aspectRatio();

    void blit(Image image);

    interface Image {
        PixelDimension size();

        int[] data();
    }

    interface Renderer extends Synchronizable.Receiver {
        void update(InputStream stream) throws IOException;

        void render(FrameBuffer buffer);
    }
}
