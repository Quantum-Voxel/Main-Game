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

package dev.ultreon.qvoxel.server;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.featureflags.FeatureSet;
import dev.ultreon.qvoxel.network.packets.s2c.S2CRegistriesSyncPacket;
import dev.ultreon.qvoxel.registry.*;
import dev.ultreon.qvoxel.network.handler.ServerPacketHandler;
import dev.ultreon.qvoxel.network.handler.ClientPacketHandler;
import dev.ultreon.qvoxel.network.system.IConnection;
import dev.ultreon.qvoxel.resource.GameComponent;
import dev.ultreon.qvoxel.resource.GameNode;
import dev.ultreon.qvoxel.world.DimensionInfo;
import dev.ultreon.qvoxel.world.gen.biome.Biome;
import dev.ultreon.qvoxel.world.gen.chunk.ChunkGenerator;
import dev.ultreon.qvoxel.world.gen.noise.NoiseConfig;

import java.util.Objects;
import java.util.stream.Stream;

public class ServerRegistries implements GameComponent, RegistryHandle {

    public final Registry<Registry<?>> registries;
    private final Registry<Biome> biomes;
    private final Registry<ChunkGenerator> chunkGenerators;
    private final Registry<DimensionInfo> dimensions;
    private final Registry<NoiseConfig> noiseConfigs;
    private final QuantumServer server;

    public ServerRegistries(QuantumServer server) {
        this.server = server;
        registries = SimpleRegistry.<Registry<?>>builder(Identifier.tryParse("server_registry")).build();
        biomes = create(RegistryKeys.BIOME);
        chunkGenerators = create(RegistryKeys.CHUNK_GENERATOR);
        dimensions = create(RegistryKeys.DIMENSION);
        noiseConfigs = create(RegistryKeys.NOISE_CONFIG);

        //TODO! This is a temp fix!
        biomes.createTag(new Identifier("overworld_biomes"));
    }

    public Registry<Biome> biomes() {
        return biomes;
    }

    public Registry<ChunkGenerator> chunkGenerators() {
        return chunkGenerators;
    }

    public Registry<DimensionInfo> dimensions() {
        return dimensions;
    }

    public Registry<NoiseConfig> noiseConfigs() {
        return noiseConfigs;
    }

    @SafeVarargs
    @SuppressWarnings({"rawtypes", "unchecked"})
    public final <T> Registry<T> create(RegistryKey<Registry<T>> key, T... typeGetter) {
        Registry<T> registry = SimpleRegistry.builder(key.id(), typeGetter).build();
        registries.register((RegistryKey) key, registry);
        return registry;
    }

    public final void sendRegistries(IConnection<ServerPacketHandler, ClientPacketHandler> connection) {
        connection.send(new S2CRegistriesSyncPacket(registries));

        for (Registry<?> registry : registries.values()) {
            registry.send(connection);
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> Registry<T> get(RegistryKey<? extends Registry<T>> registryKey) {
        if (registries.contains((RegistryKey) registryKey)) {
            return (Registry<T>) registries.get((RegistryKey) registryKey);
        } else {
            return Registries.REGISTRY.get((RegistryKey) registryKey);
        }
    }

    @Override
    public FeatureSet getFeatures() {
        return server.getFeatures();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> Registry<T> getOrGeneric(RegistryKey<Registry<T>> registryKey) {
        Registry<T> tRegistry = (Registry<T>) registries.get((RegistryKey) registryKey);
        if (tRegistry == null) {
            return (Registry<T>) Registries.REGISTRY.get(registryKey.id());
        }
        return tRegistry;
    }

    @SuppressWarnings("unchecked")
    public Stream<Registry<?>> stream() {
        return registries.values().stream().filter(Objects::nonNull);
    }

    public void close() {
        for (Registry<?> registry : registries.values()) {
            Registries.REGISTRY.unregister(registry.id());
        }
        Registries.REGISTRY.unregister(registries.id());
    }
}
