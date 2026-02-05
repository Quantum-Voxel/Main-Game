package dev.ultreon.qvoxel.network.packets.c2s;

import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.handler.InGameServerPacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.PacketId;
import dev.ultreon.qvoxel.player.PlayerAbilities;

public record C2SPlayerAbilitiesPacket(boolean flying) implements Packet<InGameServerPacketHandler> {
    public static final PacketId<C2SPlayerAbilitiesPacket> ID = new PacketId<>("serverbound/player_abilities", C2SPlayerAbilitiesPacket.class);
    public static final PacketCodec<C2SPlayerAbilitiesPacket> CODEC = PacketCodec.packed(
            PacketCodec.BOOLEAN, C2SPlayerAbilitiesPacket::flying,
            C2SPlayerAbilitiesPacket::new
    );
    public C2SPlayerAbilitiesPacket(PlayerAbilities playerAbilities) {
        this(playerAbilities.flying);
    }
}
