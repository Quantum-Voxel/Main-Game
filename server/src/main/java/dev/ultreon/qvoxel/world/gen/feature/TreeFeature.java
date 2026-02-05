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
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.block.Blocks;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.world.Fork;
import dev.ultreon.qvoxel.server.ServerWorld;
import dev.ultreon.qvoxel.world.gen.TerrainFeature;
import dev.ultreon.qvoxel.util.JavaRNG;
import dev.ultreon.qvoxel.util.RNG;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

@Deprecated
public class TreeFeature extends TerrainFeature {
    private final Block trunk;
    private final Block leaves;
    private final float threshold;
    private final RNG random = new JavaRNG();
    private final int minTrunkHeight;
    private final int maxTrunkHeight;

    public TreeFeature(Block trunk, Block leaves, float threshold, int minTrunkHeight, int maxTrunkHeight) {
        super();

        this.trunk = trunk;
        this.leaves = leaves;
        this.threshold = threshold;
        this.minTrunkHeight = minTrunkHeight;
        this.maxTrunkHeight = maxTrunkHeight;
    }

    @Override
    public boolean shouldPlace(int x, int y, int z, @NotNull BlockState origin, long seed, @NotNull ServerWorld world) {
        Random random = new Random(x * 341873128712L ^ z * 132897987541L);
        if (random.nextFloat() > threshold) {
            return false;
        }

        for (int yOffset = 1; yOffset <= maxTrunkHeight + 2; yOffset++) {
            for (int xOffset = -1; xOffset <= 1; xOffset++) {
                for (int zOffset = -1; zOffset <= 1; zOffset++) {
                    if (world.getChunkOrNull(BlockVec.chunkOf(x + xOffset), BlockVec.chunkOf(y + yOffset), BlockVec.chunkOf(z + zOffset)) == null)
                        return false;
                    BlockState blockState = world.get(x + xOffset, yOffset, z + zOffset);
                    if (!blockState.isAir()) {
                        return false;
                    }
                }
            }
        }
        return origin.is(Blocks.GRASS_BLOCK) || origin.is(Blocks.SNOWY_GRASS_BLOCK) || origin.is(Blocks.DIRT);
    }

    @Override
    public boolean handle(@NotNull Fork setter, long seed, int x, int y, int z) {
        y++;

        random.setSeed(x * 341873128712L ^ z * 132897987541L);
        random.setSeed(random.nextLong());

        if (random.nextFloat() < threshold) {
            var trunkHeight = random.nextInt(minTrunkHeight, maxTrunkHeight);

            for (int ty = y; ty < y + trunkHeight; ty++) {
                setter.set(x, ty, z, trunk.getDefaultState());
            }

            setter.set(x, y - 1, z, Blocks.DIRT.getDefaultState());
            for (int xOffset = -1; xOffset <= 1; xOffset++) {
                for (int zOffset = -1; zOffset <= 1; zOffset++) {
                    for (int ty = trunkHeight - 1; ty <= trunkHeight + 1; ty++) {
                        if (xOffset == 0 && zOffset == 0 && ty != trunkHeight + 1) continue;
                        setter.set(x + xOffset, ty, z + zOffset, leaves.getDefaultState());
                    }
                }
            }

            return true;
        }

        return false;
    }
}
