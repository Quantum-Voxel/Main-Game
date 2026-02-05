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
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.PacketId;

public record S2CTeleportPacket(
        float yaw,
        float pitch,
        double x,
        double y,
        double z
) implements Packet<InGameClientPacketHandler> {
    public static final PacketId<S2CTeleportPacket> ID = new PacketId<>("clientbound/teleport", S2CTeleportPacket.class);
    public static final PacketCodec<S2CTeleportPacket> CODEC = PacketCodec.packed(
            PacketCodec.FLOAT, S2CTeleportPacket::yaw,
            PacketCodec.FLOAT, S2CTeleportPacket::pitch,
            PacketCodec.DOUBLE, S2CTeleportPacket::x,
            PacketCodec.DOUBLE, S2CTeleportPacket::y,
            PacketCodec.DOUBLE, S2CTeleportPacket::z,
            S2CTeleportPacket::new
    );
}
