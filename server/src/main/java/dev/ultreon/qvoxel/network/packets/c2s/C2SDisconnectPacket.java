package dev.ultreon.qvoxel.network.packets.c2s;

import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.handler.ServerPacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.PacketId;

public record C2SDisconnectPacket(int code, String message) implements Packet<ServerPacketHandler> {
    public static final PacketId<C2SDisconnectPacket> ID = new PacketId<>("serverbound/disconnect", C2SDisconnectPacket.class);
    public static final PacketCodec<C2SDisconnectPacket> CODEC = PacketCodec.packed(
            PacketCodec.INT, C2SDisconnectPacket::code,
            PacketCodec.string(256), C2SDisconnectPacket::message,
            C2SDisconnectPacket::new
    );
}
