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

import dev.ultreon.qvoxel.block.Blocks;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.fluid.FluidState;
import dev.ultreon.qvoxel.registry.RegistryKey;
import dev.ultreon.qvoxel.server.*;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.util.ChunkVec;
import dev.ultreon.qvoxel.util.Direction;
import dev.ultreon.qvoxel.util.PaletteStorage;
import dev.ultreon.qvoxel.world.gen.GenerationBarrier;
import dev.ultreon.qvoxel.world.gen.biome.Biome;
import dev.ultreon.qvoxel.world.gen.biome.BiomeGenerator;
import dev.ultreon.ubo.types.IntArrayType;
import dev.ultreon.ubo.types.ListType;
import dev.ultreon.ubo.types.MapType;
import dev.ultreon.ubo.types.StringType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * The BuilderChunk class is an extension of the Chunk class,
 * specifically designed to handle chunk operations on a dedicated builder thread.
 * It includes functionality for handling biome data and block state manipulation.
 */
public final class BuilderChunk extends ServerChunk {
    private @Nullable Thread thread;
    private final @NotNull PaletteStorage<@NotNull BiomeGenerator> biomeData;
    public boolean carved;
    public GenerationBarrier currentBarrier = GenerationBarrier.NONE;
    private @Nullable List<Vector3i> biomeCenters;
    private final @NotNull Region region;
    private final QuantumServer server;
    private final AtomicBoolean generating = new AtomicBoolean(false);

    public BuilderChunk(@NotNull ServerWorld world, ChunkVec pos, @NotNull Region region) {
        super(world, pos, region);
        this.region = region;
        biomeData = new PaletteStorage<>(World.CHUNK_SURFACE, world.getServer().getBiomes().plains.create(getWorld()));
        server = world.getServer();
    }

    public void set(BlockVec pos, BlockState block) {
        setSafe(pos.x, pos.y, pos.z, block);
    }

    private void setSafe(int x, int y, int z, BlockState block) {
        synchronized (biomeStorage) {
            if (x < 0 || x >= World.CHUNK_SIZE || y < 0 || y >= World.CHUNK_SIZE || z < 0 || z >= World.CHUNK_SIZE) {
                int wx = vec.x * World.CHUNK_SIZE + x;
                int wy = vec.y * World.CHUNK_SIZE + y;
                int wz = vec.z * World.CHUNK_SIZE + z;

                Chunk chunkOrNull = getWorld().getChunkOrNull(BlockVec.chunkOf(wx), BlockVec.chunkOf(wy), BlockVec.chunkOf(wz));
                if (chunkOrNull != null) {
                    chunkOrNull.set(BlockVec.localize(wx), BlockVec.localize(wy), BlockVec.localize(wz), block);
                } else {
                    // Neighbor chunk not loaded yet: record the change to be applied when that chunk is generated.
                    getWorld().recordChange(new RecordedChange(wx, wy, wz, block));
                }
                return;
            }

            blockStorage.set(getIndex(x, y, z), block);
        }
    }

    @Override
    protected void retrieveNeighbors() {
        for (Direction direction : Direction.values()) {
            Chunk chunk = getWorld().getChunkOrNull(tmpCV.set(vec).add(direction.getNormal(tmp3F)));

            neighbors[direction.ordinal()] = chunk;
            if (chunk != null)
                chunk.neighbors[direction.opposite().ordinal()] = this;
        }
    }

    @Override
    public void close() {
        // Nothing to do here.
    }

    @Override
    public MapType save() {
        MapType save = new MapType();
        save.put("BlockData", blockStorage.save(new MapType(), MapType.class, BlockState::save));
        save.put("FluidData", fluidStorage.save(new MapType(), MapType.class, FluidState::save));
        save.put("BiomeData", biomeData.save(new MapType(), StringType.class, gen -> new StringType(gen.getBiome().save(server).toString())));
        save.putBoolean("generated", false);
        save.putBoolean("carved", carved);
        if (currentBarrier != null) {
            save.putString("currentBarrier", currentBarrier.name());
        }
        if (biomeCenters != null) {
            ListType<IntArrayType> centers = new ListType<>();
            for (Vector3i center : biomeCenters) {
                centers.add(new IntArrayType(new int[]{center.x, center.y, center.z}));
            }
            save.put("biomeCenters", centers);
        }
        return save;
    }

