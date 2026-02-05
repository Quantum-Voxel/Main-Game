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
import dev.ultreon.qvoxel.featureflags.FeatureSet;
import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.PacketContext;
import dev.ultreon.qvoxel.network.PacketDestination;
import dev.ultreon.qvoxel.network.packets.c2s.C2SDisconnectPacket;
import dev.ultreon.qvoxel.network.packets.c2s.C2SPlayerAbilitiesPacket;
import dev.ultreon.qvoxel.network.system.CloseCodes;
import dev.ultreon.qvoxel.network.system.IConnection;
import dev.ultreon.qvoxel.registry.IdRegistry;
import dev.ultreon.qvoxel.registry.Registry;
import dev.ultreon.qvoxel.registry.RegistryKey;
import dev.ultreon.qvoxel.server.QuantumServer;
import dev.ultreon.qvoxel.server.ServerPlayerEntity;

public abstract class ServerPacketHandler implements PacketHandler {
    protected QuantumServer server;
    private boolean connected;
    protected IConnection<ServerPacketHandler, ClientPacketHandler> connection;

    protected ServerPacketHandler(IConnection<ServerPacketHandler, ClientPacketHandler> connection) {
        this.connection = connection;
    }

    @Override
    public <T> IdRegistry<T> get(RegistryKey<? extends Registry<T>> key) {
        return server.getRegistries().get(key);
    }

    @Override
    public FeatureSet getFeatures() {
        return server.getFeatures();
    }

    @Override
    public PacketDestination destination() {
        return PacketDestination.CLIENT;
    }

    @Override
    public void onDisconnect(String message) {
        // noop
    }

    @Override
    public boolean isAcceptingPackets() {
        return connected;
    }

    @Override
    public PacketContext context() {
        return null;
    }

    @Override
    public boolean isDisconnected() {
        return !connected;
    }

    @Override
    public Packet<?> reply(long sequenceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IConnection<?, ?> connection() {
        return connection;
    }

    public void onPlayerAbilities(C2SPlayerAbilitiesPacket packet) {
        ServerPlayerEntity player = connection.getPlayer();
        if (player == null) {
            CommonConstants.LOGGER.warn("No player available to handle player abilities");
            return;
        }
        if (packet.flying() && !player.getAbilities().canFly) {
            return;
        }
        player.getAbilities().onPacket(packet);
    }

    public void onDisconnect(C2SDisconnectPacket packet) {
        connection.on3rdPartyDisconnect(packet.code(), packet.message());
        connection.close();
    }
}
