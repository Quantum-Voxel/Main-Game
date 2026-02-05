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

package dev.ultreon.qvoxel.client.registry;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.gui.screen.MissingRegistriesScreen;
import dev.ultreon.qvoxel.featureflags.FeatureSet;
import dev.ultreon.qvoxel.network.packets.s2c.S2CRegistriesSyncPacket;
import dev.ultreon.qvoxel.network.packets.s2c.S2CRegistrySyncPacket;
import dev.ultreon.qvoxel.network.system.CloseCodes;
import dev.ultreon.qvoxel.registry.*;
import dev.ultreon.qvoxel.server.QuantumServer;

import java.util.HashSet;
import java.util.Set;

public class ClientSyncRegistries implements RegistryHandle {
    private final LocalRegistries registries = new LocalRegistries();
    private final QuantumClient client;

    public ClientSyncRegistries(QuantumClient client) {
        this.client = client;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> IdRegistry<T> get(RegistryKey<? extends Registry<T>> registryKey) {
        if (Registries.REGISTRY.contains((RegistryKey) registryKey)) {
            return Registries.REGISTRY.get((RegistryKey) registryKey);
        }
        return registries.get((RegistryKey) registryKey);
    }

    @Override
    public FeatureSet getFeatures() {
        return QuantumServer.get().getFeatures();
    }

    public <T> void set(RegistryKey<ExternalRegistry<T>> registryKey, ExternalRegistry<T> registry) {
        registries.set(registryKey, registry);
    }

    public void load(S2CRegistrySyncPacket packet) {
        if (Registries.REGISTRY.contains(packet.getRegistryID())) {
            Registries.REGISTRY.get(packet.getRegistryID()).sync(packet.getRegistryMap());
            return;
        }
        registries.get(packet.getRegistryID()).load(packet.getRegistryMap());
    }

    public void load(S2CRegistriesSyncPacket registries) {
        Set<Identifier> set = this.registries.ids();
        Set<Identifier> notFound = new HashSet<>();
        for (Identifier entry : registries.registries()) {
            if (!set.contains(entry)) {
                notFound.add(entry);
            }

            set.remove(entry);
        }

        if (!notFound.isEmpty()) {
            if (!set.isEmpty()) {
                client.players.forEach(player -> player.connection.disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), "Missing registries from both sides: " + notFound + " and " + set));
                client.showScreen(new MissingRegistriesScreen(notFound, set));
                return;
            }

            client.players.forEach(player -> player.connection.disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), "Missing registries from client: " + notFound));
            client.showScreen(new MissingRegistriesScreen(notFound, Set.of()));
            return;
        }

        if (!set.isEmpty()) {
            client.players.forEach(player -> player.connection.disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), "Missing registries from server: " + set));
            client.showScreen(new MissingRegistriesScreen(Set.of(), set));
        }
    }
}
