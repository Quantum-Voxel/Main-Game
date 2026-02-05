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
import dev.ultreon.qvoxel.world.gen.biome.IntRange;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Generates simple ore veins inside stone-like blocks.
 */
public class OreFeature extends TerrainFeature {
    private final BlockState ore;
    private final float rarity;
    private final IntRange veinSize;
    private final IntRange yLevel;
    private final Block replacement;

    public OreFeature(BlockState ore, float rarity, IntRange veinSize, IntRange yLevel) {
        this(ore, rarity, veinSize, yLevel, Blocks.STONE);
    }

    public OreFeature(BlockState ore, float rarity, IntRange veinSize, IntRange yLevel, Block replacement) {
        this.ore = ore;
        this.rarity = rarity;
        this.veinSize = veinSize;
        this.yLevel = yLevel;
        this.replacement = replacement;
    }

    @Override
    public boolean shouldPlace(int x, int y, int z, BlockState origin, long seed, ServerWorld world) {
        // Only place in stone within height range
        Random random = new Random(seed);
        if (random.nextInt(ServerWorld.CHUNK_VOLUME) > rarity) {
            return false;
        }

        return y >= yLevel.getMin() && y <= yLevel.getMax() && origin.is(replacement);
    }

    @Override
    public boolean handle(@NotNull Fork setter, long seed, int x, int y, int z) {
        Random random = new Random(seed);
        random.nextFloat();

        int dx = x;
        int dy = y;
        int dz = z;

        int size = veinSize.random(random);
        for (int i = 0; i < size; i++) {
            if (setter.get(dx, dy, dz).is(replacement))
                setter.set(dx, dy, dz, ore);
            dx += random.nextInt(3) - 1;
            dy += random.nextInt(3) - 1;
            dz += random.nextInt(3) - 1;
        }
        return true;
    }
}
