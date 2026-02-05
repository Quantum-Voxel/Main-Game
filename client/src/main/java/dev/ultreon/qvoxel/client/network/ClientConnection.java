/*
 * Copyright 2025. Quinten 'Qubix' Jungblut
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.ultreon.qvoxel.client.network;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.Env;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.world.ClientPlayerEntity;
import dev.ultreon.qvoxel.network.Connection;
import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.PacketContext;
import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.network.handler.ClientPacketHandler;
import dev.ultreon.qvoxel.network.handler.ServerPacketHandler;
import dev.ultreon.qvoxel.network.packets.c2s.C2SDisconnectPacket;
import dev.ultreon.qvoxel.network.packets.c2s.C2SLoginPacket;
import dev.ultreon.qvoxel.network.system.PacketData;
import dev.ultreon.qvoxel.network.system.PacketStages;
import dev.ultreon.qvoxel.registry.RegistryHandle;
import dev.ultreon.qvoxel.server.QuantumServer;
import dev.ultreon.qvoxel.server.ServerPlayerEntity;
import dev.ultreon.qvoxel.util.Result;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.channels.ClosedChannelException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ClientConnection extends Connection<ClientPacketHandler, ServerPacketHandler> {
    private final QuantumClient client;
    private final boolean singleplayer;
    private Channel channel;
    private final ClientPlayerEntity player;

    public ClientConnection(boolean singleplayer, ClientPlayerEntity player) {
        QuantumClient client = QuantumClient.get();
        super(Env.CLIENT, client.registries);
        this.singleplayer = singleplayer;
        this.player = player;
        this.client = client;
    }

    @Override
    public boolean isSingleplayer() {
        return singleplayer;
    }

    public static CompletableFuture<ClientConnection> connectToServer(String location, ClientPlayerEntity player) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ClientConnection clientConnection = new ClientConnection(false, player);
                Bootstrap bootstrap = new Bootstrap()
                        .group(QuantumClient.REMOTE_EVENT_GROUP)
                        .handler(new ChannelInitializer<>() {

                            @Override
                            protected void initChannel(Channel ch) {
                                clientConnection.init(ch.pipeline(), QuantumClient.get().registries);
                            }
                        })
                        .channelFactory(NioSocketChannel::new);
                bootstrap.remoteAddress(location, 38800);
                bootstrap.connect().awaitUninterruptibly(10, TimeUnit.SECONDS);
                return clientConnection;
            } catch (Exception e) {
                CommonConstants.LOGGER.error("Failed to connect to server", e);
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<ClientConnection> connectToLocalServer(ClientPlayerEntity player) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ClientConnection clientConnection = new ClientConnection(false, player);
                Bootstrap bootstrap = new Bootstrap()
                        .group(QuantumClient.LOCAL_EVENT_GROUP)
                        .handler(new ChannelHandler() {
                            @Override
                            public void handlerAdded(ChannelHandlerContext ctx) {
                                player.connection = clientConnection;
                                clientConnection.channel = ctx.channel();
                            }

                            @Override
                            public void handlerRemoved(ChannelHandlerContext ctx) {
                                clientConnection.on3rdPartyDisconnect(200, "Connection closed");
                                clientConnection.close();
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                if (cause instanceof ClosedChannelException) return;

                                CommonConstants.LOGGER.error("Internal error:", cause);
                                clientConnection.on3rdPartyDisconnect(500, "Internal error:\n" + cause);
                                clientConnection.disconnect(500, "Internal error:\n" + cause);
                            }
                        })
                        .channelFactory(LocalChannel::new);
                bootstrap.remoteAddress(new LocalAddress("quantum-voxel"));
                bootstrap.connect().syncUninterruptibly();
                QuantumClient client = QuantumClient.get();
                clientConnection.init(clientConnection.channel.pipeline(), client.registries);
                clientConnection.send(new C2SLoginPacket(player.getUsername(), client.renderDistance));
                return clientConnection;
            } catch (Exception e) {
                CommonConstants.LOGGER.warn("Failed to local channel", e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    protected PacketData<ClientPacketHandler> getPackets() {
        return stage.getClientPackets();
    }

    @Override
    protected PacketData<ServerPacketHandler> getOtherSidePackets() {
        return stage.getServerPackets();
    }

    @Override
    public void close() {
        super.close();
        client.onDisconnect("Disconnected", true);
    }

    @Override
    protected PacketContext createPacketContext() {
        return new PacketContext(null, this, Env.CLIENT);
    }

    @Override
    public boolean isClientSide() {
        return true;
    }

    @Override
    public Packet<ClientPacketHandler> decode(int id, PacketIO buffer) {
        return getPackets().decode(getOurHandler(), id, buffer);
    }

    @Override
    protected boolean isRunning() {
        return !client.isShutdown();
    }

    @Override
    public ServerPlayerEntity getPlayer() {
        return null; // Guaranteed to be null
    }

    @Override
    public void setServer(QuantumServer server) {
        // Guaranteed to be null
    }

    @Override
    public QuantumServer getServer() {
        return null;
    }

    @Override
    public void init(ChannelPipeline pipeline, RegistryHandle handle) {
        super.init(pipeline, handle);

        moveTo(PacketStages.LOGIN.get(), new LoginClientPacketHandlerImpl(this, player));
        stage.registerPackets();
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    @Override
    protected Packet<? extends ServerPacketHandler> createDisconnectPacket(int code, String message) {
        return new C2SDisconnectPacket(code, message);
    }

    @Override
    public Result<Void> on3rdPartyDisconnect(int statusCode, String message) {
        client.onDisconnect(message, isSingleplayer());
        return Result.ok(null);
    }
}
