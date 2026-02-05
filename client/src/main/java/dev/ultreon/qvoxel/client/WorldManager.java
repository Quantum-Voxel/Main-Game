package dev.ultreon.qvoxel.client;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.server.WorldSaveInfo;
import dev.ultreon.qvoxel.server.WorldStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class WorldManager {
    public List<WorldStorage> search(String query, boolean favoritesOnly, boolean deleted) {
        List<WorldStorage> worlds = new ArrayList<>();
        Path worldsPath = Path.of("worlds");
        if (!Files.exists(worldsPath)) {
            return worlds;
        }
        try (Stream<Path> list = Files.list(worldsPath)) {
            for (Path worldPath : list.toList()) {
                if (!Files.isDirectory(worldPath) || worldPath.getFileName().toString().startsWith(".")) {
                    continue;
                }
                matchWorld(query, favoritesOnly, deleted, worldPath, worlds);
            }
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to list worlds", e);
        }
        return worlds;
    }

    private static void matchWorld(String query, boolean favoritesOnly, boolean deleted, Path worldPath, List<WorldStorage> worlds) {
        try {
            WorldStorage world = new WorldStorage(worldPath);
            if (checkQuery(query, world.getName())) return;
            WorldSaveInfo worldSaveInfo = world.loadInfo();
            if (favoritesOnly && !worldSaveInfo.isFavorite()) return;
            if (deleted != worldSaveInfo.isDeleted()) return;
            worlds.add(world);
        } catch (Exception e) {
            CommonConstants.LOGGER.error("Failed to load world info for {}", worldPath.getFileName(), e);
        }
    }

    private static boolean checkQuery(String query, String worldPath) {
        for (String keyword : query.split(" ")) {
            if (!worldPath.toLowerCase().contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
