package dev.ultreon.qvoxel.network.system;

import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.network.handler.PacketHandler;
import dev.ultreon.qvoxel.network.packets.PacketReader;

public record PacketIdInfo<H extends PacketHandler, T extends Packet<H>>(
        PacketReader<T> reader,
        int flags
) {
    public static final int PII_OPTIONAL = 0b0000001;

    public PacketIdInfo(PacketReader<T> codec) {
        this(codec, 0);
    }

    public void write(PacketIO io) {
        io.writeShort(flags);
    }
}
