// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.base.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method in an object-based peripheral which should be exposed to the end user.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ComponentMethod {
    /**
     * @return The target name of the method. If empty, the Java method name is used.
     */
    String name() default "";

    /**
     * @return If this is true and the method's return type is an array, the array will be returned as multiple
     *         arguments on compatible platforms.
     */
    boolean returnsMultipleArguments() default false;

    /**
     * @return If the method execution requires synchronization with the in-game execution thread, as opposed to running
     *         on the computer's separate thread.
     */
    boolean synchronize() default false;
}
