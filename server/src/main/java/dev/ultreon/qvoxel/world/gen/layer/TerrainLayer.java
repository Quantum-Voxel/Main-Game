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

import dev.ultreon.qvoxel.server.ServerWorld;
import dev.ultreon.qvoxel.util.RNG;
import dev.ultreon.qvoxel.world.BlockSetter;
import dev.ultreon.qvoxel.world.World;
import org.jetbrains.annotations.ApiStatus;

/**
 * Abstract class representing a single layer in terrain generation.
 * This class is used to define how blocks are set within a specific layer
 * during the construction of a chunk in the world generation process.
 */
public abstract class TerrainLayer implements AutoCloseable {
    /**
     * Handles the setting of blocks in a specific layer during the generation of a chunk in the world.
     *
     * @param world  The world where the chunk resides.
     * @param chunk  The chunk being generated.
     * @param rng    The random number generator used for terrain features.
     * @param x      The x-coordinate within the chunk.
     * @param y      The y-coordinate within the chunk.
     * @param z      The z-coordinate within the chunk.
     * @param height The height at which the layer is being generated.
     * @return True if the handling is successful, false otherwise.
     */
    @ApiStatus.OverrideOnly
    public abstract boolean handle(World world, BlockSetter chunk, RNG rng, int x, int y, int z, int height);

    public void create(ServerWorld world) {

    }

    @Override
    public void close() {

    }
}
