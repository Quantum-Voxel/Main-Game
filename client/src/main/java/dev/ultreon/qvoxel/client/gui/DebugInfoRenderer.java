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

package dev.ultreon.qvoxel.client.gui;

import dev.ultreon.qvoxel.ServerException;
import dev.ultreon.qvoxel.client.IntegratedServer;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.client.world.ClientPlayerEntity;
import dev.ultreon.qvoxel.client.world.ClientWorld;
import dev.ultreon.qvoxel.client.world.WorldRenderer;
import dev.ultreon.qvoxel.resource.GameNode;
import dev.ultreon.qvoxel.server.QuantumServer;
import dev.ultreon.qvoxel.server.ServerPlayerEntity;
import dev.ultreon.qvoxel.server.ServerWorld;
import dev.ultreon.qvoxel.server.WorldChunk;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.world.BlockHitResult;
import dev.ultreon.qvoxel.world.Chunk;
import dev.ultreon.qvoxel.world.HeightmapType;
import dev.ultreon.qvoxel.world.HitResult;
import dev.ultreon.qvoxel.world.light.LightMap;
import net.fabricmc.loader.api.FabricLoader;
import org.joml.Vector3d;

import java.util.List;

public class DebugInfoRenderer extends GameNode {
    private final QuantumClient client;
    private final BlockVec tmpBV = new BlockVec();
    public boolean enabled;
    private int leftLine = 0;
    private int rightLine = 0;

    public DebugInfoRenderer(QuantumClient client) {
        this.client = client;
    }

    public void render(GuiRenderer guiRenderer) {
        if (!enabled)
            return;

        leftLine = 0;
        rightLine = 0;

        leftLines(guiRenderer);
        rightLines(guiRenderer);
    }

