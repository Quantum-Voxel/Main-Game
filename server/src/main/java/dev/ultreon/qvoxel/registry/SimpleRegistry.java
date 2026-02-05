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

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.event.RegistryDumpEvent;
import dev.ultreon.qvoxel.event.system.EventSystem;
import org.apache.commons.collections4.OrderedMap;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class SimpleRegistry<T> extends Registry<T> {
    private static final OrderedMap<RegistryKey<Registry<?>>, SimpleRegistry<?>> REGISTRIES = new ListOrderedMap<>();

    private SimpleRegistry(Builder<T> builder, RegistryKey<Registry<T>> key) throws IllegalStateException {
        super(builder, key);

        EventSystem.addListenerDefault(RegistryDumpEvent.class, event -> dumpRegistry());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private SimpleRegistry(Builder<T> builder) {
        super(builder);

        SimpleRegistry.REGISTRIES.put((RegistryKey) key, this);
    }

    public static Collection<SimpleRegistry<?>> getRegistries() {
        return SimpleRegistry.REGISTRIES.values();
    }

    @SafeVarargs
    @Deprecated
    public static <T> SimpleRegistry<T> create(Identifier id, @NotNull T... type) {
        return new Builder<>(id, type).build();
    }

    @SafeVarargs
    @Deprecated
    public static <T> Builder<T> builder(Identifier id, T... typeGetter) {
        return new Builder<>(id, typeGetter);
    }

    @SafeVarargs
    @Deprecated
    public static <T> Builder<T> builder(RegistryKey<Registry<T>> key, T... typeGetter) {
        return new Builder<T>(key, typeGetter);
    }

    public static class Builder<T> extends Registry.Builder<T> {
        @SafeVarargs
        @Deprecated
        public Builder(Identifier id, T... typeGetter) {
            super(id, typeGetter);
        }

        @SafeVarargs
        public Builder(RegistryKey<Registry<T>> block, T... typeGetter) {
            super(block, typeGetter);
        }

        public SimpleRegistry<T> build() {
            SimpleRegistry<T> tSimpleRegistry = new SimpleRegistry<>(this);
            if (Registries.REGISTRY != null)
                Registries.REGISTRY.register(tSimpleRegistry.key.id(), tSimpleRegistry);
            return tSimpleRegistry;
        }
    }
}
