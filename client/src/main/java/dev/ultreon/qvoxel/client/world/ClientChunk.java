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

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.debug.DebugRenderer;
import dev.ultreon.qvoxel.client.debug.ImGuiEx;
import dev.ultreon.qvoxel.client.debug.ImGuiOverlay;
import dev.ultreon.qvoxel.client.debug.Renderer;
import dev.ultreon.qvoxel.network.packets.s2c.S2CChunkDataPacket;
import dev.ultreon.qvoxel.registry.Registries;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.util.ChunkVec;
import dev.ultreon.qvoxel.util.Direction;
import dev.ultreon.qvoxel.world.Chunk;
import dev.ultreon.qvoxel.world.World;
import imgui.ImGui;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@DebugRenderer(ClientChunk.DebugRenderer.class)
public class ClientChunk extends Chunk {
    public final ChunkModel model;
    public Vector3f renderOrigin = new Vector3f();
    public final Object lock = new Object();
    private boolean needRebuild;
    private boolean initialized;
    private int[] lightHeights = new int[World.CHUNK_SIZE * World.CHUNK_SIZE];

    public ClientChunk(ClientWorld world, ChunkVec vec) {
        super(world, vec);
        model = new ChunkModel(this);
        add("Model", model);
    }

    public RenderType getRenderType(int x, int y, int z) {
        return BlockRenderTypeRegistry.getRenderType(get(x, y, z).getBlock());
    }

    public BlockState getSafe(int x, int y, int z) {
        if (x < 0 || x >= World.CHUNK_SIZE || y < 0 || y >= World.CHUNK_SIZE || z < 0 || z >= World.CHUNK_SIZE) {
            return getWorld().get(
                    x + World.CHUNK_SIZE * vec.x,
                    y + World.CHUNK_SIZE * vec.y,
                    z + World.CHUNK_SIZE * vec.z
            );
        } else {
            return get(x, y, z);
        }
    }

    public boolean isVisible(ClientPlayerEntity player, Camera camera) {
        if (isHiddenByNeighbors()) {
            return false;
        }

        Vector3d playerPos = player.getPosition();
        Vector3d chunkPos = new Vector3d(vec.x * World.CHUNK_SIZE, vec.y * World.CHUNK_SIZE, vec.z * World.CHUNK_SIZE);
        return playerPos.distanceSquared(chunkPos) < getWorld().getRenderDistanceSquared() &&
                camera.frustum.testAab(
                        (float) (chunkPos.x - playerPos.x), (float) (chunkPos.y - playerPos.y), (float) (chunkPos.z - playerPos.z),
                        (float) (chunkPos.x - playerPos.x + World.CHUNK_SIZE), (float) (chunkPos.y - playerPos.y + World.CHUNK_SIZE), (float) (chunkPos.z - playerPos.z + World.CHUNK_SIZE)
                );
    }

    private boolean isHiddenByNeighbors() {
        List<BlockState> data = blockStorage.getData();
        if (blockStorage.isUniform() && (data.isEmpty() || data.getFirst().isSolid() || data.getFirst().isFluid())) {
            boolean hidden = false;
            for (Direction dir : Direction.values()) {
                ClientChunk neighbor = (ClientChunk) neighbors[dir.ordinal()];
                if (neighbor == null) {
                    return false;
                }
                if (neighbor.blockStorage.isEmpty()) {
                    hidden = true;
                } else {
                    BlockState first = neighbor.blockStorage.getData().getFirst();
                    hidden |= neighbor.blockStorage.isUniform() && (first.isSolid() || first.isFluid());
                }
            }
            return hidden;
        }

        return false;
    }

    public boolean needRebuild() {
        return needRebuild;
    }

    @Override
    public void set(BlockVec pos, BlockState state) {
        super.set(pos, state);

        if (!initialized) return;
        for (Direction dir : Direction.values()) {
            ClientChunk neighbor = (ClientChunk) neighbors[dir.ordinal()];
            if (neighbor == null) continue;
            neighbor.needRebuild = true;
        }
        needRebuild = true;
    }

    public void done() {
        initialized = true;
        needRebuild = false;
    }

    public int getLight(int x, int y, int z) {
        if (x < 0 || x >= World.CHUNK_SIZE || y < 0 || y >= World.CHUNK_SIZE || z < 0 || z >= World.CHUNK_SIZE) {
            int wx = x + World.CHUNK_SIZE * vec.x;
            int wy = y + World.CHUNK_SIZE * vec.y;
            int wz = z + World.CHUNK_SIZE * vec.z;
            Chunk c = getWorld().getChunkAt(wx, wy, wz);
            if (c instanceof ClientChunk cc) {
                return cc.getLight(BlockVec.localize(wx), BlockVec.localize(wy), BlockVec.localize(wz));
            }
            return (byte) 0;
        }
        return lightMap.getLight(x, y, z);
    }

    public QuantumClient getClient() {
        return QuantumClient.get();
    }

    public void close() {
        model.close();
        getWorld().remove(this);
    }

    public void onChunkData(S2CChunkDataPacket s2CChunkDataPacket) {
        blockStorage.set(s2CChunkDataPacket.statePalette(), s2CChunkDataPacket.states());
        biomeStorage.set(s2CChunkDataPacket.biomePalette(), s2CChunkDataPacket.biomes());
        if (s2CChunkDataPacket.lightMap() != null) {
            lightMap.load(s2CChunkDataPacket.lightMap().getData());
        }
        if (s2CChunkDataPacket.lightHeights() != null) {
            lightHeights = s2CChunkDataPacket.lightHeights();
        }
        if (s2CChunkDataPacket.lightHeights() != null) {
            for (Map.Entry<BlockVec, Identifier> entry : s2CChunkDataPacket.blockActors().entrySet()) {
                BlockVec key = entry.getKey();
                blockActors.put(key, Registries.BLOCK_ACTOR.get(entry.getValue()).create(getWorld(), key));
            }
        }
        needRebuild = true;
    }

    public static class DebugRenderer implements Renderer<ClientChunk> {
        @Override
        public void render(ClientChunk object, @Nullable Consumer<ClientChunk> setter) {
            ImGuiOverlay.renderObject(object.model);
            ImGui.separator();
            ImGuiEx.editVec3f("Render origin", "renderOrigin", () -> object.renderOrigin, object.renderOrigin::set);
        }
    }
}
