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

import com.google.gson.JsonObject;
import org.terasology.kallisti.base.component.ComponentContext;

import java.util.HashMap;
import java.util.Map;

public class SimulatorInstantiationManager {
    @FunctionalInterface
    public interface Registrar<T> {
        T apply(Object owner, ComponentContext context, JsonObject json) throws Exception;
    }

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
}
