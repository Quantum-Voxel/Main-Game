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

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.registry.Registry;
import dev.ultreon.qvoxel.registry.RegistryKey;
import dev.ultreon.qvoxel.registry.RegistryKeys;
import dev.ultreon.qvoxel.resource.GameNode;
import dev.ultreon.qvoxel.world.DimensionInfo;
import dev.ultreon.qvoxel.world.gen.chunk.ChunkGenerator;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.ApiStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages dimension and world relationships within a QuantumServer context.
 */
public class DimensionManager extends GameNode {
    private final QuantumServer server;
    private final Map<RegistryKey<DimensionInfo>, Dimension> dimensions = new HashMap<>();
    private final Map<RegistryKey<DimensionInfo>, ServerWorld> worlds = new HashMap<>();

    /**
     * Constructs a DimensionManager with the specified QuantumServer.
     *
     * @param server the QuantumServer instance associated with this DimensionManager
     */
    @ApiStatus.Internal
    public DimensionManager(QuantumServer server) {
        this.server = server;
    }

    /**
     * Retrieves the QuantumServer instance associated with this DimensionManager.
     *
     * @return the QuantumServer instance.
     */
    public QuantumServer getServer() {
        return server;
    }

    /**
     * Sets the default dimensions for the DimensionManager by populating the dimensions map with
     * pre-defined dimensions such as OVERWORLD, TEST, and SPACE using the provided ServerRegistries.
     *
     * @param registries The ServerRegistries instance containing the necessary registries for dimensions
     *                   and chunk generators.
     */
    public void setDefaults(ServerRegistries registries) {
        Registry<DimensionInfo> dimRegistry = registries.get(RegistryKeys.DIMENSION);
        Registry<ChunkGenerator> chunkGenRegistry = registries.get(RegistryKeys.CHUNK_GENERATOR);
        addDim(DimensionInfo.OVERWORLD, new Dimension(dimRegistry.get(DimensionInfo.OVERWORLD), chunkGenRegistry.get(ChunkGenerator.OVERWORLD)));
        addDim(DimensionInfo.TEST, new Dimension(dimRegistry.get(DimensionInfo.TEST), chunkGenRegistry.get(ChunkGenerator.TEST)));
        addDim(DimensionInfo.SPACE, new Dimension(dimRegistry.get(DimensionInfo.SPACE), chunkGenRegistry.get(ChunkGenerator.FLOATING_ISLANDS)));
    }

    private void addDim(RegistryKey<DimensionInfo> key, Dimension dimension) {
        dimensions.put(key, dimension);
        add("Dim " + key.id(), dimension);
    }

    /**
     * Loads worlds into the server based on the provided seed.
     * Iterates through the dimensions map and sets up each world with its data.
     * If an IOException occurs, it logs an error message.
     *
     * @param seed The seed value used to generate worlds if no specific seed is provided for a dimension.
     */
    public void loadWorlds(long seed) {
        for (Map.Entry<RegistryKey<DimensionInfo>, Dimension> e : dimensions.entrySet()) {
            RegistryKey<DimensionInfo> key = e.getKey();
            Dimension dimension = e.getValue();

            try {
                WorldStorage storage = server.getStorage();
                MapType data = new MapType();
                if (storage.exists("world.ubo"))
                    data = storage.read("world.ubo");
                ServerWorld world = new ServerWorld(server, key, storage, dimension.generator(), e.getValue().info().seed().orElse(seed ^ key.hashCode()), data);
                worlds.put(key, world);
                add("World " + key.id(), world);

                world.load();
            } catch (IOException ex) {
                CommonConstants.LOGGER.error("Failed to load server data");
            }
        }
    }

    /**
     * Loads dimensions and their respective chunk generators into the DimensionManager.
     * Iterates through the entries of the dimension registry, retrieves the corresponding
     * chunk generator for each dimension, and populates the dimensions map.
     *
     * @param registries The ServerRegistries instance containing the necessary
     *                   registries for dimensions and chunk generators.
     */
    public void load(ServerRegistries registries) {
        Registry<DimensionInfo> dimRegistry = registries.get(RegistryKeys.DIMENSION);
        Registry<ChunkGenerator> chunkGenRegistry = registries.get(RegistryKeys.CHUNK_GENERATOR);
        for (Map.Entry<RegistryKey<DimensionInfo>, DimensionInfo> e : dimRegistry.entries()) {
            RegistryKey<DimensionInfo> key = e.getKey();
            DimensionInfo info = e.getValue();
            RegistryKey<ChunkGenerator> chunkGenKey = info.generatorKey();
            ChunkGenerator chunkGenerator = chunkGenRegistry.get(chunkGenKey);

            dimensions.put(key, new Dimension(info, chunkGenerator));
        }
    }

    /**
     * Retrieves a Dimension object associated with the provided registry key.
     *
     * @param key the RegistryKey of the DimensionInfo to retrieve the Dimension object for
     * @return the Dimension associated with the specified key
     */
    public Dimension get(RegistryKey<DimensionInfo> key) {
        return dimensions.get(key);
    }

    /**
     * Retrieves the ServerWorld instance associated with the provided registry key.
     *
     * @param key the RegistryKey of the DimensionInfo to retrieve the ServerWorld for
     * @return the ServerWorld associated with the specified key
     */
    public ServerWorld getWorld(RegistryKey<DimensionInfo> key) {
        return worlds.get(key);
    }

    /**
     * Retrieves a map of all ServerWorld instances managed by the DimensionManager,
     * keyed by their RegistryKey&lt;DimensionInfo&gt;.
     *
     * @return a map containing the RegistryKey&lt;DimensionInfo&gt; as keys and the corresponding
     * ServerWorld instances as values
     */
    public Map<RegistryKey<DimensionInfo>, ServerWorld> getWorlds() {
        return worlds;
    }

    public void close() {
        for (ServerWorld world : worlds.values()) {
            world.close();
        }
    }
}
