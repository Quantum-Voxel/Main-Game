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
import dev.ultreon.qvoxel.server.ServerWorld;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.world.BuilderChunk;
import dev.ultreon.qvoxel.world.World;
import dev.ultreon.qvoxel.world.gen.GenerationBarrier;
import dev.ultreon.qvoxel.world.gen.biome.Biome;
import dev.ultreon.qvoxel.world.gen.carver.Carver;
import dev.ultreon.qvoxel.world.gen.carver.HellLandscapeCarver;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * HellGenerator is a specialized chunk generator designed to create a hellish
 * terrain in a Quantum Voxel world. It extends the SimpleChunkGenerator to leverage
 * common chunk generation functionalities, and introduces specific terrain
 * carving mechanisms for a hell-like environment.
 */
@ApiStatus.Experimental
public class HellGenerator extends SimpleChunkGenerator {
    private Carver carver;

    public HellGenerator(Registry<Biome> biomeRegistry) {
        super(biomeRegistry);
    }

    @Override
    public void create(@NotNull ServerWorld world, long seed) {
        super.create(world, seed);

        carver = new HellLandscapeCarver(seed);
    }

    @Override
    protected void generateTerrain(@NotNull BuilderChunk chunk, @NotNull Carver carver, GenerationBarrier barrier) {
        BlockVec offset = chunk.blockStart;

        for (int x = offset.x; x < offset.x + World.CHUNK_SIZE; x++) {
            for (int z = offset.z; z < offset.z + World.CHUNK_SIZE; z++) {
                this.carver.carve(chunk, x, z);
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
        return 4.0;
    }

    @Override
    public double evaluateNoise(int x, int z) {
        return carver.evaluateNoise(x, z);
    }
}
