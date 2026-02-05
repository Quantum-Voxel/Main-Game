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

import dev.ultreon.qvoxel.registry.Registry;
import dev.ultreon.qvoxel.registry.RegistryKey;
import dev.ultreon.qvoxel.server.ServerWorld;
import dev.ultreon.qvoxel.world.BuilderChunk;
import dev.ultreon.qvoxel.world.HeightmapType;
import dev.ultreon.qvoxel.world.World;
import dev.ultreon.qvoxel.world.gen.GenerationBarrier;
import dev.ultreon.qvoxel.world.gen.biome.Biome;
import dev.ultreon.qvoxel.world.gen.carver.Carver;
import dev.ultreon.qvoxel.world.light.LightingSystem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * An abstract class implementing the ChunkGenerator interface, providing a base
 * for generating chunks in a world using biomes and domain warping layers.
 */
public abstract class SimpleChunkGenerator implements ChunkGenerator {
    private final Registry<Biome> biomesRegistry;
    final List<Biome> biomes = new ArrayList<>();

    /**
     * Constructs a SimpleChunkGenerator with a registry of biomes.
     *
     * @param biomeRegistry The Registry instance containing biome information. Must not be null.
     */
    public SimpleChunkGenerator(Registry<Biome> biomeRegistry) {
        biomesRegistry = biomeRegistry;
    }

    @Override
    public void create(@NotNull ServerWorld world, long seed) {

    }

    @Override
    public void generate(@NotNull ServerWorld world, BuilderChunk chunk, GenerationBarrier barrier, LightingSystem lightingSystem) {
        Carver carver = getCarver();
        if (barrier.compareTo(GenerationBarrier.FEATURE_INFO) < 0) return;
        if (chunk.currentBarrier.compareTo(GenerationBarrier.FEATURE_INFO) < 0) {
            world.getFeatureData().prepareChunk(chunk);
        }

        generateTerrain(chunk, carver, barrier);


        if (barrier.compareTo(GenerationBarrier.FEATURES) < 0) return;
        if (chunk.currentBarrier.compareTo(GenerationBarrier.FEATURES) < 0) {
            chunk.drainRecordedChanges();
            generateFeatures(chunk);
        }

        if (barrier.compareTo(GenerationBarrier.STRUCTURES) < 0) return;
        if (chunk.currentBarrier.compareTo(GenerationBarrier.STRUCTURES) < 0) {
            generateStructures(chunk);
        }

        if (barrier.compareTo(GenerationBarrier.LIGHTING) < 0) return;
        if (chunk.currentBarrier.compareTo(GenerationBarrier.LIGHTING) < 0) {
            lightingSystem.initSkyLight(chunk);
        }
    }

    /**
     * Generates terrain for a given chunk using a specified carver and records the changes.
     *
     * @param chunk   The chunk in which the terrain generation is to be performed. Must not be null.
     * @param carver  The carver used to shape the terrain within the chunk. Must not be null.
     * @param barrier
     */
    protected abstract void generateTerrain(@NotNull BuilderChunk chunk, @NotNull Carver carver, GenerationBarrier barrier);

    /**
     * Generates terrain features within a specified chunk based on biome and recording information.
     *
     * @param chunk The chunk for which the terrain features are being generated. Must not be null.
     */
    protected void generateFeatures(BuilderChunk chunk) {
        Random random = new Random();
        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int z = 0; z < World.CHUNK_SIZE; z++) {
                int height = chunk.getHeight(x, z, HeightmapType.TERRAIN);
                chunk.getBiomeGenerator(x, z).generateTerrainFeatures(chunk, random, chunk.blockStart.x + x, chunk.blockStart.z + z, height);
            }
        }
    }

    /**
     * Generates various structures within the provided chunk by leveraging the biome generator
     * for the chunk's regions.
     * It iterates through the X and Z coordinates of the chunk, determines
     * the highest Y coordinate at each (X, Z) pair, and triggers the biome-specific structure
     * generation at those points.
     *
     * @param chunk The BuilderChunk instance where the structures will be generated. Must not be null.
     */
    protected void generateStructures(BuilderChunk chunk) {
        for (var x = 0; x < World.CHUNK_SIZE; x++) {
            for (var z = 0; z < World.CHUNK_SIZE; z++) {
                chunk.getBiomeGenerator(x, z).generateStructureFeatures(chunk);
            }
        }
    }

    /**
     * Adds a biome to the list of biomes managed by this chunk generator.
     *
     * @param biome The registry key of the biome to be added. Must not be null.
     */
    protected final void addBiome(RegistryKey<Biome> biome) {
        biomes.add(biomesRegistry.get(biome));
    }

    /**
     * Provides the Carver instance responsible for shaping and carving terrain within a chunk.
     *
     * @return An instance of Carver which is used for terrain generation. Must not be null.
     */
    public abstract @NotNull Carver getCarver();

    @Override
    public void close() {

    }
}
