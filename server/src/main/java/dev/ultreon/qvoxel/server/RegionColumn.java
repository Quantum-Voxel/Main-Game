package dev.ultreon.qvoxel.server;

import dev.ultreon.qvoxel.world.Heightmap;
import dev.ultreon.qvoxel.world.HeightmapType;
import dev.ultreon.ubo.DataIo;
import dev.ultreon.ubo.types.MapType;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

import static dev.ultreon.qvoxel.world.World.REGION_SIZE;

public class RegionColumn {
    private final Int2ObjectMap<Region> regionSections = new Int2ObjectOpenHashMap<>();
    private final Map<HeightmapType, Heightmap> heightmaps = new EnumMap<>(HeightmapType.class);
    private final QuantumServer server;
    private final RegionMap regionMap;
    private final ServerWorld world;
    private final ChunkManager chunkManager;
    private final int x, z;

    public RegionColumn(QuantumServer server, RegionMap regionMap, ServerWorld world, ChunkManager chunkManager, int x, int z) {
        this.server = server;
        this.regionMap = regionMap;
        this.world = world;
        this.chunkManager = chunkManager;
        this.x = x;
        this.z = z;
    }

    public Region getRegion(int index) {
        return regionSections.computeIfAbsent(index, i -> {
            try {
                return new Region(server, this, chunkManager, x, i, z, world);
            } catch (IOException e) {
                throw new StorageException("Region %s,%s,%s failed to load".formatted(x, i, z), e);
            }
        });
    }

    public void setRegion(int index, Region region) {
        regionSections.put(index, region);
    }

    public void discardRegion(int index) {
        regionSections.remove(index);
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public void save() {
        for (Region region : regionSections.values()) {
            region.save();
        }
        MapType map = new MapType();
        for (Map.Entry<HeightmapType, Heightmap> entry : heightmaps.entrySet()) {
            HeightmapType type = entry.getKey();
            Heightmap heightmap = entry.getValue();
            map.putIntArray(type.name(), heightmap.save());
        }
        WorldStorage storage = world.getStorage();
        Path path = storage.regionFile(x, 0, z).resolveSibling("heightmaps.ubo");
        if (Files.notExists(path.getParent())) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                world.getServer().onSaveError("Failed to create heightmaps directory", e);
                return;
            }
        }
        try {
            DataIo.write(map, path);
        } catch (IOException e) {
            world.getServer().onSaveError("Failed to save heightmaps for region %d:%d".formatted(x, z), e);
        }
    }

    public void tick() {
        for (Region region : regionSections.values()) {
            region.tick();
        }
    }

    public void close() {
        for (Region region : regionSections.values()) {
            region.close();
        }
        regionSections.clear();
    }

    public @Nullable Region getRegionOrNull(int y) {
        return regionSections.get(y);
    }

    public void remove(int y) {
        regionSections.remove(y);
        if (regionSections.isEmpty()) {
            close();
            regionMap.removeColumn(x, z);
        }
    }

    public Heightmap getHeightmap(HeightmapType heightmapType) {
        synchronized (heightmaps) {
            return heightmaps.computeIfAbsent(heightmapType, _ -> new Heightmap(REGION_SIZE));
        }
    }

    public Heightmap getHeightmapOrNull(HeightmapType heightmapType) {
        return heightmaps.get(heightmapType);
    }
}
