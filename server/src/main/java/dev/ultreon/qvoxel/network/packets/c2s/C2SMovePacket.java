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
import dev.ultreon.qvoxel.network.handler.InGameServerPacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.PacketId;

public record C2SMovePacket(
        float yaw,
        float pitch,
        double x,
        double y,
        double z
) implements Packet<InGameServerPacketHandler> {
    public static final PacketId<C2SMovePacket> ID = new PacketId<>("serverbound/move", C2SMovePacket.class);
    public static final PacketCodec<C2SMovePacket> CODEC = PacketCodec.packed(
            PacketCodec.FLOAT, C2SMovePacket::yaw,
            PacketCodec.FLOAT, C2SMovePacket::pitch,
            PacketCodec.DOUBLE, C2SMovePacket::x,
            PacketCodec.DOUBLE, C2SMovePacket::y,
            PacketCodec.DOUBLE, C2SMovePacket::z,
            C2SMovePacket::new
    );
}
