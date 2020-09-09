// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.base.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method or constructor which, taking in multiple arguments, creates a new component.
 * <p>
 * Arguments may be components or provided non-component objects, such as a Machine. All permutations of them will be
 * evaluated with the Rule. All objects are required to be present at least once, unless Optional is utilized. The
 * Contexts of all Components will be joined together.
 * <p>
 * There are two types of rules: - creation rules - non-variable argument methods, - joining rules - variable argument
 * methods returning the same type as their argument.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface ComponentRule {
    /**
     * The priority of the rule. Higher priorities are attempted first.
     *
     * @return The priority of the rule.
     */
    int priority() default 0;
}
