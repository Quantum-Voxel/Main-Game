package dev.ultreon.qvoxel.network.system;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.network.handler.ClientPacketHandler;
import dev.ultreon.qvoxel.network.handler.ServerPacketHandler;
import dev.ultreon.qvoxel.server.QuantumServer;
import dev.ultreon.qvoxel.server.dedicated.ServerConnection;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

import java.nio.channels.ClosedChannelException;
import java.util.HashMap;
import java.util.Map;

public class NetworkInitializer implements ChannelHandler {
    private final Map<Channel, IConnection<ServerPacketHandler, ClientPacketHandler>> connectionMap = new HashMap<Channel, IConnection<ServerPacketHandler, ClientPacketHandler>>();
    private final QuantumServer server;

    public NetworkInitializer(QuantumServer server) {
        this.server = server;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        if (connectionMap.containsKey(channel))
            return;

        ServerConnection serverConnection = new ServerConnection(server, !server.isDedicated());
        serverConnection.setChannel(channel);
        serverConnection.init(channel.pipeline(), server.getRegistries());
        this.connectionMap.put(channel, serverConnection);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        var connection = connectionMap.remove(ctx.channel());
        connection.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof ClosedChannelException) return;

        Channel key = ctx.channel();
        var connection = connectionMap.get(key);
        CommonConstants.LOGGER.error("Internal error handling network channel for {}", key.remoteAddress(), cause);
        connection.disconnect(500, "Internal error:\n" + cause.getMessage());
    }
}
