// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.oc.proxy;

/**
 * This interface is used by the internal "userdata" library in OpenComputers for proxying certain metamethod calls for
 * Java-originating objects.
 * <p>
 * You may either implement it on the class or register it through a method in MachineOpenComputers.
 *
 * @param <T> The type this interface handles.
 */
public interface OCUserdataProxy<T> {
    Object index(T object, Object key);

    default void newindex(T object, Object key, Object value) {
    }

    default Object[] invoke(T object, Object... args) {
        System.err.println("Cannot invoke on " + object.getClass() + "!");
        throw new RuntimeException("Cannot invoke on " + object.getClass() + "!");
    }
}
