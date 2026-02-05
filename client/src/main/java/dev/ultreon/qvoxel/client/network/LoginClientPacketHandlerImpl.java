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
import dev.ultreon.qvoxel.Env;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.gui.LoadingScreen;
import dev.ultreon.qvoxel.client.gui.Screen;
import dev.ultreon.qvoxel.client.world.ClientPlayerEntity;
import dev.ultreon.qvoxel.client.world.ClientWorld;
import dev.ultreon.qvoxel.featureflags.FeatureSet;
import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.PacketContext;
import dev.ultreon.qvoxel.network.PacketDestination;
import dev.ultreon.qvoxel.network.handler.ClientPacketHandler;
import dev.ultreon.qvoxel.network.handler.LoginClientPacketHandler;
import dev.ultreon.qvoxel.network.handler.ServerPacketHandler;
import dev.ultreon.qvoxel.network.packets.s2c.*;
import dev.ultreon.qvoxel.network.system.IConnection;
import dev.ultreon.qvoxel.network.system.PacketStages;
import dev.ultreon.qvoxel.registry.IdRegistry;
import dev.ultreon.qvoxel.registry.Registry;
import dev.ultreon.qvoxel.registry.RegistryKey;

public class LoginClientPacketHandlerImpl implements LoginClientPacketHandler {
    private final QuantumClient client = QuantumClient.get();
    private final IConnection<ClientPacketHandler, ServerPacketHandler> connection;
    private final ClientPlayerEntity player;

    public LoginClientPacketHandlerImpl(IConnection<ClientPacketHandler, ServerPacketHandler> connection, ClientPlayerEntity player) {
        this.connection = connection;
        this.player = player;
    }

    @Override
    public void onRegistriesSync(S2CRegistriesSyncPacket packet) {
        client.registries.load(packet);
    }

    @Override
    public void onRegistrySync(S2CRegistrySyncPacket s2CRegistrySyncPacket) {
        client.registries.load(s2CRegistrySyncPacket);
    }

    @Override
    public void onLoginAccepted(S2CLoginAcceptedPacket packet) {
        connection.moveTo(PacketStages.IN_GAME.get(), new InGameClientPacketHandlerImpl(connection, player));
        connection.makeAsync();

        CommonConstants.LOGGER.info("Successfully logged in with UUID {}", packet.uuid());

        QuantumClient.invokeAndWait(() -> {
            Screen screen = client.getScreen();
            if (screen instanceof LoadingScreen messageScreen) {
                messageScreen.setMessage("Joining world...");
            }
            CommonConstants.LOGGER.info("Moving to world {}", packet.dimension());
            player.setPosition(packet.spawnPos());
            player.setYawBody(packet.yaw());
            player.setPitchHead(packet.pitch());
            player.setDimension(packet.dimension());
            player.setUuid(packet.uuid());
            player.setOnline(true);

            if (client.getWorld() == null) {
                client.setWorld(new ClientWorld(packet.dimension(), player));
            }

            player.setWorld(client.getWorld());
            if (connection.isRemoteConnection())
                client.showScreen(null);
        });
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
        return connection.isConnected();
    }

    @Override
    public PacketContext context() {
        return new PacketContext(null, connection, Env.SERVER);
    }

    @Override
    public boolean isDisconnected() {
        return !connection.isConnected();
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
    public void onGameMode(S2CGameModePacket packet) {
        if (player == null) {
            CommonConstants.LOGGER.warn("Received game mode packet while not in game!");
            return;
        }

        player.onGameMode(packet.gameMode());
    }

    @Override
    public void onPing(S2CPingPacket packet) {

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
