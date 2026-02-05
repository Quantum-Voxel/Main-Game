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

package dev.ultreon.qvoxel.network.system;

import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.PacketContext;
import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.network.handler.PacketHandler;

public record PacketData<T extends PacketHandler>(PacketCollection<T> collection) {
    public Packet<T> decode(T handler, int id, PacketIO buffer) {
        return collection.decode(handler, id, buffer);
    }

    @SuppressWarnings("unchecked")
    public void encode(PacketHandler handler, Packet<?> packet, PacketIO buffer) {
        collection.encode(handler, (Packet<T>) packet, buffer);
    }

    public void handle(Packet<T> packet, PacketContext context, T listener) {
        collection.handle(packet, context, listener);
    }

    public void receivePacketMap(PacketIO io) {
        collection.receivePacketMap(io);
    }
}
