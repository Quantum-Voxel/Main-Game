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
import dev.ultreon.qvoxel.network.handler.InGameClientPacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.PacketId;

public record S2COpenMenuPacket(int menuRawId) implements Packet<InGameClientPacketHandler> {
    public static final PacketId<S2COpenMenuPacket> ID = new PacketId<>("clientbound/open_menu", S2COpenMenuPacket.class);
    public static final PacketCodec<S2COpenMenuPacket> CODEC = PacketCodec.packed(
            PacketCodec.INT, S2COpenMenuPacket::menuRawId,
            S2COpenMenuPacket::new
    );
}
