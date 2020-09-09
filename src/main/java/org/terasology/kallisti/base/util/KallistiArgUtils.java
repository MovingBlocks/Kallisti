// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.base.util;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

public final class KallistiArgUtils {
    private KallistiArgUtils() {

    }

    public static Object getNullObjectFor(Class type) {
        if (type == Optional.class) {
            return Optional.empty();
        } else {
            return null;
        }
    }

    public static Object getObjectFor(Class type, Object o) {
        if (type == Optional.class) {
            return Optional.ofNullable(o);
        } else {
            return o;
        }
    }

    public static Optional<Method> findClosestMethod(int argCount, Stream<Method> methods, boolean allowOptionals) {
        return methods.filter((m) -> {
            if (m.isVarArgs()) {
                return argCount >= m.getParameterCount() - 1;
            } else {
                if (allowOptionals) {
                    int maxArgCount = m.getParameterCount();
                    int minArgCount = maxArgCount;
                    while (minArgCount > 0) {
                        if (m.getParameterTypes()[minArgCount - 1] == Optional.class) {
                            minArgCount--;
                        } else {
                            break;
                        }
                    }
                    return argCount >= minArgCount && argCount <= maxArgCount;
                } else {
                    return argCount == m.getParameterCount();
                }
            }
        }).findFirst();
    }
}
