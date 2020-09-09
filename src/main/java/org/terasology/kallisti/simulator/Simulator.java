// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

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
    private final double tickDuration;

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
            populateContext(componentsJson.get(i).getAsJsonObject(), objects.get(machineContext), manager, components
                    , objects);
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

    private static ComponentContext appendContextInitial(JsonObject object, Map<String, ComponentContext> components) {
        ComponentContext initial = new SimulatorComponentContext(object.get("id").getAsString());
        components.put(initial.identifier(), initial);
        return initial;
    }

    private static void populateContext(JsonObject object, Object owner, SimulatorInstantiationManager manager,
                                        Map<String, ComponentContext> components,
                                        Map<ComponentContext, Object> objects) {
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
}
