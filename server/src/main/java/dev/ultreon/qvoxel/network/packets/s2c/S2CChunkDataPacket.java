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

package dev.ultreon.qvoxel.network.packets.s2c;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.handler.InGameClientPacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.PacketId;
import dev.ultreon.qvoxel.registry.RegistryKey;
import dev.ultreon.qvoxel.server.WorldChunk;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.util.ChunkVec;
import dev.ultreon.qvoxel.world.light.LightMap;
import dev.ultreon.qvoxel.world.gen.biome.Biome;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static dev.ultreon.qvoxel.network.packets.PacketCodec.*;
import static dev.ultreon.qvoxel.registry.RegistryKeys.BIOME;
import static dev.ultreon.qvoxel.world.World.CHUNK_SURFACE;
import static dev.ultreon.qvoxel.world.World.CHUNK_VOLUME;

public record S2CChunkDataPacket(
        ChunkVec chunkVec,
        short[] statePalette,
        BlockState[] states,
        short[] biomePalette,
        RegistryKey<Biome>[] biomes,
        LightMap lightMap,
        int[] surfaceHeights,
        int[] lightHeights,
        Map<BlockVec, Identifier> blockActors
) implements Packet<InGameClientPacketHandler> {
    public static final PacketId<S2CChunkDataPacket> ID = new PacketId<>("clientbound/chunk_data", S2CChunkDataPacket.class);
    public static final PacketCodec<S2CChunkDataPacket> CODEC = packed(
            CHUNK_VEC, S2CChunkDataPacket::chunkVec,
            shorts(CHUNK_VOLUME), S2CChunkDataPacket::statePalette,
            BlockState.PACKET_CODEC.array(BlockState.class), S2CChunkDataPacket::states,
            shorts(CHUNK_SURFACE), S2CChunkDataPacket::biomePalette,
            objects(key(BIOME)), S2CChunkDataPacket::biomes,
            LightMap.PACKET_CODEC, S2CChunkDataPacket::lightMap,
            ints(CHUNK_SURFACE), S2CChunkDataPacket::surfaceHeights,
            ints(CHUNK_SURFACE), S2CChunkDataPacket::lightHeights,
            map(CHUNK_VOLUME, BLOCK_VEC, PacketCodec.ID, ConcurrentHashMap::new), S2CChunkDataPacket::blockActors,
            S2CChunkDataPacket::new
    );

    public S2CChunkDataPacket(WorldChunk worldChunk) {
        short[] statePalette = worldChunk.getStorage().getPalette();
        BlockState[] states = worldChunk.getStorage().getData().toArray(BlockState[]::new);
        short[] biomePalette = worldChunk.getBiomeStorage().getPalette();
        RegistryKey<Biome>[] biomes = worldChunk.getBiomeStorage().getData().<RegistryKey<Biome>>toArray(RegistryKey[]::new);
        LightMap lightMap = worldChunk.getLightMap();
        int[] surfaceHeights = worldChunk.getSurfaceHeights();
        int[] lightHeights = worldChunk.getLightHeights();
        Map<BlockVec, Identifier> blockActors = worldChunk.getBlockActors();
        this(worldChunk.vec, statePalette, states, biomePalette, biomes, lightMap, surfaceHeights, lightHeights, blockActors);
    }
}
