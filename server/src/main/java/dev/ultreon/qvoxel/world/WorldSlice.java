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
import org.joml.Vector3i;

public class WorldSlice implements BlockSetter {
    private final Chunk chunk;

    public WorldSlice(Chunk chunk) {
        this.chunk = chunk;
        chunk.getWorld();
    }

    @Override
    public boolean set(int x, int y, int z, BlockState block) {
        Vector3i offset = chunk.blockStart;
        if (offset.x <= x && offset.y <= y && offset.z <= z && offset.x + World.CHUNK_SIZE > x && offset.y + World.CHUNK_SIZE > y && offset.z + World.CHUNK_SIZE > z) {
            chunk.set(x - offset.x, y - offset.y, z - offset.z, block);
            return true;
        }
        return false;
    }
}
