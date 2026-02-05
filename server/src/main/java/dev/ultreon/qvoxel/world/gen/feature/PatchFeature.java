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
import dev.ultreon.qvoxel.world.Fork;
import dev.ultreon.qvoxel.server.ServerWorld;
import dev.ultreon.qvoxel.world.gen.TerrainFeature;
import dev.ultreon.qvoxel.world.gen.noise.NoiseConfig;
import dev.ultreon.qvoxel.world.gen.noise.NoiseInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PatchFeature extends TerrainFeature {
    private final NoiseConfig settingsBase;
    private final Block patchBlock;
    private final float threshold;
    @Nullable
    private NoiseInstance baseNoise;
    private final int depth;

    /**
     * Creates a new patch feature with the given settings
     *
     * @param settingsBase the noise config to use
     * @param patchBlock   the block to use for the patch
     * @param threshold    the threshold to use for the patch
     * @deprecated Use {@link #PatchFeature(NoiseConfig, Block, float, int)} instead
     */
    @Deprecated(since = "0.1.0", forRemoval = true)
    public PatchFeature(NoiseConfig settingsBase, Block patchBlock, float threshold) {
        this(settingsBase, patchBlock, threshold, 4);
    }

    /**
     * Creates a new patch feature with the given settings
     *
     * @param settingsBase the noise config to use
     * @param patchBlock   the block to use for the patch
     * @param threshold    the threshold to use for the patch
     * @param depth        the depth for the patch generation.
     */
    public PatchFeature(NoiseConfig settingsBase, Block patchBlock, float threshold, int depth) {
        this.settingsBase = settingsBase;
        this.patchBlock = patchBlock;
        this.threshold = threshold;
        this.depth = depth;
    }

    @Override
    public void create(@NotNull ServerWorld world, long seed) {
        super.create(world, seed);

        baseNoise = settingsBase.create(world.getSeed());
    }

    @Override
    public boolean handle(@NotNull Fork setter, long seed, int x, int y, int z) {
        if (baseNoise == null) return false;

        boolean changed = false;
        for (int blkY = -depth; blkY < 0; blkY++) {
            float value = (float) baseNoise.eval(x, blkY + y, z);
            changed |= value < threshold && setter.set(0, blkY, z, patchBlock.getDefaultState());
        }

        return changed;
    }

    @Override
    public void close() {
        if (baseNoise != null) baseNoise.close();
    }
}
