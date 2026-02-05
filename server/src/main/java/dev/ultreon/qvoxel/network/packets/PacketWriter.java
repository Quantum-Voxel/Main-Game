package dev.ultreon.qvoxel.network.packets;

import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.network.handler.PacketHandler;

public interface PacketWriter<T> {
    void toBytes(T packet, PacketHandler handler, PacketIO io);
}
