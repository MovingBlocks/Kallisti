// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.oc;

import org.terasology.kallisti.base.component.ComponentMethod;
import org.terasology.kallisti.base.util.KallistiReflect;
import org.terasology.kallisti.oc.proxy.OCUserdataProxy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ShimUserdata extends ShimInvoker<Object> {
    final Map<Class, OCUserdataProxy> proxyMap = new HashMap<>();

    public ShimUserdata(MachineOpenComputers machine) {
        super(machine);
    }

    @Override
    protected Object get(Object value) {
        return value;
    }

    protected OCUserdataProxy getProxyOrThrow(Object entry) {
        if (entry instanceof OCUserdataProxy) {
            return (OCUserdataProxy) entry;
        }

        OCUserdataProxy luaProxy = KallistiReflect.findClosestMatchingClass(proxyMap, entry.getClass());
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
            return new Object[]{true, getProxyOrThrow(entry).index(entry, key)};
        } catch (Exception e) {
            return new Object[]{false, e.getMessage()};
        }
    }

    @ComponentMethod(returnsMultipleArguments = true)
    public Object[] unapply(Object entry, Object key) {
        return unapply(entry, key, null);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @ComponentMethod(returnsMultipleArguments = true)
    public Object[] unapply(Object entry, Object key, Optional<Object> value) {
        try {
            getProxyOrThrow(entry).newindex(entry, key, value.orElse(null));
            return new Object[]{true};
        } catch (Exception e) {
            return new Object[]{false, e.getMessage()};
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
            return new Object[]{false, e.getMessage()};
        }
    }

    @ComponentMethod(returnsMultipleArguments = true)
    public Object[] dispose(Object entry) {
        return new Object[]{true};
    }
}
