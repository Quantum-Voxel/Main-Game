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
import dev.ultreon.qvoxel.world.World;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class RegionChannel {
    @NotNull
    private final Path path;
    private @Nullable RegionDataChannel channel;
    private final Map<Integer, Integer> chunkLengths = new HashMap<>();

    public RegionChannel(Path path) throws IOException {
        this.path = path;
        if (Files.notExists(path.getParent()))
            Files.createDirectories(path.getParent());
        channel();
    }

    public int getChunkIndex(int cx, int cy, int cz) {
        return (cy * ServerWorld.REGION_SIZE + cz) * ServerWorld.REGION_SIZE + cx;
    }

    public void saveChunk(int cx, int cy, int cz, @NotNull MapType chunk) throws IOException {
        if (cx < 0 || cx >= ServerWorld.REGION_SIZE || cy < 0 || cy >= ServerWorld.REGION_SIZE || cz < 0 || cz >= World.REGION_SIZE)
            throw new IndexOutOfBoundsException("Chunk coordinates out of bounds: " + cx + ", " + cy + ", " + cz);

        RegionDataChannel channel = channel();

        synchronized (this) {
            byte[] raw;
            try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                 DataOutputStream output = new DataOutputStream(out)) {
                chunk.write(output);
                raw = out.toByteArray();
            }

            int chunkId = 32 + getChunkIndex(cx, cy, cz);
            channel.writeChunk(chunkId, raw);
            chunkLengths.put(chunkId, raw.length);
        }
    }

    public @Nullable MapType loadChunk(int cx, int cy, int cz) throws IOException {
        if (cx < 0 || cx >= World.REGION_SIZE || cy < 0 || cy >= World.REGION_SIZE || cz < 0 || cz >= World.REGION_SIZE)
            throw new IndexOutOfBoundsException("Chunk coordinates out of bounds: " + cx + ", " + cy + ", " + cz);

        RegionDataChannel channel = this.channel;
        if (channel == null) return null;

        synchronized (this) {
            int chunkId = 32 + getChunkIndex(cx, cy, cz);
            Integer length = chunkLengths.get(chunkId);
            if (length == null) return null;

            byte[] input = channel.readChunk(chunkId, length);
            if (input == null) return null;

            try (ByteArrayInputStream bais = new ByteArrayInputStream(input);
                 DataInputStream dis = new DataInputStream(bais)) {
                return MapType.read(dis);
            } catch (Exception e) {
                CommonConstants.LOGGER.error("Failed to load chunk {} {} {}", cx, cy, cz, e);
                return null;
            }
        }
    }

    private RegionDataChannel channel() throws IOException {
        if (channel != null) return channel;

        boolean existed = Files.exists(path);

        channel = new RegionDataChannel(path);

        if (existed) {
            channel.readSectorReferenceMap();

            // populate chunkLengths map from sector reference map
            for (Map.Entry<Integer, RegionDataChannel.ChunkEntry> e : channel.chunkSectorMap.entrySet()) {
                int chunkId = e.getKey();
                int size = e.getValue().sectorCount() * RegionDataChannel.SECTOR_SIZE;
                chunkLengths.put(chunkId, size);
            }
        }

        flush();
        return channel;
    }

    public Path getTarget() {
        return path;
    }

    public void close() throws IOException {
        if (channel == null) return;
        flush();
        channel.close();
    }

    public void flush() throws IOException {
        if (channel == null) return;
        synchronized (this) {
            channel.writeSectorReferenceMap();
            CommonConstants.LOGGER.debug("Flushed region channel: {}", channel.getTarget());
        }
    }
}
