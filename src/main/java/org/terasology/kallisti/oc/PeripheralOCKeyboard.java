// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.oc;

import org.terasology.kallisti.base.component.ComponentEventListener;
import org.terasology.kallisti.base.component.ComponentRule;
import org.terasology.kallisti.base.component.ComponentTickEvent;
import org.terasology.kallisti.base.component.Peripheral;
import org.terasology.kallisti.base.interfaces.KeyboardInputProvider;
import org.terasology.kallisti.base.util.keyboard.TranslationAWTLWJGL;

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
