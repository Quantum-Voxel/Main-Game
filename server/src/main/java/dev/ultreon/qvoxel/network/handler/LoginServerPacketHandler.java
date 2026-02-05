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
import dev.ultreon.qvoxel.ServerException;
import dev.ultreon.qvoxel.network.packets.c2s.C2SLoginPacket;
import dev.ultreon.qvoxel.network.packets.s2c.S2CLoginAcceptedPacket;
import dev.ultreon.qvoxel.network.system.IConnection;
import dev.ultreon.qvoxel.network.system.PacketStages;
import dev.ultreon.qvoxel.server.QuantumServer;
import dev.ultreon.qvoxel.server.ServerChunk;
import dev.ultreon.qvoxel.server.ServerPlayerEntity;
import dev.ultreon.qvoxel.server.ServerWorld;
import dev.ultreon.qvoxel.world.HeightmapType;
import dev.ultreon.qvoxel.world.gen.GenerationBarrier;

import java.util.Random;

public class LoginServerPacketHandler extends ServerPacketHandler {
    public LoginServerPacketHandler(IConnection<ServerPacketHandler, ClientPacketHandler> connection) {
        super(connection);
    }

    public void onLogin(C2SLoginPacket packet) {
        QuantumServer server = QuantumServer.get();
        if (server == null) {
            CommonConstants.LOGGER.error("Server is null");
            throw new RuntimeException("Server is null");
        }
        CommonConstants.LOGGER.info("Player '{}' is attempting to log in", packet);
        if (server.getPlayerManager().getPlayer(packet.name()) != null) {
            CommonConstants.LOGGER.error("Player '{}' is already logged in", packet.name());
            connection.disconnect("You are already logged in!");
            return;
        }

        ServerPlayerEntity player = server.placePlayer(packet, connection);
        server.submit(() -> {
            ServerWorld defaultWorld;
            try {
                defaultWorld = server.getDefaultWorld();
            } catch (ServerException e) {
                connection.disconnect("Failed to transfer player to default world:\n" + e.getMessage());
                return;
            }
            ServerChunk chunkAt = defaultWorld.getChunkAt(server.getSpawnX(), 0, server.getSpawnZ(), GenerationBarrier.TERRAIN);
            int height = defaultWorld.getHeight(server.getSpawnX(), server.getSpawnZ(), HeightmapType.MOTION_BLOCKING);
            chunkAt.discard();
            Random random = new Random(defaultWorld.getSeed());
            if (height < 72 && !server.isSpawnSet()) {
                int attempts = 0;
                do {
                    server.onSpawnAttempt(attempts);
                    server.setSpawnX(server.getSpawnX() + random.nextInt(2000) - 1000);
                    server.setSpawnZ(server.getSpawnZ() + random.nextInt(2000) - 1000);
                    chunkAt = defaultWorld.getChunkAt(server.getSpawnX(), 3, server.getSpawnZ(), GenerationBarrier.SPAWN);
                    height = defaultWorld.getHeight(server.getSpawnX(), server.getSpawnZ(), HeightmapType.MOTION_BLOCKING);
                    if (height >= 72) {
                        if (chunkAt == null)
                            throwSpawnChunkError();
                        break;
                    } else {
                        chunkAt.discard();
                    }
                } while (attempts++ < 1000);
            } else {
                chunkAt = defaultWorld.getChunkAt(server.getSpawnX(), 0, server.getSpawnZ(), GenerationBarrier.SPAWN);
                if (chunkAt == null)
                    throwSpawnChunkError();
            }

            if (!player.isDataLoaded()) {
                player.setPosition(server.getSpawnX() + 0.5, height + 1, server.getSpawnZ() + 0.5);
                player.setHealth(player.getMaxHealth());
            }
            player.onSpawn();
            this.server = server;
            connection.setServer(this.server);
            server.getPlayerManager().addPlayer(player);
            CommonConstants.LOGGER.info("Player '{}' logged in", packet);

            connection.send(new S2CLoginAcceptedPacket(player.getUuid(), player.getPosition(), player.getGameMode(), player.getHealth(), player.getFoodStatus(), player.getYawBody(), player.getPitchHead(), player.getServerWorld().getDimension()));
            connection.moveTo(PacketStages.IN_GAME.get(), new InGameServerPacketHandler(connection));
            connection.makeAsync();

            player.init();
        }).exceptionally(throwable -> {
            CommonConstants.LOGGER.error("A serious error occurred while logging in", throwable);
            connection.disconnect("A serious error occurred while logging in:\n" + getCleanMessage(throwable));
            return null;
        });
    }

    private String getCleanMessage(Throwable throwable) {
        while (throwable.getCause() != null && throwable.getLocalizedMessage().equals(throwable.getCause().getClass().getName() + ": " + throwable.getCause().getLocalizedMessage())) {
            throwable = throwable.getCause();
        }
        if (throwable.getLocalizedMessage() != null) {
            return throwable.getLocalizedMessage();
        } else {
            return throwable.getClass().getName();
        }
    }

    private static void throwSpawnChunkError() {
        CommonConstants.LOGGER.error("Could not find spawn chunk");
        throw new SpawnChunkException("Could not find spawn chunk");
    }

    @Override
    public boolean isAsync() {
        return false;
    }
}
