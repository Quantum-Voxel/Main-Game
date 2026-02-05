package dev.ultreon.qvoxel.network.packets;

import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.network.handler.PacketHandler;

public interface PacketReader<T> {
    T fromBytes(PacketHandler handler, PacketIO io);
}
