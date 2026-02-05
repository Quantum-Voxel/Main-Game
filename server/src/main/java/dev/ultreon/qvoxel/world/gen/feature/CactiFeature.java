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
import dev.ultreon.qvoxel.util.JavaRNG;
import dev.ultreon.qvoxel.util.RNG;
import dev.ultreon.qvoxel.world.Fork;
import dev.ultreon.qvoxel.world.gen.TerrainFeature;
import dev.ultreon.qvoxel.world.gen.noise.NoiseConfig;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class CactiFeature extends TerrainFeature {
    private final NoiseConfig noiseConfig;
    private final Block block;
    private final float threshold;
    private final RNG random = new JavaRNG();
    private final int minTrunkHeight;
    private final int maxTrunkHeight;

    public CactiFeature(NoiseConfig trees, Block block, float threshold, int minTrunkHeight, int maxTrunkHeight) {
        super();

        noiseConfig = trees;
        this.block = block;
        this.threshold = threshold;
        this.minTrunkHeight = minTrunkHeight;
        this.maxTrunkHeight = maxTrunkHeight;
    }

    @Override
    public boolean handle(@NotNull Fork setter, long seed, int x, int height, int z) {
        if (noiseConfig == null) return false;

        random.setSeed(seed);
        random.setSeed(random.nextLong());

        if (random.nextFloat() < threshold) {
            var trunkHeight = random.randint(minTrunkHeight, maxTrunkHeight);

            for (int blkY = 1; blkY < trunkHeight; blkY++) {
                setter.set(x, blkY, z, block.getDefaultState());
            }
            return true;
        }

        return false;
    }
}
