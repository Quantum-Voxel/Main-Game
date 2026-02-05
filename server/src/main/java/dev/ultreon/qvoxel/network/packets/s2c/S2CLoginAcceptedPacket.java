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
import dev.ultreon.qvoxel.network.handler.LoginClientPacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.PacketId;
import dev.ultreon.qvoxel.registry.RegistryKey;
import dev.ultreon.qvoxel.registry.RegistryKeys;
import dev.ultreon.qvoxel.server.FoodStatus;
import dev.ultreon.qvoxel.server.GameMode;
import dev.ultreon.qvoxel.world.DimensionInfo;
import org.joml.Vector3d;

import java.util.UUID;

public record S2CLoginAcceptedPacket(
        UUID uuid,
        Vector3d spawnPos,
        GameMode gameMode,
        float health,
        int foodLevel,
        float saturationLevel,
        float exhaustion,
        float yaw,
        float pitch,
        RegistryKey<DimensionInfo> dimension
) implements Packet<LoginClientPacketHandler> {
    public static final PacketId<S2CLoginAcceptedPacket> ID = new PacketId<>("clientbound/login_accepted", S2CLoginAcceptedPacket.class);
    public static final PacketCodec<S2CLoginAcceptedPacket> CODEC = PacketCodec.packed(
            PacketCodec.UUID, S2CLoginAcceptedPacket::uuid,
            PacketCodec.VECTOR3D, S2CLoginAcceptedPacket::spawnPos,
            PacketCodec.enumClass(GameMode.class), S2CLoginAcceptedPacket::gameMode,
            PacketCodec.FLOAT, S2CLoginAcceptedPacket::health,
            PacketCodec.INT, S2CLoginAcceptedPacket::foodLevel,
            PacketCodec.FLOAT, S2CLoginAcceptedPacket::saturationLevel,
            PacketCodec.FLOAT, S2CLoginAcceptedPacket::exhaustion,
            PacketCodec.FLOAT, S2CLoginAcceptedPacket::yaw,
            PacketCodec.FLOAT, S2CLoginAcceptedPacket::pitch,
            PacketCodec.key(RegistryKeys.DIMENSION), S2CLoginAcceptedPacket::dimension,
            S2CLoginAcceptedPacket::new
    );

    public S2CLoginAcceptedPacket(UUID uuid, Vector3d spawnPos, GameMode gameMode, float health, FoodStatus status, float yaw, float pitch, RegistryKey<DimensionInfo> dimension) {
        this(uuid, spawnPos, gameMode, health, status.getFoodLevel(), status.getSaturationLevel(), status.getExhaustionLevel(), yaw, pitch, dimension);
    }
}
