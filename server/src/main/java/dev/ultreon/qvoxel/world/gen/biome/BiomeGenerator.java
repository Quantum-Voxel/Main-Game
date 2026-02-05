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

package dev.ultreon.qvoxel.world.gen.biome;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.registry.RegistryKey;
import dev.ultreon.qvoxel.registry.RegistryKeys;
import dev.ultreon.qvoxel.server.QuantumServer;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.util.RNG;
import dev.ultreon.qvoxel.world.BuilderChunk;
import dev.ultreon.qvoxel.world.BuilderFork;
import dev.ultreon.qvoxel.world.World;
import dev.ultreon.qvoxel.world.WorldSlice;
import dev.ultreon.qvoxel.world.gen.StructureInstance;
import dev.ultreon.qvoxel.world.gen.TerrainFeature;
import dev.ultreon.qvoxel.world.gen.layer.TerrainLayer;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * The BiomeGenerator class is responsible for generating terrain and features for a given biome in the world.
 * It applies various terrain layers and world generation features to specific chunk columns.
 */
public class BiomeGenerator implements AutoCloseable {
    private final World world;
    private final List<TerrainLayer> layers;
    private final List<TerrainFeature> surfaceFeatures;
    private final List<TerrainFeature> undergroundFeatures;
    private final Biome biome;

    /**
     * Constructs a BiomeGenerator with the specified parameters.
     *
     * @param world           the world in which the biome generator will operate
     * @param biome           the biome type to be generated
     * @param layers          the list of terrain layers to apply in the world generation
     * @param surfaceFeatures the list of world generation surface features to include
     */
    public BiomeGenerator(World world, Biome biome, List<TerrainLayer> layers, List<TerrainFeature> surfaceFeatures, List<TerrainFeature> undergroundFeatures) {
        this.world = world;
        this.biome = biome;
        this.layers = layers;
        this.surfaceFeatures = surfaceFeatures;
        this.undergroundFeatures = undergroundFeatures;
    }

    public void processColumn(BuilderChunk chunk, int x, int y, int z) {
//        LightMap lightMap = chunk.getLightMap();

        generateTerrainLayers(chunk, x, z, y);
    }

    /**
     * Generates terrain features for a specified chunk column by applying various world generation features.
     *
     * @param chunk     the chunk in which to generate the terrain features
     * @param x         the x-coordinate within the chunk
     * @param z         the z-coordinate within the chunk
     * @param groundPos the ground relativePos at the specified coordinates
     */
    public void generateTerrainFeatures(BuilderChunk chunk, Random random, int x, int z, int groundPos) {
        try {
            for (int y = 0; y < World.CHUNK_SIZE; y++) {
                BlockVec blockInWorld = chunk.vec.blockInWorldSpace(x, y, z);

                for (var feature : undergroundFeatures) {
                    genFeature(chunk, random, blockInWorld.x, blockInWorld.y, blockInWorld.z, groundPos, feature);
                }
            }

            if (chunk.blockEnd.y >= groundPos && chunk.blockStart.y <= groundPos) {
                for (var feature : surfaceFeatures) {
                    genFeature(chunk, random, x, groundPos, z, groundPos, feature);
                }
            }
        } catch (Exception e) {
            CommonConstants.LOGGER.error("Error generating terrain features for chunk {}", chunk.vec, e);
            throw e;
        }
    }

    private static long getPosSeed(long worldSeed, long featureSeed, int x, int y, int z) {
        long seed = worldSeed;
        seed ^= featureSeed * 0x9E3779B97F4A7C15L; // large odd constant (golden ratio in hex)
        seed ^= (long) x * 0x632BE59BD9B4E019L;
        seed ^= (long) y * 0x9E3779B97F4A7C15L;
        seed ^= (long) z * 0xC2B2AE3D27D4EB4FL;
        seed = Long.rotateLeft(seed, 27) ^ seed >>> 33; // final scramble
        return seed;
    }

    public void genFeature(BuilderChunk chunk, Random random, int x, int y, int z, int worldHeight, TerrainFeature feature) {
        BuilderFork fork = chunk.createFork(BlockVec.localize(x), BlockVec.localize(y), BlockVec.localize(z));
        BlockVec localVec = new BlockVec(x, y, z).chunkLocal();
        long posSeed = getPosSeed(chunk.getWorld().getSeed(), feature.seed(), x, y, z);
        if (feature.shouldPlace(x, y, z, chunk.get(localVec.x, localVec.y, localVec.z), posSeed, chunk.getWorld())) {
            feature.handle(fork, posSeed, x, y, z);
        }
    }

    /**
     * Generates terrain layers for a specified chunk column by applying various terrain layers.
     *
     * @param chunk     the chunk in which to generate the terrain layers
     * @param x         the x-coordinate within the chunk
     * @param z         the z-coordinate within the chunk
     * @param groundPos the ground relativePos at the specified coordinates
     */
    public void generateTerrainLayers(BuilderChunk chunk, int x, int z, int groundPos) {
        RNG rng = chunk.getRNG();
        if (chunk.vec.y > 256 / World.CHUNK_SIZE)
            return;

        BlockVec offset = chunk.blockStart;
        for (int y = 0; y < World.CHUNK_SIZE; y++) {
            if (chunk.get(x, y, z).isAir()) continue;

            for (var layer : layers) {
                if (layer.handle(world, new WorldSlice(chunk), rng, offset.x + x, offset.y + y, offset.z + z, groundPos)) {
                    break;
                }
            }
        }
    }

    public void generateStructureFeatures(BuilderChunk recordingChunk) {
        Collection<StructureInstance> structures = recordingChunk.getWorld().getStructuresAt(recordingChunk.vec);

        for (StructureInstance struc : structures) {
            struc.placeSlice(recordingChunk);
        }
    }

    @Override
    public void close() {
        layers.forEach(TerrainLayer::close);
        surfaceFeatures.forEach(TerrainFeature::close);
        undergroundFeatures.forEach(TerrainFeature::close);
    }

    public World getWorld() {
        return world;
    }

    public Biome getBiome() {
        return biome;
    }

    public RegistryKey<Biome> getBiomeKey(QuantumServer server) {
        return server.getRegistries().get(RegistryKeys.BIOME).getKey(biome);
    }

    public static class Index {
        public BiomeGenerator biomeGenerator;
        @Nullable
        public Integer terrainSurfaceNoise;

        public Index(BiomeGenerator biomeGenerator) {
            this(biomeGenerator, null);
        }

        public Index(BiomeGenerator biomeGenerator, @Nullable Integer terrainSurfaceNoise) {
            this.biomeGenerator = biomeGenerator;
            this.terrainSurfaceNoise = terrainSurfaceNoise;
        }
    }
}
