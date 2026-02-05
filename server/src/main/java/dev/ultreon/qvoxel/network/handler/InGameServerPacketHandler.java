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

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.network.packets.c2s.*;
import dev.ultreon.qvoxel.network.packets.s2c.S2CKeepAlivePacket;
import dev.ultreon.qvoxel.network.packets.s2c.S2CPingPacket;
import dev.ultreon.qvoxel.network.system.CloseCodes;
import dev.ultreon.qvoxel.network.system.IConnection;
import dev.ultreon.qvoxel.server.QuantumServer;
import dev.ultreon.qvoxel.server.ServerOfflineException;
import dev.ultreon.qvoxel.server.ServerPlayerEntity;

import java.util.concurrent.CompletableFuture;

public class InGameServerPacketHandler extends ServerPacketHandler {
    public InGameServerPacketHandler(IConnection<ServerPacketHandler, ClientPacketHandler> connection) {
        super(connection);
    }

    public void onPing(C2SPingPacket packet) {
        connection.send(new S2CPingPacket(packet.time()));
    }

    public void onKeepAlive() {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(CommonConstants.KEEP_ALIVE_INTERVAL);
            } catch (InterruptedException e) {
                return;
            }
            connection.send(new S2CKeepAlivePacket());
        });
    }

    public void onMove(C2SMovePacket packet) {
        try {
            ServerPlayerEntity player = connection.getPlayer();
            player.setRotation(packet.yaw(), packet.pitch());
            QuantumServer.invokeAndWait(() -> player.onMove(packet.x(), packet.y(), packet.z()));
        } catch (ServerOfflineException _) {

        } catch (Exception e) {
            CommonConstants.LOGGER.error("Failed to process move packet!", e);
            connection.disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), "Failed to process move packet!");
            connection.on3rdPartyDisconnect(CloseCodes.PROTOCOL_ERROR.getCode(), "Failed to process move packet!");
        }
    }

    public void onBlockBreak(C2SBlockBreakPacket packet) {
        try {
            QuantumServer.invokeAndWait(() -> connection.getPlayer().breakBlock(packet.x(), packet.y(), packet.z()));
        } catch (Exception e) {
            CommonConstants.LOGGER.error("Failed to process block break packet!", e);
            connection.disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), "Failed to process block break packet!");
            connection.on3rdPartyDisconnect(CloseCodes.PROTOCOL_ERROR.getCode(), "Failed to process block break packet!");
        }
    }

    public void onUseItem() {
        try {
            QuantumServer.invokeAndWait(() -> connection.getPlayer().useItem());
        } catch (Exception e) {
            CommonConstants.LOGGER.error("Failed to process use item packet!", e);
            connection.disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), "Failed to process use item packet!");
            connection.on3rdPartyDisconnect(CloseCodes.PROTOCOL_ERROR.getCode(), "Failed to process use item packet!");
        }
    }

    public void onItemSelect(C2SItemSelectPacket packet) {
        try {
            QuantumServer.invokeAndWait(() -> connection.getPlayer().onSelectItem(packet.selected()));
        } catch (Exception e) {
            CommonConstants.LOGGER.error("Failed to process item select packet!", e);
            connection.disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), "Failed to process item select packet!");
            connection.on3rdPartyDisconnect(CloseCodes.PROTOCOL_ERROR.getCode(), "Failed to process item select packet!");
        }
    }

    public void onChatMessage(C2SChatMessagePacket packet) {
        try {
            QuantumServer.invokeAndWait(() -> connection.getPlayer().processMessage(packet.message()));
        } catch (ServerOfflineException _) {

        }
    }

    public void onCrouching(C2SPlayerCrouchingPacket packet) {
        connection.getPlayer().setCrouching(packet.crouching());
    }

    public void onRotate(C2SRotatePacket packet) {
        connection.getPlayer().setRotation(packet.yaw(), packet.pitch());
    }

    public void onOpenInventory(C2SOpenInventoryPacket packet) {
        connection.getPlayer().openInventory();
    }

    public void onRespawn(C2SRespawnPacket packet) {
        try {
            QuantumServer.invokeAndWait(() -> connection.getPlayer().respawn());
        } catch (ServerOfflineException e) {
            connection.disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), "Failed to respawn player!");
        }
    }
}
