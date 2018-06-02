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

package org.terasology.kallisti.simulator;

import org.terasology.kallisti.base.component.ComponentContext;
import org.terasology.kallisti.base.interfaces.ConnectedContext;
import org.terasology.kallisti.base.interfaces.Identifiable;

import java.util.ArrayList;
import java.util.List;

public class SimulatorComponentContext implements ComponentContext, ConnectedContext {
    private final String id;
    private final List<ComponentContext> contexts;

    public SimulatorComponentContext(String id) {
        this.id = id;
        this.contexts = new ArrayList<>();
    }

    public void addConnection(SimulatorComponentContext other) {
        if (!contexts.contains(other)) {
            contexts.add(other);
        }

        if (!other.contexts.contains(this)) {
            other.contexts.add(this);
        }
    }

    @Override
    public String identifier() {
        return id;
    }

    @Override
    public List<ComponentContext> getNeighbors() {
        return contexts;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Identifiable)) {
            return false;
        } else {
            return ((Identifiable) other).identifier().equals(identifier());
        }
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
