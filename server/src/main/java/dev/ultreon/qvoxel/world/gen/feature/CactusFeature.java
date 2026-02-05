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
 * Cactus feature for desert biomes.
 * Generates tall cactus columns with occasional arms.
 */
public class CactusFeature extends TerrainFeature {
    private final BlockState cactus;
    private final float threshold;

    public CactusFeature(BlockState cactus, float threshold) {
        this.cactus = cactus;
        this.threshold = threshold;
    }

    @Override
    public boolean shouldPlace(int x, int y, int z, BlockState origin, long seed, ServerWorld world) {
        // Only place on sand or red sand, with air above
        if (!origin.is(Blocks.SAND) && !origin.is(Blocks.RED_SAND)) {
            return false;
        }

        Random random = new Random(seed ^ x * 42317831L ^ z * 98123761L);
        if (random.nextFloat() > threshold) {
            return false;
        }

        return world.get(x, y + 1, z).isAir();
    }

    @Override
    public boolean handle(@NotNull Fork fork, long seed, int x, int y, int z) {
        Random random = new Random(seed ^ x * 42317831L ^ z * 98123761L);
        random.nextFloat();

        int height = 2 + random.nextInt(4); // cactus 2–5 tall

        // Main column
        for (int dy = 0; dy < height; dy++) {
            fork.set(x, y + dy, z, cactus);
        }

        // TODO: Add cacti arms 🌵
/*
        // Chance to generate 1–2 arms
        int arms = random.nextInt(3); // 0–2 arms
        for (int i = 0; i < arms; i++) {
            int armY = y + 1 + random.nextInt(height - 1);
            int dx = 0, dz = 0;

            // Pick a horizontal direction
            switch (random.nextInt(4)) {
                case 0 -> dx = 1;
                case 1 -> dx = -1;
                case 2 -> dz = 1;
                case 3 -> dz = -1;
            }

            // Only generate arm if space is free
            if (fork.get(x + dx, armY, z + dz).isAir()) {
                fork.set(x + dx, armY, z + dz, cactus);

                // Small vertical growth at arm end
                if (random.nextBoolean() && fork.get(x + dx, armY + 1, z + dz).isAir()) {
                    fork.set(x + dx, armY + 1, z + dz, cactus);
                }
            }
        }
*/

        return true;
    }
}
