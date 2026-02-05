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

import dev.ultreon.qvoxel.Audience;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.block.Blocks;
import dev.ultreon.qvoxel.entity.Entity;
import dev.ultreon.qvoxel.featureflags.FeatureSet;
import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.handler.ClientPacketHandler;
import dev.ultreon.qvoxel.network.packets.s2c.S2CParticleEventPacket;
import dev.ultreon.qvoxel.particle.BlockParticleData;
import dev.ultreon.qvoxel.particle.ParticleData;
import dev.ultreon.qvoxel.particle.ParticleType;
import dev.ultreon.qvoxel.particle.ParticleTypes;
import dev.ultreon.qvoxel.registry.RegistryKey;
import dev.ultreon.qvoxel.util.BlockFlags;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.util.ChunkVec;
import dev.ultreon.qvoxel.util.Util;
import dev.ultreon.qvoxel.world.*;
import dev.ultreon.qvoxel.world.gen.FeatureData;
import dev.ultreon.qvoxel.world.gen.GenerationBarrier;
import dev.ultreon.qvoxel.world.gen.StructureInstance;
import dev.ultreon.qvoxel.world.gen.biome.Biome;
import dev.ultreon.qvoxel.world.gen.biome.BiomeGenerator;
import dev.ultreon.qvoxel.world.gen.chunk.ChunkGenerator;
import dev.ultreon.qvoxel.world.light.LightingSystem;
import dev.ultreon.ubo.types.MapType;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerWorld extends World implements AutoCloseable, Audience {
    private final long seed;
    private final QuantumServer server;
    private final WorldStorage storage;
    private final ChunkGenerator chunkGenerator;
    private final FeatureData featureData = new FeatureData();
    private final Map<ChunkVec, Collection<StructureInstance>> structuresMap = new HashMap<>();
    private final RegistryKey<DimensionInfo> dimension;
    private final List<@NotNull RecordedChange> recordedChanges = new CopyOnWriteArrayList<>();
    private final List<ServerPlayerEntity> players = new CopyOnWriteArrayList<>();
    final LightingSystem lightingSystem = new LightingSystem(this);
    private final ChunkManager chunkManager;
    private final Map<Biome, BiomeGenerator> biomeGenMap = new HashMap<>();
    private final Int2ObjectMap<Entity> entities = new Int2ObjectArrayMap<>();

    public ServerWorld(QuantumServer server, RegistryKey<DimensionInfo> key, WorldStorage storage, ChunkGenerator generator, long seed, MapType data) {
        super();
        this.server = server;
        this.storage = storage;

        for (Biome biome : server.getRegistries().biomes().values()) {
            biomeGenMap.put(biome, biome.create(this));
        }

        chunkGenerator = generator;
        chunkManager = new ChunkManager(this, generator);
        this.seed = seed;
        if (data != null) {
            loadSingleplayerData(data);
        }
        dimension = key;
    }

    public void loadSingleplayerData(MapType data) {
        // TODO: Load singleplayer data
    }

    @Override
    public BlockState get(int x, int y, int z) {
        Chunk chunk = getChunkOrNull(BlockVec.chunkOf(x), BlockVec.chunkOf(y), BlockVec.chunkOf(z));
        if (chunk == null) {
            return Blocks.VOID_BARRIER.getDefaultState();
        } else {
            return chunk.get(BlockVec.localize(x), BlockVec.localize(y), BlockVec.localize(z));
        }
    }

    @Override
    public boolean set(int x, int y, int z, BlockState state, int flags) {
        if (!server.isServerThread())
            throw new IllegalStateException("Cannot set block state outside of server thread!");

        ServerChunk chunkAt = getChunkAt(x, y, z, GenerationBarrier.TERRAIN);
        BlockState old = get(x, y, z);
        int rgb = old.getLightEmission();
        if (rgb > 0) lightingSystem.removeLightSource(x, y, z);
        int newRgb = state.getLightEmission();
        if (newRgb > 0) lightingSystem.addLightSource(x, y, z, newRgb >> 16 & 0xFF, newRgb >> 8 & 0xFF, newRgb & 0xFF);

        if (chunkAt == null) {
            CommonConstants.LOGGER.warn("Tried to set block at {} {} {}, {} but chunk was null", x, y, z, state);
            return false;
        }
        int lx = BlockVec.localize(x);
        int ly = BlockVec.localize(y);
        int lz = BlockVec.localize(z);
        chunkAt.set(lx, ly, lz, state);
        if (!chunkAt.get(lx, ly, lz).equals(state)) {
            CommonConstants.LOGGER.warn("Block state mismatch at {}, {}, {}: {} != {}", x, y, z, chunkAt.get(lx, ly, lz), state);
        }
        if ((flags & BlockFlags.NOTIFY_CLIENTS) != 0) {
            if (newRgb != rgb) {
                if (chunkAt instanceof WorldChunk worldChunk)
                    worldChunk.sendChunkData();
                for (int xi = x - 1; xi <= x + 1; xi++) {
                    for (int yi = y - 1; yi <= y + 1; yi++) {
                        for (int zi = z - 1; zi <= z + 1; zi++) {
                            if (xi == x && yi == y && zi == z) continue;
                            ServerChunk chunk = (ServerChunk) getChunkOrNull(xi, yi, zi);
                            if (chunk instanceof WorldChunk worldChunk) {
                                worldChunk.sendChunkData();
                            }
                        }
                    }
                }
            } else server.broadcastBlockChange(this, x, y, z, state);
        }
        return true;
    }

    @Override
    public boolean set(BlockVec blockVec, BlockState blockState) {
        return set(blockVec.x, blockVec.y, blockVec.z, blockState, BlockFlags.NONE);
    }

    @Override
    @Deprecated
    public WorldChunk getChunk(int x, int y, int z) {
        return (WorldChunk) chunkManager.getChunk(x, y, z, GenerationBarrier.ALL);
    }

    @Override
    @Deprecated
    public WorldChunk getChunk(ChunkVec vec) {
        return (WorldChunk) chunkManager.getChunk(vec.x, vec.y, vec.z, GenerationBarrier.ALL);
    }

    public @NotNull ServerChunk getChunk(ChunkVec vec, GenerationBarrier barrier) {
        return chunkManager.getChunk(vec.x, vec.y, vec.z, barrier);
    }

    public @NotNull ServerChunk getChunk(int x, int y, int z, GenerationBarrier barrier) {
        return chunkManager.getChunk(x, y, z, barrier);
    }

    @Deprecated
    public @NotNull CompletableFuture<@Nullable WorldChunk> getChunkAsync(int x, int y, int z) {
        return chunkManager.getChunkAsync(x, y, z, GenerationBarrier.ALL).thenApply(chunk -> (WorldChunk) chunk);
    }

    @Deprecated
    public @NotNull CompletableFuture<@Nullable WorldChunk> loadChunkAsync(int x, int y, int z, ChunkLoadTicket ticket) {
        return chunkManager.loadChunkAsync(x, y, z, ticket, GenerationBarrier.ALL).thenApply(chunk -> (WorldChunk) chunk);
    }

    public @NotNull CompletableFuture<@Nullable ServerChunk> getChunkAsync(int x, int y, int z, GenerationBarrier barrier) {
        return chunkManager.getChunkAsync(x, y, z, barrier);
    }

    public @NotNull CompletableFuture<@NotNull ServerChunk> loadChunkAsync(int x, int y, int z, ChunkLoadTicket ticket, GenerationBarrier barrier) {
        return chunkManager.loadChunkAsync(x, y, z, ticket, barrier);
    }

    public Chunk getChunkOrNull(ChunkVec vec) {
        return chunkManager.getChunkOrNull(vec.x, vec.y, vec.z);
    }

    public Chunk getChunkOrNull(int x, int y, int z) {
        return chunkManager.getChunkOrNull(x, y, z);
    }

    @Override
    @Deprecated
    public WorldChunk getChunkAt(int x, int y, int z) {
        return getChunk(BlockVec.chunkOf(x), BlockVec.chunkOf(y), BlockVec.chunkOf(z));
    }

    public ServerChunk getChunkAt(int x, int y, int z, GenerationBarrier barrier) {
        return getChunk(BlockVec.chunkOf(x), BlockVec.chunkOf(y), BlockVec.chunkOf(z), barrier);
    }

    @Override
    public float getRenderDistance() {
        return 128;
    }

    public int getHeight(int x, int z) {
        return getHeight(x, z, HeightmapType.WORLD_SURFACE);
    }

    public int getHeight(int x, int z, HeightmapType heightmapType) {
        return chunkManager.getHeightmap(BlockVec.chunkOf(x), BlockVec.chunkOf(z), heightmapType).get(BlockVec.localize(x), BlockVec.localize(z));
    }

    public @Nullable Integer readHeight(int x, int z, HeightmapType heightmapType) {
        ServerChunk lowestChunkAt = chunkManager.getLowestChunkAt(x, z);
        if (lowestChunkAt == null) return null;
        return lowestChunkAt.getLowestWorldBlockAt(BlockVec.localize(x), BlockVec.localize(z));
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public FeatureSet getFeatures() {
        return server.getFeatures();
    }

    @Override
    public Iterable<Entity> getEntities() {
        return entities.values();
    }

    @Override
    public void destroyBlock(int x, int y, int z) {
        BlockState state = get(x, y, z);

        super.destroyBlock(x, y, z);

        set(x, y, z, Blocks.AIR.getDefaultState(), BlockFlags.NOTIFY_CLIENTS | BlockFlags.NEIGHBOR_UPDATE);
        spawnParticles(ParticleTypes.BLOCK, 32, Util.make(new BlockParticleData(state), data -> {
            data.position.x = x + 0.5F;
            data.position.y = y + 0.5F;
            data.position.z = z + 0.5F;
            data.delta.set(0.4F);
            data.minSize = 0.1f;
            data.maxSize = 0.1f;
            data.minSpeed = 0.01f;
            data.maxSpeed = 0.02f;
        }));
    }

    public long getSeed() {
        return seed;
    }

    public RegionMap getRegionMap() {
        return chunkManager.getRegionMap();
    }

    public QuantumServer getServer() {
        return server;
    }

    @Override
    public void close() {
        chunkManager.close();
    }

    public WorldStorage getStorage() {
        return storage;
    }

    public ChunkGenerator getGenerator() {
        return chunkGenerator;
    }

    public FeatureData getFeatureData() {
        return featureData;
    }

    public Collection<StructureInstance> getStructuresAt(ChunkVec vec) {
        return structuresMap.getOrDefault(vec, List.of());
    }

    public void tick() {
        chunkManager.tick();
    }

    public RegistryKey<DimensionInfo> getDimension() {
        return dimension;
    }

    public void load() {
        chunkGenerator.create(this, seed);

        // TODO: Load world data
    }

    public void unloadChunk(ChunkVec vec) {
        chunkManager.queueUnload(vec);
    }

    public void loadChunk(ChunkVec vec) {
        chunkManager.queueLoad(vec);
    }

    /**
     * Record a world-space block change to be applied when the target chunk is generated.
     */
    public void recordChange(RecordedChange change) {
        synchronized (recordedChanges) {
            recordedChanges.add(change);
        }


    }

    /**
     * Drain and return all recorded changes that target the specified chunk.
     * Matching is based on chunk-of world coordinates of each recorded change.
     */
    public List<RecordedChange> drainRecordedChangesForChunk(ChunkVec target) {
        List<RecordedChange> result = new ArrayList<>();
        synchronized (recordedChanges) {
            recordedChanges.removeIf(rc -> {
                int cx = BlockVec.chunkOf(rc.x());
                int cy = BlockVec.chunkOf(rc.y());
                int cz = BlockVec.chunkOf(rc.z());
                if (cx == target.x && cy == target.y && cz == target.z) {
                    result.add(rc);
                    return true;
                }
                return false;
            });
        }
        return result;
    }

    public void addPlayer(ServerPlayerEntity player) {
        players.add(player);
    }

    public void removePlayer(ServerPlayerEntity player) {
        players.remove(player);
    }

    public <T extends ParticleData> void spawnParticles(ParticleType<T> type, int quantity, T data) {
        sendPacket(new S2CParticleEventPacket<>(type, quantity, data));
    }

    @Override
    public void sendPacket(Packet<? extends ClientPacketHandler> packet) {
        for (ServerPlayerEntity player : players) {
            player.sendPacket(packet);
        }
    }

    public List<ServerPlayerEntity> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public ChunkManager getChunkManager() {
        return chunkManager;
    }

    public @NotNull BiomeGenerator getBiomeGen(Biome biome) {
        return biomeGenMap.computeIfAbsent(biome, b -> b.create(this));
    }

    public Heightmap getHeightmap(ChunkVec vec, HeightmapType heightmapType) {
        return chunkManager.getHeightmap(vec.x, vec.z, heightmapType);
    }

    public Heightmap heightMapAt(int x, int z, HeightmapType heightmapType) {
        return chunkManager.getHeightmap(BlockVec.chunkOf(x), BlockVec.chunkOf(z), heightmapType);
    }

    public LightingSystem getLightingSystem() {
        return lightingSystem;
    }

    public void save() {
        chunkManager.save();
    }
}
