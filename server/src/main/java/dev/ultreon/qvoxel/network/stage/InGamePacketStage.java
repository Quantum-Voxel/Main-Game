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

import dev.ultreon.qvoxel.network.handler.ClientPacketHandler;
import dev.ultreon.qvoxel.network.handler.InGameClientPacketHandler;
import dev.ultreon.qvoxel.network.handler.InGameServerPacketHandler;
import dev.ultreon.qvoxel.network.packets.c2s.*;
import dev.ultreon.qvoxel.network.packets.s2c.*;
import dev.ultreon.qvoxel.network.system.PacketStage;

public class InGamePacketStage extends PacketStage {
    public InGamePacketStage() {
        super();
    }

    @Override
    public void registerPackets() {
        super.registerPackets();

        addServerBound(C2SMovePacket.ID, C2SMovePacket.CODEC, InGameServerPacketHandler::onMove);
        addServerBound(C2SBlockBreakPacket.ID, C2SBlockBreakPacket.CODEC, InGameServerPacketHandler::onBlockBreak);
        addServerBound(C2SUseItemPacket.ID, C2SUseItemPacket.CODEC, InGameServerPacketHandler::onUseItem);
        addServerBound(C2SItemSelectPacket.ID, C2SItemSelectPacket.CODEC, InGameServerPacketHandler::onItemSelect);
        addServerBound(C2SChatMessagePacket.ID, C2SChatMessagePacket.CODEC, InGameServerPacketHandler::onChatMessage);
        addServerBound(C2SPlayerAbilitiesPacket.ID, C2SPlayerAbilitiesPacket.CODEC, InGameServerPacketHandler::onPlayerAbilities);
        addServerBound(C2SPlayerCrouchingPacket.ID, C2SPlayerCrouchingPacket.CODEC, InGameServerPacketHandler::onCrouching);
        addServerBound(C2SRotatePacket.ID, C2SRotatePacket.CODEC, InGameServerPacketHandler::onRotate);
        addServerBound(C2SOpenInventoryPacket.ID, C2SOpenInventoryPacket.CODEC, InGameServerPacketHandler::onOpenInventory);
        addServerBound(C2SRespawnPacket.ID, C2SRespawnPacket.CODEC, InGameServerPacketHandler::onRespawn);

        addClientBound(S2CTeleportPacket.ID, S2CTeleportPacket.CODEC, InGameClientPacketHandler::onTeleport);
        addClientBound(S2CChunkDataPacket.ID, S2CChunkDataPacket.CODEC, InGameClientPacketHandler::onChunkData);
        addClientBound(S2CBlockSetPacket.ID, S2CBlockSetPacket.CODEC, InGameClientPacketHandler::onBlockSet);
        addClientBound(S2CChatMessagePacket.ID, S2CChatMessagePacket.CODEC, InGameClientPacketHandler::onChatMessage);
        addClientBound(S2CInventoryContentChangedPacket.ID, S2CInventoryContentChangedPacket.CODEC, InGameClientPacketHandler::onInventoryItemChanged);
        addClientBound(S2CMenuContentChangedPacket.ID, S2CMenuContentChangedPacket.CODEC, InGameClientPacketHandler::onMenuItemChanged);
        addClientBound(S2COpenMenuPacket.ID, S2COpenMenuPacket.CODEC, InGameClientPacketHandler::onOpenMenu);
        addClientBound(S2CHealthPacket.ID, S2CHealthPacket.CODEC, InGameClientPacketHandler::onHealth);
        addClientBound(S2CSoundEventPacket.ID, S2CSoundEventPacket.CODEC, InGameClientPacketHandler::onSoundEvent);
        addClientBound(S2CParticleEventPacket.ID, S2CParticleEventPacket.CODEC, InGameClientPacketHandler::onParticleEvent);
        addClientBound(S2CAbilitiesPacket.ID, S2CAbilitiesPacket.CODEC, ClientPacketHandler::onAbilities);
        addClientBound(S2CGameModePacket.ID, S2CGameModePacket.CODEC, ClientPacketHandler::onGameMode);
        addClientBound(S2COpenMenuPacket.ID, S2COpenMenuPacket.CODEC, InGameClientPacketHandler::onOpenMenu);
        addClientBound(S2CDeathPacket.ID, S2CDeathPacket.CODEC, InGameClientPacketHandler::onDeath);
    }
}
