// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.base.component;

public class MachineInvalidStateException extends Exception {
    public MachineInvalidStateException(Machine.MachineState current) {
        super("Invalid state " + current);
    }
}
