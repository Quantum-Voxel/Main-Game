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

package dev.ultreon.qvoxel.network.handler;

import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.PacketContext;
import dev.ultreon.qvoxel.network.PacketDestination;
import dev.ultreon.qvoxel.network.packets.s2c.S2CDisconnectPacket;
import dev.ultreon.qvoxel.network.system.IConnection;
import dev.ultreon.qvoxel.registry.RegistryHandle;

public interface PacketHandler extends RegistryHandle {
    PacketDestination destination();

    void onDisconnect(String message);

    boolean isAcceptingPackets();

    default boolean shouldHandlePacket(Packet<?> packet) {
        return isAcceptingPackets();
    }

    PacketContext context();

    default boolean isAsync() {
        return true;
    }

    boolean isDisconnected();

    Packet<?> reply(long sequenceId);

    IConnection<?, ?> connection();
}
