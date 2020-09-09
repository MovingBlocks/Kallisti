// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.simulator;

import com.google.gson.JsonObject;
import org.terasology.kallisti.base.component.ComponentContext;

import java.util.HashMap;
import java.util.Map;

public class SimulatorInstantiationManager {
    private final Map<String, Registrar<Object>> instanceCreators;

    public SimulatorInstantiationManager() {
        instanceCreators = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T> void register(String name, Registrar<T> function) {
        instanceCreators.put(name, (Registrar<Object>) function);
    }

    @SuppressWarnings("unchecked")
    public <T> T instantiate(String name, Object owner, ComponentContext context, JsonObject object, Class<T> c) {
        Registrar<Object> creator = instanceCreators.get(name);
        if (creator == null) {
            throw new RuntimeException("Could not instantiate " + name + "!");
        }
        try {
            Object o = creator.apply(owner, context, object);
            if (o == null || !(c.isAssignableFrom(o.getClass()))) {
                throw new RuntimeException("Could not instantiate " + name + "!");
            }
            return (T) o;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface Registrar<T> {
        T apply(Object owner, ComponentContext context, JsonObject json) throws Exception;
    }
}
