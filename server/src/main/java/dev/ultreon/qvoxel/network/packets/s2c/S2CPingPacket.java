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

import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.PacketContext;
import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.network.handler.ClientPacketHandler;
import dev.ultreon.qvoxel.network.handler.InGameClientPacketHandler;
import dev.ultreon.qvoxel.network.handler.ServerPacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.PacketId;
import org.jetbrains.annotations.NotNull;

public record S2CPingPacket(long serverTime, long time) implements Packet<ClientPacketHandler> {

    public static final PacketId<S2CPingPacket> ID = new PacketId<>("clientbound/ping", S2CPingPacket.class);
    public static final PacketCodec<S2CPingPacket> CODEC = PacketCodec.packed(
            PacketCodec.LONG, S2CPingPacket::serverTime,
            PacketCodec.LONG, S2CPingPacket::time,
            S2CPingPacket::new
    );

    public S2CPingPacket(long time) {
        this(System.currentTimeMillis(), time);
    }

    public static S2CPingPacket read(PacketIO buffer) {
        var serverTime = buffer.readLong();
        var time = buffer.readLong();

        return new S2CPingPacket(serverTime, time);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeLong(serverTime);
        buffer.writeLong(time);
    }

    @Override
    public void handle(PacketContext ctx, ClientPacketHandler handler) {
        if (handler instanceof InGameClientPacketHandler inGameHandler) {
            inGameHandler.onPing(this);
        }
    }

    @Override
    public @NotNull String toString() {
        return "S2CPingPacket{serverTime=" + serverTime + ", time=" + time + '}';
    }

}
