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

import dev.ultreon.qvoxel.block.Blocks;
import dev.ultreon.qvoxel.world.BlockSetter;
import dev.ultreon.qvoxel.world.World;
import dev.ultreon.qvoxel.util.RNG;

public class WaterTerrainLayer extends TerrainLayer {
    private final int waterLevel;

    public WaterTerrainLayer() {
        this(World.SEA_LEVEL);
    }

    public WaterTerrainLayer(int waterLevel) {
        this.waterLevel = waterLevel;
    }

    @Override
    public boolean handle(World world, BlockSetter chunk, RNG rng, int x, int y, int z, int height) {
        // Set water layer from height up to water level y
        if (y <= waterLevel + 1 && y > height) {
            return false;
        }

        // Set sand layer from the height - 3 up to water level + 2
        if (y <= waterLevel + 2 && y <= height && y >= height - 3 && height <= waterLevel + 2) {
            chunk.set(x, y, z, Blocks.SAND.getDefaultState());
            return true;
        }

        return false;

    }
}
