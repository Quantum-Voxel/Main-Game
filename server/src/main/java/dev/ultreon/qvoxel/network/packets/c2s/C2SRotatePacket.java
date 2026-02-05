package dev.ultreon.qvoxel.network.packets.c2s;

import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.handler.InGameServerPacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.PacketId;

public record C2SRotatePacket(float yaw, float pitch) implements Packet<InGameServerPacketHandler> {
    public static final PacketId<C2SRotatePacket> ID = new PacketId<>("serverbound/rotate", C2SRotatePacket.class);
    public static final PacketCodec<C2SRotatePacket> CODEC = PacketCodec.packed(
            PacketCodec.FLOAT, C2SRotatePacket::yaw,
            PacketCodec.FLOAT, C2SRotatePacket::pitch,
            C2SRotatePacket::new
    );
}
