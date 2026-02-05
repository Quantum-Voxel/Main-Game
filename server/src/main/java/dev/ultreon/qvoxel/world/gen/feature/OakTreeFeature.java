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
 * Oak tree feature.
 * A medium-height sturdy tree with a broad, irregular canopy.
 */
public class OakTreeFeature extends TerrainFeature {
    private final BlockState log;
    private final BlockState leaves;
    private final float threshold;

    public OakTreeFeature(BlockState log, BlockState leaves, float threshold) {
        this.log = log;
        this.leaves = leaves;
        this.threshold = threshold;
    }

    @Override
    public boolean shouldPlace(int x, int y, int z, BlockState origin, long seed, ServerWorld world) {
        // Require grass or dirt ground
        if (!origin.is(Blocks.GRASS_BLOCK) && !origin.is(Blocks.DIRT)) {
            return false;
        }

        // Ensure there’s enough air above
        for (int dy = 1; dy <= 9; dy++) {
            if (!world.get(x, y + dy, z).isAir()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean handle(@NotNull Fork fork, long seed, int x, int y, int z) {
        Random random = new Random(seed ^ x * 177889L ^ z * 998377L);
        if (random.nextFloat() > threshold) {
            return false;
        }

        int height = 5 + random.nextInt(3); // 5–7 tall

        // Slightly thicker trunk: 1×1 or 2×1
        int trunkWidth = random.nextBoolean() ? 1 : 2;

        int topY = y + height;

        // Canopy: spread out, irregular layers
        generateLeafLayer(fork, random, x, topY, z, 3, 0.25f);   // top layer
        generateLeafLayer(fork, random, x, topY - 1, z, 4, 0.3f); // middle
        generateLeafLayer(fork, random, x, topY - 2, z, 5, 0.45f); // bottom spread

        // Occasionally add an "overhang" branch
        if (random.nextFloat() < 0.4f) {
            int dx = random.nextBoolean() ? random.nextBoolean() ? 2 : -2 : 0;
            int dz = dx == 0 ? random.nextBoolean() ? 2 : -2 : 0;
            fork.set(x + dx, topY - 1, z + dz, log);
            fork.set(x + dx, topY, z + dz, leaves);
            fork.set(x + dx, topY + 1, z + dz, leaves);
        }

        for (int dy = 0; dy < height; dy++) {
            fork.set(x, y + dy, z, log);
            if (trunkWidth == 2) {
                fork.set(x + 1, y + dy, z, log);
            }
        }

        // Small roots around base
        if (random.nextFloat() < 0.5f) fork.set(x - 1, y, z, log);
        if (random.nextFloat() < 0.5f) fork.set(x + trunkWidth, y, z, log);
        if (random.nextFloat() < 0.5f) fork.set(x, y, z - 1, log);
        if (random.nextFloat() < 0.5f) fork.set(x, y, z + 1, log);

        return true;
    }

    private void generateLeafLayer(Fork fork, Random random, int x, int y, int z, int radius, float holeChance) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int dist = Math.abs(dx) + Math.abs(dz);
                if (dist <= radius && random.nextFloat() > holeChance) {
                    fork.set(x + dx, y, z + dz, leaves);
                }
            }
        }
    }
}
