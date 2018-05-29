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

package org.terasology.kallisti.base.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method or constructor which, taking in multiple arguments, creates
 * a new component.
 *
 * Arguments may be components or provided non-component objects, such
 * as a Machine. All permutations of them will be evaluated with the Rule.
 * All objects are required to be present at least once, unless Optional is
 * utilized. The Contexts of all Components will be joined together.
 *
 * There are two types of rules:
 * - creation rules - non-variable argument methods,
 * - joining rules - variable argument methods returning the same type as
 *   their argument.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface ComponentRule {
    int priority() default 0;
}
