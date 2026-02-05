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
import dev.ultreon.qvoxel.block.Blocks;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.server.ServerWorld;
import dev.ultreon.qvoxel.world.Fork;
import dev.ultreon.qvoxel.world.gen.TerrainFeature;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Foliage feature that generates short grass patches on the ground.
 */
public class FoliageFeature extends TerrainFeature {
    private final BlockState foliage;
    private final float threshold;
    private final Block placesOn;

    public FoliageFeature(BlockState foliage, float threshold) {
        this(foliage, threshold, Blocks.SHORT_GRASS);
    }

    public FoliageFeature(BlockState foliage, float threshold, Block placesOn) {
        this.foliage = foliage;
        this.threshold = threshold;
        this.placesOn = placesOn;
    }

    @Override
    public boolean shouldPlace(int x, int y, int z, BlockState origin, long seed, ServerWorld world) {
        // Only place on grass blocks with air above
        if (!origin.is(placesOn)) {
            return false;
        }
        return world.get(x, y + 1, z).isAir();
    }

    @Override
    public boolean handle(@NotNull Fork fork, long seed, int x, int y, int z) {
        Random random = new Random(seed);

        // Chance: only sometimes place foliage
        if (random.nextFloat() < threshold) { // 75% chance
            fork.set(x, y + 1, z, foliage);
            return true;
        }
        return false;
    }
}
