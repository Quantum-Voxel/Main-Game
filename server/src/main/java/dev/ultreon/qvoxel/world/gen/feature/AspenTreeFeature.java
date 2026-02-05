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
 * Aspen tree feature for world generation.
 * Generates a thin white trunk with light-green leaves at the top.
 */
public class AspenTreeFeature extends TerrainFeature {
    private final BlockState log;
    private final BlockState leaves;
    private final float threshold;

    public AspenTreeFeature(BlockState log, BlockState leaves, float threshold) {
        this.log = log;
        this.leaves = leaves;
        this.threshold = threshold;
    }

    @Override
    public boolean shouldPlace(int x, int y, int z, BlockState origin, long seed, ServerWorld world) {
        // Only place if the origin block is grass or dirt and space above is air
        if (!origin.is(Blocks.GRASS_BLOCK) && !origin.is(Blocks.DIRT)) {
            return false;
        }

        Random random = new Random(x * 341873128712L ^ z * 132897987541L);
        if (random.nextFloat() > threshold) {
            return false;
        }

        // Check that there’s enough air above
        for (int dy = 1; dy <= 8; dy++) {
            if (!world.get(x, y + dy, z).isAir()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean handle(@NotNull Fork fork, long seed, int x, int y, int z) {
        Random random = new Random(seed ^ x * 341873128712L ^ z * 132897987541L);
        random.nextFloat(); // Skip the first random float to avoid seed collision

        int height = 8 + random.nextInt(3); // Aspen trees are thin and medium tall

        // Generate leaves (clustered at top, irregular)
        int topY = y + height;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = -2; dy <= 4; dy++) {
                    int dist = Math.abs(dx) + Math.abs(dz) + Math.abs(dy / 2);
                    if (dist <= 3 && random.nextFloat() > 0.25f) {
                        fork.set(x + dx, topY + dy, z + dz, leaves);
                    }
                }
            }
        }

        // Generate trunk
        for (int dy = 0; dy < height; dy++) {
            fork.set(x, y + dy, z, log);
        }

        // Small extra leaves along trunk (aspen has leafy shoots)
        for (int dy = 2; dy < height - 2; dy++) {
            if (random.nextFloat() < 0.2f) {
                fork.set(x + 1, y + dy, z, leaves);
            }
            if (random.nextFloat() < 0.2f) {
                fork.set(x - 1, y + dy, z, leaves);
            }
            if (random.nextFloat() < 0.2f) {
                fork.set(x, y + dy, z + 1, leaves);
            }
            if (random.nextFloat() < 0.2f) {
                fork.set(x, y + dy, z - 1, leaves);
            }
        }

        return true;
    }

    @Override
    public void close() {
        // Cleanup if needed
    }

}
