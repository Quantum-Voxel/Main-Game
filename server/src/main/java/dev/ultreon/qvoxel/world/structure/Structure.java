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

package dev.ultreon.qvoxel.world.structure;

import dev.ultreon.qvoxel.world.Chunk;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.world.BlockSetter;
import dev.ultreon.qvoxel.world.WorldSlice;
import org.joml.Vector3i;

public abstract class Structure {
    protected Structure() {
    }

    public abstract void place(BlockSetter setter, int x, int y, int z);

    public abstract BlockVec getSize();

    public abstract BlockVec getCenter();

    public final void placeSlice(Chunk recordingChunk, int worldX, int worldY, int worldZ) {
        WorldSlice worldSlice = new WorldSlice(recordingChunk);
        place(worldSlice, worldX, worldY, worldZ);
    }

    public boolean contains(int x, int y, int z) {
        BlockVec size = getSize();
        Vector3i start = getCenter().copy().sub(size.copy().div(2));

        return x >= start.x && x < start.x + size.x && y >= start.y && y < start.y + size.y && z >= start.z && z < start.z + size.z;
    }
}
