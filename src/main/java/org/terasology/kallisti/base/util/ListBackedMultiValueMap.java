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

package org.terasology.kallisti.base.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A basic implementation of a multi-value-per-key map, using a map of lists.
 * @param <K> The key type.
 * @param <V> The value type.
 */
public class ListBackedMultiValueMap<K, V> implements MultiValueMap<K, V> {
    private final Map<K, List<V>> map;
    private final Supplier<List<V>> listSupplier;

    public ListBackedMultiValueMap(Map<K, List<V>> map, Supplier<List<V>> listSupplier) {
        this.map = map;
        this.listSupplier = listSupplier;
    }

    protected final List<V> createForKey(K key) {
        return listSupplier.get();
    }

    @Override
    public boolean add(K key, V value) {
        List<V> list = map.computeIfAbsent(key, this::createForKey);
        return list.add(value);
    }

    @Override
    public boolean remove(K key) {
        return map.remove(key) != null;
    }

    @Override
    public boolean remove(K key, V value) {
        List<V> list = map.get(key);
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
        List<V> list = map.get(key);
        return list != null ? Collections.unmodifiableList(list) : Collections.emptyList();
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
        for (List<V> l : map.values()) {
            i += l.size();
        }
        return i;
    }
}
