// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.oc;

import org.terasology.jnlua.LuaState;
import org.terasology.jnlua.LuaValueProxy;
import org.terasology.kallisti.base.component.ComponentMethod;
import org.terasology.kallisti.base.util.KallistiArgUtils;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public abstract class ShimInvoker<V> {
    protected final MachineOpenComputers machine;

    public ShimInvoker(MachineOpenComputers machine) {
        this.machine = machine;
    }

    protected abstract V get(Object value);

    @ComponentMethod(returnsMultipleArguments = true)
    public Object[] invoke(Object key, String name, Object... args) {
        V p = get(key);
        if (p == null) {
            return null;
        }

        Optional<Method> om = KallistiArgUtils.findClosestMethod(args.length,
                Arrays.stream(p.getClass().getMethods()).filter(
                        (m) -> {
                            ComponentMethod pm;
                            if ((pm = m.getAnnotation(ComponentMethod.class)) != null) {
                                String methodName = pm.name();
                                if (methodName.isEmpty()) {
                                    methodName = m.getName();
                                }

                                return methodName.equals(name);
                            } else {
                                return false;
                            }
                        }
                ), true);

        if (!om.isPresent()) {
            return new Object[]{false, "could not find method " + name};
        }

        Method m = om.get();
        ComponentMethod pm = m.getAnnotation(ComponentMethod.class);
        Object o;
        Object[] realArgs = new Object[m.getParameterCount()];
        for (int i = 0; i < m.getParameterCount(); i++) {
            Class c = m.getParameterTypes()[i];
            if (i >= args.length) {
                realArgs[i] = KallistiArgUtils.getNullObjectFor(c);
            } else {
                realArgs[i] = KallistiArgUtils.getObjectFor(c, args[i]);
            }
        }

        try {
            o = m.invoke(p, realArgs);
        } catch (InvocationTargetException e) {
            return new Object[]{false, e.getTargetException().getMessage()};
        } catch (IllegalAccessException e) {
            return new Object[]{false, e.getMessage()};
        }

        Object[] returns = null;
        if (pm.returnsMultipleArguments()) {
            if (o.getClass().isArray()) {
                returns = new Object[Array.getLength(o) + 1];
                for (int i = 0; i < returns.length - 1; i++) {
                    returns[i + 1] = Array.get(o, i);
                }
            }
        }

        if (returns == null) {
            if (m.getReturnType() == Void.TYPE) {
                returns = new Object[1];
            } else {
                returns = new Object[2];
                returns[1] = o;
            }
        }
        returns[0] = true;
        return returns;
    }

    @ComponentMethod(returnsMultipleArguments = true)
    public LuaValueProxy methods(Object key) {
        V p = get(key);
        if (p == null) {
            return null;
        }

        LuaState state = machine.getLuaState();
        state.newTable();
        for (Method m : p.getClass().getMethods()) {
            ComponentMethod pm;
            if ((pm = m.getAnnotation(ComponentMethod.class)) != null) {
                state.newTable();
                state.pushBoolean(!pm.synchronize());
                state.setField(-2, "direct");

                state.setField(-2, pm.name().isEmpty() ? m.getName() : pm.name());
            }
        }

        LuaValueProxy proxy = state.getProxy(-1);
        state.pop(1);
        return proxy;
    }
}
