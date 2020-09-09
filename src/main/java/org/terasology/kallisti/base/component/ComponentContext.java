// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.base.component;

import org.terasology.kallisti.base.interfaces.Identifiable;

/**
 * The context of a component, linking it to its in-game representation.
 * <p>
 * Please note that a valid ComponentContext MUST implement equals() and hashCode().
 */
public interface ComponentContext extends Identifiable {
}
