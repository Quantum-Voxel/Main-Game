package dev.ultreon.qvoxel.network.packets.s2c;

import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.PacketContext;
import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.network.handler.ClientPacketHandler;
import dev.ultreon.qvoxel.network.handler.InGameClientPacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.PacketId;
import dev.ultreon.qvoxel.server.GameMode;

public record S2CGameModePacket(GameMode gameMode) implements Packet<ClientPacketHandler> {
    public static final PacketId<S2CGameModePacket> ID = new PacketId<>("clientbound/game_mode", S2CGameModePacket.class);
    public static final PacketCodec<S2CGameModePacket> CODEC = PacketCodec.packed(
            PacketCodec.enumClass(GameMode.class), S2CGameModePacket::gameMode,
            S2CGameModePacket::new
    );
}
