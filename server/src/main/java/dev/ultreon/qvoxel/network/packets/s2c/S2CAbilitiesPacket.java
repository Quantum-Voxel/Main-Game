package dev.ultreon.qvoxel.network.packets.s2c;

import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.handler.ClientPacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.PacketId;
import dev.ultreon.qvoxel.player.PlayerAbilities;

public record S2CAbilitiesPacket(
        int bits,
        float flyingSpeed,
        float walkingSpeed
) implements Packet<ClientPacketHandler> {
    public static final PacketId<S2CAbilitiesPacket> ID = new PacketId<>("clientbound/abilities", S2CAbilitiesPacket.class);
    public static final PacketCodec<S2CAbilitiesPacket> CODEC = PacketCodec.packed(
            PacketCodec.INT, S2CAbilitiesPacket::bits,
            PacketCodec.FLOAT, S2CAbilitiesPacket::flyingSpeed,
            PacketCodec.FLOAT, S2CAbilitiesPacket::walkingSpeed,
            S2CAbilitiesPacket::new
    );


    public S2CAbilitiesPacket(PlayerAbilities abilities) {
        int bits = 0;
        if (abilities.canFly) bits = (bits | 1);
        if (abilities.flying) bits = (bits | (1 << 1));
        if (abilities.invulnerable) bits = (bits | (1 << 2));
        if (abilities.instantMine) bits = (bits | (1 << 3));
        float flyingSpeed = abilities.flyingSpeed;
        float walkingSpeed = abilities.walkingSpeed;
        this(bits, flyingSpeed, walkingSpeed);
    }

    public boolean canFly() {
        return (bits & 1) == 1;
    }

    public boolean flying() {
        return (bits >> 1 & 1) == 1;
    }

    public boolean invulnerable() {
        return (bits >> 2 & 1) == 1;
    }

    public boolean instantMine() {
        return (bits >> 3 & 1) == 1;
    }
}
