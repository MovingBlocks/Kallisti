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

import com.sun.org.apache.bcel.internal.generic.IFNONNULL;

import java.lang.reflect.Method;
import java.util.Collection;
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
