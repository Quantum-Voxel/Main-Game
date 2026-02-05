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

public interface BlockSetter {
    /**
     * Sets a block at the specified coordinates with the given block state.
     *
     * @param x     the x-coordinate of the block to set
     * @param y     the y-coordinate of the block to set
     * @param z     the z-coordinate of the block to set
     * @param block the BlockState to set at the specified coordinates
     * @return true if the block was successfully set, false otherwise
     */
    boolean set(int x, int y, int z, BlockState block);

    /**
     * Sets a block at the specified relativePos with the given block state.
     *
     * @param pos   the relativePos of the block to set
     * @param block the BlockState to set at the specified relativePos
     * @return true if the block was successfully set, false otherwise
     */
    default boolean set(BlockVec pos, BlockState block) {
        return set(pos.x, pos.y, pos.z, block);
    }
}
