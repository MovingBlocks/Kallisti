// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.base.proxy;

public class Proxy<T> {
    protected final T parent;

    public Proxy(T parent) {
        this.parent = parent;
    }

    public T getParent() {
        return parent;
    }
}
