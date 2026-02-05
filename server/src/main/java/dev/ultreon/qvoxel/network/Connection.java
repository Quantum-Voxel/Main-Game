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

package dev.ultreon.qvoxel.network;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.Env;
import dev.ultreon.qvoxel.network.handler.PacketHandler;
import dev.ultreon.qvoxel.network.stage.LoginPacketStage;
import dev.ultreon.qvoxel.network.system.*;
import dev.ultreon.qvoxel.registry.RegistryHandle;
import dev.ultreon.qvoxel.server.ServerPlayerEntity;
import dev.ultreon.qvoxel.util.Result;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.jetbrains.annotations.Nullable;

public abstract class Connection<OurHandler extends PacketHandler, TheirHandler extends PacketHandler> extends IConnection<OurHandler, TheirHandler> {
    private final Env env;
    private OurHandler ourHandler;
    private TheirHandler theirHandler;
    protected PacketStage stage;
    protected long ping;
    private final RegistryHandle handle;
    private boolean async;

    public Connection(Env env, RegistryHandle handle) {
        this.env = env;
        this.handle = handle;
    }

    public void init(ChannelPipeline pipeline, RegistryHandle handle) {
        pipeline.addLast("Packet Encoder", new NettyPacketEncoder(handle, this));
        pipeline.addLast("Frame Encoder", new LengthFieldPrepender(4));
        pipeline.addLast("Packet Decoder", new NettyPacketDecoder(handle, this));
        pipeline.addLast("Frame Decoder", new LengthFieldBasedFrameDecoder(1024 * 1024 * 64, 0, 4));
    }

    @Override
    public void makeAsync() {
        this.async = true;
    }

    public void setOurHandler(OurHandler ourHandler) {
        this.ourHandler = ourHandler;
    }

    @Override
    public ChannelFuture send(Packet<? extends TheirHandler> packet, @Nullable PacketListener resultListener) {
        synchronized (this) {
            Channel channel = getChannel();
            if (channel == null || !channel.isOpen()) {
                on3rdPartyDisconnect(200, "Connection closed");
                return null;
            }
            ChannelFuture packetFuture = channel.writeAndFlush(packet, channel.newPromise().addListener(future -> {
                if (!future.isSuccess()) {
                    Throwable throwable = future.exceptionNow();
                    CommonConstants.LOGGER.error("Failed to send packet", throwable);
                    disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), throwable.toString());
                }
            }));
            if (!async)
                return packetFuture.syncUninterruptibly();
            return packetFuture;
        }
    }

    public abstract @Nullable Channel getChannel();

    @Override
    public void moveTo(PacketStage stage, OurHandler handler) {
        this.stage = stage;
        this.ourHandler = handler;
        stage.registerPackets();
    }

    @Override
    public boolean isCompressed() {
        return false;
    }

    @Override
    public void disconnect(int code, String message) {
        try {
            send(createDisconnectPacket(code, message));
            getChannel().close().sync();
        } catch (Exception e) {
            CommonConstants.LOGGER.error("Failed to disconnect!", e);
        }
        ServerPlayerEntity player = getPlayer();
        if (player != null) {
            player.getServer().getPlayerManager().removePlayer(player);
            player.onDisconnect(message);
        }
    }

    protected abstract Packet<? extends TheirHandler> createDisconnectPacket(int code, String message);

    @Override
    public Result<Void> on3rdPartyDisconnect(int statusCode, String message) {
        ServerPlayerEntity player = getPlayer();
        if (player != null) {
            player.getServer().getPlayerManager().removePlayer(player);
            player.onDisconnect(message);
        }
        try {
            getChannel().close();
            return Result.ok();
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    @Override
    public void queue(Runnable handler) {

    }

    @Override
    public boolean isConnected() {
        return getChannel().isOpen();
    }

    @Override
    public abstract boolean isSingleplayer();

    @Override
    public void setPlayer(ServerPlayerEntity player) {

    }

    @Override
    public long getPing() {
        return ping;
    }

    @Override
    public void onPing(long ping) {
        this.ping = ping;
    }

    @Override
    public boolean isLoggingIn() {
        return stage instanceof LoginPacketStage;
    }

    @Override
    public void close() {
        Channel channel = getChannel();
        if (channel.isOpen()) {
            try {
                channel.close().sync();
            } catch (InterruptedException _) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public OurHandler getOurHandler() {
        return ourHandler;
    }

    @Override
    public void encode(Packet<TheirHandler> msg, PacketIO io) {
        getOtherSidePackets().encode(theirHandler, msg, io);
    }

    @Override
    public void handle(Packet<OurHandler> packet) {
        getPackets().handle(packet, createPacketContext(), getOurHandler());
    }

    protected abstract PacketContext createPacketContext();

    protected abstract PacketData<TheirHandler> getOtherSidePackets();

    protected abstract PacketData<OurHandler> getPackets();

    protected abstract boolean isRunning();

    public abstract ServerPlayerEntity getPlayer();

    public void setStage(PacketStage stage) {
        this.stage = stage;
    }

    @Override
    public PacketStage getStage() {
        return stage;
    }
}
