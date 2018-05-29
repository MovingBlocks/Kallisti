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

package org.terasology.kallisti.jnlua;

import org.terasology.kallisti.base.proxy.Proxy;
import org.terasology.kallisti.base.proxy.ProxyFactory;
import org.terasology.kallisti.base.util.KallistiReflect;
import org.terasology.jnlua.Converter;
import org.terasology.jnlua.LuaState;
import org.terasology.jnlua.LuaType;

import java.util.*;

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

		return parent.convertLuaValue(luaState, index, formalType);
	}

	@Override
	public void convertJavaObject(LuaState luaState, Object object) {
		if (object == null) {
			luaState.pushNil();
			return;
		}

		if (object instanceof Proxy) {
			throw new RuntimeException("Should never get here - Proxy should only be fed by convertJavaObject into pushJavaObjectRaw!");
		}

		ProxyFactory proxy = KallistiReflect.findClosestMatchingClass(proxyMap, object.getClass());
		if (proxy != null) {
			luaState.pushJavaObjectRaw(proxy.create(object));
		} else {
			parent.convertJavaObject(luaState, object);
		}
	}
}
