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

package dev.ultreon.qvoxel.world.gen.chunk;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.registry.RegistryKey;
import dev.ultreon.qvoxel.registry.RegistryKeys;
import dev.ultreon.qvoxel.server.ServerWorld;
import dev.ultreon.qvoxel.world.BuilderChunk;
import dev.ultreon.qvoxel.world.gen.GenerationBarrier;
import dev.ultreon.qvoxel.world.gen.carver.Carver;
import dev.ultreon.qvoxel.world.light.LightingSystem;
import org.jetbrains.annotations.NotNull;

/**
 * The ChunkGenerator interface defines the required methods for creating and generating chunks in a server world
 * using specific parameters. Implementations should provide the logic for terrain and feature generation, along
 * with any domain warping functionalities.
 */
public interface ChunkGenerator extends AutoCloseable {
    RegistryKey<ChunkGenerator> OVERWORLD = RegistryKey.of(RegistryKeys.CHUNK_GENERATOR, CommonConstants.id("overworld"));
    RegistryKey<ChunkGenerator> TEST = RegistryKey.of(RegistryKeys.CHUNK_GENERATOR, CommonConstants.id("test"));
    RegistryKey<ChunkGenerator> FLOATING_ISLANDS = RegistryKey.of(RegistryKeys.CHUNK_GENERATOR, CommonConstants.id("floating_islands"));

    /**
     * Creates a new instance of the chunk generator in the specified world, using the provided seed.
     *
     * @param world the server world in which the chunk generator is being created
     * @param seed  the seed used for generating the chunk
     */
    void create(@NotNull ServerWorld world, long seed);

    /**
     * Generates terrain and features for the provided chunk in the specified world, taking into account the changes
     * made in neighboring chunks.
     *
     * @param world          the server world in which the chunk is being generated
     * @param chunk          the chunk to be generated
     * @param barrier
     * @param lightingSystem
     */
    void generate(@NotNull ServerWorld world, BuilderChunk chunk, GenerationBarrier barrier, LightingSystem lightingSystem);

    Carver getCarver();

    double getTemperature(int x, int z);

    double evaluateNoise(int x, int z);

    default boolean is2DNoise() {
        return false;
    }

}
