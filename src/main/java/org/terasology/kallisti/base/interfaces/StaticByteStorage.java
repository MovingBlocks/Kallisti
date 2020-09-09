// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.base.interfaces;

import org.terasology.kallisti.base.component.ComponentInterface;

/**
 * An interface for static-sized, byte array-backed storage.
 * <p>
 * For example, OpenComputers EEPROMs, or a ROM used by an 8-bit microcomputer.
 */
@ComponentInterface
public interface StaticByteStorage {
    /**
     * Returns the backing byte array, which can also be queried for size.
     * <p>
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
