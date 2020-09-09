// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.base.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A basic implementation of a multi-value-per-key map, using a map of lists.
 *
 * @param <K> The key type.
 * @param <V> The value type.
 */
public class CollectionBackedMultiValueMap<K, V> implements MultiValueMap<K, V> {
    private final Map<K, Collection<V>> map;
    private final Supplier<Collection<V>> listSupplier;

    public CollectionBackedMultiValueMap(Map<K, Collection<V>> map, Supplier<Collection<V>> listSupplier) {
        this.map = map;
        this.listSupplier = listSupplier;
    }

    protected final Collection<V> createForKey(K key) {
        return listSupplier.get();
    }

    @Override
    public boolean add(K key, V value) {
        Collection<V> list = map.computeIfAbsent(key, this::createForKey);
        return list.add(value);
    }

    @Override
    public boolean remove(K key) {
        return map.remove(key) != null;
    }

    @Override
    public boolean remove(K key, V value) {
        Collection<V> list = map.get(key);
        if (list != null) {
            return list.remove(value);
        } else {
            return false;
        }
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Collection<K> keys() {
        return map.keySet();
    }

    @Override
    public Collection<V> values(K key) {
        Collection<V> list = map.get(key);
        return list != null ? Collections.unmodifiableCollection(list) : Collections.emptyList();
    }

    @Override
    public boolean contains(K key) {
        return map.containsKey(key);
    }

    @Override
    public boolean contains(K key, V value) {
        return map.containsKey(key) && map.get(key).contains(value);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public int size() {
        int i = 0;
        for (Collection<V> l : map.values()) {
            i += l.size();
        }
        return i;
    }
}
