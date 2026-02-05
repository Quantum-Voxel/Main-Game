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
import dev.ultreon.qvoxel.world.gen.carver.Carver;
import dev.ultreon.qvoxel.world.gen.carver.FlatWorldCarver;
import org.jetbrains.annotations.NotNull;

public class TestGenerator extends SimpleChunkGenerator {
    private final FlatWorldCarver carver;

    public TestGenerator(Registry<Biome> biomeRegistry) {
        super(biomeRegistry);

        carver = new FlatWorldCarver();
    }

    @Override
    protected void generateTerrain(@NotNull BuilderChunk chunk, @NotNull Carver carver, GenerationBarrier barrier) {
        for (int x = chunk.blockStart.x; x < chunk.blockStart.x + World.CHUNK_SIZE; x++) {
            for (int z = chunk.blockStart.z; z < chunk.blockStart.z + World.CHUNK_SIZE; z++) {
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
        return 1.1;
    }

    @Override
    public double evaluateNoise(int x, int z) {
        return carver.evaluateNoise(x, z);
    }
}
