package dev.ultreon.qvoxel.network.system;

import dev.ultreon.qvoxel.network.Connection;
import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.registry.RegistryHandle;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class NettyPacketEncoder extends MessageToByteEncoder<Packet<?>> {
    private final RegistryHandle handle;
    private final Connection<?, ?> connection;

    public NettyPacketEncoder(RegistryHandle handle, Connection<?, ?> connection) {
        this.handle = handle;
        this.connection = connection;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet<?> msg, ByteBuf out) throws Exception {
        PacketIO packetIO = new PacketIO(out, handle);
        connection.encode((Packet) msg, packetIO);
    }
}
