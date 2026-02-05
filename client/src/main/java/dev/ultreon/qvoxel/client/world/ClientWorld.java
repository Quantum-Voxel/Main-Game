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

package dev.ultreon.qvoxel.client.world;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.block.Blocks;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.entity.Entity;
import dev.ultreon.qvoxel.featureflags.FeatureSet;
import dev.ultreon.qvoxel.network.packets.s2c.S2CChunkDataPacket;
import dev.ultreon.qvoxel.registry.RegistryKey;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.util.ChunkVec;
import dev.ultreon.qvoxel.world.Chunk;
import dev.ultreon.qvoxel.world.DimensionInfo;
import dev.ultreon.qvoxel.world.HeightmapType;
import dev.ultreon.qvoxel.world.World;
import dev.ultreon.qvoxel.world.gen.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The ClientWorld class represents a client-side implementation of a game world.
 * It is responsible for managing chunks, handling client-side world properties, and
 * providing interaction methods specific to the client-side context.
 * This class extends the World class and implements the AutoCloseable interface,
 * ensuring proper resource management and cleanup.
 */
public class ClientWorld extends World implements AutoCloseable {
    private final Map<ChunkVec, ClientChunk> chunks = new ConcurrentHashMap<>();
    private final RegistryKey<DimensionInfo> dimension;
    private final ClientPlayerEntity localPlayer;
    private long time = 3000;
    private BlockVec tmpBV = new BlockVec();

    public ClientWorld(RegistryKey<DimensionInfo> dimension, ClientPlayerEntity localPlayer) {
        this.dimension = dimension;
        this.localPlayer = localPlayer;
    }

    @Override
    public BlockState get(int x, int y, int z) {
        ClientChunk clientChunk = chunks.get(new ChunkVec(BlockVec.chunkOf(x), BlockVec.chunkOf(y), BlockVec.chunkOf(z)));
        if (clientChunk == null) return Blocks.VOID_BARRIER.getDefaultState();
        return clientChunk.get(BlockVec.localize(x), BlockVec.localize(y), BlockVec.localize(z));
    }

    public void tick() {
        for (ClientChunk chunk : getAllChunks()) {
            chunk.tick();

            int distance = QuantumClient.get().renderDistance;
            if (chunk.blockCenter.distanceSquared(localPlayer.getBlockVec(tmpBV)) > (long) distance * distance) {
                chunk.close();
                removeChunk(chunk.vec);
                CommonConstants.LOGGER.debug("Removed chunk {} from client world", chunk.vec);
            }
        }
    }

    @Override
    public boolean set(int x, int y, int z, BlockState state, int flags) {
        ChunkVec chunkVec = new ChunkVec(BlockVec.chunkOf(x), BlockVec.chunkOf(y), BlockVec.chunkOf(z));
        if (chunks.containsKey(chunkVec)) {
            ClientChunk clientChunk = chunks.get(chunkVec);
            if (clientChunk.vec.x != BlockVec.chunkOf(x) || clientChunk.vec.y != BlockVec.chunkOf(y) || clientChunk.vec.z != BlockVec.chunkOf(z)) {
                CommonConstants.LOGGER.warn("Chunk relativePos mismatch: {} != {}", clientChunk.vec, new ChunkVec(x, y, z));
                return false;
            }
            clientChunk.set(new BlockVec(BlockVec.localize(x), BlockVec.localize(y), BlockVec.localize(z)), state);
            return true;
        }
        CommonConstants.LOGGER.warn("Attempted to set block in an unloaded space: {} {} {}", x, y, z);
        return false;
    }

    @Override
    public Chunk getChunk(int x, int y, int z) {
        return chunks.get(new ChunkVec(x, y, z));
    }

    @Override
    public Chunk getChunkAt(int x, int y, int z) {
        return chunks.get(new ChunkVec(BlockVec.chunkOf(x), BlockVec.chunkOf(y), BlockVec.chunkOf(z)));
    }

    @Override
    public float getRenderDistance() {
        return QuantumClient.get().renderDistance;
    }

