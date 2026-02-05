package dev.ultreon.qvoxel.client;

import dev.ultreon.qvoxel.CommandSender;

public interface CommandExecutor {
    void execute(CommandSender sender, String[] args);
}
