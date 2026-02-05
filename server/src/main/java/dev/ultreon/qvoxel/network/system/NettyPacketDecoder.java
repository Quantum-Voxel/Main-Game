package dev.ultreon.qvoxel.network.system;

import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.registry.RegistryHandle;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class NettyPacketDecoder extends ByteToMessageDecoder {
    private final RegistryHandle handle;
    private final IConnection<?, ?> connection;

    public NettyPacketDecoder(RegistryHandle handle, IConnection<?, ?> connection) {
        this.handle = handle;
        this.connection = connection;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        connection.decodeAndHandle(msg.readInt(), new PacketIO(msg, handle));
    }
}
