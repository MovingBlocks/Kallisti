// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.simulator;

import org.terasology.kallisti.base.interfaces.KeyboardInputProvider;

import javax.swing.JFrame;
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
        int k = keyEvent.getKeyChar();
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
