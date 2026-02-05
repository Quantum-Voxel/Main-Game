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
 * Pine tree feature for world generation.
 * Tall, narrow trunk with conical foliage.
 */
public class PineTreeFeature extends TerrainFeature {
    private final BlockState log;
    private final BlockState leaves;
    private final float threshold;

    public PineTreeFeature(BlockState log, BlockState leaves, float threshold) {
        this.log = log;
        this.leaves = leaves;
        this.threshold = threshold;
    }

    @Override
    public boolean shouldPlace(int x, int y, int z, BlockState origin, long seed, ServerWorld world) {
        Random random = new Random(seed);
        if (random.nextFloat() > threshold) {
            return false;
        }

        return origin.is(Blocks.GRASS_BLOCK) || origin.is(Blocks.DIRT);
    }

    @Override
    public boolean handle(@NotNull Fork fork, long seed, int x, int y, int z) {
        Random random = new Random(seed);
        random.nextFloat(); // Skip the first random float to avoid seed collision

        int height = 8 + random.nextInt(5); // Tall tree: 8–12 blocks

        // Generate trunk
        for (int dy = 0; dy < height; dy++) {
            fork.set(x, y + dy, z, log);
        }

        int topY = y + height;

        // Conical foliage: starts a few blocks above base, tapering to the top
        for (int layer = 0; layer < height / 2; layer++) {
            int radius = (height / 2 - layer) / 2 + 1; // Tapering radius
            int yLayer = y + height - height / 2 + layer; // start mid-trunk
            generateLeafLayer(fork, random, x, yLayer, z, radius);
        }

        return true;
    }

    private void generateLeafLayer(Fork fork, Random random, int x, int y, int z, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int dist = Math.abs(dx) + Math.abs(dz);
                if (dist <= radius) {
                    // Occasionally skip a leaf for natural gaps
                    if (random.nextFloat() > 0.1f) {
                        fork.set(x + dx, y, z + dz, leaves);
                    }
                }
            }
        }
    }
}
