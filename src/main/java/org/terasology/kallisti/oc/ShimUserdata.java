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
import org.terasology.kallisti.base.util.KallistiReflect;

import java.util.HashMap;
import java.util.Map;

public class ShimUserdata extends ShimInvoker<Object> {
    final Map<Class, OCLuaProxy> proxyMap = new HashMap<>();

    public ShimUserdata(MachineOpenComputers machine) {
        super(machine);
    }

    @Override
    protected Object get(Object value) {
        return value;
    }

    protected OCLuaProxy getProxyOrThrow(Object entry) {
        if (entry instanceof OCLuaProxy) {
            return (OCLuaProxy) entry;
        }

        OCLuaProxy luaProxy = KallistiReflect.findClosestMatchingClass(proxyMap, entry.getClass());
        if (luaProxy != null) {
            return luaProxy;
        } else {
            RuntimeException e = new RuntimeException("cannot proxy userdata for " + entry.getClass().getSimpleName());
            System.err.println(e.getMessage());
            throw e;
        }
    }

    @ComponentMethod(returnsMultipleArguments = true)
    public Object[] apply(Object entry, Object key) {
        try {
            return new Object[] {true, getProxyOrThrow(entry).index(entry, key)};
        } catch (Exception e) {
            return new Object[] {false, e.getMessage()};
        }
    }

    @ComponentMethod(returnsMultipleArguments = true)
    public Object[] unapply(Object entry, Object key, Object value) {
        try {
            getProxyOrThrow(entry).newindex(entry, key, value);
            return new Object[] { true };
        } catch (Exception e) {
            return new Object[] { false, e.getMessage() };
        }
    }

    @ComponentMethod(returnsMultipleArguments = true)
    public Object[] call(Object entry, Object... args) {
        try {
            Object[] result = getProxyOrThrow(entry).invoke(entry, args);
            Object[] result2 = new Object[result.length + 1];
            System.arraycopy(result, 0, result2, 1, result.length);
            result2[0] = true;
            return result2;
        } catch (Exception e) {
            return new Object[] { false, e.getMessage() };
        }
    }

    @ComponentMethod(returnsMultipleArguments = true)
    public Object[] dispose(Object entry) {
        return new Object[] { true };
    }
}
