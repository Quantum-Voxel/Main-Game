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
import dev.ultreon.qvoxel.world.gen.chunk.ChunkGenerator;

public class BuilderFork implements Fork {
    private final BuilderChunk chunk;
    private final int x;
    private final int y;
    private final int z;
    private final ChunkGenerator generator;

    public BuilderFork(BuilderChunk chunk, int x, int y, int z, ChunkGenerator generator) {
        this.chunk = chunk;
        this.x = x;
        this.y = y;
        this.z = z;
        this.generator = generator;
    }

    @Override
    public boolean set(int x, int y, int z, BlockState block) {
        // Apply immediately using local coordinates. BuilderChunk will route or record
        // cross-chunk changes if x/y/z are outside this chunk's bounds.
        chunk.set(x - chunk.blockStart.x, y - chunk.blockStart.y, z - chunk.blockStart.z, block);
        return true;
    }

    @Override
    public Chunk getChunk() {
        return chunk;
    }

    @Override
    public boolean isAir(int x, int y, int z) {
        return generator.getCarver().isAir(x - chunk.blockStart.x, y - chunk.blockStart.y, z - chunk.blockStart.z);
    }

    @Override
    public BlockState get(int x, int y, int z) {
        return chunk.get(x - chunk.blockStart.x, y - chunk.blockStart.y, z - chunk.blockStart.z);
    }
}
