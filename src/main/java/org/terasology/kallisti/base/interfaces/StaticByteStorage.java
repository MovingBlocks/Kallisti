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
 * An interface for static-sized, byte array-backed storage.
 *
 * For example, OpenComputers EEPROMs, or a ROM used by an 8-bit
 * microcomputer.
 */
@ComponentInterface
public interface StaticByteStorage {
    /**
     * Returns the backing byte array, which can also be queried for size.
     *
     * Please do not modify the byte array without first calling .canModify().
     *
     * @return The backing byte array.
     */
    byte[] get();

    /**
     * @return Whether the byte array can be modified.
     */
    default boolean canModify() {
        return true;
    }

    /**
     * Marks the backing byte array as having been modified.
     */
    void markModified();
}
