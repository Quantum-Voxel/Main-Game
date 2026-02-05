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
import dev.ultreon.qvoxel.network.handler.LoginClientPacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.PacketId;
import dev.ultreon.qvoxel.registry.Registry;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

public record S2CRegistrySyncPacket(
        Identifier registryID,
        BidiMap<Integer, Identifier> registryMap
) implements Packet<LoginClientPacketHandler> {
    public static final PacketId<S2CRegistrySyncPacket> ID = new PacketId<>("clientbound/registry_sync", S2CRegistrySyncPacket.class);
    public static final PacketCodec<S2CRegistrySyncPacket> CODEC = PacketCodec.packed(
            PacketCodec.ID, S2CRegistrySyncPacket::registryID,
            PacketCodec.map(524288, PacketCodec.INT, PacketCodec.ID, DualHashBidiMap::new), S2CRegistrySyncPacket::registryMap,
            S2CRegistrySyncPacket::new
    );

    public <T> S2CRegistrySyncPacket(Registry<T> registry) {
        var registryID = registry.id();
        var registryMap = new DualHashBidiMap<Integer, Identifier>();

        for (var i = 0; i < registry.size(); i++) {
            var object = registry.byRawId(i);
            var namespaceID = registry.getId(object);
            registryMap.put(i, namespaceID);
        }

        this(registryID, registryMap);
    }

    public BidiMap<Integer, Identifier> getRegistryMap() {
        return registryMap;
    }

    @Override
    public void toBytes(PacketIO buffer) {
        buffer.writeVarInt(registryMap.size());
        for (var i = 0; i < registryMap.size(); i++) {
            buffer.writeId(registryMap.get(i));
        }
    }

    @Override
    public void handle(PacketContext ctx, LoginClientPacketHandler handler) {
        handler.onRegistrySync(this);
    }

    public Identifier getRegistryID() {
        return registryID;
    }

    public static S2CRegistrySyncPacket read(PacketIO packetIO) {
        var registryID = packetIO.readId();
        var size = packetIO.readVarInt();
        var registryMap = new DualHashBidiMap<Integer, Identifier>();
        for (var i = 0; i < size; i++) {
            registryMap.put(i, packetIO.readId());
        }
        return new S2CRegistrySyncPacket(registryID, registryMap);
    }
}
