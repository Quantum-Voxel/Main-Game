package dev.ultreon.qvoxel.network.system;

import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.PacketContext;
import dev.ultreon.qvoxel.network.handler.PacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import org.apache.commons.lang3.function.TriConsumer;

public record PacketClassInfo<H extends PacketHandler, T extends Packet<H>>(
        int id,
        PacketCodec<T> codec,
        TriConsumer<T, PacketContext, H> handler
) {
}
