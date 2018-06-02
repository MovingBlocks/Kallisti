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

package org.terasology.kallisti.oc;

/**
 * This interface is used by the internal "userdata" library in OpenComputers
 * for proxying certain metamethod calls for Java-originating objects.
 *
 * You may either implement it on the class or register it through
 * a method in MachineOpenComputers.
 *
 * @param <T> The type this interface handles.
 */
public interface OCLuaProxy<T> {
    Object index(T object, Object key);

    default void newindex(T object, Object key, Object value) {
    }

    default Object[] invoke(T object, Object... args) {
        System.err.println("Cannot invoke on " + object.getClass() + "!");
        throw new RuntimeException("Cannot invoke on " + object.getClass() + "!");
    }
}
