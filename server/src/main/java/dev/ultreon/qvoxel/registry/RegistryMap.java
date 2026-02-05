/*
 * Copyright 2025. Quinten 'Qubix' Jungblut
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

package dev.ultreon.qvoxel.registry;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Base registry.
 *
 * @param <K> The type key type for the registry.
 * @param <V> The type value type for the registry.
 */
public interface RegistryMap<K, V> {
    V get(K obj);

    void register(K key, V val);

    int size();

    Set<Map.Entry<K, V>> entries() throws IllegalAccessException;

    Set<K> keys();

    Collection<V> values();

    default boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns the key relevant to the raw ID
     *
     * @param rawID the raw ID
     * @return the key relevant to the raw ID.
     * @throws UnsupportedOperationException if nameById is not supported
     */
    RegistryKey<V> nameById(int rawID);
}
