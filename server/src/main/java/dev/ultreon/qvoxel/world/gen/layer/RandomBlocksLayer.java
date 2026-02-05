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

package dev.ultreon.qvoxel.world.gen.layer;

import dev.ultreon.qvoxel.block.Block;
import dev.ultreon.qvoxel.world.BlockSetter;
import dev.ultreon.qvoxel.server.ServerWorld;
import dev.ultreon.qvoxel.world.World;
import dev.ultreon.qvoxel.world.gen.noise.NoiseConfig;
import dev.ultreon.qvoxel.world.gen.noise.NoiseInstance;
import dev.ultreon.qvoxel.util.RNG;
import org.jetbrains.annotations.NotNull;

public abstract class RandomBlocksLayer extends TerrainLayer {
    private final int from;
    private final int to;
    private final Block[] blocks;

    protected RandomBlocksLayer(int from, int to, Block[] blocks) {
        this.from = from;
        this.to = to;
        this.blocks = blocks;
    }

    public static RandomBlocksLayer surface(int thickness, int from, int to, Block... blocks) {
        return new RandomBlocksLayer(from, to, blocks) {
            public boolean shouldGenerate(int x, int y, int z, int height) {
                return y >= height - thickness && y <= height;
            }
        };
    }

    public static RandomBlocksLayer underground(@NotNull NoiseConfig noiseConfig, double threshold, int from, int to, Block... blocks) {
        return new RandomBlocksLayer(from, to, blocks) {
            private NoiseInstance noise;

            @Override
            public void create(@NotNull ServerWorld world) {
                super.create(world);

                noise = noiseConfig.create(world.getSeed() + noiseConfig.seed());
            }

            @Override
            public void close() {
                super.close();

                noise.close();
                noise = null;
            }

            public boolean shouldGenerate(int x, int y, int z, int height) {
                return y <= height && noise.eval(x, y, z) <= threshold;
            }
        };
    }

    @Override
    public boolean handle(@NotNull World world, BlockSetter chunk, @NotNull RNG rng, int x, int y, int z, int height) {
        if (from <= y && y <= to && y <= height && shouldGenerate(x, y, z, height)) {
            Block block = blocks[rng.nextInt(blocks.length)];
            chunk.set(x, y, z, block.getDefaultState());
            return true;
        }

        return false;
    }

    protected abstract boolean shouldGenerate(int x, int y, int z, int height);
}
