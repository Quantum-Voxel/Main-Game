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

package dev.ultreon.qvoxel.util;

import dev.ultreon.qvoxel.world.World;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

public class ChunkVec extends Vector3i implements Comparable<ChunkVec> {
    public ChunkVec(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public ChunkVec() {
        this(0, 0, 0);
    }

    public static Long toLong(int x, int y, int z) {
        long result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }

    public static int regionOf(int coord) {
        return Math.floorDiv(coord, World.REGION_SIZE);
    }

    public static int localize(int coord) {
        int local = coord % World.REGION_SIZE;
        if (coord < 0 && local != 0) local += World.REGION_SIZE;
        return local;
    }

    public long toLong() {
        return toLong(x, y, z);
    }

    @Override
    public String toString() {
        return x + ", " + y + ", " + z;
    }

    public ChunkVec add(Vector3fc vec) {
        x += (int) vec.x();
        y += (int) vec.y();
        z += (int) vec.z();
        return this;
    }

    public ChunkVec add(Vector3ic vec) {
        x += vec.x();
        y += vec.y();
        z += vec.z();
        return this;
    }

    public ChunkVec sub(Vector3fc vec) {
        x -= (int) vec.x();
        y -= (int) vec.y();
        z -= (int) vec.z();
        return this;
    }

    public ChunkVec sub(Vector3ic vec) {
        x -= vec.x();
        y -= vec.y();
        z -= vec.z();
        return this;
    }

    public ChunkVec copy() {
        return new ChunkVec(x, y, z);
    }

    public BlockVec blockInWorldSpace(int x, int y, int z) {
        return new BlockVec(this.x * World.CHUNK_SIZE + x, this.y * World.CHUNK_SIZE + y, this.z * World.CHUNK_SIZE + z);
    }

    public BlockVec blockInWorldSpace(BlockVec pos) {
        return new BlockVec(this.x * World.CHUNK_SIZE + pos.x, this.y * World.CHUNK_SIZE + pos.y, this.z * World.CHUNK_SIZE + pos.z);
    }

    public ChunkVec set(ChunkVec vec) {
        x = vec.x;
        y = vec.y;
        z = vec.z;
        return this;
    }

    @Override
    public int compareTo(@NotNull ChunkVec o) {
        if (x == o.x) {
            if (y == o.y) {
                return z - o.z;
            }
            return y - o.y;
        }
        return x - o.x;
    }
}
