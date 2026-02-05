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

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.block.ActorBlock;
import dev.ultreon.qvoxel.block.Block;
import dev.ultreon.qvoxel.block.Blocks;
import dev.ultreon.qvoxel.block.actor.BlockActor;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.fluid.FluidState;
import dev.ultreon.qvoxel.fluid.Fluids;
import dev.ultreon.qvoxel.registry.RegistryKey;
import dev.ultreon.qvoxel.registry.RegistryKeys;
import dev.ultreon.qvoxel.resource.GameObject;
import dev.ultreon.qvoxel.server.ChunkLoadTicket;
import dev.ultreon.qvoxel.server.WorldChunk;
import dev.ultreon.qvoxel.util.*;
import dev.ultreon.qvoxel.world.gen.biome.Biome;
import dev.ultreon.qvoxel.world.light.LightMap;
import org.joml.Vector3f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Chunk extends GameObject {
    protected final PaletteStorage<BlockState> blockStorage;
    protected final PaletteStorage<FluidState> fluidStorage;
    protected final Map<BlockVec, BlockActor> blockActors;
    protected final PaletteStorage<RegistryKey<Biome>> biomeStorage;
    private final World world;
    public final ChunkVec vec;
    public Chunk[] neighbors = new Chunk[6];
    protected final ChunkVec tmpCV = new ChunkVec();
    protected final Vector3f tmp3F = new Vector3f();
    public BlockVec blockStart = new BlockVec();
    public BlockVec blockCenter = new BlockVec();
    public BlockVec blockEnd = new BlockVec();
    protected LightMap lightMap;

    public Chunk(World world, ChunkVec vec) {
        lightMap = new LightMap(world.getFeatures(), World.CHUNK_VOLUME);
        addComponent(lightMap);
        this.world = world;
        this.vec = vec;
        blockStart.set(vec.x * World.CHUNK_SIZE, vec.y * World.CHUNK_SIZE, vec.z * World.CHUNK_SIZE);
        blockCenter.set(blockStart.x + World.CHUNK_SIZE / 2, blockStart.y + World.CHUNK_SIZE / 2, blockStart.z + World.CHUNK_SIZE / 2);
        blockEnd.set(blockStart.x + World.CHUNK_SIZE - 1, blockStart.y + World.CHUNK_SIZE - 1, blockStart.z + World.CHUNK_SIZE - 1);
        blockStorage = new PaletteStorage<>(World.CHUNK_VOLUME, Blocks.AIR.getDefaultState());
        fluidStorage = new PaletteStorage<>(World.CHUNK_VOLUME, Fluids.EMPTY.getState(0));
        biomeStorage = new PaletteStorage<>(World.CHUNK_SURFACE, RegistryKey.of(RegistryKeys.BIOME, CommonConstants.id("plains")));
        blockActors = new ConcurrentHashMap<>();

        add("Block Storage", blockStorage);
        add("Fluid Storage", fluidStorage);
        add("Biome Storage", biomeStorage);

        retrieveNeighbors();
    }

    public Chunk(World world, ChunkVec vec, PaletteStorage<BlockState> blockStorage, PaletteStorage<FluidState> fluidStorage, Map<BlockVec, BlockActor> blockActors, PaletteStorage<RegistryKey<Biome>> biomeStorage) {
        this.world = world;
        this.vec = vec;
        this.blockStorage = blockStorage;
        this.fluidStorage = fluidStorage;
        this.blockActors = blockActors;
        this.biomeStorage = biomeStorage;
        lightMap = new LightMap(world.getFeatures(), World.CHUNK_VOLUME);

        addComponent(lightMap);

        add("Block Storage", blockStorage);
        add("Fluid Storage", fluidStorage);
        add("Biome Storage", biomeStorage);

        retrieveNeighbors();
    }

    public static int getIndex(int x, int y, int z) {
        int size = World.CHUNK_SIZE;
        if (x >= 0 && x < size && y >= 0 && y < size && z >= 0 && z < size) {
            return z * World.CHUNK_SURFACE + y * World.CHUNK_SIZE + x;
        }
        throw new IndexOutOfBoundsException("Block coordinates out of bounds: " + x + ", " + y + ", " + z);
    }

    public BlockState get(int x, int y, int z) {
        return blockStorage.get(getIndex(x, y, z));
    }

    public BlockState get(BlockVec pos) {
        return blockStorage.get(getIndex(pos.x, pos.y, pos.z));
    }

    public void set(int x, int y, int z, BlockState state) {
        BlockVec pos = new BlockVec(x, y, z);
        blockActors.remove(pos);
        blockStorage.set(getIndex(x, y, z), state);
        Block block = state.getBlock();
        if (block instanceof ActorBlock actorBlock) {
            BlockActor actor = actorBlock.newBlockActor(getWorld(), vec.blockInWorldSpace(pos));
            blockActors.put(pos, actor);
        }
    }

    public void set(BlockVec pos, BlockState state) {
        blockActors.remove(pos);
        blockStorage.set(getIndex(pos.x, pos.y, pos.z), state);
        Block block = state.getBlock();
        if (block instanceof ActorBlock actorBlock) {
            BlockActor actor = actorBlock.newBlockActor(getWorld(), vec.blockInWorldSpace(pos));
            blockActors.put(pos, actor);
        }
    }

    protected void retrieveNeighbors() {
        for (Direction direction : Direction.values()) {
            Chunk chunk = getWorld().getChunk(tmpCV.set(vec).add(direction.getNormal(tmp3F)));

            neighbors[direction.ordinal()] = chunk;
            if (chunk != null) {
                chunk.neighbors[direction.opposite().ordinal()] = this;
                if (chunk instanceof WorldChunk worldChunk) {
                    worldChunk.sendChunkData();
                }
            }
        }
    }

    public World getWorld() {
        return world;
    }

    public void tick() {
        for (Map.Entry<BlockVec, BlockActor> entry : Map.copyOf(blockActors).entrySet()) {
            BlockActor curActor = entry.getValue();
            BlockVec key = entry.getKey();
            BlockVec wPos = curActor.getPos();
            BlockVec lPos = wPos.chunkLocal();
            if (get(lPos).isAir()) {
                CommonConstants.LOGGER.warn("Removing orphaned block actor: {}", wPos);
                blockActors.remove(key);
                continue;
            }
            curActor.tick();
        }

        RNG rng = world.getRNG();
        int x = rng.nextInt(0, World.CHUNK_SIZE);
        int y = rng.nextInt(0, World.CHUNK_SIZE);
        int z = rng.nextInt(0, World.CHUNK_SIZE);

        BlockState blockToTick = get(x, y, z);
        if (blockToTick.doesRandomTick()) {
            blockToTick.randomTick(this, new BlockVec(x, y, z));
        }
    }

    protected int getIndex(int x, int z) {
        return x + z * World.CHUNK_SIZE;
    }

    public RegistryKey<Biome> getBiome(int x, int z) {
        return biomeStorage.get(getIndex(x, z));
    }

    public BlockState getSafe(BlockVec corner) {
        return getSafe(corner.x, corner.y, corner.z);
    }

    public BlockState getSafe(int x, int y, int z) {
        if (x >= blockStart.x && x <= blockEnd.x && y >= blockStart.y && y <= blockEnd.y && z >= blockStart.z && z <= blockEnd.z) {
            return get(x, y, z);
        }
        return world.get(blockStart.x + x, blockStart.y + y, blockStart.z + z);
    }

    public LightMap getLightMap() {
        return lightMap;
    }

    public void consumeTicket(ChunkLoadTicket chunkLoadTicket) {

    }
}
