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

package dev.ultreon.qvoxel.world.gen.feature;

import dev.ultreon.qvoxel.block.Block;
import dev.ultreon.qvoxel.world.Fork;
import dev.ultreon.qvoxel.server.ServerWorld;
import dev.ultreon.qvoxel.world.gen.TerrainFeature;
import dev.ultreon.qvoxel.world.gen.noise.NoiseConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class RockFeature extends TerrainFeature {
    private final NoiseConfig noiseConfig;
    private final Block material;
    private final float threshold;
    private final Random random = new Random();

    public RockFeature(NoiseConfig trees, Block material, float threshold) {
        super();

        noiseConfig = trees;
        this.material = material;
        this.threshold = threshold;
    }

    @Override
    public boolean handle(@NotNull Fork setter, long seed, int x, int y, int z) {
        if (noiseConfig == null) return false;

        random.setSeed(seed);
        random.setSeed(random.nextLong());

        if (random.nextFloat() < threshold) {
            for (int xOffset = -1; xOffset < 1; xOffset++) {
                for (int zOffset = -1; zOffset < 1; zOffset++) {
                    for (int blkY = 0; blkY <= 1; blkY++) {
                        setter.set(x + xOffset, blkY, z + zOffset, material.getDefaultState());
                    }
                }
            }
            return true;
        }

        return false;
    }
}
