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

import dev.ultreon.qvoxel.network.packets.s2c.S2CLoginAcceptedPacket;
import dev.ultreon.qvoxel.network.packets.s2c.S2CRegistriesSyncPacket;
import dev.ultreon.qvoxel.network.packets.s2c.S2CRegistrySyncPacket;

public interface LoginClientPacketHandler extends ClientPacketHandler {
    void onRegistriesSync(S2CRegistriesSyncPacket packet);

    void onRegistrySync(S2CRegistrySyncPacket s2CRegistrySyncPacket);

    void onLoginAccepted(S2CLoginAcceptedPacket packet);

    @Override
    default boolean isAsync() {
        return false;
    }
}