    private void leftLines(GuiRenderer guiRenderer) {
        ClientWorld world = client.getWorld();
        List<ClientPlayerEntity> players = client.players;
        ClientPlayerEntity player = players.isEmpty() ? null : players.getFirst();
        renderLeft(guiRenderer, "Quantum Voxel v" + FabricLoader.getInstance().getModContainer("quantum").orElseThrow().getMetadata().getVersion().getFriendlyString());
        renderLeft(guiRenderer, "Fabric Loader v" + FabricLoader.getInstance().getModContainer("fabricloader").orElseThrow().getMetadata().getVersion().getFriendlyString());
        renderLeft(guiRenderer, "FPS: " + QuantumClient.getFps());
        renderLeft(guiRenderer, "Memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 + "MB" + " / " + Runtime.getRuntime().maxMemory() / 1024 / 1024 + "MB");

        if (player != null && world != null) {
            BlockVec blockVec = player.getBlockVec(tmpBV);
            Chunk chunkAt = world.getChunkAt(blockVec);
            renderLeft(guiRenderer, "Position: " + blockVec);
            renderLeft(guiRenderer, "Chunk: " + blockVec.chunk());
            renderLeft(guiRenderer, "Rotation: " + player.yawHead + ", " + player.pitchHead);
            if (chunkAt != null) {
                LightMap lightMap = chunkAt.getLightMap();
                renderLeft(guiRenderer, "Light RGB: " + String.format("#%02x%02x%02x", lightMap.getRed(blockVec.chunkLocal()), lightMap.getGreen(blockVec.chunkLocal()), lightMap.getBlue(blockVec.chunkLocal())) + ", S: " + lightMap.getSky(blockVec.chunkLocal()));
                HitResult hitResult = player.castRay(6.0F);
                if (hitResult instanceof BlockHitResult blockHitResult) {
                    renderLeft(guiRenderer, "Block: " + blockHitResult.state().getBlock().getId());
                }
                renderLeft(guiRenderer, "Biome: " + world.getBiome(blockVec).id());

                IntegratedServer integratedServer = client.getIntegratedServer();
                if (integratedServer != null) {
                    WorldChunk worldChunk = null;
                    try {
                        worldChunk = (WorldChunk) integratedServer.getDefaultWorld().getChunkAt(blockVec);
                    } catch (ServerException e) {
                        renderLeft(guiRenderer, "::! Error: " + e.getMessage());
                    }
                    if (worldChunk != null) {
                        LightMap lightMap1 = worldChunk.getLightMap();
                        renderLeft(guiRenderer, "Server Light RGB: " + String.format("#%02x%02x%02x", lightMap1.getRed(blockVec.chunkLocal()), lightMap1.getGreen(blockVec.chunkLocal()), lightMap1.getBlue(blockVec.chunkLocal())) + ", S: " + lightMap1.getSky(blockVec.chunkLocal()));
                    }
                }
            } else {
                renderLeft(guiRenderer, "Waiting for chunk...");
            }
            WorldRenderer worldRenderer = player.getWorldRenderer();
            if (worldRenderer != null) {
                renderLeft(guiRenderer, "Visible Chunks: " + worldRenderer.getVisibleChunks());
            }

            QuantumServer server = QuantumServer.get();
            if (server != null) {
                renderLeft(guiRenderer, "Current Server TPS: " + QuantumServer.getCurrentTps());
            }
        }
    }

    private void rightLines(GuiRenderer guiRenderer) {
        QuantumServer server = QuantumServer.get();
        List<ClientPlayerEntity> players = client.players;
        ClientPlayerEntity player = players.isEmpty() ? null : players.getFirst();
        if (server != null) {
            renderRight(guiRenderer, "Players: " + server.getPlayerManager().getPlayerCount());
        }
    }

    private void renderServerLines(GuiRenderer guiRenderer, ServerPlayerEntity serverPlayer, ClientPlayerEntity player) {
        ServerWorld serverWorld = serverPlayer.getServerWorld();
        Vector3d position = serverPlayer.position;
        tmpBV.set(position);
        renderRight(guiRenderer, "Heights: ws=" + serverWorld.getHeight(tmpBV.x, tmpBV.z, HeightmapType.WORLD_SURFACE)
                + ", lb=" + serverWorld.getHeight(tmpBV.x, tmpBV.z, HeightmapType.LIGHT_BLOCKING)
                + ", t=" + serverWorld.getHeight(tmpBV.x, tmpBV.z, HeightmapType.TERRAIN)
                + ", of=" + serverWorld.getHeight(tmpBV.x, tmpBV.z, HeightmapType.OCEAN_FLOOR)
                + ", mb=" + serverWorld.getHeight(tmpBV.x, tmpBV.z, HeightmapType.MOTION_BLOCKING)
                + ", mb!l=" + serverWorld.getHeight(tmpBV.x, tmpBV.z, HeightmapType.MOTION_BLOCKING_NO_LEAVES)
        );
        renderRight(guiRenderer, "Position: " + position);
        renderRight(guiRenderer, "Rotation: " + player.yawHead + ", " + player.pitchHead);
        renderRight(guiRenderer, "Chunk: " + BlockVec.chunkOf(tmpBV.x) + ", " + BlockVec.chunkOf(tmpBV.y) + ", " + BlockVec.chunkOf(tmpBV.z));
        Chunk serverChunk = serverWorld.getChunkOrNull(BlockVec.chunkOf(tmpBV.x), BlockVec.chunkOf(tmpBV.y), BlockVec.chunkOf(tmpBV.z));
        if (serverChunk != null) {
            LightMap lightMap = serverChunk.getLightMap();
            BlockVec pos = tmpBV.chunkLocal();
            renderRight(guiRenderer, "Light RGB: " + String.format("#%02x%02x%02x", lightMap.getRed(pos), lightMap.getGreen(pos), lightMap.getBlue(pos)) + ", S: " + lightMap.getSky(pos));
            HitResult hitResult = player.castRay(6.0F);
            if (hitResult instanceof BlockHitResult blockHitResult) {
                renderRight(guiRenderer, "Block: " + blockHitResult.state().getBlock().getId());
            }
        } else {
            renderRight(guiRenderer, "Waiting for server chunk...");
        }
    }

    public void renderLeft(GuiRenderer guiRenderer, String text) {
        guiRenderer.drawString(text, 20, 20 + client.font.lineHeight * leftLine);
        leftLine++;
    }

    public void renderRight(GuiRenderer guiRenderer, String text) {
        guiRenderer.drawString(text, client.getScaledWidth() - 20 - client.font.widthOf(text), 20 + client.font.lineHeight * rightLine);
        rightLine++;
    }
}
