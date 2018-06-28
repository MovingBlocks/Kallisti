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

import org.terasology.kallisti.base.component.ComponentEventListener;
import org.terasology.kallisti.base.component.ComponentRule;
import org.terasology.kallisti.base.component.ComponentTickEvent;
import org.terasology.kallisti.base.component.Peripheral;
import org.terasology.kallisti.base.interfaces.KeyboardInputProvider;
import org.terasology.kallisti.base.util.keyboard.TranslationAWTLWJGL;

import java.util.HashMap;
import java.util.Map;

import static java.awt.event.KeyEvent.*;
import static java.awt.event.KeyEvent.VK_DELETE;
import static java.awt.event.KeyEvent.VK_INSERT;

public class PeripheralOCKeyboard implements Peripheral {
    private final MachineOpenComputers machine;
    private final KeyboardInputProvider provider;

    @ComponentRule
    public PeripheralOCKeyboard(MachineOpenComputers machine, KeyboardInputProvider provider) {
        this.machine = machine;
        this.provider = provider;
    }

    @ComponentEventListener
    public void onTick(ComponentTickEvent event) {
        while (provider.hasNextKey()) {
            KeyboardInputProvider.Key key = provider.nextKey();

            int chr = key.getChar();
            int code = key.getCode();
            if (chr < 0) {
                chr = 0;
            }

            if (TranslationAWTLWJGL.hasAwt(code)) {
                String name = key.getType() == KeyboardInputProvider.KeyType.PRESSED ? "key_down" : "key_up";
                String address = machine.getComponentAddress(this);

                machine.pushSignal(name, address, chr, TranslationAWTLWJGL.toLwjgl(code), "Player");
            }
        }
    }

    @Override
    public String type() {
        return "keyboard";
    }
}
