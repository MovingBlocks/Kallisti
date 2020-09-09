// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.oc;

import org.terasology.kallisti.base.component.ComponentMethod;

public class ShimSystem {
    private final MachineOpenComputers machine;

    public ShimSystem(MachineOpenComputers machine) {
        this.machine = machine;
    }

    @ComponentMethod
    public double timeout() {
        return 1.0; // TODO
    }

    @ComponentMethod
    public boolean allowBytecode() {
        return false; // TODO
    }

    @ComponentMethod
    public boolean allowGC() {
        return false; // TODO
    }
}
