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
import dev.ultreon.qvoxel.util.RNG;
import dev.ultreon.qvoxel.world.BlockSetter;
import dev.ultreon.qvoxel.world.World;

public class SurfaceTerrainLayer extends TerrainLayer {
    public final Block surfaceBlock;
    private final int height;

    public SurfaceTerrainLayer(Block surfaceBlock, int height) {
        this.surfaceBlock = surfaceBlock;
        this.height = height;
    }

    @Override
    public boolean handle(World world, BlockSetter chunk, RNG rng, int x, int y, int z, int height) {
        if (y >= height - this.height && y <= height) {
            chunk.set(x, y, z, surfaceBlock.getDefaultState());
            return true;
        }
        return false;
    }
}
