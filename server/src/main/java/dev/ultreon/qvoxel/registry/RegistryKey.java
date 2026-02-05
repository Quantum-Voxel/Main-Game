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
import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record RegistryKey<T>(@Nullable RegistryKey<? extends Registry<T>> parent, @NotNull Identifier id) {
    public static final RegistryKey<Registry<Registry<?>>> ROOT = new RegistryKey<>(null, new Identifier("root"));

    public RegistryKey(@Nullable RegistryKey<? extends Registry<T>> parent, @NotNull Identifier id) {
        this.parent = parent;
        this.id = id;
    }

    public static <T> RegistryKey<T> of(RegistryKey<? extends Registry<T>> parent, Identifier element) {
        if (element == null) throw new IllegalArgumentException("Element ID cannot be null");
        return new RegistryKey<>(parent, element);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Registry<?>> RegistryKey<T> registry(T registry) {
        return (RegistryKey<T>) new RegistryKey<>(ROOT, registry.id());
    }

    @SuppressWarnings("unchecked")
    public static <T extends Registry<?>> RegistryKey<T> registry(Identifier id) {
        return (RegistryKey<T>) new RegistryKey<>(ROOT, id);
    }

    @Override
    public String toString() {
        if (parent == null) return id.toString();
        return parent.id + " @ " + id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistryKey<?> that = (RegistryKey<?>) o;
        return id.equals(that.id) && Objects.equals(parent, that.parent);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        return result;
    }

    public static <T> RegistryKey<T> read(RegistryKey<? extends Registry<T>> registry, PacketIO packetIO) {
        Identifier id = packetIO.readId();
        return new RegistryKey<>(registry, id);
    }

    public void write(PacketIO packetIO) {
        packetIO.writeId(id);
    }
}
