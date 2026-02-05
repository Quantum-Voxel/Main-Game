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

package dev.ultreon.qvoxel.world.gen.carver;

import de.articdive.jnoise.core.api.pipeline.NoiseSource;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex2DVariant;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex3DVariant;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex4DVariant;
import de.articdive.jnoise.pipeline.JNoise;
import dev.ultreon.qvoxel.block.Blocks;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.world.BuilderChunk;
import dev.ultreon.qvoxel.world.World;

public class FloatingIslandsCarver implements Carver {
    private final NoiseSource source;

    public FloatingIslandsCarver(long seed) {
        source = JNoise.newBuilder()
                .fastSimplex(seed, Simplex2DVariant.CLASSIC, Simplex3DVariant.CLASSIC, Simplex4DVariant.CLASSIC)
                .scale(1 / 32.0f)
                .build();
    }

    @Override
    public double carve(BuilderChunk chunk, int x, int z) {
        BlockVec offset = chunk.blockStart;
        for (int y = 0; y < World.CHUNK_SIZE; y++) {
            double noise = source.evaluateNoise(x, offset.y + y, z);
            if (noise > 0.7) {
                chunk.set(x, y, z, Blocks.COBBLESTONE.getDefaultState());
            }
        }

        return -1;
    }

    @Override
    public double evaluateNoise(double x, double z) {
        return 0;
    }

    @Override
    public boolean isAir(int x, int y, int z) {
        double noise = source.evaluateNoise(x, y, z);
        return noise <= 0.7;
    }
}
