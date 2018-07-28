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

import org.terasology.kallisti.base.interfaces.Persistable;
import org.terasology.kallisti.base.util.KallistiReflect;
import org.terasology.kallisti.base.util.ListBackedMultiValueMap;
import org.terasology.kallisti.base.util.MultiValueMap;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The base class for a Machine, implementing component management logic.
 */
public abstract class Machine {
    public enum MachineState {
        UNINITIAILIZED,
        RUNNING,
        STOPPED
    }

    private interface Rule {
        int getPriority();
        Class[] getInput();
        Type[] getInputGeneric();
        Class getOutput();
        Object invoke(Object... input) throws Throwable;
    }

    private static class RuleMethod implements Rule {
        private final Object parent;
        private final Method method;
        private final int priority;

        RuleMethod(Object parent, Method method, int priority) {
            this.parent = parent;
            this.method = method;
            this.priority = priority;
        }

        @Override
        public Class getOutput() {
            return method.getReturnType();
        }

        @Override
        public Class[] getInput() {
            return method.getParameterTypes();
        }

        @Override
        public Type[] getInputGeneric() {
            return method.getGenericParameterTypes();
        }

        @Override
        public Object invoke(Object... input) throws Throwable {
            return method.invoke(parent, input);
        }

        @Override
        public int getPriority() {
            return priority;
        }
    }

    private static class RuleConstructor implements Rule {
        private final Constructor method;
        private final int priority;

        RuleConstructor(Constructor method, int priority) {
            this.method = method;
            this.priority = priority;
        }

        @Override
        public Class getOutput() {
            return method.getDeclaringClass();
        }

        @Override
        public Class[] getInput() {
            return method.getParameterTypes();
        }

        @Override
        public Type[] getInputGeneric() {
            return method.getGenericParameterTypes();
        }

        @Override
        public Object invoke(Object... input) throws Throwable {
            return method.newInstance(input);
        }

        @Override
        public int getPriority() {
            return priority;
        }
    }

    private static class ComponentEntry {
        private final ComponentContext context;
        private final Object object;

        public ComponentEntry(ComponentContext context, Object object) {
            this.context = context;
            this.object = object;
        }

