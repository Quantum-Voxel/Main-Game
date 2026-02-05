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

package dev.ultreon.qvoxel.world.gen.carver;

import dev.ultreon.qvoxel.block.Blocks;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.world.BuilderChunk;
import dev.ultreon.qvoxel.world.Heightmap;
import dev.ultreon.qvoxel.world.HeightmapType;
import dev.ultreon.qvoxel.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * FlatWorldCarver is an implementation of the Carver interface designed to carve a flat terrain in a chunk.
 */
public class FlatWorldCarver implements Carver {
    @Override
    public double carve(@NotNull BuilderChunk chunk, int x, int z) {
        Heightmap worldSurface = chunk.getWorld().heightMapAt(x, z, HeightmapType.WORLD_SURFACE);
        Heightmap motionBlocking = chunk.getWorld().heightMapAt(x, z, HeightmapType.MOTION_BLOCKING);
        Heightmap lightBlocking = chunk.getWorld().heightMapAt(x, z, HeightmapType.LIGHT_BLOCKING);

        for (int y = chunk.blockStart.y; y < World.CHUNK_SIZE; y++) {
            BlockVec vec = new BlockVec(x, y, z).chunkLocal();

            if (y < 0) chunk.set(vec.x, vec.y, vec.z, Blocks.STONE.getDefaultState());
            else if (y < 3) chunk.set(vec.x, vec.y, vec.z, Blocks.DIRT.getDefaultState());
            else if (y == 3) chunk.set(vec.x, vec.y, vec.z, Blocks.GRASS_BLOCK.getDefaultState());
        }

        BlockVec vec = new BlockVec(x, 3, z).chunkLocal();
        worldSurface.set(vec.x, vec.z, (short) 3);
        motionBlocking.set(vec.x, vec.z, (short) 3);
        lightBlocking.set(vec.x, vec.z, (short) 3);

        return 3;
    }

    @Override
    public double evaluateNoise(double x, double z) {
        return 3;
    }

    @Override
    public boolean isAir(int x, int y, int z) {
        return y <= 3;
    }
}
