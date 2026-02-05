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

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.util.ChunkVec;
import dev.ultreon.qvoxel.world.Chunk;
import dev.ultreon.qvoxel.world.World;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.io.IOException;

public class Region {
    private final QuantumServer server;
    private RegionColumn regionColumn;
    private final ChunkManager chunkManager;
    private final int x;
    private final int y;
    private final int z;
    private final @Nullable ChunkColumn[] columns = new ChunkColumn[World.REGION_SIZE * World.REGION_SIZE];
    private final ServerWorld world;
    private final RegionChannel channel;

    public Region(QuantumServer server, RegionColumn regionColumn, ChunkManager chunkManager, int x, int y, int z, ServerWorld world) throws IOException {
        this.server = server;
        this.regionColumn = regionColumn;
        this.chunkManager = chunkManager;
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        channel = new RegionChannel(world.getStorage().regionFile(x, y, z));
    }

    public Region(QuantumServer server, RegionColumn regionColumn, ChunkManager chunkManager, Vector3i vector3i, ServerWorld world) throws IOException {
        this(server, regionColumn, chunkManager, vector3i.x, vector3i.y, vector3i.z, world);
    }

    public void tick() {
        for (ChunkColumn chunk : columns) {
            if (chunk != null) {
                chunk.tick();
            }
        }
    }

    public Chunk getChunkOrNull(int x, int y, int z) {
        int index = getIndex(x, z);
        if (columns[index] == null) {
            return null;
        } else {
            return columns[index].getChunk(y);
        }
    }

    public @Nullable ServerChunk getChunk(int x, int y, int z) {
        int index = getIndex(x, z);
        ChunkColumn column = columns[index];
        if (column == null) {
            column = new ChunkColumn(server, world, this, x, z);
            columns[index] = column;
            try {
                @Nullable MapType sector = channel.loadChunk(x, y, z);
                if (sector == null) {
                    return null;
                }
                CommonConstants.LOGGER.debug("Loading chunk from disk at {}, {}, {}", x, y, z);
                WorldChunk load = WorldChunk.load(world, sector, this.x * World.REGION_SIZE + x, this.y * World.REGION_SIZE + y, this.z * World.REGION_SIZE + z, this);
                column.setChunk(y, load);
                return load;
            } catch (IOException e) {
                CommonConstants.LOGGER.error("Failed to load chunk at {}, {}, {}", x, y, z, e);
            }
            return null;
        }
        ServerChunk chunk = column.getChunk(y);
        if (chunk == null) {
            try {
                @Nullable MapType sector = channel.loadChunk(x, y, z);
                if (sector == null) {
                    return null;
                }
                CommonConstants.LOGGER.debug("Loading chunk from disk at {}, {}, {}", x, y, z);
                WorldChunk load = WorldChunk.load(world, sector, this.x * World.REGION_SIZE + x, this.y * World.REGION_SIZE + y, this.z * World.REGION_SIZE + z, this);
                column.setChunk(y, load);
                return load;
            } catch (IOException e) {
                CommonConstants.LOGGER.error("Failed to load chunk at {}, {}, {}", x, y, z, e);
            }
        }
        return chunk;
    }

    public @Nullable ServerChunk getChunk(ChunkVec pos) {
        return getChunk(pos.x, pos.y, pos.z);
    }

    @Contract("_, _, _, _ -> param4")
    public @NotNull ServerChunk setChunk(int x, int y, int z, ServerChunk chunk) {
        ChunkColumn column = columns[getIndex(x, z)];
        if (column == null) {
            column = new ChunkColumn(server, world, this, x, z);
            columns[getIndex(x, z)] = column;
        }
        ServerChunk old = column.getChunk(y);
        if (old != null) {
            old.close();
        }
        column.setChunk(y, chunk);
        return chunk;
    }

    private int getIndex(int x, int z) {
        int size = World.REGION_SIZE;
        if (x >= 0 && x < size && z >= 0 && z < size) {
            return x + size * z;
        }
        throw new IndexOutOfBoundsException("Chunk coordinates out of bounds: " + x + ", " + z);
    }

    public Vector3i pos() {
        return new Vector3i(x, y, z);
    }

    public void close() {
        for (ChunkColumn chunk : columns) {
            if (chunk != null) {
                chunk.close();
            }
        }
    }

    public void unloadChunk(ChunkVec vec) {
        int cx = ChunkVec.localize(vec.x);
        int cy = ChunkVec.localize(vec.y);
        int cz = ChunkVec.localize(vec.z);
        ChunkColumn column = columns[getIndex(cx, cz)];
        if (column == null) return;
        ServerChunk chunk = column.getChunk(cy);
        if (chunk == null) return;
        chunk.close();
        MapType save = chunk.save();
        columns[getIndex(cx, cz)] = null;
        if (!chunk.modified) return;
        try {
            channel.saveChunk(cx, cy, cz, save);
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to save chunk at {}, {}, {}", cx, cy, cz, e);
        }
    }

    public ChunkManager getChunkManager() {
        return chunkManager;
    }

    public @Nullable ServerChunk getLoadedChunk(int x, int y, int z) {
        ChunkColumn column = columns[getIndex(x, z)];
        if (column == null) return null;
        return column.getChunk(y);

    }

    public void save() {
        try {
            for (ChunkColumn column : columns) {
                if (column == null) continue;
                column.save();
            }
            CommonConstants.LOGGER.debug("Saved region {} with {} columns", pos(), columns.length);
            channel.flush();
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to save region {}", this, e);
        }
    }

    public void saveChunk(ServerChunk serverChunk) {
        if (!serverChunk.modified) {
            return;
        }

        try {
            channel.saveChunk(
                    ChunkVec.localize(serverChunk.vec.x),
                    ChunkVec.localize(serverChunk.vec.y),
                    ChunkVec.localize(serverChunk.vec.z),
                    serverChunk.save()
            );
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to save chunk at {}, {}, {}", serverChunk.vec.x, serverChunk.vec.y, serverChunk.vec.z, e);
        }
    }

    public void removeChunk(ServerChunk serverChunk) {
        ChunkColumn column = columns[getIndex(ChunkVec.localize(serverChunk.vec.x), ChunkVec.localize(serverChunk.vec.z))];
        if (column == null) return;
        column.removeChunk(ChunkVec.localize(serverChunk.vec.y));
    }

    public void removeColumn(int x, int z) {
        columns[getIndex(x, z)] = null;
        for (int i = 0; i < World.REGION_SIZE * World.REGION_SIZE; i++) {
            if (columns[i] != null) return;
        }

        try {
            channel.flush();
            channel.close();
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to close region {}", this, e);
        }

        regionColumn.remove(y);
    }
}
