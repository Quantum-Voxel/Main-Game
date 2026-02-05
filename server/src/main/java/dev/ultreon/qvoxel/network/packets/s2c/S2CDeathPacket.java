package dev.ultreon.qvoxel.network.packets.s2c;

import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.network.handler.InGameClientPacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.PacketId;

public record S2CDeathPacket() implements Packet<InGameClientPacketHandler> {
    public static final PacketId<S2CDeathPacket> ID = new PacketId<>("clientbound/death", S2CDeathPacket.class);
    public static final PacketCodec<S2CDeathPacket> CODEC = PacketCodec.unit(S2CDeathPacket::new);
}
