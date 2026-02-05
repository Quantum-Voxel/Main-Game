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

package dev.ultreon.qvoxel.world;

import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.util.BlockVec;

/**
 * A Fork is a wrapper around a Chunk. Used for forking around an origin point within a chunk.
 * Used for world generation.
 *
 * @author <a href="https://github.com/Qubilux">Qubix</a>
 */
public interface Fork extends BlockSetter {
    /**
     * @return The chunk that this Fork is wrapping around.
     */
    Chunk getChunk();

    /**
     * Checks if the block at the specified coordinates is air.
     *
     * @param x The x-coordinate of the block to check from the origin point.
     * @param y The y-coordinate of the block to check from the origin point.
     * @param z The z-coordinate of the block to check from the origin point.
     * @return true if the block at the specified coordinates is air, false otherwise.
     */
    boolean isAir(int x, int y, int z);

    /**
     * Gets the block at the specified coordinates.
     *
     * @param x The x-coordinate of the block to get from the origin point.
     * @param y The y-coordinate of the block to get from the origin point.
     * @param z The z-coordinate of the block to get from the origin point.
     * @return The block at the specified coordinates.
     */
    BlockState get(int x, int y, int z);

    /**
     * Gets the block at the specified coordinates.
     *
     * @param x     The x-coordinate of the block to get from the origin point.
     * @param y     The y-coordinate of the block to get from the origin point.
     * @param z     The z-coordinate of the block to get from the origin point.
     * @param block The BlockState to set at the specified coordinates.
     * @return true if the block was successfully set, false otherwise
     */
    @Override
    boolean set(int x, int y, int z, BlockState block);

    /**
     * Sets a block at the specified relativePos with the given block state.
     *
     * @param pos   The relativePos of the block to set from the origin point.
     * @param block The BlockState to set at the specified coordinates.
     * @return true if the block was successfully set, false otherwise
     */
    @Override
    default boolean set(BlockVec pos, BlockState block) {
        return BlockSetter.super.set(pos, block);
    }
}
