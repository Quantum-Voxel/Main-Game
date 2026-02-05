package dev.ultreon.qvoxel.network.packets.c2s;

import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.handler.InGameServerPacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.PacketId;

public record C2SPlayerCrouchingPacket(boolean crouching) implements Packet<InGameServerPacketHandler> {
    public static final PacketId<C2SPlayerCrouchingPacket> ID = new PacketId<>("serverbound/crouching", C2SPlayerCrouchingPacket.class);
    public static final PacketCodec<C2SPlayerCrouchingPacket> CODEC = PacketCodec.packed(
            PacketCodec.BOOLEAN, C2SPlayerCrouchingPacket::crouching,
            C2SPlayerCrouchingPacket::new
    );
}

