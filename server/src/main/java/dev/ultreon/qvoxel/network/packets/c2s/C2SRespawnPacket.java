package dev.ultreon.qvoxel.network.packets.c2s;

import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.handler.InGameServerPacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.PacketId;

public record C2SRespawnPacket() implements Packet<InGameServerPacketHandler> {
    public static final PacketId<C2SRespawnPacket> ID = new PacketId<>("serverbound/respawn", C2SRespawnPacket.class);
    public static final PacketCodec<C2SRespawnPacket> CODEC = PacketCodec.unit(C2SRespawnPacket::new);
}
