package dev.ultreon.qvoxel.network.packets.s2c;

import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.handler.ClientPacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.PacketId;

public record S2CDisconnectPacket(int code, String message) implements Packet<ClientPacketHandler> {
    public static final PacketId<S2CDisconnectPacket> ID = new PacketId<>("clientbound/disconnect", S2CDisconnectPacket.class);
    public static final PacketCodec<S2CDisconnectPacket> CODEC = PacketCodec.packed(
            PacketCodec.INT, S2CDisconnectPacket::code,
            PacketCodec.string(256), S2CDisconnectPacket::message,
            S2CDisconnectPacket::new
    );
}
