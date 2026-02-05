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
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.featureflags.FeatureSet;
import dev.ultreon.qvoxel.registry.Registry;
import dev.ultreon.qvoxel.registry.RegistryHandle;
import dev.ultreon.qvoxel.registry.RegistryKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LocalRegistries implements RegistryHandle {
    private final Map<Identifier, ExternalRegistry<?>> registries = new HashMap<>();

    @Override
    @SuppressWarnings({"unchecked"})
    public <T> ExternalRegistry<T> get(RegistryKey<? extends Registry<T>> registryKey) {
        return (ExternalRegistry<T>) registries.computeIfAbsent(registryKey.id(), id -> new ExternalRegistry<>(registryKey));
    }

    @Override
    public FeatureSet getFeatures() {
        return QuantumClient.get().getFeatures();
    }

    public <T> void set(RegistryKey<ExternalRegistry<T>> registryKey, ExternalRegistry<T> registry) {
        registries.put(registryKey.id(), registry);
    }

    public ExternalRegistry<?> get(Identifier registryID) {
        return registries.get(registryID);
    }

    public Set<Identifier> ids() {
        return registries.keySet();
    }
}
