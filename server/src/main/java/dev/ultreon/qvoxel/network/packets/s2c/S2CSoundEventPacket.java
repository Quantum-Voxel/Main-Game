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
import dev.ultreon.qvoxel.sound.SoundEvent;
import org.joml.Vector3f;

public record S2CSoundEventPacket(
        SoundEvent soundEvent,
        float volume,
        float pitch,
        Vector3f relativePos,
        Vector3f velocity
) implements Packet<InGameClientPacketHandler> {
    public static final PacketId<S2CSoundEventPacket> ID = new PacketId<>("clientbound/sound_event", S2CSoundEventPacket.class);
    public static final PacketCodec<S2CSoundEventPacket> CODEC = PacketCodec.packed(
            SoundEvent.PACKET_CODEC, S2CSoundEventPacket::soundEvent,
            PacketCodec.FLOAT, S2CSoundEventPacket::volume,
            PacketCodec.FLOAT, S2CSoundEventPacket::pitch,
            PacketCodec.VECTOR3F, S2CSoundEventPacket::relativePos,
            PacketCodec.VECTOR3F, S2CSoundEventPacket::velocity,
            S2CSoundEventPacket::new
    );

    public static S2CSoundEventPacket read(PacketIO buffer) {
        return new S2CSoundEventPacket(
                buffer.readSoundEvent(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readVector3f(new Vector3f()),
                buffer.readVector3f(new Vector3f())
        );
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeSoundEvent(soundEvent)
                .writeFloat(volume)
                .writeFloat(pitch)
                .writeVector3f(relativePos)
                .writeVector3f(velocity);
    }

    @Override
    public void handle(PacketContext ctx, InGameClientPacketHandler handler) {
        handler.onSoundEvent(this);
    }
}
