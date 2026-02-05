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

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.PacketContext;
import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.network.handler.ClientPacketHandler;
import dev.ultreon.qvoxel.network.handler.LoginClientPacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.PacketId;
import dev.ultreon.qvoxel.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public record S2CRegistriesSyncPacket(List<Identifier> registries) implements Packet<LoginClientPacketHandler> {
    public static final PacketId<S2CRegistriesSyncPacket> ID = new PacketId<>("clientbound/registries_sync", S2CRegistriesSyncPacket.class);
    public static final PacketCodec<S2CRegistriesSyncPacket> CODEC = PacketCodec.packed(
            PacketCodec.collection(65536, PacketCodec.ID, ArrayList::new), S2CRegistriesSyncPacket::registries,
            S2CRegistriesSyncPacket::new
    );

    public S2CRegistriesSyncPacket(Registry<Registry<?>> registries) {
        var regs = new ArrayList<Identifier>();
        for (int i = 0; i < registries.size(); i++) {
            Registry<?> obj = registries.byRawId(i);
            if (obj == null || obj.isSyncDisabled()) continue;
            regs.add(registries.getId(obj));
        }
        this(regs);
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeVarInt(registries.size());
        for (Identifier entry : registries) {
            buffer.writeId(entry);
        }
    }

    @Override
    public void handle(PacketContext ctx, LoginClientPacketHandler handler) {
        handler.onRegistriesSync(this);
    }

    public List<Identifier> registries() {
        return registries;
    }

    public static S2CRegistriesSyncPacket read(PacketIO packetIO) {
        int size = packetIO.readVarInt();
        List<Identifier> registries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            registries.add(packetIO.readId());
        }
        return new S2CRegistriesSyncPacket(registries);
    }
}
