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

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.block.Blocks;
import dev.ultreon.qvoxel.registry.NamedTag;
import dev.ultreon.qvoxel.registry.Registry;
import dev.ultreon.qvoxel.server.ServerWorld;
import dev.ultreon.qvoxel.world.BuilderChunk;
import dev.ultreon.qvoxel.world.World;
import dev.ultreon.qvoxel.world.gen.GenerationBarrier;
import dev.ultreon.qvoxel.world.gen.biome.Biome;
import dev.ultreon.qvoxel.world.gen.biome.BiomeGenerator;
import dev.ultreon.qvoxel.world.gen.carver.Carver;
import dev.ultreon.qvoxel.world.gen.noise.NoiseConfig;
import dev.ultreon.qvoxel.world.light.LightingSystem;
import org.jetbrains.annotations.NotNull;

/**
 * The OverworldGenerator is responsible for generating the terrain of the Overworld.
 * It uses various noise configurations and biome data to create diverse and immersive biomes.
 * It extends the SimpleChunkGenerator, inheriting its basic functionalities and providing further customization.
 */
public class DebugGenerator implements ChunkGenerator {

    private NoiseConfig noiseConfig;
    private final NamedTag<Biome> biomeTag;

    private BiomeGenerator voidBiome;

    public DebugGenerator(Registry<Biome> biomeRegistry) {
        super();

        biomeTag = biomeRegistry.getTag(new Identifier("overworld_biomes")).orElseThrow();
    }

    @Override
    public void create(@NotNull ServerWorld world, long seed) {
        voidBiome = world.getServer().getBiomes().void_.create(world);
    }

    @Override
    public void generate(@NotNull ServerWorld world, BuilderChunk chunk, GenerationBarrier barrier, LightingSystem lightingSystem) {
        fillBiome(chunk);

        if (chunk.vec.equals(0, 0, 0)) {
            sphere(chunk);
        } else if (chunk.vec.equals(2, 0, 0)) {
            cube(chunk);
        } else if (chunk.vec.equals(0, 0, 2)) {
            pyramid(chunk);
        } else if (chunk.vec.equals(2, 0, 2)) {
            cone(chunk);
        } else if (chunk.vec.equals(0, 2, 0)) {
            cylinder(chunk);
        } else if (chunk.vec.equals(2, 2, 0)) {
            checkerboard(chunk);
        }

        if (chunk.vec.y <= -1) {
            for (int x = 0; x < World.CHUNK_SIZE; x++) {
                for (int y = 0; y < World.CHUNK_SIZE; y++) {
                    for (int z = 0; z < World.CHUNK_SIZE; z++) {
                        chunk.set(x, y, z, Blocks.VOID_BARRIER.getDefaultState());
                    }
                }
            }
        }
    }

    @Override
    public Carver getCarver() {
        return new VoidCarver();
    }

    private void random(@NotNull BuilderChunk chunk) {
        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int y = 0; y < World.CHUNK_SIZE; y++) {
                for (int z = 0; z < World.CHUNK_SIZE; z++) {
                    if (Math.random() < 0.1) {
                        chunk.set(x, y, z, Blocks.STONE.getDefaultState());
                    }
                }
            }
        }
    }

    private void checkerboard(@NotNull BuilderChunk chunk) {
        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int y = 0; y < World.CHUNK_SIZE; y++) {
                for (int z = 0; z < World.CHUNK_SIZE; z++) {
                    if (x % 2 == 0 && y % 2 == 0 && z % 2 == 0) {
                        chunk.set(x, y, z, Blocks.STONE.getDefaultState());
                    }
                }
            }
        }
    }

    private static void sphere(@NotNull BuilderChunk chunk) {
        int centerX = World.CHUNK_SIZE / 2;
        int centerZ = World.CHUNK_SIZE / 2;
        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int y = 0; y < World.CHUNK_SIZE; y++) {
                for (int z = 0; z < World.CHUNK_SIZE; z++) {
                    double distance = Math.sqrt((x - centerX) * (x - centerX) + (y - World.CHUNK_SIZE / 2) * (y - World.CHUNK_SIZE / 2) + (z - centerZ) * (z - centerZ));
                    if (distance <= World.CHUNK_SIZE / 2 - 2) {
                        chunk.set(x, y, z, Blocks.STONE.getDefaultState());
                    }
                }
            }
        }
    }

    private static void cube(@NotNull BuilderChunk chunk) {
        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int y = 0; y < World.CHUNK_SIZE; y++) {
                for (int z = 0; z < World.CHUNK_SIZE; z++) {
                    if (x == 0 || x == World.CHUNK_SIZE - 1 || y == 0 || y == World.CHUNK_SIZE - 1 || z == 0 || z == World.CHUNK_SIZE - 1) {
                        chunk.set(x, y, z, Blocks.STONE.getDefaultState());
                    }
                }
            }
        }
    }

    private static void pyramid(@NotNull BuilderChunk chunk) {
        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int y = 0; y < World.CHUNK_SIZE; y++) {
                for (int z = 0; z < World.CHUNK_SIZE; z++) {
                    if (x == 0 || x == World.CHUNK_SIZE - 1 || y == 0 || y == World.CHUNK_SIZE - 1 || z == 0) {
                        chunk.set(x, y, z, Blocks.STONE.getDefaultState());
                    }
                }
            }
        }
    }

    private static void cone(@NotNull BuilderChunk chunk) {
        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int y = 0; y < World.CHUNK_SIZE; y++) {
                for (int z = 0; z < World.CHUNK_SIZE; z++) {
                    int distance = Math.max(Math.abs(x - World.CHUNK_SIZE / 2), Math.abs(z - World.CHUNK_SIZE / 2));
                    if (distance <= World.CHUNK_SIZE - y - 2) {
                        chunk.set(x, y, z, Blocks.STONE.getDefaultState());
                    }
                }
            }
        }
    }

    private static void cylinder(@NotNull BuilderChunk chunk) {
        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int y = 0; y < World.CHUNK_SIZE; y++) {
                for (int z = 0; z < World.CHUNK_SIZE; z++) {
                    int distance = Math.max(Math.abs(x - World.CHUNK_SIZE / 2), Math.abs(z - World.CHUNK_SIZE / 2));
                    if (distance <= World.CHUNK_SIZE / 2 - 2) {
                        chunk.set(x, y, z, Blocks.STONE.getDefaultState());
                    }
                }
            }
        }
    }

    private void fillBiome(@NotNull BuilderChunk chunk) {
        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int z = 0; z < World.CHUNK_SIZE; z++) {
                chunk.setBiomeGenerator(x, z, voidBiome);
            }
        }
    }

    @Override
    public double getTemperature(int x, int z) {
        return 1.0;
    }

    @Override
    public double evaluateNoise(int x, int z) {
        return getCarver().evaluateNoise(x, z);
    }

    @Override
    public void close() {

    }
}
