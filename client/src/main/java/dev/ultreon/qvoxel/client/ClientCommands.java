package dev.ultreon.qvoxel.client;

import java.util.HashMap;
import java.util.Map;

public class ClientCommands {
    private static final Map<String, CommandExecutor> commands = new HashMap<>();

    public static CommandExecutor get(String cmd) {
        return commands.get(cmd);
    }

    public static void register(String cmd, CommandExecutor executor) {
        commands.put(cmd, executor);
    }
}
