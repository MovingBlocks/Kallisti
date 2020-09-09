// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.base.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public final class KallistiReflect {
    private KallistiReflect() {

    }

    /**
     * Get an iterable view of all classes extended or implemented by the given class, recursively.
     *
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
