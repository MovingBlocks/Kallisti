// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.base.interfaces;

/**
 * A generic interface for objects which can be labeled. Primarily used on ComponentContexts.
 */
public interface Labelable {
    String getLabel();

    boolean setLabel(String label);
}
