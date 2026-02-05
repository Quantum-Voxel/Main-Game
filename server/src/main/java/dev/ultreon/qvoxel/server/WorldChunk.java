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

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.block.Blocks;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.block.actor.BlockActor;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.fluid.FluidState;
import dev.ultreon.qvoxel.fluid.Fluids;
import dev.ultreon.qvoxel.network.packets.s2c.S2CChunkDataPacket;
import dev.ultreon.qvoxel.registry.RegistryKey;
import dev.ultreon.qvoxel.registry.RegistryKeys;
import dev.ultreon.qvoxel.util.*;
import dev.ultreon.qvoxel.world.Chunk;
import dev.ultreon.qvoxel.world.HeightmapType;
import dev.ultreon.qvoxel.world.gen.GenerationBarrier;
import dev.ultreon.qvoxel.world.World;
import dev.ultreon.qvoxel.world.gen.biome.Biome;
import dev.ultreon.ubo.types.ListType;
import dev.ultreon.ubo.types.MapType;
import dev.ultreon.ubo.types.StringType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorldChunk extends ServerChunk {
    private long unloadTicks = 100L;

    public WorldChunk(ServerWorld world, ChunkVec vec, @NotNull Region region) {
        super(world, vec, region);
    }

    public WorldChunk(ServerWorld world,
                      ChunkVec vec,
                      PaletteStorage<BlockState> blockStorage,
                      PaletteStorage<FluidState> fluidStorage,
                      Map<BlockVec, BlockActor> blockActors, PaletteStorage<RegistryKey<Biome>> biomeStorage,
                      @NotNull Region region) {
        super(world, vec, blockStorage, fluidStorage, blockActors, biomeStorage, region);
    }

    @Override
    public void set(BlockVec pos, BlockState state) {
        super.set(pos, state);
        modified = true;
    }

    @Override
    public void set(int x, int y, int z, BlockState state) {
        super.set(x, y, z, state);
        modified = true;
    }

    public static WorldChunk load(ServerWorld world, @Nullable MapType sector, int x, int y, int z, @NotNull Region region) {
        if (sector == null) {
            throw new IllegalArgumentException("Sector cannot be null");
        }
        PaletteStorage<BlockState> blockStorage = new PaletteStorage<>(World.CHUNK_VOLUME, Blocks.AIR.getDefaultState());
        blockStorage.load(sector.getMap("Blocks"), MapType.class, BlockState::load);
        PaletteStorage<FluidState> fluidStorage = new PaletteStorage<>(World.CHUNK_VOLUME, Fluids.EMPTY.getState(0));
        fluidStorage.load(sector.getMap("Fluids", new MapType()), MapType.class, FluidState::load);
        PaletteStorage<RegistryKey<Biome>> biomeStorage = new PaletteStorage<>(World.CHUNK_SURFACE, RegistryKey.of(RegistryKeys.BIOME, CommonConstants.id("plains")));
        biomeStorage.load(sector.getMap("Biomes"), StringType.class, id -> RegistryKey.of(RegistryKeys.BIOME, Identifier.tryParse(id.getValue())));
        Map<BlockVec, BlockActor> actors = new ConcurrentHashMap<>();
        ListType<MapType> blockActorsData = sector.getList("BlockActors");
        for (MapType blockActorData : blockActorsData) {
            try {
                BlockActor actor = BlockActor.loadFully(blockActorData, world);
                if (actor != null && actors.putIfAbsent(actor.getPos(), actor) != null) {
                    CommonConstants.LOGGER.error("Multiple block actors loaded on same block: {}", actor.getPos());
                }
            } catch (Exception e)  {
                CommonConstants.LOGGER.error("Failed to load block actor!", e);
            }
        }
        return new WorldChunk(world, new ChunkVec(x, y, z), blockStorage, fluidStorage, actors, biomeStorage, region);
    }

    public MapType save() {
        CommonConstants.LOGGER.debug("Saving chunk {} {} {}", vec.x, vec.y, vec.z);

        MapType sector = new MapType();
        MapType blocks = new MapType();
        MapType biomes = new MapType();
        ListType<MapType> blockActorsData = new ListType<>();
        blockStorage.save(blocks, MapType.class, BlockState::save);
        biomeStorage.save(biomes, StringType.class, biome -> new StringType(biome.id().toString()));

        for (BlockActor actor : blockActors.values()) {
            blockActorsData.add(actor.save(new MapType()));
        }

        sector.put("Blocks", blocks);
        sector.put("Fluids", fluidStorage.save(new MapType(), MapType.class, FluidState::save));
        sector.put("Biomes", biomes);
        sector.put("BlockActors", blockActorsData);
        sector.putBoolean("generated", true);
        sector.putBoolean("carved", true);
        sector.putString("currentBarrier", GenerationBarrier.ALL.name());
        sector.putBoolean("modified", modified);

        return sector;
    }

    @Override
    protected void retrieveNeighbors() {
        for (Direction direction : Direction.values()) {
            Chunk chunk = getWorld().getChunkOrNull(tmpCV.set(vec).add(direction.getNormal(tmp3F)));

            neighbors[direction.ordinal()] = chunk;
            if (chunk != null) {
                chunk.neighbors[direction.opposite().ordinal()] = this;
                if (chunk instanceof WorldChunk worldChunk) {
                    worldChunk.sendChunkData();
                }
            }
        }
    }

    @Override
    public ServerWorld getWorld() {
        return super.getWorld();
    }

    @Override
    public int getHeight(int x, int z, HeightmapType type) {
        return getWorld().getHeight(x + blockStart.x, z + blockStart.z, type);
    }

    @Override
    public void sendChunk(ServerPlayerEntity serverPlayer) {
        super.sendChunk(serverPlayer);
        serverPlayer.connection.send(new S2CChunkDataPacket(this));
    }

    public void close() {

    }

    @Override
    public void tick() {
        super.tick();

        if (unloadTicks-- == 0) {
            unloadTicks = 0L;
            CommonConstants.LOGGER.debug("Unloading chunk {} {} {}", vec.x, vec.y, vec.z);
            getWorld().unloadChunk(vec);
        }
    }

    public void consumeTicket(ChunkLoadTicket ticket) {
        if (ticket.isIndefinite()) {
            unloadTicks = -1L;
        }
        unloadTicks = Math.max(unloadTicks, ticket.getTimeout());
    }

    public void sendChunkData(ServerPlayerEntity serverPlayer) {
        for (Chunk neighbor : neighbors) if (neighbor == null) return;
        serverPlayer.connection.send(new S2CChunkDataPacket(this));
        serverPlayer.loadedChunks.add(vec);
    }

    public PaletteStorage<BlockState> getStorage() {
        return blockStorage;
    }

    public PaletteStorage<RegistryKey<Biome>> getBiomeStorage() {
        return biomeStorage;
    }

    public int[] getSurfaceHeights() {
        return getWorld().getHeightmap(vec, HeightmapType.WORLD_SURFACE).getMap();
    }

    public int[] getLightHeights() {
        return getWorld().getHeightmap(vec, HeightmapType.LIGHT_BLOCKING).getMap();
    }

    public void sendChunkData() {
        for (ServerPlayerEntity player : getWorld().getPlayers()) {
            sendChunkData(player);
        }
    }

    public Map<BlockVec, Identifier> getBlockActors() {
        Map<BlockVec, Identifier> result = new HashMap<>();
        for (Map.Entry<BlockVec, BlockActor> entry : blockActors.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getFactory().getId());
        }
        return result;
    }
}
