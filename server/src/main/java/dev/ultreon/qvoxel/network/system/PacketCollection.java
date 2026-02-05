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

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.PacketContext;
import dev.ultreon.qvoxel.network.PacketException;
import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.network.handler.PacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.PacketReader;
import dev.ultreon.qvoxel.network.packets.PacketWriter;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.function.TriConsumer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.ultreon.qvoxel.network.system.PacketIdInfo.PII_OPTIONAL;

/**
 * Represents a collection of packets.
 * Also contains methods for encoding, decoding and handling packets.
 *
 * @param <H> the packet handler type.
 */
public class PacketCollection<H extends PacketHandler> {
    private int id = 1;
    private final Map<Class<? extends Packet<H>>, PacketClassInfo<H, ?>> classInfoMap = new HashMap<>();
    private final Map<Identifier, PacketIdInfo<H, ?>> idInfoMap = new HashMap<>();
    private final BidiMap<Identifier, Integer> idRawId = new DualHashBidiMap<>();

    public PacketCollection() {

    }

    /**
     * Sends the mapping that maps {@link Identifier} with {@code int}.
     */
    public void sendPacketMap(PacketIO io) {
        Set<Map.Entry<Identifier, Integer>> entries = idRawId.entrySet();
        io.writeVarInt(entries.size());
        for (var entry : entries) {
            Identifier key = entry.getKey();
            Integer value = entry.getValue();

            io.writeId(key);
            io.writeVarInt(value);
            idInfoMap.get(key).write(io);
        }
    }

    public void receivePacketMap(PacketIO io) {
        idRawId.clear();
        int size = io.readVarInt();
        Set<Identifier> requirements = new HashSet<>();
        for (int i = 0; i < size; i++) {
            Identifier id = io.readId();
            int rawId = io.readVarInt();
            short flags = io.readShort();
            if (!idInfoMap.containsKey(id) && (flags & PII_OPTIONAL) != PII_OPTIONAL) {
                requirements.add(id);
            } else {
                idRawId.put(id, rawId);
            }
        }

        if (!requirements.isEmpty()) {
            throw new PacketException("""
                    Required packets:
                    %s
                    """.formatted(requirements.stream().map(Identifier::toString).collect(Collectors.joining("\n - "))));
        }
    }

    /**
     * Adds a packet to this collection.
     *
     * @param type    the type of the packet to add.
     * @param codec   the encoder to use for encoding/decoding.
     * @param handler the handler to use.
     * @return the ID of the packet.
     */
    @SuppressWarnings("unchecked")
    public <I extends H, T extends Packet<I>> int add(Class<T> type, Identifier id, PacketCodec<T> codec, TriConsumer<T, PacketContext, I> handler) {
        classInfoMap.put((Class<? extends Packet<H>>) type, (PacketClassInfo<H, ?>) new PacketClassInfo<I, T>(this.id, codec, handler));
        idInfoMap.put(id, new PacketIdInfo<>((PacketReader<Packet<H>>) codec));
        idRawId.put(id, this.id);
        return this.id++;
    }

    /**
     * Adds a packet to this collection.
     *
     * @param type    the type of the packet to add.
     * @param codec   the encoder to use for encoding/decoding.
     * @param handler the handler to use.
     * @return the ID of the packet.
     */
    @SuppressWarnings("unchecked")
    public <I extends H, T extends Packet<I>> int addOptional(Class<T> type, Identifier id, PacketCodec<T> codec, TriConsumer<T, PacketContext, I> handler) {
        classInfoMap.put((Class<? extends Packet<H>>) type, (PacketClassInfo<H, ?>) new PacketClassInfo<I, T>(this.id, codec, handler));
        idInfoMap.put(id, new PacketIdInfo<>((PacketReader<Packet<H>>) codec, PII_OPTIONAL));
        idRawId.put(id, this.id);
        return this.id++;
    }

    /**
     * Adds a packet to this collection.
     *
     * @param type    the type of the packet to add.
     * @param encoder the encoder to use.
     * @param decoder the decoder to use.
     * @param handler the handler to use.
     * @return the ID of the packet.
     */
    @SuppressWarnings("unchecked")
    public <I extends H, T extends Packet<I>> int add(Class<T> type, PacketWriter<T> encoder, PacketReader<T> decoder, TriConsumer<T, PacketContext, I> handler) {
        classInfoMap.put((Class<? extends Packet<H>>) type, new PacketClassInfo<>(
                id,
                (PacketCodec<Packet<H>>) PacketCodec.of(decoder, encoder),
                (TriConsumer<Packet<H>, PacketContext, H>) handler
        ));
        Identifier id = new Identifier("legacy/" + this.id);
        idInfoMap.put(id, new PacketIdInfo<>((PacketReader<Packet<H>>) decoder));
        idRawId.put(id, this.id);
        return this.id++;
    }

    /**
     * Encodes a packet to a buffer.
     *
     * @param packet the packet to encode.
     * @param buffer the buffer to encode to.
     * @throws PacketException if the packet is not registered.
     */
    @SuppressWarnings("unchecked")
    public void encode(PacketHandler handler, Packet<H> packet, PacketIO buffer) {
        PacketClassInfo<H, ?> info = classInfoMap.get(packet.getClass());
        if (info == null)
            throw new PacketException("Unknown packet: " + packet.getClass());
        buffer.writeInt(info.id());
        ((PacketCodec<Packet<H>>) info.codec()).toBytes(packet, handler, buffer);
    }

    /**
     * Decodes a packet from a buffer
     *
     * @param id     the ID of the packet to decode.
     * @param buffer the buffer to decode from.
     * @return the decoded packet.
     * @throws PacketException if the packet ID is unknown.
     */
    public Packet<H> decode(H handler, int id, PacketIO buffer) {
        Identifier key = idRawId.inverseBidiMap().get(id);
        if (key == null)
            throw new PacketException("Unknown packet ID: " + id);
        PacketIdInfo<H, ?> idInfo = idInfoMap.get(key);
        return idInfo.reader().fromBytes(handler, buffer);
    }

    /**
     * Handles a packet.
     *
     * @param packet        the packet to handle.
     * @param context       the context of the packet received, for accessing player and connection for example¶.
     * @param packetHandler the object that handles the packets for the current state and environment.
     * @throws PacketException if the packet is not registered.
     */
    @SuppressWarnings("unchecked")
    public void handle(Packet<H> packet, PacketContext context, H packetHandler) {
        PacketClassInfo<H, ?> info = classInfoMap.get(packet.getClass());
        if (info == null)
            throw new PacketException("Unknown packet: " + packet.getClass());
        ((TriConsumer<Packet<H>, PacketContext, H>) info.handler()).accept(packet, context, packetHandler);
    }

    public void clear() {
        classInfoMap.clear();
        idInfoMap.clear();
        idRawId.clear();
        id = 1;
    }
}
