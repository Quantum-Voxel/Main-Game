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

package dev.ultreon.qvoxel.server.dedicated;

import dev.ultreon.qvoxel.Env;
import dev.ultreon.qvoxel.network.*;
import dev.ultreon.qvoxel.network.handler.ClientPacketHandler;
import dev.ultreon.qvoxel.network.handler.LoginServerPacketHandler;
import dev.ultreon.qvoxel.network.handler.ServerPacketHandler;
import dev.ultreon.qvoxel.network.packets.s2c.S2CDisconnectPacket;
import dev.ultreon.qvoxel.network.stage.InGamePacketStage;
import dev.ultreon.qvoxel.network.system.*;
import dev.ultreon.qvoxel.registry.RegistryHandle;
import dev.ultreon.qvoxel.server.QuantumServer;
import dev.ultreon.qvoxel.server.ServerPlayerEntity;
import dev.ultreon.qvoxel.util.Result;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import org.jetbrains.annotations.Nullable;

public class ServerConnection extends Connection<ServerPacketHandler, ClientPacketHandler> {
    private @Nullable ServerPlayerEntity player = null;
    private long ping;
    private final RegistryHandle handle;
    private boolean loggingIn = true;
    private QuantumServer server;
    private Channel channel;
    private boolean disconnecting;
    private final boolean singleplayer;

    public ServerConnection(QuantumServer server, boolean singleplayer) {
        super(Env.SERVER, server.getRegistries());
        this.singleplayer = singleplayer;
        handle = server.getRegistries();
    }

    @Override
    public boolean isCompressed() {
        return false;
    }

    @Override
    public void disconnect(int code, String message) {
        if (disconnecting) return;
        disconnecting = true;
        send(new S2CDisconnectPacket(code, message));
        getChannel().close();
        disconnecting = false;
    }

    @Override
    protected Packet<? extends ClientPacketHandler> createDisconnectPacket(int code, String message) {
        return new S2CDisconnectPacket(code, message);
    }

    @Override
    public Result<Void> on3rdPartyDisconnect(int statusCode, String message) {
        ServerPlayerEntity player1 = player;
        QuantumServer server1 = server;
        if (player1 != null && server1 != null) {
            server1.getPlayerManager().removePlayer(player1);
            player1.onDisconnect(message);
            return Result.ok();
        }
        return Result.failure(new Exception("Player or server is null"));
    }

    @Override
    public void queue(Runnable handler) {
        server.execute(handler);
    }

    @Override
    public void init(ChannelPipeline pipeline, RegistryHandle handle) {
        super.init(pipeline, handle);

        moveTo(PacketStages.LOGIN.get(), new LoginServerPacketHandler(this));
        stage.registerPackets();
    }

    @Override
    public void moveTo(PacketStage stage, ServerPacketHandler handler) {
        super.moveTo(stage, handler);
        if (stage instanceof InGamePacketStage) loggingIn = false;
    }

    @Override
    public boolean isConnected() {
        return getChannel().isOpen();
    }

    @Override
    public boolean isSingleplayer() {
        return singleplayer;
    }

    @Override
    public void setPlayer(@Nullable ServerPlayerEntity player) {
        this.player = player;
    }

    @Override
    public long getPing() {
        return ping;
    }

    @Override
    public void onPing(long ping) {

    }

    @Override
    public boolean isLoggingIn() {
        return loggingIn;
    }

    @Override
    public @Nullable ServerPlayerEntity getPlayer() {
        return player;
    }

    @Override
    public void setServer(QuantumServer server) {
        this.server = server;
    }

    @Override
    public QuantumServer getServer() {
        return server;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    protected PacketContext createPacketContext() {
        return new PacketContext(player, this, Env.CLIENT);
    }

    @Override
    protected PacketData<ClientPacketHandler> getOtherSidePackets() {
        return stage.getClientPackets();
    }

    @Override
    protected PacketData<ServerPacketHandler> getPackets() {
        return stage.getServerPackets();
    }

    @Override
    protected boolean isRunning() {
        return channel.isOpen();
    }

    @Override
    public Packet<ServerPacketHandler> decode(int id, PacketIO buffer) {
        return getPackets().decode(getOurHandler(), id, buffer);
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
