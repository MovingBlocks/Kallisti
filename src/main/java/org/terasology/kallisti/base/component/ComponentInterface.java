// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.base.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an interface (or class) used in component queries.
 * <p>
 * Any class implementing an interface annotated with this, extending a class annotated with this or being annotated
 * with this itself will be counted as a component which may be queried using the Machine's lookup methods. As such,
 * only one of its type will be able to exist per Context.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ComponentInterface {
}
