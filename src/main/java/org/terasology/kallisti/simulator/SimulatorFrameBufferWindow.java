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

package org.terasology.kallisti.simulator;

import org.terasology.kallisti.base.component.ComponentEventListener;
import org.terasology.kallisti.base.component.ComponentTickEvent;
import org.terasology.kallisti.base.interfaces.FrameBuffer;
import org.terasology.kallisti.base.interfaces.Synchronizable;
import org.terasology.kallisti.base.util.PixelDimension;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SimulatorFrameBufferWindow implements FrameBuffer {
    public class Canvas extends JComponent {
        @Override
        public void paintComponent(Graphics gz) {
            if (image != null) {
                gz.drawImage(image, 0, 0, null);
            }
        }
    }

    private final JFrame window;
    private final Canvas canvas;
    private BufferedImage image;
    private Synchronizable source;
    private Renderer rendererSrc, rendererDst;
    private boolean sentInitialPacket = false;

    public SimulatorFrameBufferWindow(String windowName) {
        window = new JFrame(windowName);
        canvas = new Canvas();
        window.add(canvas);
        window.setVisible(true);
    }

    @ComponentEventListener
    public void update(ComponentTickEvent event) {
        if (source != null && rendererSrc != null) {
            if (rendererDst != null) {
                // send source to renderer
                try {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                    source.writeSyncPacket(sentInitialPacket ? Synchronizable.Type.DELTA : Synchronizable.Type.INITIAL, outputStream);

                    ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                    outputStream.close();

                    rendererDst.update(inputStream);

                    inputStream.close();
                    sentInitialPacket = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                rendererDst.render(this);
            } else {
                rendererSrc.render(this);
            }
            Dimension oldDim = canvas.getPreferredSize();
            Dimension dim = new Dimension(image.getWidth(), image.getHeight());
            canvas.setSize(dim);
            canvas.setPreferredSize(dim);
            if (!dim.equals(oldDim)) {
                window.pack();
            }
            canvas.repaint();
        }
    }

    @Override
    public void bind(Synchronizable source, Renderer renderer) {
        this.source = source;
        this.rendererSrc = renderer;
        // TODO: Add logic to spawn the new Renderer?
        /* if (renderer instanceof OCGPURenderer) {
            this.rendererDst = new OCGPURenderer(((OCGPURenderer) renderer).getTextRenderer());
        } else {
            this.rendererDst = null;
        } */
        this.sentInitialPacket = false;
    }

    @Override
    public org.terasology.kallisti.base.util.Dimension aspectRatio() {
        return new org.terasology.kallisti.base.util.Dimension(1, 1); // TODO
    }

    @Override
    public void blit(Image image) {
        PixelDimension size = image.size();
        int[] data = image.data();

        this.image = new BufferedImage(size.getX(), size.getY(), BufferedImage.TYPE_INT_ARGB);
        this.image.setRGB(0, 0, this.image.getWidth(), this.image.getHeight(), data, 0, this.image.getWidth());
    }
}
