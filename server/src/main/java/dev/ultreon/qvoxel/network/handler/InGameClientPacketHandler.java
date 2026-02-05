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

import dev.ultreon.qvoxel.network.packets.s2c.*;
import dev.ultreon.qvoxel.particle.ParticleData;

public interface InGameClientPacketHandler extends ClientPacketHandler {
    void onPing(S2CPingPacket packet);

    void onKeepAlive();

    void onTeleport(S2CTeleportPacket packet);

    void onChunkData(S2CChunkDataPacket packet);

    void onBlockSet(S2CBlockSetPacket packet);

    void onChatMessage(S2CChatMessagePacket packet);

    void onOpenMenu(S2COpenMenuPacket packet);

    void onInventoryItemChanged(S2CInventoryContentChangedPacket packet);

    void onMenuItemChanged(S2CMenuContentChangedPacket packet);

    void onHealth(S2CHealthPacket packet);

    void onSoundEvent(S2CSoundEventPacket packet);

    <T extends ParticleData> void onParticleEvent(S2CParticleEventPacket<T> packet);

    @Override
    void onGameMode(S2CGameModePacket gameMode);

    void onDeath(S2CDeathPacket packet);
}
