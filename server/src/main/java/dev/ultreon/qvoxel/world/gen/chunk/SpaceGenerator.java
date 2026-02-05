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
import dev.ultreon.qvoxel.world.World;
import dev.ultreon.qvoxel.world.gen.GenerationBarrier;
import dev.ultreon.qvoxel.world.gen.biome.Biome;
import dev.ultreon.qvoxel.world.BuilderChunk;
import dev.ultreon.qvoxel.server.ServerWorld;
import dev.ultreon.qvoxel.world.gen.biome.BiomeGenerator;
import dev.ultreon.qvoxel.world.gen.carver.Carver;
import dev.ultreon.qvoxel.world.gen.carver.FloatingIslandsCarver;
import org.jetbrains.annotations.NotNull;

/**
 * The SpaceGenerator class extends the functionality of SimpleChunkGenerator to create
 * a custom terrain generator that produces space-like floating islands.
 */
public class SpaceGenerator extends SimpleChunkGenerator {
    private Carver carver;
    private BiomeGenerator space;

    public SpaceGenerator(Registry<Biome> biomeRegistry) {
        super(biomeRegistry);
    }

    @Override
    public void create(@NotNull ServerWorld world, long seed) {
        super.create(world, seed);

        carver = new FloatingIslandsCarver(seed);
        space = world.getServer().getBiomes().space.create(world);
    }

    @Override
    protected void generateTerrain(@NotNull BuilderChunk chunk, @NotNull Carver carver, GenerationBarrier barrier) {
        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int z = 0; z < World.CHUNK_SIZE; z++) {
                carver.carve(chunk, x, z);

                for (int y = 0; y < World.CHUNK_SIZE; y++) {
                    // Set biomes to registry key "quantum:space"
                    chunk.setBiomeGenerator(x, z, space);
                }
            }
        }
    }

    @Override
    @NotNull
    public Carver getCarver() {
        return carver;
    }

    @Override
    public double getTemperature(int x, int z) {
        return -4.0;
    }

    @Override
    public double evaluateNoise(int x, int z) {
        return carver.evaluateNoise(x, z);
    }
}
