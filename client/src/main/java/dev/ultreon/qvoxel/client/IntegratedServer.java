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

package dev.ultreon.qvoxel.client;

import dev.ultreon.libs.crash.v0.CrashLog;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.debug.DebugRenderer;
import dev.ultreon.qvoxel.client.debug.ImGuiOverlay;
import dev.ultreon.qvoxel.client.debug.Renderer;
import dev.ultreon.qvoxel.client.gui.LoadingScreen;
import dev.ultreon.qvoxel.client.gui.Screen;
import dev.ultreon.qvoxel.client.gui.screen.DisconnectingScreen;
import dev.ultreon.qvoxel.client.gui.screen.NotificationScreen;
import dev.ultreon.qvoxel.client.network.ClientConnection;
import dev.ultreon.qvoxel.client.world.ClientPlayerEntity;
import dev.ultreon.qvoxel.featureflags.FeatureSet;
import dev.ultreon.qvoxel.network.handler.ClientPacketHandler;
import dev.ultreon.qvoxel.network.handler.ServerPacketHandler;
import dev.ultreon.qvoxel.network.packets.c2s.C2SLoginPacket;
import dev.ultreon.qvoxel.network.system.IConnection;
import dev.ultreon.qvoxel.network.system.NetworkInitializer;
import dev.ultreon.qvoxel.server.QuantumServer;
import dev.ultreon.qvoxel.server.ServerPlayerEntity;
import dev.ultreon.qvoxel.server.WorldSaveInfo;
import dev.ultreon.qvoxel.server.WorldStorage;
import dev.ultreon.qvoxel.spark.QuantumSparkPlugin;
import dev.ultreon.qvoxel.util.ChunkVec;
import dev.ultreon.ubo.types.MapType;
import imgui.ImGui;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalIoHandler;
import io.netty.channel.local.LocalServerChannel;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@DebugRenderer(IntegratedServer.DebugRenderer.class)
public class IntegratedServer extends QuantumServer {
    private static final EventLoopGroup EVENT_LOOP = new MultiThreadIoEventLoopGroup(Math.max(Runtime.getRuntime().availableProcessors() / 2, 1), LocalIoHandler.newFactory());
    public static final String PLAYER_FILE = "player.ubo";
    private final QuantumClient client;
    private boolean chunksLoaded;
    private ServerPlayerEntity host;
    private MapType playerInfo;

    public IntegratedServer(QuantumClient client, WorldStorage storage, FeatureSet features) {
        super(storage, features);
        client.add("Integrated Server", this);

        this.client = client;

        if (storage.exists(PLAYER_FILE)) {
            try {
                playerInfo = storage.read(PLAYER_FILE);
            } catch (IOException e) {
                CommonConstants.LOGGER.error("Failed to load player info", e);
            }
        }

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(EVENT_LOOP);
        serverBootstrap.channelFactory(LocalServerChannel::new);
        serverBootstrap.childHandler(new NetworkInitializer(this));
        serverBootstrap.bind(new LocalAddress("quantum-voxel"));

        thread = new Thread(this::run, "server");
        thread.start();
    }

    @Override
    protected void hostLoad() {
        super.hostLoad();
    }

    @Override
    public void save() {
        super.save();

        MapType save = host.save();
        try {
            getStorage().write(save, PLAYER_FILE);
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to save player info", e);
        }

        WorldSaveInfo worldSaveInfo = getStorage().loadInfo();
        worldSaveInfo.setSeed(getSeed());
        worldSaveInfo.setLastSave(LocalDateTime.now());
        try {
            getStorage().saveInfo(worldSaveInfo);
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to save world info", e);
        }

        CommonConstants.LOGGER.info("Saved world");
    }

    @Override
    protected void runTick() {
        QuantumClient client = QuantumClient.get();
        Screen screen = client.getScreen();
        if (screen != null && screen.doesPauseGame()) {
            pollAll();
            hostLoad();
            return;
        }

        super.runTick();
    }

    public ServerPlayerEntity placePlayer(C2SLoginPacket packet, IConnection<ServerPacketHandler, ClientPacketHandler> connection) {
        ServerPlayerEntity player = super.placePlayer(packet, connection);
        if (playerInfo != null)
            player.load(playerInfo);
        playerInfo = null;
        if (host == null)
            host = player;
        return player;
    }

    @Override
    public void onSaveError(String formatted, Exception e) {
        super.onSaveError(formatted, e);
        client.showScreen(new NotificationScreen("Error", formatted));
    }

    public ServerPlayerEntity getHost() {
        return host;
    }

    public void setHost(ServerPlayerEntity player) {
        host = player;
    }

    public void onSpawnAttempt(int attempt) {
        if (client.getScreen() instanceof LoadingScreen messageScreen) {
            messageScreen.setMessage("Attempting to spawn player nr. " + attempt);
        }
    }

    @Override
    public void onChunkError(int x, int y, int z, Throwable error) {
        super.onChunkError(x, y, z, error);
        client.notifications.add("Failed to load chunk %s, %s, %s".formatted(x, y, z), error.getMessage());
    }

    @Override
    public QuantumSparkPlugin getSparkPlugin() {
        return client.getSparkPlugin();
    }

    @Override
    public void onChunkLoad(int loaded, int total, ChunkVec vec) {
        if (chunksLoaded) return;

        if (client.getScreen() instanceof LoadingScreen messageScreen) {
            if (vec != null)
                messageScreen.setMessage("Loaded " + loaded + " / " + total + " chunks\nChunk: " + vec);
            else messageScreen.setMessage("Loaded " + loaded + " / " + total + " chunks");
        }

        if (loaded == total) {
            chunksLoaded = true;
            QuantumClient.invoke(() -> {
                client.clearGuiLayers();
                client.getWindow().captureMouse();
            });
            done();
        }
    }

    @Override
    protected long getSeed() {
        return getStorage().loadInfo().getSeed();
    }

    @Override
    public void shutdown(Runnable finalizer) {
        super.shutdown(finalizer);
        if (host != null)
            host.connection.close();
    }

    @Override
    protected void close() {
        super.close();

        client.remove(this);
    }

    @Override
    protected void onDisconnectMessage(String message) {
        if (client.getScreen() instanceof DisconnectingScreen disconnectingScreen) {
            disconnectingScreen.setMessage(message);
        }
    }

    @Override
    public void crash(CrashLog crashLog) {
        QuantumClient.crash(crashLog);
    }

    @Override
    public boolean isDedicated() {
        return false;
    }

    public QuantumClient getClient() {
        return client;
    }

    public CompletableFuture<ClientConnection> connect(ClientPlayerEntity player) {
        return ClientConnection.connectToLocalServer(player);
    }

    public static class DebugRenderer implements Renderer<IntegratedServer> {
        @Override
        public void render(IntegratedServer object, @Nullable Consumer<IntegratedServer> setter) {
            ImGui.pushID("$");
            if (ImGui.treeNode("Host Player")) {
                try {
                    ImGuiOverlay.renderObject(object.host);
                } catch (Exception _) {

                }
                ImGui.treePop();
            }
            if (ImGui.treeNode("Player Manager")) {
                try {
                    ImGuiOverlay.renderObject(object.getPlayerManager());
                } catch (Exception _) {

                }
                ImGui.treePop();
            }
            ImGui.popID();
        }
    }
}
