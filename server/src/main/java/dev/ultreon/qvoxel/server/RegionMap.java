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

package dev.ultreon.qvoxel.server;

import dev.ultreon.qvoxel.util.ChunkVec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector3i;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegionMap {
    private final Map<@NotNull Vector2i, @NotNull RegionColumn> columns = new ConcurrentHashMap<>();
    private final @NotNull QuantumServer server;
    private final @NotNull ChunkManager chunkManager;
    private final @NotNull ServerWorld world;

    public RegionMap(@NotNull QuantumServer server, @NotNull ChunkManager chunkManager, @NotNull ServerWorld world) {
        this.server = server;
        this.chunkManager = chunkManager;
        this.world = world;
    }

    public @NotNull ServerWorld getWorld() {
        return world;
    }

    public @NotNull RegionColumn getRegionColumn(@NotNull Vector2i pos) {
        synchronized (columns) {
            return columns.computeIfAbsent(pos, v2i -> new RegionColumn(server, this, world, chunkManager, v2i.x, v2i.y));
        }
    }

    public void tick() {
        for (RegionColumn column : columns.values()) {
            column.tick();
        }
    }

    public void close() {
        for (RegionColumn column : columns.values()) {
            column.close();
        }
        columns.clear();
    }

    public @NotNull ChunkManager getChunkManager() {
        return chunkManager;
    }

    public @NotNull Region getRegion(int x, int y, int z) {
        return getRegionColumn(x, z).getRegion(y);
    }

    public @NotNull Region getRegion(@NotNull Vector3i vector3i) {
        return getRegionColumn(vector3i.x, vector3i.z).getRegion(vector3i.y);
    }

    @NotNull
    public RegionColumn getRegionColumn(int x, int z) {
        return getRegionColumn(new Vector2i(x, z));
    }

    public @Nullable Region getRegionOrNull(int x, int y, int z) {
        RegionColumn regionColumn = columns.get(new Vector2i(x, z));
        if (regionColumn == null) return null;
        return regionColumn.getRegionOrNull(y);
    }

    public @NotNull Region getRegionAt(@NotNull ChunkVec vec) {
        return getRegion(ChunkVec.regionOf(vec.x), ChunkVec.regionOf(vec.y), ChunkVec.regionOf(vec.z));
    }

    public @NotNull Region getRegionAt(int x, int y, int z) {
        return getRegion(ChunkVec.regionOf(x), ChunkVec.regionOf(y), ChunkVec.regionOf(z));
    }

    public void removeColumn(int x, int z) {
        RegionColumn remove = columns.remove(new Vector2i(x, z));
        remove.save();
    }

    public RegionColumn getRegionColumnOrNull(int x, int z) {
        return columns.get(new Vector2i(x, z));
    }

    public void save() {
        for (RegionColumn column : columns.values()) {
            column.save();
        }
    }
}
