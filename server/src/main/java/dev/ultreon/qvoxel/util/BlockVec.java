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
import dev.ultreon.ubo.types.MapType;
import org.joml.*;

import java.lang.Math;

public class BlockVec extends Vector3i {
    private static final int R = 37;
    private static final int M = 433494437;
    private static final long RL = 37L;
    private static final long ML = 99194853094755497L;

    public BlockVec(int x, int y, int z) {
        super(x, y, z);
    }

    public BlockVec() {
        this(0, 0, 0);
    }

    public BlockVec(BlockVec pos) {
        this(pos.x, pos.y, pos.z);
    }

    public BlockVec(int[] pos) {
        this(pos[0], pos[1], pos[2]);
    }

    public BlockVec(Vector3dc min) {
        this((int) Math.ceil(min.x()), (int) Math.ceil(min.y()), (int) Math.ceil(min.z()));
    }

    public BlockVec(Vector3fc min) {
        this((int) Math.ceil(min.x()), (int) Math.ceil(min.y()), (int) Math.ceil(min.z()));
    }

    public BlockVec(Vector3ic min) {
        this(min.x(), min.y(), min.z());
    }

    public BlockVec(double x, double y, double z) {
        this((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }


    public static int localize(int coord) {
        int local = coord % World.CHUNK_SIZE;
        if (coord < 0 && local != 0) local += World.CHUNK_SIZE;
        return local;
    }

    public static int chunkOf(int coord) {
        return Math.floorDiv(coord, World.CHUNK_SIZE);
    }

    public static BlockVec of(Vector3d position) {
        return new BlockVec((int) Math.floor(position.x()), (int) Math.floor(position.y()), (int) Math.floor(position.z()));
    }

    public static BlockVec load(MapType data) {
        return new BlockVec(data.getInt("x"), data.getInt("y"), data.getInt("z"));
    }

    public int[] getInt() {
        return new int[]{x, y, z};
    }

    @Override
    public String toString() {
        return x + ", " + y + ", " + z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        var that = (BlockVec) obj;
        return x == that.x && y == that.y && z == that.z;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }

    public long toLong() {
        int x = this.x & 0x3FFFFFFF;
        int y = this.y & 0x3FFFFFFF;
        int z = this.z & 0x3FFFFFFF;
        return (long) x << 40 | (long) y << 20 | z;
    }

    public static BlockVec fromLong(long pos) {
        int x = (int) (pos >> 40);
        int y = (int) (pos >> 20) & 0x3FFFFFFF;
        int z = (int) pos & 0x3FFFFFFF;
        return new BlockVec(x, y, z);
    }

    public BlockVec add(BlockVec other, BlockVec result) {
        result.x = x + other.x;
        result.y = y + other.y;
        result.z = z + other.z;
        return result;
    }

    public BlockVec sub(BlockVec other, BlockVec result) {
        result.x = x - other.x;
        result.y = y - other.y;
        result.z = z - other.z;
        return result;
    }

    public BlockVec copy() {
        return new BlockVec(this);
    }

    public BlockVec with(int x, int y, int z) {
        return new BlockVec(x, y, z);
    }

    public BlockVec withX(int x) {
        return new BlockVec(x, y, z);
    }

    public BlockVec withY(int y) {
        return new BlockVec(x, y, z);
    }

    public BlockVec withZ(int z) {
        return new BlockVec(x, y, z);
    }

    public BlockVec set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public BlockVec chunkLocal() {
        return new BlockVec(BlockVec.localize(x), BlockVec.localize(y), BlockVec.localize(z));
    }

    public BlockVec relative(Direction dir) {
        return new BlockVec(x + dir.getNormalX(), y + dir.getNormalY(), z + dir.getNormalZ());
    }

    public ChunkVec chunk() {
        return new ChunkVec(BlockVec.chunkOf(x), BlockVec.chunkOf(y), BlockVec.chunkOf(z));
    }

    public BlockVec offset(Direction dir) {
        return new BlockVec(x + dir.getNormalX(), y + dir.getNormalY(), z + dir.getNormalZ());
    }
    
    public static long hash64(int x, int y, int z) {
        return ((x * RL + y) % ML * RL + z) % ML;
    }

    public static int hash32(int x, int y, int z) {
        return ((x * R + y) % M * R + z) % M;
    }

    public MapType save() {
        MapType data = new MapType();
        data.putInt("x", x);
        data.putInt("y", y);
        data.putInt("z", z);
        return data;
    }
}
