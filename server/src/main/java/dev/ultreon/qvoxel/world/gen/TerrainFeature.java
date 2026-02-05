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

package dev.ultreon.qvoxel.world.gen;

import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.server.ServerWorld;
import dev.ultreon.qvoxel.world.Fork;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * The WorldGenFeature abstract class defines the structure for world generation features.
 * Classes that extend WorldGenFeature must implement the handle method to specify
 * how to generate features in a world.
 */
public abstract class TerrainFeature implements AutoCloseable {
    private long seed;

    /**
     * Determines whether the feature should be placed at the specified coordinates.
     *
     * @param x      the x-coordinate in the world
     * @param y      the y-coordinate in the world
     * @param z      the z-coordinate in the world
     * @param origin the original block state at the specified coordinates
     * @param seed
     * @param world
     * @return true if the feature should be placed, false otherwise
     */
    public boolean shouldPlace(int x, int y, int z, BlockState origin, long seed, ServerWorld world) {
        return true;
    }

    /**
     * Handles the generation of a world feature at a specific location within a chunk.
     *
     * @param setter the world in which the feature is being generated
     * @param seed
     * @param x      the x-coordinate of the location within the chunk
     * @param y      the y-coordinate of the location within the chunk
     * @param z      the z-coordinate of the location within the chunk
     * @return true if the feature was successfully generated, false otherwise
     */
    @ApiStatus.OverrideOnly
    public abstract boolean handle(@NotNull Fork setter, long seed, int x, int y, int z);

    /**
     * Create the world generator feature in the given world.
     * <p>NOTE: Always override {@link #close()} to avoid memory leaks.</p>
     *
     * @param world the world to create the feature in.
     */
    @ApiStatus.OverrideOnly
    public void create(ServerWorld world, long seed) {
        this.seed = seed;
    }

    /**
     * Dispose the feature when the world is being unloaded.
     */
    @Override
    @ApiStatus.OverrideOnly
    public void close() {

    }

    public long seed() {
        return seed;
    }
}
