package dev.ultreon.qvoxel.server;

import dev.ultreon.qvoxel.ServerException;

public class ServerOfflineException extends ServerException {
    public ServerOfflineException() {
        super("Server is not running");
    }
}
