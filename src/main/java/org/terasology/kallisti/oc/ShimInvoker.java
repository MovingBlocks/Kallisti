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

package org.terasology.kallisti.oc;

import org.terasology.kallisti.base.component.ComponentMethod;
import org.terasology.jnlua.LuaState;
import org.terasology.jnlua.LuaValueProxy;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class ShimInvoker<V> {
    protected final MachineOpenComputers machine;

    public ShimInvoker(MachineOpenComputers machine) {
        this.machine = machine;
    }

    protected abstract V get(Object value);

    public MultiArgReturn invoke(Object key, String name, Object... args) {
//        System.out.println("[" + getClass().getSimpleName() + "] invoke " + name + "(" + args.length + ")");
        V p = get(key);
        if (p == null) {
            return null;
        }

        for (Method m : p.getClass().getMethods()) {
            // TODO: Check for argument count/type match
            if (m.getName().equals(name) && (m.isVarArgs() || m.getParameterCount() == args.length)) {
                ComponentMethod pm;
                if ((pm = m.getAnnotation(ComponentMethod.class)) != null) {
                    Object o;
                    try {
                        o = m.invoke(p, args);
                    } catch (InvocationTargetException e) {
                        return new MultiArgReturn(false, e.getTargetException().getMessage());
                    } catch (IllegalAccessException e) {
                        return new MultiArgReturn(false, e.getMessage());
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
                    return new MultiArgReturn(returns);
                }
            }
        }

        return new MultiArgReturn(false, "could not find method " + name);
    }

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

                state.setField(-2, m.getName());
            }
        }

        LuaValueProxy proxy = state.getProxy(-1);
        state.pop(1);
        return proxy;
    }
}
