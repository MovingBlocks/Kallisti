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

package org.terasology.kallisti.base.component;

import org.terasology.kallisti.base.util.CollectionBackedMultiValueMap;
import org.terasology.kallisti.base.util.MultiValueMap;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Handler for emitting ComponentEvents.
 */
public class ComponentEventHandler {
    private static class Listener {
        private final Object parent;
        private final MethodHandle handle;

        public Listener(Object parent, Method method) {
            this.parent = parent;
            try {
                this.handle = MethodHandles.lookup().unreflect(method);
            } catch (IllegalAccessException e) {
                // Should be caught earlier!
                throw new RuntimeException(e);
            }
        }

        public void invoke(ComponentEvent event) throws Throwable {
            this.handle.invoke(parent, event);
        }
    }

    private final MultiValueMap<Class, Listener> listeners;

    public ComponentEventHandler() {
        listeners = new CollectionBackedMultiValueMap<>(new HashMap<>(), ArrayList::new);
    }

    /**
     * Register an object's @ComponentEventListener-marked methods
     * as event listeners.
     * @param o The object.
     */
    public void register(Object o) {
        for (Method m : o.getClass().getMethods()) {
            if (m.getAnnotation(ComponentEventListener.class) != null
                    && m.getParameterCount() == 1
                    && !m.isVarArgs()
                    && ComponentEvent.class.isAssignableFrom(m.getParameterTypes()[0])) {
                listeners.add(m.getParameterTypes()[0], new Listener(o, m));
            }
        }
    }

    /**
     * Emit a given ComponentEvent to all matching listeners.
     * @param event The event.
     */
    public void emit(ComponentEvent event) {
        for (Listener l : listeners.values(event.getClass())) {
            try {
                l.invoke(event);
            } catch (Throwable t) {
                t.printStackTrace();
                // TODO: pass?
            }
        }
    }
}
