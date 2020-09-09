// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.jnlua;

import org.terasology.jnlua.Converter;
import org.terasology.jnlua.LuaState;
import org.terasology.jnlua.LuaType;
import org.terasology.kallisti.base.proxy.Proxy;
import org.terasology.kallisti.base.proxy.ProxyFactory;
import org.terasology.kallisti.base.util.KallistiReflect;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class KallistiConverter implements Converter {
    private final Converter parent;
    private final Map<Class, ProxyFactory> proxyMap = new HashMap<>();

    public KallistiConverter(Converter parent) {
        this.parent = parent;
    }

    public void registerProxyFactory(Class c, ProxyFactory factory) {
        proxyMap.put(c, factory);
    }

    @Override
    public int getTypeDistance(LuaState luaState, int index, Class<?> formalType) {
        LuaType luaType = luaState.type(index);
        if (luaType == LuaType.USERDATA) {
            Object rawObject = luaState.toJavaObjectRaw(index);
            if (rawObject instanceof Proxy) {
                return 1;
            }
        }

        return parent.getTypeDistance(luaState, index, formalType);
    }

    @Override
    public <T> T convertLuaValue(LuaState luaState, int index, Class<T> formalType) {
        LuaType luaType = luaState.type(index);
        if (luaType == LuaType.USERDATA) {
            Object rawObject = luaState.toJavaObjectRaw(index);
            if (rawObject instanceof Proxy) {
                return (T) ((Proxy) rawObject).getParent();
            }
        }

        // TODO: Lua->Java conversion of Lists and Maps

        return parent.convertLuaValue(luaState, index, formalType);
    }

    @Override
    public void convertJavaObject(LuaState luaState, Object object) {
        if (object == null) {
            luaState.pushNil();
            return;
        }

        if (object instanceof Proxy) {
            throw new RuntimeException("Should never get here - Proxy should only be fed by convertJavaObject into " +
                    "pushJavaObjectRaw!");
        }

        ProxyFactory proxy = KallistiReflect.findClosestMatchingClass(proxyMap, object.getClass());
        if (proxy != null) {
            luaState.pushJavaObjectRaw(proxy.create(object));
        } else {
            // TODO: Move to JNLua?
            if (object instanceof Collection) {
                luaState.newTable();
                int i = 1;
                for (Object o : (Collection) object) {
                    luaState.pushInteger(i++);
                    luaState.pushJavaObject(o);
                    luaState.setTable(-3);
                }
                return;
            } else if (object instanceof Map) {
                luaState.newTable();
                for (Object o : ((Map) object).keySet()) {
                    luaState.pushJavaObject(o);
                    luaState.pushJavaObject(((Map) object).get(o));
                    luaState.setTable(-3);
                }
                return;
            }

            parent.convertJavaObject(luaState, object);
        }
    }
}
