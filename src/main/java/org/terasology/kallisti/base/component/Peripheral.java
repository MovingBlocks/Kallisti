// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.base.component;

/**
 * Interface for Components which serve as Peripherals for a given computer.
 * <p>
 * TODO: Is this temporary?
 */
@ComponentInterface
public interface Peripheral {
    String type();
}