    public static BuilderChunk load(ServerWorld world, ChunkVec pos, MapType save, @NotNull Region region) {
        BuilderChunk chunk = new BuilderChunk(world, pos, region);
        chunk.blockStorage.load(save.getMap("BlockData"), MapType.class, BlockState::load);
        chunk.fluidStorage.load(save.getMap("FluidData"), MapType.class, FluidState::load);
        chunk.biomeData.load(save.getMap("BiomeData"), StringType.class, str -> world.getServer().getBiomes().load(world, new StringType(str.toString())));
        chunk.carved = save.getBoolean("carved");
        if (save.contains("currentBarrier")) {
            chunk.currentBarrier = GenerationBarrier.valueOf(save.getString("currentBarrier"));
        }

        if (save.contains("biomeCenters")) {
            ListType<IntArrayType> centers = save.getList("biomeCenters");
            chunk.biomeCenters = new java.util.ArrayList<>();
            for (IntArrayType center : centers) {
                chunk.biomeCenters.add(new Vector3i(center.get(0), center.get(1), center.get(2)));
            }
        }
        chunk.retrieveNeighbors();
        return chunk;
    }

    @Override
    public BlockState get(int x, int y, int z) {
        return getSafe(x, y, z);
    }

    @Override
    public BlockState get(BlockVec pos) {
        return getSafe(pos.x, pos.y, pos.z);
    }

    public BlockState getSafe(int x, int y, int z) {
        if (x < 0 || x >= World.CHUNK_SIZE || y < 0 || y >= World.CHUNK_SIZE || z < 0 || z >= World.CHUNK_SIZE) {
            int wx = vec.x * World.CHUNK_SIZE + x;
            int wy = vec.y * World.CHUNK_SIZE + y;
            int wz = vec.z * World.CHUNK_SIZE + z;

            Chunk chunkOrNull = getWorld().getChunkOrNull(BlockVec.chunkOf(wx), BlockVec.chunkOf(wy), BlockVec.chunkOf(wz));
            if (chunkOrNull != null) {
                return chunkOrNull.get(BlockVec.localize(wx), BlockVec.localize(wy), BlockVec.localize(wz));
            } else {
                return Blocks.VOID_BARRIER.getDefaultState();
            }
        }

        synchronized (this) {
            return blockStorage.get(getIndex(x, y, z));
        }
    }

    @Override
    public void set(int x, int y, int z, BlockState block) {
        setSafe(x, y, z, block);
    }

    public boolean isOnInvalidThread() {
        if (thread == null) return true;
        return thread.threadId() != Thread.currentThread().threadId();
    }

    @SuppressWarnings("unchecked")
    public WorldChunk build() {
        PaletteStorage<RegistryKey<Biome>> biomeStorage = biomeData.map(getWorld().getServer().getBiomes().getDefaultKey(), RegistryKey[]::new, gen -> gen.getBiomeKey(getWorld().getServer()));
        return new WorldChunk(getWorld(), vec, blockStorage, fluidStorage, new ConcurrentHashMap<>(), biomeStorage, region);
    }

    public void drainRecordedChanges() {
        // Apply any previously recorded cross-chunk changes targeting this chunk before building.
        var pending = getWorld().drainRecordedChangesForChunk(vec);
        if (!pending.isEmpty()) {
            for (var change : pending) {
                int lx = BlockVec.localize(change.x());
                int ly = BlockVec.localize(change.y());
                int lz = BlockVec.localize(change.z());
                setSafe(lx, ly, lz, change.block());
            }
        }
    }

    public void setBiomeGenerator(int x, int z, BiomeGenerator generator) {
        int index = getIndex(x, z);
        biomeData.set(index, generator);
    }

    public BiomeGenerator getBiomeGenerator(int x, int z) {
        int index = getIndex(x, z);
        return biomeData.get(index);
    }

    public void setBiomeCenters(@Nullable List<Vector3i> biomeCenters) {
        this.biomeCenters = biomeCenters;
    }

    public @Nullable List<Vector3i> getBiomeCenters() {
        return biomeCenters;
    }

    @Override
    public int getHeight(int x, int z, HeightmapType type) {
        return getWorld().getHeight(blockStart.x + x, blockStart.z + z, type);
    }

    @Override
    public int getLowestBlockInChunk(int x, int z) {
        if (x < 0 || x >= World.CHUNK_SIZE || z < 0 || z >= World.CHUNK_SIZE)
            throw new IndexOutOfBoundsException("Block coordinates out of bounds: " + x + ", " + z);

        for (int y = World.CHUNK_SIZE - 1; y >= 0; y--) {
            BlockState blockState = blockStorage.get(getIndex(x, y, z));
            if (!blockState.isAir())
                return y + 1;
        }
        return -1;
    }

    public BuilderFork createFork(int x, int y, int z) {
        return new BuilderFork(this, x, y, z, getWorld().getGenerator());
    }

    public <T> T process(Supplier<T> supplier) {
        if (thread != null) throw new IllegalStateException("Already processing on a thread!");
        thread = Thread.currentThread();
        generating.set(true);
        T t = supplier.get();
        generating.set(false);
        thread = null;
        return t;
    }
}
