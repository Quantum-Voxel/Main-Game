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

package dev.ultreon.qvoxel.network.system;

import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.network.PacketListener;
import dev.ultreon.qvoxel.network.handler.PacketHandler;
import dev.ultreon.qvoxel.registry.RegistryHandle;
import dev.ultreon.qvoxel.server.QuantumServer;
import dev.ultreon.qvoxel.server.ServerPlayerEntity;
import dev.ultreon.qvoxel.util.Result;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class IConnection<OurHandler extends PacketHandler, TheirHandler extends PacketHandler> implements AutoCloseable {
    public static final AtomicInteger rx = new AtomicInteger();
    public static final AtomicInteger tx = new AtomicInteger();

    public ChannelFuture send(Packet<? extends TheirHandler> packet) {
        return send(packet, null);
    }

    public abstract ChannelFuture send(Packet<? extends TheirHandler> packet, @Nullable PacketListener resultListener);

    public abstract boolean isCompressed();

    public abstract void disconnect(int code, String message);

    public abstract Result<Void> on3rdPartyDisconnect(int statusCode, String message);

    public abstract void queue(Runnable handler);

    public abstract void moveTo(PacketStage stage, OurHandler handler);

    public boolean isConnecting() {
        return false;
    }

    public abstract boolean isConnected();

    public void tick() {

    }

    public abstract boolean isSingleplayer();

    public boolean isRemoteConnection() {
        return !isSingleplayer();
    }

    public abstract void setPlayer(ServerPlayerEntity player);

    public void disconnect(String message) {
        disconnect(CloseCodes.NORMAL_CLOSURE.getCode(), message);
    }

    public abstract long getPing();

    public abstract void onPing(long ping);

    public abstract boolean isLoggingIn();

    public abstract PacketStage getStage();

    public abstract void handle(Packet<OurHandler> packet);

    public abstract ServerPlayerEntity getPlayer();

    public abstract void setServer(QuantumServer server);

    public abstract QuantumServer getServer();

    @Override
    public abstract void close();

    public abstract boolean isClientSide();

    public abstract OurHandler getOurHandler();

    public abstract Packet<OurHandler> decode(int id, PacketIO buffer);

    public abstract void encode(Packet<TheirHandler> msg, PacketIO io);

    public void decodeAndHandle(int id, PacketIO buffer) {
        var packet = decode(id, buffer);
        handle(packet);
    }

    public abstract void init(ChannelPipeline pipeline, RegistryHandle registries);

    public abstract void makeAsync();
}
