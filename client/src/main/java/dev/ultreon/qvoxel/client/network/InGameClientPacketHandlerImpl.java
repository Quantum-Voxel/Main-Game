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

package dev.ultreon.qvoxel.client.network;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.gui.ChatMessage;
import dev.ultreon.qvoxel.client.gui.Overlays;
import dev.ultreon.qvoxel.client.world.ClientPlayerEntity;
import dev.ultreon.qvoxel.client.world.ClientWorld;
import dev.ultreon.qvoxel.featureflags.FeatureSet;
import dev.ultreon.qvoxel.menu.ContainerMenu;
import dev.ultreon.qvoxel.menu.MenuType;
import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.PacketContext;
import dev.ultreon.qvoxel.network.PacketDestination;
import dev.ultreon.qvoxel.network.handler.ClientPacketHandler;
import dev.ultreon.qvoxel.network.handler.InGameClientPacketHandler;
import dev.ultreon.qvoxel.network.handler.ServerPacketHandler;
import dev.ultreon.qvoxel.network.packets.c2s.C2SKeepAlivePacket;
import dev.ultreon.qvoxel.network.packets.s2c.*;
import dev.ultreon.qvoxel.network.system.IConnection;
import dev.ultreon.qvoxel.particle.ParticleData;
import dev.ultreon.qvoxel.registry.IdRegistry;
import dev.ultreon.qvoxel.registry.Registry;
import dev.ultreon.qvoxel.registry.RegistryKey;
import dev.ultreon.qvoxel.registry.RegistryKeys;
import dev.ultreon.qvoxel.sound.SoundEvent;
import dev.ultreon.qvoxel.util.BlockFlags;
import dev.ultreon.qvoxel.util.ChunkVec;

import java.util.TimerTask;

public class InGameClientPacketHandlerImpl implements InGameClientPacketHandler {
    private final IConnection<ClientPacketHandler, ServerPacketHandler> connection;
    private final QuantumClient client = QuantumClient.get();
    private ClientPlayerEntity player;

    public InGameClientPacketHandlerImpl(IConnection<ClientPacketHandler, ServerPacketHandler> connection, ClientPlayerEntity player) {
        this.connection = connection;
        this.player = player;
    }

    @Override
    public PacketDestination destination() {
        return PacketDestination.SERVER;
    }

    @Override
    public void onDisconnect(String message) {
        client.onDisconnect(message, connection.isSingleplayer());
    }

    @Override
    public boolean isAcceptingPackets() {
        return false;
    }

    @Override
    public PacketContext context() {
        return null;
    }

    @Override
    public boolean isDisconnected() {
        return false;
    }

    @Override
    public Packet<?> reply(long sequenceId) {
        return null;
    }

    @Override
    public IConnection<ClientPacketHandler, ServerPacketHandler> connection() {
        return connection;
    }

    @Override
    public void onAbilities(S2CAbilitiesPacket s2CAbilitiesPacket) {
        if (player == null) {
            CommonConstants.LOGGER.warn("Received abilities packet while not in game!");
            return;
        }

        player.getAbilities().onPacket(s2CAbilitiesPacket);
    }

    @Override
    public void onPing(S2CPingPacket packet) {

    }

    @Override
    public void onKeepAlive() {
        CommonConstants.TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
                connection.send(new C2SKeepAlivePacket());
            }
        }, 1000L);
    }

    @Override
    public void onTeleport(S2CTeleportPacket packet) {
        if (player != null) {
            player.setPosition(packet.x(), packet.y(), packet.z());
            player.setRotation(packet.yaw(), packet.pitch());
            player.setVelocity(0, 0, 0);
        } else {
            CommonConstants.LOGGER.warn("Received teleport packet while not in game!");
        }
    }

    @Override
    public void onChunkData(S2CChunkDataPacket packet) {
        ClientWorld world = client.getWorld();
        if (world != null) {
            ChunkVec chunkVec = packet.chunkVec();
            world.onChunkData(chunkVec.x, chunkVec.y, chunkVec.z, packet);
        } else {
            CommonConstants.LOGGER.warn("Received chunk data packet while not in game!");
        }
    }

    @Override
    public void onBlockSet(S2CBlockSetPacket packet) {
        ClientWorld world = client.getWorld();
        if (world != null) {
            world.set(packet.x(), packet.y(), packet.z(), packet.blockState(), BlockFlags.NONE);
        } else {
            CommonConstants.LOGGER.warn("Received block set packet while not in game!");
        }
    }

    @Override
    public void onChatMessage(S2CChatMessagePacket packet) {
        Overlays.CHAT.addMessage(ChatMessage.system(packet.message()));
    }

    @Override
    public void onOpenMenu(S2COpenMenuPacket packet) {
        if (packet.menuRawId() == -1) {
            client.closeMenu();
            return;
        }

        // Registries might return null here, so we need to check for that
        MenuType<?> menuType = client.registries.get(RegistryKeys.MENU_TYPE).byRawId(packet.menuRawId());
        if (menuType == null) {
            CommonConstants.LOGGER.warn("Received open menu packet for unknown menu type {}!", packet.menuRawId());
            return;
        }

        // We also need to check if we're in-game
        ClientWorld world = client.getWorld();
        if (player == null || world == null) {
            CommonConstants.LOGGER.warn("Received open menu packet while not in game!");
            return;
        }

        // Finally, we can open the menu
        ContainerMenu o = menuType.create(player);
        client.openMenu(o);
    }

    @Override
    public void onInventoryItemChanged(S2CInventoryContentChangedPacket packet) {
        if (player == null) {
            CommonConstants.LOGGER.warn("Received inventory item changed packet while not in game!");
            return;
        }

        packet.changes().forEach((slot, stack) -> player.getInventory().setItem(slot, stack));
    }

    @Override
    public void onMenuItemChanged(S2CMenuContentChangedPacket packet) {
        if (player == null) {
            CommonConstants.LOGGER.warn("Received menu item changed packet while not in game!");
            return;
        }

        ContainerMenu menu = client.getMenu();
        if (menu == null) {
            CommonConstants.LOGGER.warn("Received menu item changed packet while not in a menu!");
            return;
        }

        packet.changes().forEach(menu::setItem);
    }

    @Override
    public void onHealth(S2CHealthPacket packet) {
        if (player == null) {
            CommonConstants.LOGGER.warn("Received health packet while not in game!");
            return;
        }

        player.setHealth(packet.health());
    }

    @Override
    public void onSoundEvent(S2CSoundEventPacket packet) {
        SoundEvent soundEvent = packet.soundEvent();
        QuantumClient.invoke(() -> client.playSound(soundEvent, packet.volume(), packet.pitch(), packet.relativePos(), packet.velocity()));
    }

    @Override
    public <T extends ParticleData> void onParticleEvent(S2CParticleEventPacket<T> packet) {
        client.particleSystem.handle(packet.particleEventType(), packet.quantity(), packet.particleData());
    }

    @Override
    public void onGameMode(S2CGameModePacket packet) {
        if (player != null) {
            player.onGameMode(packet.gameMode());
        }
    }

    @Override
    public void onDeath(S2CDeathPacket packet) {
        if (player != null) {
            player.onDeath();
        }
    }

    @Override
    public <T> IdRegistry<T> get(RegistryKey<? extends Registry<T>> key) {
        return client.registries.get(key);
    }

    @Override
    public FeatureSet getFeatures() {
        return client.getFeatures();
    }
}
