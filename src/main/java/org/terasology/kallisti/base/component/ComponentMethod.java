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
import java.lang.reflect.Method;

/**
 * Marks a method in an object-based peripheral which should be exposed
 * to the end user.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ComponentMethod {
    /**
     * @return The target name of the method. If empty, the Java method name
     * is used.
     */
    String name() default "";

    /**
     * @return If this is true and the method's return type is an array,
     * the array will be returned as multiple arguments on compatible
     * platforms.
     */
    boolean returnsMultipleArguments() default false;

    /**
     * @return If the method execution requires synchronization with the
     * in-game execution thread, as opposed to running on the
     * computer's separate thread.
     */
    boolean synchronize() default false;
}
