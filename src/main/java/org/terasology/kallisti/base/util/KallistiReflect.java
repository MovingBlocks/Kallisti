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

package org.terasology.kallisti.base.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * Utility methods for
 */
public final class KallistiReflect {
    private KallistiReflect() {

    }

    /**
     * Get an iterable view of all classes extended or implemented by the
     * given class, recursively.
     * @param cc The class.
     */
    public static Iterable<Class> classes(Class cc) {
        return () -> {
            final LinkedList<Class> clist = new LinkedList<>();
            clist.add(cc);

            return new Iterator<Class>() {
                @Override
                public boolean hasNext() {
                    return !clist.isEmpty();
                }

                @Override
                public Class next() {
                    Class c = clist.remove();
                    if (c != Object.class && c.getSuperclass() != null) {
                        clist.add(c.getSuperclass());
                    }
                    for (Class ci : c.getInterfaces()) {
                        if (ci != null) {
                            clist.add(ci);
                        }
                    }
                    return c;
                }
            };
        };
    }

    /**
     * Find the closest matching entry in a given map of classes.
     *
     * "Closest" means "jumping the fewest subclasses/interfaces", or
     * "most accurate".
     *
     * @param map The map of classes.
     * @param cc The class to find in the map.
     * @param <V> The type of the desired entry.
     * @return The desired entry, or null if not present.
     */
    public static <V> V findClosestMatchingClass(Map<Class, V> map, Class cc) {
        V v;

        for (Class c : classes(cc)) {
            if ((v = map.get(c)) != null) {
                return v;
            }
        }

        return null;
    }
}
