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

package dev.ultreon.qvoxel.server;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.resource.GameNode;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager extends GameNode implements Iterable<ServerPlayerEntity> {
    @Override
    public @NotNull Iterator<ServerPlayerEntity> iterator() {
        return playersByUuid.values().iterator();
    }

    private final Map<String, ServerPlayerEntity> playersByUsername = new ConcurrentHashMap<>();
    private final Map<UUID, ServerPlayerEntity> playersByUuid = new ConcurrentHashMap<>();

    public ServerPlayerEntity getPlayer(String username) {
        if (username == null) return null;
        return playersByUsername.get(username);
    }

    public ServerPlayerEntity getPlayer(UUID uuid) {
        return playersByUuid.get(uuid);
    }

    public void addPlayer(ServerPlayerEntity player) {
        playersByUsername.put(player.getUsername(), player);
        playersByUuid.put(player.getUuid(), player);
        add("Player '" + player.getUsername() + "'", player);
    }

    public void removePlayer(ServerPlayerEntity player) {
        playersByUsername.remove(player.getUsername());
        playersByUuid.remove(player.getUuid());

        save(player);
        remove(player);
    }

    protected void save(ServerPlayerEntity player) {
        CommonConstants.LOGGER.info("Saving player '{}'...", player.getUsername());
        player.save();
    }

    public void saveAll() {
        for (ServerPlayerEntity value : playersByUsername.values()) {
            save(value);
        }
    }

    public void close() {
        for (ServerPlayerEntity value : playersByUsername.values()) {
            save(value);
            remove(value);
        }

        playersByUsername.clear();
        playersByUuid.clear();
    }

    public int getPlayerCount() {
        return playersByUuid.size();
    }
}