    @Override
    protected @Nullable Integer readHeight(int x, int z, HeightmapType heightmapType) {
        throw new UnsupportedOperationException("ClientWorld#readHeight is not supported!");
    }

    @Override
    public boolean isClientSide() {
        return true;
    }

    public void addChunk(ChunkVec chunkVec, ClientChunk chunk) {
        chunks.put(chunkVec.copy(), chunk);
    }

    public void removeChunk(ChunkVec chunkVec) {
        chunks.remove(chunkVec);
    }

    public Collection<ClientChunk> getAllChunks() {
        return chunks.values();
    }

    public ClientPlayerEntity getLocalPlayer() {
        return localPlayer;
    }

    public RegistryKey<DimensionInfo> getDimension() {
        return dimension;
    }

    public void close() {
        for (ClientChunk chunk : getAllChunks()) {
            chunk.close();
        }
        QuantumClient.get().remove(this);
    }

    public void onChunkData(int x, int y, int z, S2CChunkDataPacket s2CChunkDataPacket) {
        ClientChunk chunk = chunks.get(new ChunkVec(x, y, z));
        if (chunk != null) {
            chunk.onChunkData(s2CChunkDataPacket);
            return;
        }
        chunk = new ClientChunk(this, new ChunkVec(x, y, z));
        chunk.onChunkData(s2CChunkDataPacket);
        chunks.put(new ChunkVec(x, y, z), chunk);
        add("Chunk " + x + " , " + y + " , " + z, chunk);
    }

    public RegistryKey<Biome> getBiome(BlockVec blockVec) {
        Chunk chunk = getChunkAt(blockVec);
        if (chunk == null) return null;
        return chunk.getBiome(BlockVec.localize(blockVec.x), BlockVec.localize(blockVec.z));
    }

    public RegistryKey<Biome> getBiome(int x, int y, int z) {
        Chunk chunk = getChunkAt(x, y, z);
        return chunk.getBiome(BlockVec.localize(x), BlockVec.localize(z));
    }

    public long getTime() {
        return time;
    }

    public int getTimeOfDay() {
        return (int) (time % DAY_TIME);
    }

    public int getTicksFromMidnight() {
        return (getTimeOfDay() + 6000) % DAY_TIME;
    }

    public float getTimeOfDay(float partialTick) {
        return (int) (time % DAY_TIME) + partialTick;
    }

    public float getTicksFromMidnight(float partialTick) {
        return (getTimeOfDay(partialTick) + 6000) % DAY_TIME;
    }

    public float getSkyLight() {
        int ticksFromMidnight = getTicksFromMidnight();
        if (ticksFromMidnight < 5000 || ticksFromMidnight > 19000) {
            return 0f;
        } else if (ticksFromMidnight > 5000 && ticksFromMidnight < 7000) {
            return ticksFromMidnight - 5000 / 2000f;
        } else if (ticksFromMidnight > 7000 && ticksFromMidnight < 17000) {
            return 1f;
        } else if (ticksFromMidnight > 17000 && ticksFromMidnight < 19000) {
            return 1f - (ticksFromMidnight - 17000 / 2000f);
        } else {
            return -1f;
        }
    }

    @Override
    public FeatureSet getFeatures() {
        return QuantumClient.get().getFeatures();
    }

    @Override
    public Iterable<Entity> getEntities() {
        return List.copyOf(QuantumClient.get().players);
    }

    public float getGlobalSunLight(float partialTick) {
        float ticksFromMidnight = getTicksFromMidnight(partialTick);
        if (ticksFromMidnight < 5000 || ticksFromMidnight > 19000) {
            return 0f;
        } else if (ticksFromMidnight > 5000 && ticksFromMidnight < 7000) {
            return (ticksFromMidnight - 5000) / 2000f;
        } else if (ticksFromMidnight > 7000 && ticksFromMidnight < 17000) {
            return 1f;
        } else if (ticksFromMidnight > 17000 && ticksFromMidnight < 19000) {
            return 1f - (ticksFromMidnight - 17000) / 2000f;
        } else {
            return -1f;
        }
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void unloadAllChunks() {
        for (ClientChunk chunk : chunks.values()) {
            chunk.close();
        }
        chunks.clear();
    }
}
