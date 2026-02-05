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

import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.PacketContext;
import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.network.handler.ClientPacketHandler;
import dev.ultreon.qvoxel.network.handler.InGameClientPacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.PacketId;

import static dev.ultreon.qvoxel.network.packets.PacketCodec.*;

public record S2CBlockSetPacket(int x, int y, int z,
                                BlockState blockState) implements Packet<InGameClientPacketHandler> {
    public static final PacketId<S2CBlockSetPacket> ID = new PacketId<>("clientbound/block_set", S2CBlockSetPacket.class);
    public static final PacketCodec<S2CBlockSetPacket> CODEC = packed(
            INT, S2CBlockSetPacket::x,
            INT, S2CBlockSetPacket::y,
            INT, S2CBlockSetPacket::z,
            BlockState.PACKET_CODEC, S2CBlockSetPacket::blockState,
            S2CBlockSetPacket::new
    );

    public static S2CBlockSetPacket read(PacketIO buffer) {
        return new S2CBlockSetPacket(buffer.readInt(), buffer.readInt(), buffer.readInt(), buffer.readBlockState());
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeInt(x)
                .writeInt(y)
                .writeInt(z)
                .writeBlockState(blockState);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onBlockSet(this);
    }
}
