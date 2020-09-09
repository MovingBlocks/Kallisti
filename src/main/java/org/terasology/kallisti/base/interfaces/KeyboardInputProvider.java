// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.base.interfaces;

import org.terasology.kallisti.base.component.ComponentInterface;

/**
 * Engine-side interface for providing keyboard input to the computer.
 */
@ComponentInterface
public interface KeyboardInputProvider {
    /**
     * @return Whether or not there's a key waiting.
     */
    boolean hasNextKey();

    /**
     * @return The waiting key, which is then removed.
     */
    Key nextKey();

    enum KeyType {
        PRESSED,
        RELEASED
    }

    class Key {
        /**
         * The type of the registered key operation.
         */
        private final KeyType type;

        /**
         * The key code, as defined by Java's VK_ constants.
         */
        private final int code;

        /**
         * The codepoint of the key, or -1 if none.
         */
        private final int chr;

        public Key(KeyType type, int code, int chr) {
            this.type = type;
            this.code = code;
            this.chr = chr;
        }

        public KeyType getType() {
            return type;
        }

        public int getCode() {
            return code;
        }

        public int getChar() {
            return chr;
        }
    }
}
