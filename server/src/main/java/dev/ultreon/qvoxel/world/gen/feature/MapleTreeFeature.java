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

import dev.ultreon.qvoxel.block.Blocks;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.server.ServerWorld;
import dev.ultreon.qvoxel.world.Fork;
import dev.ultreon.qvoxel.world.gen.TerrainFeature;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Maple tree feature for world generation.
 * Generates a thicker trunk with a wide, round canopy.
 */
public class MapleTreeFeature extends TerrainFeature {
    private final BlockState log;
    private final BlockState leaves;
    private final float threshold;

    public MapleTreeFeature(BlockState log, BlockState leaves, float threshold) {
        this.log = log;
        this.leaves = leaves;
        this.threshold = threshold;
    }

    @Override
    public boolean shouldPlace(int x, int y, int z, BlockState origin, long seed, ServerWorld world) {
        // Only place on grass or dirt
        if (!origin.is(Blocks.GRASS_BLOCK) && !origin.is(Blocks.DIRT)) {
            return false;
        }

        Random random = new Random(x * 341873128712L ^ z * 132897987541L);
        if (random.nextFloat() > threshold) {
            return false;
        }

        // Check space for canopy
        for (int dy = 1; dy <= 10; dy++) {
            if (!world.get(x, y + dy, z).isAir()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean handle(@NotNull Fork fork, long seed, int x, int y, int z) {
        Random random = new Random(seed);
        random.nextFloat(); // Skip the first random float to avoid seed collision

        int height = 6 + random.nextInt(3); // 6–8 tall

        // Thicker 2×2 trunk
        for (int dy = 0; dy < height; dy++) {
            fork.set(x, y + dy, z, log);
            fork.set(x + 1, y + dy, z, log);
            fork.set(x, y + dy, z + 1, log);
            fork.set(x + 1, y + dy, z + 1, log);
        }

        int topY = y + height;

        // Irregular layered canopy
        // Bottom layer (wide, spread out)
        generateLeafLayer(fork, random, x, topY - 2, z, 3, 0.5f);

        // Middle layer (dense)
        generateLeafLayer(fork, random, x, topY - 1, z, 3, 0.3f);

        // Top layer (smaller, less wide)
        generateLeafLayer(fork, random, x, topY, z, 2, 0.2f);

        // A few extra leaves above the top for irregular shape
        if (random.nextBoolean()) {
            fork.set(x, topY + 1, z, leaves);
            fork.set(x + 1, topY + 1, z, leaves);
        }

        // Small roots
        if (random.nextFloat() < 0.5f) fork.set(x - 1, y, z, log);
        if (random.nextFloat() < 0.5f) fork.set(x + 2, y, z, log);
        if (random.nextFloat() < 0.5f) fork.set(x, y, z - 1, log);
        if (random.nextFloat() < 0.5f) fork.set(x, y, z + 2, log);

        return true;
    }

    private void generateLeafLayer(Fork fork, Random random, int x, int y, int z, int radius, float holeChance) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                // Cheaper "roundness": diamond-shaped with randomness
                if (Math.abs(dx) + Math.abs(dz) <= radius + random.nextInt(2)) {
                    if (random.nextFloat() > holeChance) {
                        fork.set(x + dx, y, z + dz, leaves);
                        fork.set(x + dx + 1, y, z + dz, leaves);
                        fork.set(x + dx, y, z + dz + 1, leaves);
                        fork.set(x + dx + 1, y, z + dz + 1, leaves);
                    }
                }
            }
        }
    }
}
