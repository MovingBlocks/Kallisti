// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.kallisti.base.util;

import java.util.Collection;

/**
 * A clean interface for handling a multi-value-per-key map.
 *
 * @param <K> The key type.
 * @param <V> The value type.
 */
public interface MultiValueMap<K, V> {
    boolean add(K key, V value);

    boolean remove(K key);

    boolean remove(K key, V value);

    void clear();

    Collection<K> keys();

    Collection<V> values(K key);

    boolean contains(K key);

    boolean contains(K key, V value);

    boolean isEmpty();

    int size();
}
