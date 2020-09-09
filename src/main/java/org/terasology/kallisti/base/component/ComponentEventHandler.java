// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

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
    private final MultiValueMap<Class, Listener> listeners;

    public ComponentEventHandler() {
        listeners = new CollectionBackedMultiValueMap<>(new HashMap<>(), ArrayList::new);
    }

    /**
     * Register an object's @ComponentEventListener-marked methods as event listeners.
     *
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
     *
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
}
