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

package dev.ultreon.qvoxel.network;

import dev.ultreon.qvoxel.network.handler.PacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;

public interface Packet<T extends PacketHandler> {
    /**
     * Serializes the packet data into a byte buffer.
     *
     * @param buffer The {@link PacketIO} object that will contain the serialized packet data.
     */
    @Deprecated
    default void toBytes(PacketIO buffer) {
        throw new PacketException("Modern packet");
    }

    default PacketCodec<? extends Packet<?>> codec() {
        throw new PacketException("Legacy packet");
    }

    /**
     * Handles the packet.
     *
     * @param ctx     The context in which the packet is being handled, providing necessary environment details.
     * @param handler The handler specific to the packet type, responsible for executing the handling logic.
     */
    default void handle(PacketContext ctx, T handler) {

    }

    @Deprecated
    @SuppressWarnings({"rawtypes", "unchecked"})
    default void toBytes(PacketHandler handler, PacketIO buffer) {
        ((PacketCodec)codec()).toBytes(this, handler, buffer);
    }
}
