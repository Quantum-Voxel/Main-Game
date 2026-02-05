
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

import de.articdive.jnoise.core.api.functions.Interpolation;
import de.articdive.jnoise.core.api.pipeline.NoiseSource;
import de.articdive.jnoise.generators.noise_parameters.fade_functions.FadeFunction;
import de.articdive.jnoise.pipeline.JNoise;
import dev.ultreon.qvoxel.block.Blocks;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.world.BuilderChunk;
import dev.ultreon.qvoxel.world.World;

public class HellLandscapeCarver implements Carver {
    private final NoiseSource source;

    public HellLandscapeCarver(long seed) {
        source = JNoise.newBuilder()
                .perlin(seed, Interpolation.COSINE, FadeFunction.CUBIC_POLY)
                .scale(1 / 32.0f)
                .build();
    }

    @Override
    public double carve(BuilderChunk chunk, int x, int z) {
        BlockVec offset = chunk.blockStart;
        for (int y = offset.y; y < offset.y + World.CHUNK_SIZE; y++) {
            double noise = source.evaluateNoise(x, y, z);
            if (noise < 0.1) {
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
        return noise >= 0.1;
    }
}
