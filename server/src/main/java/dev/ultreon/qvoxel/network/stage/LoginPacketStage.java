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

package dev.ultreon.qvoxel.network.stage;

import dev.ultreon.qvoxel.network.handler.LoginClientPacketHandler;
import dev.ultreon.qvoxel.network.handler.LoginServerPacketHandler;
import dev.ultreon.qvoxel.network.packets.c2s.C2SLoginPacket;
import dev.ultreon.qvoxel.network.packets.s2c.S2CLoginAcceptedPacket;
import dev.ultreon.qvoxel.network.packets.s2c.S2CRegistriesSyncPacket;
import dev.ultreon.qvoxel.network.packets.s2c.S2CRegistrySyncPacket;
import dev.ultreon.qvoxel.network.system.PacketStage;

public class LoginPacketStage extends PacketStage {
    public LoginPacketStage() {
        super();
    }

    @Override
    public void registerPackets() {
        super.registerPackets();

        addServerBound(C2SLoginPacket.ID, C2SLoginPacket.CODEC, LoginServerPacketHandler::onLogin);

        addClientBound(S2CLoginAcceptedPacket.ID, S2CLoginAcceptedPacket.CODEC, LoginClientPacketHandler::onLoginAccepted);
        addClientBound(S2CRegistriesSyncPacket.ID, S2CRegistriesSyncPacket.CODEC, LoginClientPacketHandler::onRegistriesSync);
        addClientBound(S2CRegistrySyncPacket.ID, S2CRegistrySyncPacket.CODEC, LoginClientPacketHandler::onRegistrySync);
    }
}
