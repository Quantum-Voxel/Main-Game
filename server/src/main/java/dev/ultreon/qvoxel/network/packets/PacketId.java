package dev.ultreon.qvoxel.network.packets;

import dev.ultreon.libs.commons.v0.Identifier;

public record PacketId<T>(Identifier name, Class<T> packetClass) {
    public PacketId(String name, Class<T> packetClass) {
        this(new Identifier(name), packetClass);
    }
}
