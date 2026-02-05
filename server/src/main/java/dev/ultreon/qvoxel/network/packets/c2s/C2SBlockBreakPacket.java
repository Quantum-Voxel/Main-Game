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
import dev.ultreon.qvoxel.network.handler.InGameServerPacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.PacketId;

public record C2SBlockBreakPacket(int x, int y, int z) implements Packet<InGameServerPacketHandler> {
    public static final PacketId<C2SBlockBreakPacket> ID = new PacketId<>("serverbound/block_break", C2SBlockBreakPacket.class);
    public static final PacketCodec<C2SBlockBreakPacket> CODEC = PacketCodec.packed(
            PacketCodec.INT, C2SBlockBreakPacket::x,
            PacketCodec.INT, C2SBlockBreakPacket::y,
            PacketCodec.INT, C2SBlockBreakPacket::z,
            C2SBlockBreakPacket::new
    );

    public static C2SBlockBreakPacket read(PacketIO buffer) {
        return new C2SBlockBreakPacket(buffer.readInt(), buffer.readInt(), buffer.readInt());
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeInt(x).writeInt(y).writeInt(z);
    }

    @Override
    public void handle(PacketContext ctx, InGameServerPacketHandler handler) {
        handler.onBlockBreak(this);
    }
}
