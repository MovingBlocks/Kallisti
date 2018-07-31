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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.terasology.kallisti.base.component.ComponentContext;
import org.terasology.kallisti.base.component.Machine;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Simulator {
    private static final Gson gson = new Gson();
    private final Machine machine;
    private double tickDuration;

    public Simulator(SimulatorInstantiationManager manager, File file) throws Exception {
        JsonObject object = gson.fromJson(
                new InputStreamReader(new FileInputStream(file)),
                JsonObject.class
        );

        JsonObject machineJson = object.get("machine").getAsJsonObject();
        JsonArray componentsJson = object.get("components").getAsJsonArray();

        Map<String, ComponentContext> components = new HashMap<>();
        Map<ComponentContext, Object> objects = new HashMap<>();

        tickDuration = object.get("tickDuration").getAsDouble();

        // Phase 1: Gather all contexts
        ComponentContext machineContext = appendContextInitial(machineJson, components);
        for (int i = 0; i < componentsJson.size(); i++) {
            appendContextInitial(componentsJson.get(i).getAsJsonObject(), components);
        }

        // Phase 2: Populate contexts, including registering objects
        populateContext(machineJson, null, manager, components, objects);
        for (int i = 0; i < componentsJson.size(); i++) {
            populateContext(componentsJson.get(i).getAsJsonObject(), objects.get(machineContext), manager, components, objects);
        }

        // Phase 3: Correctness checks
        if (!(objects.get(machineContext) instanceof Machine)) {
            throw new RuntimeException("Machine context is not a Machine class!");
        }

        // Phase 4: Create machine
        machine = (Machine) objects.get(machineContext);
        for (Map.Entry<ComponentContext, Object> entry : objects.entrySet()) {
            if (!(entry.getValue() instanceof Machine)) {
                machine.addComponent(entry.getKey(), entry.getValue());
            }
        }
        machine.initialize();
    }

    public Machine getMachine() {
        return machine;
    }

    public double getTickDuration() {
        return tickDuration;
    }

    public void start() throws Exception {
        machine.start();
    }

    public boolean tick() throws Exception {
        return machine.tick(getTickDuration());
    }

    private static ComponentContext appendContextInitial(JsonObject object, Map<String, ComponentContext> components) {
        ComponentContext initial = new SimulatorComponentContext(object.get("id").getAsString());
        components.put(initial.identifier(), initial);
        return initial;
    }

    private static void populateContext(JsonObject object, Object owner, SimulatorInstantiationManager manager, Map<String, ComponentContext> components, Map<ComponentContext, Object> objects) {
        SimulatorComponentContext context = (SimulatorComponentContext) components.get(object.get("id").getAsString());

        if (object.has("connects")) {
            JsonArray array = object.get("connects").getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                context.addConnection((SimulatorComponentContext) components.get(array.get(i).getAsString()));
            }
        }

        Object o = manager.instantiate(object.get("type").getAsString(), owner, context, object, Object.class);
        if (objects.containsKey(context)) {
            throw new RuntimeException("Duplicate component context: '" + context.identifier() + "'!");
        }
        objects.put(context, o);
    }
}
