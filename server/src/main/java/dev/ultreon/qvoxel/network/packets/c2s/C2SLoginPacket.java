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

package dev.ultreon.qvoxel.network.packets.c2s;

import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.PacketContext;
import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.network.handler.LoginServerPacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.PacketId;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record C2SLoginPacket(String name, int clientRenderDistance) implements Packet<LoginServerPacketHandler> {
    public static final PacketId<C2SLoginPacket> ID = new PacketId<>("serverbound/login", C2SLoginPacket.class);
    public static final PacketCodec<C2SLoginPacket> CODEC = PacketCodec.packed(
            PacketCodec.string(16), C2SLoginPacket::name,
            PacketCodec.INT, C2SLoginPacket::clientRenderDistance,
            C2SLoginPacket::new
    );

    public static C2SLoginPacket read(PacketIO buffer) {
        return new C2SLoginPacket(buffer.readString(20), buffer.readVarInt());
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeString(name, 20);
        buffer.writeVarInt(clientRenderDistance);
    }

    @Override
    public void handle(PacketContext ctx, LoginServerPacketHandler handler) {
        handler.onLogin(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        var that = (C2SLoginPacket) obj;
        return Objects.equals(name, that.name);
    }

    @Override
    public @NotNull String toString() {
        return "C2SLoginPacket[" +
                "name=" + name + ']';
    }

}
