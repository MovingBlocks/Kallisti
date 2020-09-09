// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.base.proxy;

@FunctionalInterface
public interface ProxyFactory<T> {
    Proxy<T> create(T object);
}
