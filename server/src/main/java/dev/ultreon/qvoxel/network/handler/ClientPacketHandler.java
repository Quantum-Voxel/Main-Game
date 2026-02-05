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

import dev.ultreon.qvoxel.network.packets.s2c.S2CAbilitiesPacket;
import dev.ultreon.qvoxel.network.packets.s2c.S2CDisconnectPacket;
import dev.ultreon.qvoxel.network.packets.s2c.S2CGameModePacket;
import dev.ultreon.qvoxel.network.packets.s2c.S2CPingPacket;
import dev.ultreon.qvoxel.network.system.IConnection;

public interface ClientPacketHandler extends PacketHandler {

    @Override
    IConnection<ClientPacketHandler, ServerPacketHandler> connection();

    default void onDisconnect(S2CDisconnectPacket message) {
        IConnection<ClientPacketHandler, ServerPacketHandler> connection = connection();
        connection.on3rdPartyDisconnect(message.code(), message.message());
        connection.close();
    }

    void onAbilities(S2CAbilitiesPacket s2CAbilitiesPacket);

    void onGameMode(S2CGameModePacket gameMode);

    void onPing(S2CPingPacket packet);
}
