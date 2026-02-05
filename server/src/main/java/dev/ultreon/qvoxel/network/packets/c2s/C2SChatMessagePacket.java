package dev.ultreon.qvoxel.network.packets.c2s;

import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.handler.InGameServerPacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.PacketId;

public record C2SChatMessagePacket(String message) implements Packet<InGameServerPacketHandler> {
    public static final PacketId<C2SChatMessagePacket> ID = new PacketId<>("serverbound/chat_message", C2SChatMessagePacket.class);
    public static final PacketCodec<C2SChatMessagePacket> CODEC = PacketCodec.packed(
            PacketCodec.string(256), C2SChatMessagePacket::message,
            C2SChatMessagePacket::new
    );
}
