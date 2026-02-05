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

package dev.ultreon.qvoxel.network;

import dev.ultreon.qvoxel.Env;
import dev.ultreon.qvoxel.network.system.IConnection;
import dev.ultreon.qvoxel.server.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PacketContext {
    private final @Nullable ServerPlayerEntity player;
    private final IConnection<?, ?> connection;
    private final @NotNull Env destination;

    public PacketContext(@Nullable ServerPlayerEntity player, @NotNull IConnection<?, ?> connection, @NotNull Env destination) {
        this.player = player;
        this.connection = connection;
        this.destination = destination;
    }

    public void queue(Runnable handler) {
        connection.queue(handler);
    }

    public @Nullable ServerPlayerEntity getPlayer() {
        return player;
    }

    public IConnection<?, ?> getConnection() {
        return connection;
    }

    public @NotNull Env getDestination() {
        return destination;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        var that = (PacketContext) obj;
        return Objects.equals(player, that.player) &&
                Objects.equals(connection, that.connection) &&
                Objects.equals(destination, that.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, connection, destination);
    }

    @Override
    public String toString() {
        return "PacketContext[" +
                "player=" + player + ", " +
                "connection=" + connection + ", " +
                "environment=" + destination + ']';
    }

    public @NotNull ServerPlayerEntity requirePlayer() {
        ServerPlayerEntity player = this.player;
        if (player == null)
            throw new PacketException("Packet handling requires player, but there's no player in this context.");
        return player;
    }
}