        @Override
        public int hashCode() {
            return 31 * context.hashCode() + object.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ComponentEntry)) {
                return false;
            } else {
                return ((ComponentEntry) o).context.equals(context)
                        && ((ComponentEntry) o).object.equals(object);
            }
        }
    }

    protected final ComponentEventHandler eventHandler;

    // Initialized by addComponent();
    private final List<ComponentEntry> entries;
    private final Map<Object, ComponentEntry> entriesByObject;
    private final Map<Class, Map<ComponentContext, ComponentEntry>> entryClassContextTable;

    // Used by .initialize()
    private final MultiValueMap<Class, Object> nonEntryObjectsByClass;

    // Initialized by registerRules()
    private final List<Rule> creationRules;
    private final MultiValueMap<Class, Rule> linkingRules;

    // Initialized in .initialize();
    protected MachineState state;

    public Machine() {
        eventHandler = new ComponentEventHandler();

        creationRules = new ArrayList<>();
        linkingRules = new ListBackedMultiValueMap<>(new IdentityHashMap<>(), ArrayList::new);

        entries = new ArrayList<>();
        entriesByObject = new IdentityHashMap<>();
        entryClassContextTable = new HashMap<>();
        nonEntryObjectsByClass = new ListBackedMultiValueMap<>(new IdentityHashMap<>(), ArrayList::new);

        state = MachineState.UNINITIAILIZED;

        addNonComponentObject(this);
    }

    public Iterable<? extends Object> getAllComponents() {
        return entries.stream().map((m) -> m.object).collect(Collectors.toList());
    }

    public Iterable<ComponentContext> getAllComponentContexts() {
        return entries.stream().map((m) -> m.context).collect(Collectors.toList());
    }

    private void registerRules(Object parent, Method m) throws IllegalArgumentException {
        ComponentRule rule = m.getAnnotation(ComponentRule.class);
        if (rule != null) {
            RuleMethod r = new RuleMethod(parent, m, rule.priority());
            if (r.method.getParameterCount() == 0) {
                throw new IllegalArgumentException("Rule must accept a parameter! If you want to always add a component to a machine, do so directly.");
            }

            if (r.method.isVarArgs()) {
                if (r.method.getParameterCount() > 1) {
                    throw new IllegalArgumentException("Linking rule may only have one variable argument!");
                } else {
                    Class c = r.getInput()[0];
                    linkingRules.add(c, r);
                }
            } else {
                creationRules.add(r);
            }
        }
    }

    /**
     * Register the component rules present in static methods and constructors
     * of the given class.
     * @param c The given class.
     * @throws IllegalArgumentException
     */
    public void registerRules(Class c) throws IllegalArgumentException {
        for (Method m : c.getMethods()) {
            if ((m.getModifiers() & Modifier.STATIC) != 0) {
                registerRules(null, m);
            }
        }

        for (Constructor cc : c.getConstructors()) {
            ComponentRule ruleC = (ComponentRule) cc.getAnnotation(ComponentRule.class);
            if (ruleC != null) {
                if (cc.getParameterCount() == 0) {
                    throw new IllegalArgumentException("Rule must accept a parameter! If you want to always add a component to a machine, do so directly.");
                }

                RuleConstructor rule = new RuleConstructor(cc, ruleC.priority());

                if (cc.isVarArgs()) {
                    if (cc.getParameterCount() > 1) {
                        throw new IllegalArgumentException("Linking rule may only have one variable argument!");
                    } else {
                        linkingRules.add(cc.getDeclaringClass(), rule);
                    }
                } else {
                    creationRules.add(rule);
                }
            }
        }
    }

    /**
     * Register the component rules present in methods of the given object.
     * @param o The given object.
     * @throws IllegalArgumentException
     */
    public void registerRules(Object o) throws IllegalArgumentException {
        for (Method m : o.getClass().getMethods()) {
            registerRules(o, m);
        }
    }

    protected <T> T join(T... o) {
        if (o.length == 0) {
            return null;
        } else if (o.length == 1) {
            return o[0];
        } else {
            boolean match = true;
            for (int i = 1; i < o.length; i++) {
                if (o[0] != o[i]) {
                    match = false;
                    break;
                }
            }

            if (match) {
                return o[0];
            }

            for (Rule r : linkingRules.values(o.getClass())) {
                try {
                    Object result = r.invoke(o);
                    if (result != null) {
                        return (T) result;
                    }
                } catch (Throwable e) {
                    // pass?
                }
            }

            throw new RuntimeException("Does not understand how to join " + o[0].getClass() + " objects!");
        }
    }

    /**
     * Add a non-component object accessible to components during creation.
     * In general, these should be objects related to a given Machine, such
     * as the Machine itself.
     * @param o The non-component object.
     * @return Whether the addition was successful.
     */
    protected boolean addNonComponentObject(Object o) {
        for (Class c : KallistiReflect.classes(o.getClass())) {
            if (!nonEntryObjectsByClass.contains(c, o)) {
                nonEntryObjectsByClass.add(c, o);
            }
        }

        return true;
    }

    private static final Map<Class, Boolean> isComponentItfMap = new HashMap<>();

    private static boolean isComponentInterface(Class c) {
        return isComponentItfMap.computeIfAbsent(c,
                (cc) -> {
                    for (Class cl : KallistiReflect.classes(cc)) {
                        if (cl.getAnnotation(ComponentInterface.class) != null) {
                            return true;
                        }
                    }

                    return false;
                });
    }

    /**
     * Add a new component to the Machine. If a @ComponentInterface-marked
     * class or subclass is already present with the same context, the
     * component will not be added.
     * @param context The component's context.
     * @param o The component object.
     * @return Whether or not the addition was successful.
     */
    public boolean addComponent(ComponentContext context, Object o) {
        ComponentEntry entry = new ComponentEntry(context, o);
        Class objectClass = entry.object.getClass();

        for (Class c : KallistiReflect.classes(objectClass)) {
            if (isComponentInterface(c)) {
                Map<ComponentContext, ComponentEntry> m = entryClassContextTable.computeIfAbsent(c, (cc -> new HashMap<>()));
                if (m.containsKey(context)) {
                    return false;
                }
            }
        }

        entries.add(entry);
        entriesByObject.put(entry.object, entry);

        for (Class c : KallistiReflect.classes(objectClass)) {
            if (isComponentInterface(c)) {
                Map<ComponentContext, ComponentEntry> m = entryClassContextTable.computeIfAbsent(c, (cc -> new HashMap<>()));
                m.put(context, entry);
            }
        }

        return true;
    }

    /**
     * Initialize the machine.
     */
    public void initialize() {
        if (state != MachineState.UNINITIAILIZED) {
            throw new RuntimeException("Already initialized!");
        }

        // Sort rules
        Comparator<Rule> sorter = (rule1, rule2) -> Integer.compare(rule2.getPriority(), rule1.getPriority());
        creationRules.sort(sorter);
        for (Class c : linkingRules.keys()) {
            ((List<Rule>) linkingRules.values(c)).sort(sorter);
        }

        // Add components
        boolean addedNewComponents = true;
        while (addedNewComponents) {
            addedNewComponents = false;

            for (Rule r : creationRules) {
                List<List<Object>> baseComponents = new ArrayList<>();
                int permutations = 1;

                Class[] inp = r.getInput();
                Type[] inpGeneric = r.getInputGeneric();

                for (int i = 0; i < inp.length; i++) {
                    Class c = inp[i];
                    boolean required = true;
                    if (c == Optional.class && inpGeneric[i] instanceof ParameterizedType) {
                        required = false;
                        c = (Class) (((ParameterizedType) inpGeneric[i]).getActualTypeArguments()[0]);
                    }

                    Collection ccol = entryClassContextTable.getOrDefault(c, Collections.emptyMap()).values();
                    if (ccol.isEmpty()) {
                        ccol = nonEntryObjectsByClass.values(c);
                    }

                    if (required || !ccol.isEmpty()) {
                        permutations *= ccol.size();
                        if (permutations == 0) {
                            break;
                        }
                    }

                    baseComponents.add(ccol instanceof List ? (List) ccol : new ArrayList<>(ccol));
                }

                for (int i = 0; i < permutations; i++) {
                    List<ComponentContext> contexts = new ArrayList<>();
                    Object[] params = new Object[baseComponents.size()];
                    int iCurr = i;
                    for (int j = 0; j < params.length; j++) {
                        if (!baseComponents.get(j).isEmpty()) {
                            Object o = baseComponents.get(j).get(iCurr % baseComponents.get(j).size());
                            if (o instanceof ComponentEntry) {
                                ComponentEntry entry = (ComponentEntry) o;
                                contexts.add(entry.context);
                                params[j] = entry.object;
                            } else {
                                // non-entry object
                                params[j] = o;
                            }

                            if (inp[j] == Optional.class) {
                                params[j] = Optional.ofNullable(params[j]);
                            }

                            iCurr /= baseComponents.get(j).size();
                        }
                    }

                    try {
                        Object result = r.invoke(params);
                        if (result != null) {
                            ComponentContext context = join(contexts.toArray(new ComponentContext[0]));
                            if (getComponent(context, result.getClass()) == null) {
                                addedNewComponents |= addComponent(context, result);
                            }
                        }
                    } catch (Throwable e) {
                        // TODO: forward
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        // Populate eventHandler
        Set<Object> addedObjects = new HashSet<>();

        for (ComponentEntry ce : entries) {
            if (addedObjects.add(ce.object)) {
                eventHandler.register(ce.object);
            }
        }

        state = MachineState.STOPPED;
    }

    /**
     * Retrieve a component. May return null.
     * @param context The context of the component.
     * @param c The class of the component. Should be a (sub)class or
     *          (sub)interface marked with @ComponentInterface.
     * @param <T> The type of the component.
     * @return The component, or null if not present.
     */
    @SuppressWarnings("unchecked")
    public <T> T getComponent(ComponentContext context, Class<T> c) {
        ComponentEntry entry = entryClassContextTable
                .getOrDefault(c, Collections.emptyMap())
                .get(context);

        return entry != null ? (T) entry.object : null;
    }

    /**
     * Retrieve all components of a given class.
     * @param c The class of the component.
     * @param <T> The type of the component.
     * @return The component, or null if not present.
     */
    @SuppressWarnings("unchecked")
    public <T> Collection<T> getComponentsByClass(Class<T> c) {
        return entryClassContextTable
                    .getOrDefault(c, Collections.emptyMap())
                    .values().stream().map((e) -> (T) e.object).collect(Collectors.toList());
    }


    /**
     * Retrieve all component contexts of a given class.
     * @param c The class of the component.
     * @param <T> The type of the component.
     * @return The component, or null if not present.
     */
    public Collection<ComponentContext> getContextsByClass(Class c) {
        return entryClassContextTable
                .getOrDefault(c, Collections.emptyMap())
                .values().stream().map((e) -> e.context).collect(Collectors.toList());
    }

    /**
     * Get the context of a given component.
     * @param component The component object.
     * @return The context.
     */
    public ComponentContext getContext(Object component) {
        return entriesByObject.get(component).context;
    }

    public final void start() throws Exception {
        if (state != MachineState.STOPPED) {
            throw new MachineInvalidStateException(state);
        }

        startInternal();
        state = MachineState.RUNNING;
    }
    protected abstract void startInternal() throws Exception;

    public final MachineState getState() {
        return state;
    }

    public final void stop() throws Exception {
        if (state != MachineState.RUNNING) {
            throw new MachineInvalidStateException(state);
        }

        stopInternal();
        state = MachineState.STOPPED;
    }

    protected abstract void stopInternal() throws Exception;

    /**
     * Run one tick of the computer, lasting a given amount of time.
     * @throws Exception
     */
    public final boolean tick(double time) throws Exception {
        eventHandler.emit(new ComponentTickEvent(time));
        return tickInternal(time);
    }

    protected abstract boolean tickInternal(double time) throws Exception;

    /**
     * Get the persistence handler, if the machine can be persisted or unpersisted.
     * @return The persistence handler.
     */
    public Optional<Persistable> getPersistenceHandler() {
        return Optional.empty();
    }
}
