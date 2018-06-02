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

package org.terasology.kallisti.base.interfaces;

import org.terasology.kallisti.base.component.ComponentInterface;

/**
 * Engine-side interface for providing keyboard input to the computer.
 */
@ComponentInterface
public interface KeyboardInputProvider {
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

    /**
     * @return Whether or not there's a key waiting.
     */
    boolean hasNextKey();

    /**
     * @return The waiting key, which is then removed.
     */
    Key nextKey();
}
