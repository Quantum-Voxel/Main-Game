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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class RegionDataChannel {
    static final int SECTOR_SIZE = 4096;
    private static final int SECTOR_MAP_START = 97;
    private static final int SECTOR_MAP_COUNT = 64;
    private static final int HEADER_SECTORS = 1;
    private static final int ENTRY_SIZE = 12; // 4 bytes chunkId + 4 bytes startSector + 4 bytes sectorCount

    private final BitSet usedSectors = new BitSet();
    final Map<Integer, ChunkEntry> chunkSectorMap = new HashMap<>();
    private final RandomAccessFile file;
    private final Path target;

    record ChunkEntry(int startSector, int sectorCount) {

    }

    public RegionDataChannel(Path target) throws IOException {
        this.target = target;
        file = new RandomAccessFile(target.toFile(), "rw");
        usedSectors.set(0, HEADER_SECTORS); // reserve header
    }

    public void writeChunk(int chunkId, byte[] data) throws IOException {
        synchronized (this) {
            int sectorsNeeded = (data.length + SECTOR_SIZE - 1) / SECTOR_SIZE;
            int startSector = findFreeSectors(sectorsNeeded);

            // mark all sectors as used
            usedSectors.set(startSector, startSector + sectorsNeeded);

            file.seek((long) startSector * SECTOR_SIZE);
            file.write(data);

            // pad to sector boundary
            int padding = sectorsNeeded * SECTOR_SIZE - data.length;
            if (padding > 0) file.write(new byte[padding]);

            chunkSectorMap.put(chunkId, new ChunkEntry(startSector, sectorsNeeded));
        }
    }

    public byte[] readChunk(int chunkId, int expectedSize) throws IOException {
        synchronized (this) {
            ChunkEntry entry = chunkSectorMap.get(chunkId);
            if (entry == null) return null;

            file.seek((long) entry.startSector * SECTOR_SIZE);
            byte[] buffer = new byte[entry.sectorCount * SECTOR_SIZE];
            file.readFully(buffer);

            return Arrays.copyOf(buffer, expectedSize);
        }
    }

    public void writeSectorReferenceMap() throws IOException {
        synchronized (this) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(intToBytes(chunkSectorMap.size()));

            for (Map.Entry<Integer, ChunkEntry> e : chunkSectorMap.entrySet()) {
                out.write(intToBytes(e.getKey()));
                out.write(intToBytes(e.getValue().startSector));
                out.write(intToBytes(e.getValue().sectorCount));
            }

            byte[] mapData = out.toByteArray();
            int totalBytes = SECTOR_MAP_COUNT * SECTOR_SIZE;
            if (mapData.length > totalBytes)
                throw new IOException("Chunk map too large! Exceeds reserved size.");

            file.seek((long) SECTOR_MAP_START * SECTOR_SIZE);
            file.write(mapData);

            if (mapData.length < totalBytes) {
                file.write(new byte[totalBytes - mapData.length]);
            }

            usedSectors.set(SECTOR_MAP_START, SECTOR_MAP_START + SECTOR_MAP_COUNT);
        }
    }

    public void readSectorReferenceMap() throws IOException {
        synchronized (this) {
            file.seek((long) SECTOR_MAP_START * SECTOR_SIZE);
            byte[] header = new byte[4];
            file.readFully(header);
            int entryCount = bytesToInt(header, 0);

            byte[] mapData = new byte[entryCount * ENTRY_SIZE + 4];
            file.seek((long) SECTOR_MAP_START * SECTOR_SIZE);
            file.readFully(mapData);

            chunkSectorMap.clear();
            usedSectors.set(SECTOR_MAP_START, SECTOR_MAP_START + SECTOR_MAP_COUNT);

            for (int i = 0; i < entryCount; i++) {
                int base = 4 + i * ENTRY_SIZE;
                int chunkId = bytesToInt(mapData, base);
                int startSector = bytesToInt(mapData, base + 4);
                int sectorCount = bytesToInt(mapData, base + 8);

                chunkSectorMap.put(chunkId, new ChunkEntry(startSector, sectorCount));
                usedSectors.set(startSector, startSector + sectorCount);
            }
        }
    }

    private int findFreeSectors(int count) {
        for (int i = HEADER_SECTORS; i < 0xFFFFF; i++) {
            if (i >= SECTOR_MAP_START && i < SECTOR_MAP_START + SECTOR_MAP_COUNT) continue;

            boolean free = true;
            for (int j = 0; j < count; j++) {
                if (usedSectors.get(i + j)) {
                    free = false;
                    break;
                }
            }

            if (free) return i;
        }
        throw new RuntimeException("No free sectors available for allocation.");
    }

    public ByteBuffer getChunkData(int chunkId) throws IOException {
        ChunkEntry entry = chunkSectorMap.get(chunkId);
        if (entry == null) return null;

        return file.getChannel().map(FileChannel.MapMode.READ_WRITE,
                (long) entry.startSector * SECTOR_SIZE,
                (long) entry.sectorCount * SECTOR_SIZE);
    }

    public Path getTarget() {
        return target;
    }

    public void close() throws IOException {
        file.close();
    }

    private static byte[] intToBytes(int v) {
        return new byte[]{
                (byte) (v >>> 24),
                (byte) (v >>> 16),
                (byte) (v >>> 8),
                (byte) v
        };
    }

    private static int bytesToInt(byte[] d, int i) {
        return (d[i] & 0xFF) << 24
                | (d[i + 1] & 0xFF) << 16
                | (d[i + 2] & 0xFF) << 8
                | d[i + 3] & 0xFF;
    }
}
