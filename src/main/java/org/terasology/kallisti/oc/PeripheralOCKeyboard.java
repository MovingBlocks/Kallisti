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

import org.terasology.kallisti.base.component.ComponentRule;
import org.terasology.kallisti.base.component.Peripheral;
import org.terasology.kallisti.base.interfaces.KeyboardInputProvider;

import java.util.HashMap;
import java.util.Map;

import static java.awt.event.KeyEvent.*;
import static java.awt.event.KeyEvent.VK_DELETE;
import static java.awt.event.KeyEvent.VK_INSERT;

public class PeripheralOCKeyboard implements OCEventProvider, Peripheral {
    private static final Map<Integer, Integer> vkToCode = new HashMap<>();

    static {
        int i, j;

        // 0-9
        for (j = 2, i = VK_1; i <= VK_9; i++, j++) vkToCode.put(i, j);
        vkToCode.put(VK_0, 0x0B);

        // A-Z
        int[] chars = new int[] { 0x1E, 0x30, 0x2E, 0x20, 0x12, 0x21, 0x22, 0x23, 0x17, 0x24, 0x25, 0x26, 0x32, 0x31, 0x18, 0x19, 0x10, 0x13, 0x1F, 0x14, 0x16, 0x2F, 0x11, 0x2D, 0x15, 0x2C };
        for (j = 0, i = VK_A; i <= VK_Z; i++, j++) vkToCode.put(i, chars[j]);

        // F1-F12
        for (j = 0x3B, i = VK_F1; i <= VK_F12; i++, j++) vkToCode.put(i, j);

        vkToCode.put(VK_QUOTE, 0x28);
        vkToCode.put(VK_AT, 0x91);
        vkToCode.put(VK_BACK_SPACE, 0x0E);
        vkToCode.put(VK_BACK_SLASH, 0x2B);
        vkToCode.put(VK_CAPS_LOCK, 0x3A);
        vkToCode.put(VK_COLON, 0x92);
        vkToCode.put(VK_COMMA, 0x33);
        vkToCode.put(VK_ENTER, 0x1C);
        vkToCode.put(VK_EQUALS, 0x0D);
        vkToCode.put(VK_BACK_QUOTE, 0x29);
        vkToCode.put(VK_OPEN_BRACKET, 0x1A);
        vkToCode.put(VK_CONTROL, 0x1D);
        // lmenu = 0x38
        vkToCode.put(VK_SHIFT, 0x2A);
        vkToCode.put(VK_MINUS, 0x0C);
        vkToCode.put(VK_NUM_LOCK, 0x45);
        vkToCode.put(VK_PAUSE, 0xC5);
        vkToCode.put(VK_PERIOD, 0x34);
        vkToCode.put(VK_CLOSE_BRACKET, 0x1B);
        // rcontrol = 0x9D
        // rmenu = 0xB8
        // rshift = 0x36
        vkToCode.put(VK_SCROLL_LOCK, 0x46);
        vkToCode.put(VK_SEMICOLON, 0x27);
        vkToCode.put(VK_SLASH, 0x35);
        vkToCode.put(VK_SPACE, 0x39);
        vkToCode.put(VK_STOP, 0x95);
        vkToCode.put(VK_TAB, 0x0F);
        vkToCode.put(VK_UNDEFINED, 0x93);

        // keypad
        vkToCode.put(VK_UP, 0xC8);
        vkToCode.put(VK_DOWN, 0xD0);
        vkToCode.put(VK_LEFT, 0xCB);
        vkToCode.put(VK_RIGHT, 0xCD);
        vkToCode.put(VK_HOME, 0xC7);
        vkToCode.put(VK_END, 0xCF);
        vkToCode.put(VK_PAGE_UP, 0xC9);
        vkToCode.put(VK_PAGE_DOWN, 0xD1);
        vkToCode.put(VK_INSERT, 0xD2);
        vkToCode.put(VK_DELETE, 0xD3);

        // numpad
    }

    private final KeyboardInputProvider provider;

    @ComponentRule
    public PeripheralOCKeyboard(KeyboardInputProvider provider) {
        this.provider = provider;
    }

    @Override
    public void gatherEvents(MachineOpenComputers machine) {
        while (provider.hasNextKey()) {
            KeyboardInputProvider.Key key = provider.nextKey();

            int chr = key.getChar();
            int code = key.getCode();
            if (chr < 0) {
                chr = 0;
            }

            if (vkToCode.containsKey(code)) {
                String name = key.getType() == KeyboardInputProvider.KeyType.PRESSED ? "key_down" : "key_up";
                String address = machine.getComponentAddress(this);

                machine.pushSignal(name, address, chr, vkToCode.get(code), "Player");
            }
        }
    }

    @Override
    public String type() {
        return "keyboard";
    }
}
