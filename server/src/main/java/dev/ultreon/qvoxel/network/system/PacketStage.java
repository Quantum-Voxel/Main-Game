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

package dev.ultreon.qvoxel.network.system;

import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.PacketContext;
import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.network.handler.ClientPacketHandler;
import dev.ultreon.qvoxel.network.handler.ServerPacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.PacketId;
import dev.ultreon.qvoxel.network.packets.PacketReader;
import dev.ultreon.qvoxel.network.packets.c2s.C2SDisconnectPacket;
import dev.ultreon.qvoxel.network.packets.s2c.*;
import org.apache.commons.lang3.function.TriConsumer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class PacketStage {
    private final PacketCollection<ClientPacketHandler> clientBoundList = new PacketCollection<>();
    private final PacketCollection<ServerPacketHandler> serverBoundList = new PacketCollection<>();
    private final PacketData<ClientPacketHandler> clientData;
    private final PacketData<ServerPacketHandler> serverData;

    /**
     * Constructs a new packet stage.
     */
    protected PacketStage() {
        clientData = new PacketData<>(clientBoundList);
        serverData = new PacketData<>(serverBoundList);
    }

    /**
     * Registers all packets in this packet stage.
     */
    public void registerPackets() {
        serverBoundList.clear();
        clientBoundList.clear();

        addServerBound(C2SDisconnectPacket.ID, C2SDisconnectPacket.CODEC, ServerPacketHandler::onDisconnect);

        addClientBound(S2CPingPacket.ID, S2CPingPacket.CODEC, ClientPacketHandler::onPing);
        addClientBound(S2CAbilitiesPacket.ID, S2CAbilitiesPacket.CODEC, ClientPacketHandler::onAbilities);
        addClientBound(S2CGameModePacket.ID, S2CGameModePacket.CODEC, ClientPacketHandler::onGameMode);
        addClientBound(S2CDisconnectPacket.ID, S2CDisconnectPacket.CODEC, ClientPacketHandler::onDisconnect);
    }

    /**
     * Adds a server-bound packet to this packet stage.
     *
     * @param <T>        the type of the packet
     * @return the id of the packet
     */
    protected <H extends ServerPacketHandler, T extends Packet<H>> int addServerBound(PacketId<T> id, PacketCodec<T> codec, TriConsumer<T, PacketContext, H> handler) {
        return serverBoundList.add(id.packetClass(), id.name(), codec, handler);
    }

    /**
     * Adds a server-bound packet to this packet stage.
     *
     * @param <T>        the type of the packet
     * @return the id of the packet
     */
    protected <H extends ClientPacketHandler, T extends Packet<H>> int addClientBound(PacketId<T> id, PacketCodec<T> codec, TriConsumer<T, PacketContext, H> handler) {
        return clientBoundList.add(id.packetClass(), id.name(), codec, handler);
    }

    /**
     * Adds a server-bound packet to this packet stage.
     *
     * @param <T>        the type of the packet
     * @return the id of the packet
     */
    protected <H extends ClientPacketHandler, T extends Packet<H>> int addClientBound(PacketId<T> id, PacketCodec<T> codec, BiConsumer<H, T> handler) {
        return clientBoundList.add(id.packetClass(), id.name(), codec, (t, _, h) -> handler.accept(h, t));
    }

    /**
     * Adds a server-bound packet to this packet stage.
     *
     * @param <T>        the type of the packet
     * @return the id of the packet
     */
    protected <H extends ServerPacketHandler, T extends Packet<H>> int addServerBound(PacketId<T> id, PacketCodec<T> codec, BiConsumer<H, T> handler) {
        return serverBoundList.add(id.packetClass(), id.name(), codec, (t, _, h) -> handler.accept(h, t));
    }

    /**
     * Adds a server-bound packet to this packet stage.
     *
     * @param <T>        the type of the packet
     * @return the id of the packet
     */
    protected <H extends ClientPacketHandler, T extends Packet<H>> int addClientBound(PacketId<T> id, PacketCodec<T> codec, Consumer<H> handler) {
        return clientBoundList.add(id.packetClass(), id.name(), codec, (_, _, h) -> handler.accept(h));
    }

    /**
     * Adds a server-bound packet to this packet stage.
     *
     * @param <T>        the type of the packet
     * @return the id of the packet
     */
    protected <H extends ServerPacketHandler, T extends Packet<H>> int addServerBound(PacketId<T> id, PacketCodec<T> codec, Consumer<H> handler) {
        return serverBoundList.add(id.packetClass(), id.name(), codec, (_, _, h) -> handler.accept(h));
    }

    /**
     * Adds a server-bound packet to this packet stage.
     *
     * @param decoder the packet decoder
     * @param <T>     the type of the packet
     * @return the id of the packet
     */
    @Deprecated
    protected <R extends ServerPacketHandler, T extends Packet<R>> int addServerBound(PacketReader<T> decoder, Class<T> type) {
        return serverBoundList.add(type, Packet::toBytes, decoder, T::handle);
    }

    /**
     * Adds a client-bound packet to this packet stage.
     *
     * @param decoder    the packet decoder
     * @param typeGetter the type getter for the packet
     * @param <T>        the type of the packet
     * @return the id of the packet
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    protected <R extends ServerPacketHandler, T extends Packet<R>> int addServerBound(Function<PacketIO, T> decoder, T... typeGetter) {
        Class<T> type = (Class<T>) typeGetter.getClass().getComponentType();
        return addServerBound((_, io) -> decoder.apply(io), type);
    }

    /**
     * Adds a client-bound packet to this packet stage.
     *
     * @param decoder    the packet decoder
     * @param typeGetter the type getter for the packet
     * @param <T>        the type of the packet
     * @return the id of the packet
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    protected <R extends ClientPacketHandler, T extends Packet<R>> int addClientBound(Function<PacketIO, T> decoder, T... typeGetter) {
        Class<T> type = (Class<T>) typeGetter.getClass().getComponentType();
        return addClientBound((_, io) -> decoder.apply(io), type);
    }

    /**
     * Adds a client-bound packet to this packet stage.
     *
     * @param decoder the packet decoder
     * @param type    the type of the packet
     * @param <R>     the type of the packet handler
     * @param <T>     the type of the packet
     * @return the id of the packet
     */
    @Deprecated
    protected <R extends ClientPacketHandler, T extends Packet<R>> int addClientBound(PacketReader<T> decoder, Class<T> type) {
        return clientBoundList.add(type, Packet::toBytes, decoder, T::handle);
    }

    /**
     * @return the client bound packet data.
     */
    public PacketData<ClientPacketHandler> getClientPackets() {
        return clientData;
    }

    /**
     * @return the server bound packet data.
     */
    public PacketData<ServerPacketHandler> getServerPackets() {
        return serverData;
    }
}
