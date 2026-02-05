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

package dev.ultreon.qvoxel.client.registry;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.libs.registries.v0.exception.RegistryException;
import dev.ultreon.qvoxel.registry.IdRegistry;
import dev.ultreon.qvoxel.registry.Registry;
import dev.ultreon.qvoxel.registry.RegistryKey;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.List;

/**
 * Represents the client registry.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
public class ExternalRegistry<T> implements IdRegistry<T> {
    private final BidiMap<Integer, Identifier> idMap = new DualHashBidiMap<>();
    private final RegistryKey<? extends Registry<T>> key;

    /**
     * Constructs a new client registry.
     *
     * @param key the key.
     * @throws IllegalStateException if the registry is already registered.
     */
    public ExternalRegistry(RegistryKey<? extends Registry<T>> key) throws IllegalStateException {
        this.key = key;
    }

    public T byRawId(int id) {
        throw new UnsupportedOperationException("Objects not available in synced registries!");
    }

    @Override
    public int getRawId(T object) {
        throw new UnsupportedOperationException("Objects not available in synced registries!");
    }

    public RegistryKey<T> nameById(int i) {
        return RegistryKey.of(key, idMap.get(i));
    }

    @Override
    public int idByName(RegistryKey<T> key) {
        int rawId = idMap.inverseBidiMap().getOrDefault(key.id(), -1);
        if (rawId == -1) throw new RegistryException("Missing key: " + this.key);
        return rawId;
    }

    @Override
    public T get(Identifier from) {
        throw new UnsupportedOperationException("Objects not available in synced registries!");
    }

    public List<Identifier> ids() {
        throw new UnsupportedOperationException("Objects not available in synced registries!");
    }

    public void load(BidiMap<Integer, Identifier> registryMap) {
        unload();
        idMap.putAll(registryMap);
    }

    public void unload() {
        idMap.clear();
    }

    public RegistryKey<? extends Registry<T>> key() {
        return key;
    }
}
