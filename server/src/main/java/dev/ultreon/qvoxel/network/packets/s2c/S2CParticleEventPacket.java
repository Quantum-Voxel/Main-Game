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

package dev.ultreon.qvoxel.network.packets.s2c;

import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.PacketContext;
import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.network.handler.ClientPacketHandler;
import dev.ultreon.qvoxel.network.handler.InGameClientPacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.PacketId;
import dev.ultreon.qvoxel.particle.ParticleData;
import dev.ultreon.qvoxel.particle.ParticleType;
import dev.ultreon.qvoxel.registry.RegistryKeys;

public record S2CParticleEventPacket<T extends ParticleData>(
        ParticleType<T> particleEventType,
        int quantity,
        T particleData
) implements Packet<InGameClientPacketHandler> {
    public static final PacketId<S2CParticleEventPacket<?>> ID = new PacketId("clientbound/particle_event", S2CParticleEventPacket.class);
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static final PacketCodec<S2CParticleEventPacket<?>> CODEC = PacketCodec.of((io) -> {
        ParticleType read = io.read(RegistryKeys.PARTICLE_TYPE);
        return new S2CParticleEventPacket<>(
                read,
                io.readInt(),
                read.serializer().fromBytes(io)
        );
    }, (io, packet) -> {
        ParticleType type = packet.particleEventType();
        io.write(type, RegistryKeys.PARTICLE_TYPE);
        io.writeInt(packet.quantity());
        type.serializer().toBytes(packet.particleData(), io);
    });
}
