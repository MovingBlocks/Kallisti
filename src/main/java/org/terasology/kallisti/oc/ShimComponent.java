// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.oc;

import org.terasology.jnlua.LuaState;
import org.terasology.jnlua.LuaValueProxy;
import org.terasology.kallisti.base.component.ComponentContext;
import org.terasology.kallisti.base.component.ComponentMethod;
import org.terasology.kallisti.base.component.Peripheral;

import java.util.Optional;

public class ShimComponent extends ShimInvoker<Object> {
    public ShimComponent(MachineOpenComputers machine) {
        super(machine);
    }

    @Override
    protected Object get(Object value) {
        return value instanceof String ? machine.getPeripheral((String) value) : null;
    }

    @ComponentMethod
    public String doc(String address, String method) {
        return "";
    }

    @ComponentMethod
    public String type(String address) {
        Object o = get(address);
        return machine.getComponentType(o);
    }

    @ComponentMethod
    public String slot(String address) {
        // TODO
        return "unknown";
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @ComponentMethod
    public LuaValueProxy list(Optional<String> filter, Optional<Boolean> oexact) {
        boolean exact = oexact.orElse(!filter.isPresent());
        LuaState state = machine.getLuaState();
        state.newTable();
        for (ComponentContext ctx : machine.getContextsByClass(Peripheral.class)) {
            Object value = machine.getComponent(ctx, Peripheral.class);
            String type = machine.getComponentType(value);

            if (!filter.isPresent() || filter.get().length() == 0 || (exact && filter.get().equals(type)) || (!exact && type.contains(filter.get()))) {
                state.pushString(type);
                state.setField(-2, machine.getComponentAddress(value));
            }
        }

        LuaValueProxy proxy = state.getProxy(-1);
        state.pop(1);
        return proxy;
    }
}
