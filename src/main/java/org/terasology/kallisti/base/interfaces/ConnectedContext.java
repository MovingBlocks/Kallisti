// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.base.interfaces;

import org.terasology.kallisti.base.component.ComponentContext;

import java.util.List;

/**
 * Interface for Contexts which are connected with other Components in a neighbor-esque fashion.
 * <p>
 * TODO: Is this temporary?
 */
public interface ConnectedContext {
    List<ComponentContext> getNeighbors();
}
