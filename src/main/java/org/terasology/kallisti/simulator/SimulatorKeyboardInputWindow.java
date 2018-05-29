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

import org.terasology.kallisti.base.interfaces.KeyboardInputProvider;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;

public class SimulatorKeyboardInputWindow implements KeyboardInputProvider, KeyListener {
    protected final LinkedList<Key> keys = new LinkedList<>();
    private final JFrame window;

    public SimulatorKeyboardInputWindow(String windowName) {
        window = new JFrame(windowName);
        window.addKeyListener(this);
        window.pack();
        window.setVisible(true);
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {

    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        keys.add(new KeyboardInputProvider.Key(KeyboardInputProvider.KeyType.PRESSED,
                keyEvent.getKeyCode(), getKeyChar(keyEvent)));
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        keys.add(new KeyboardInputProvider.Key(KeyboardInputProvider.KeyType.RELEASED,
                keyEvent.getKeyCode(), getKeyChar(keyEvent)));
    }

    private int getKeyChar(KeyEvent keyEvent) {
        int k = (int) keyEvent.getKeyChar();
        return k == 65535 ? -1 : k;
    }

    @Override
    public boolean hasNextKey() {
        return !keys.isEmpty();
    }

    @Override
    public Key nextKey() {
        return keys.removeFirst();
    }
}
