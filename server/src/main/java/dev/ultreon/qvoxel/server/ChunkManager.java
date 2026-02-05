package dev.ultreon.qvoxel.server;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.util.ChunkVec;
import dev.ultreon.qvoxel.world.*;
import dev.ultreon.qvoxel.world.gen.GenerationBarrier;
import dev.ultreon.qvoxel.world.gen.chunk.ChunkGenerator;
import dev.ultreon.qvoxel.world.light.LightingSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;

public class ChunkManager {
    private final @NotNull RegionMap regionMap;
    private final @NotNull ServerWorld world;
    private final @NotNull ChunkGenerator generator;
    private final @NotNull Deque<ChunkVec> unloadQueue = new ArrayDeque<>();
    private final @NotNull Deque<ChunkVec> loadQueue = new ArrayDeque<>();
    private final @NotNull LightingSystem lightingSystem;
    private final @NotNull Map<ChunkVec, CompletableFuture<ServerChunk>> chunkLoadFutures = new HashMap<>();
    private boolean loadingChunk;
    private final Set<ChunkVec> generating = new ConcurrentSkipListSet<>();

    public ChunkManager(@NotNull ServerWorld world, @NotNull ChunkGenerator generator) {
        this.world = world;
        lightingSystem = world.lightingSystem;
        this.generator = generator;
        regionMap = new RegionMap(world.getServer(), this, world);
    }

    public @Nullable ServerChunk getChunk(ChunkVec vec) {
        return regionMap.getRegion(ChunkVec.regionOf(vec.x), ChunkVec.regionOf(vec.y), ChunkVec.regionOf(vec.z))
                .getChunk(ChunkVec.localize(vec.x), ChunkVec.localize(vec.y), ChunkVec.localize(vec.z));
    }

    public @Nullable ServerChunk getLoadedChunk(ChunkVec vec) {
        return regionMap.getRegion(ChunkVec.regionOf(vec.x), ChunkVec.regionOf(vec.y), ChunkVec.regionOf(vec.z))
                .getLoadedChunk(ChunkVec.localize(vec.x), ChunkVec.localize(vec.y), ChunkVec.localize(vec.z));
    }

    public void queueUnload(ChunkVec vec) {
        unloadQueue.add(vec);
    }

    public void queueLoad(ChunkVec vec) {
        loadQueue.add(vec);
    }

    public void tick() {
        while (!unloadQueue.isEmpty()) {
            ChunkVec vec = unloadQueue.pop();
            @Nullable ServerChunk chunk = getChunk(vec);
            if (chunk != null) {
                regionMap.getRegionAt(vec).unloadChunk(vec);
            }
        }

        if (!loadQueue.isEmpty() && !loadingChunk) {
            ChunkVec vec = loadQueue.pop();
            @Nullable ServerChunk chunk = getLoadedChunk(vec);
            if (chunk == null) {
                loadChunkAsync(vec.x, vec.y, vec.z, ChunkLoadTicket.PLAYER, GenerationBarrier.ALL).thenAccept(loadedChunk -> {
                    loadedChunk.tick();
                    loadingChunk = false;
                }).exceptionally(throwable -> {
                    CommonConstants.LOGGER.error("Failed to load chunk at {}", vec, throwable);
                    loadingChunk = false;
                    return null;
                });
            } else {
                chunk.tick();
            }
        }

        regionMap.tick();
    }

    public ServerChunk getChunk(int x, int y, int z, GenerationBarrier barrier) {
        Region region = regionMap.getRegion(new Vector3i(ChunkVec.regionOf(x), ChunkVec.regionOf(y), ChunkVec.regionOf(z)));
        ServerChunk chunk = region.getChunk(ChunkVec.localize(x), ChunkVec.localize(y), ChunkVec.localize(z));
        if (chunk == null) {
            return generate(region, x, y, z, ChunkLoadTicket.PLAYER, barrier).join();
        }
        if (chunk.vec.x != x || chunk.vec.y != y || chunk.vec.z != z)
            CommonConstants.LOGGER.warn("Chunk relativePos mismatch: {} != {}", chunk.vec, new ChunkVec(x, y, z));
        return chunk;
    }

    public @NotNull CompletableFuture<@Nullable ServerChunk> getChunkAsync(int x, int y, int z, GenerationBarrier barrier) {
        Region region = regionMap.getRegion(new Vector3i(ChunkVec.regionOf(x), ChunkVec.regionOf(y), ChunkVec.regionOf(z)));
        ServerChunk chunk = region.getChunk(ChunkVec.localize(x), ChunkVec.localize(y), ChunkVec.localize(z));
        if (chunk == null) {
            return generate(region, x, y, z, ChunkLoadTicket.PLAYER, barrier);
        }
        return CompletableFuture.completedFuture(chunk);
    }

    public @NotNull CompletableFuture<@NotNull ServerChunk> loadChunkAsync(int x, int y, int z, ChunkLoadTicket ticket, GenerationBarrier barrier) {
        Region region = regionMap.getRegion(new Vector3i(ChunkVec.regionOf(x), ChunkVec.regionOf(y), ChunkVec.regionOf(z)));
        ServerChunk chunk = region.getChunk(ChunkVec.localize(x), ChunkVec.localize(y), ChunkVec.localize(z));
        if (chunk == null) {
            return generate(region, x, y, z, ticket, barrier);
        } else {
            chunk.consumeTicket(ticket);
            return CompletableFuture.completedFuture(chunk);
        }
    }

    CompletableFuture<ServerChunk> generate(Region region, int x, int y, int z, ChunkLoadTicket ticket, GenerationBarrier barrier) {
        ChunkVec pos = new ChunkVec(x, y, z);
        CompletableFuture<ServerChunk> future = chunkLoadFutures.get(pos);
        ServerChunk chunk = regionMap.getRegion(ChunkVec.regionOf(x), ChunkVec.regionOf(y), ChunkVec.regionOf(z))
                .getChunk(ChunkVec.localize(x), ChunkVec.localize(y), ChunkVec.localize(z));
        if (Thread.currentThread().getThreadGroup().equals(QuantumServer.GENERATOR_SERVICE)) {
            if (chunk != null)
                return CompletableFuture.completedFuture(chunk);
        }
        if (future != null) return future;

        boolean waitForGenerator = false;
        synchronized (generating) {
            if (generating.contains(pos)) {
                waitForGenerator = true;
            }
            generating.add(pos);
        }

        if (waitForGenerator) {
            long start = System.currentTimeMillis();
            while ((future = chunkLoadFutures.get(pos)) == null) {
                if (!generating.contains(pos))
                    return getChunkAsync(x, y, z, barrier);

                if (System.currentTimeMillis() - start > 10000) {
                    CommonConstants.LOGGER.error("Chunk generation timed out: {}", pos);
                    return CompletableFuture.failedFuture(new RuntimeException("Chunk generation timed out: " + pos));
                }
            }
            return future;
        }

        if (chunk instanceof WorldChunk) return CompletableFuture.completedFuture(chunk);
        if (chunk instanceof BuilderChunk builder) {
            if (builder.currentBarrier.compareTo(barrier) >= 0)
                return CompletableFuture.completedFuture(chunk);
            return startGenerating(x, y, z, ticket, barrier, builder, pos);
        }

        BuilderChunk builder = new BuilderChunk(world, pos, region);
        region.setChunk(ChunkVec.localize(x), ChunkVec.localize(y), ChunkVec.localize(z), builder);
        return startGenerating(x, y, z, ticket, barrier, builder, pos);
    }

    private @NotNull CompletableFuture<ServerChunk> startGenerating(int x, int y, int z, ChunkLoadTicket ticket, GenerationBarrier barrier, BuilderChunk builder, ChunkVec pos) {
        CompletableFuture<ServerChunk> serverChunkCompletableFuture = CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return builder.process(() -> generateSync(x, y, z, ticket, barrier, builder));
                    } catch (Exception e) {
                        CommonConstants.LOGGER.error("Failed to generate chunk at {}", pos, e);
                        throw new RuntimeException(e);
                    } finally {
                        chunkLoadFutures.remove(pos);
                        generating.remove(pos);
                    }
                }, world.getServer().getGeneratorService()
        );
        chunkLoadFutures.put(pos, serverChunkCompletableFuture);
        return serverChunkCompletableFuture;
    }

    private @NotNull ServerChunk generateSync(int x, int y, int z, ChunkLoadTicket ticket, GenerationBarrier barrier, BuilderChunk builder) {
        try {
            // Generate terrain and structures into the builder chunk
            generator.generate(world, builder, barrier, lightingSystem);

            // Build the final server chunk and register it in the region map
            WorldChunk chunk = builder.build();
            regionMap.getRegion(ChunkVec.regionOf(x), ChunkVec.regionOf(y), ChunkVec.regionOf(z))
                    .setChunk(ChunkVec.localize(x), ChunkVec.localize(y), ChunkVec.localize(z), chunk);

            // Consume the ticket so that the chuk won't be unloaded while it is still in use.
            chunk.consumeTicket(ticket);

            if (chunk.vec.x != x || chunk.vec.y != y || chunk.vec.z != z)
                CommonConstants.LOGGER.warn("Chunk position mismatch on generation: {} != {}", chunk.vec, new ChunkVec(x, y, z));
            return chunk;
        } catch (Exception e) {
            CommonConstants.LOGGER.error("Failed to generate chunk {}, {}, {}", x, y, z);
            world.getServer().onChunkError(x, y, z, e);
            throw e;
        } finally {
            builder.currentBarrier = barrier;
        }
    }

    public @Nullable Chunk getChunkOrNull(int x, int y, int z) {
        Region region = regionMap.getRegionOrNull(ChunkVec.regionOf(x), ChunkVec.regionOf(y), ChunkVec.regionOf(z));
        if (region == null) return null;
        return region.getChunkOrNull(ChunkVec.localize(x), ChunkVec.localize(y), ChunkVec.localize(z));
    }

    public @NotNull RegionMap getRegionMap() {
        return regionMap;
    }

    public void close() {
        regionMap.close();
        unloadQueue.clear();
        loadQueue.clear();

        for (CompletableFuture<ServerChunk> value : chunkLoadFutures.values()) {
            value.cancel(true);
        }

        chunkLoadFutures.clear();
    }

    public ServerChunk getChunkAt(int x, int y, int z, GenerationBarrier generationBarrier) {
        int cx = BlockVec.chunkOf(x);
        int cy = BlockVec.chunkOf(y);
        int cz = BlockVec.chunkOf(z);

        ServerChunk chunk = getChunk(cx, cy, cz, generationBarrier);
        if (chunk == null) {
            loadingChunk = true;
            return getChunkAsync(cx, cy, cz, generationBarrier).join();
        } else {
            return chunk;
        }
    }

    public Heightmap getHeightmap(int x, int z, HeightmapType heightmapType) {
        RegionColumn regionColumn = getRegionMap().getRegionColumn(x, z);
        return regionColumn.getHeightmap(heightmapType);
    }

    public Heightmap getHeightmapOrNull(int x, int z, HeightmapType heightmapType) {
        RegionColumn regionColumn = getRegionMap().getRegionColumnOrNull(x, z);
        if (regionColumn == null) return null;
        return regionColumn.getHeightmapOrNull(heightmapType);
    }

    /**
     * Searches for the lowest chunk that contains at least one block.
     * The return value can be null if the lowest chunk is below -4096 in the world,
     * which should never happen in normal circumstances. Mods should modify this if a lower limit is expected.
     */
    public ServerChunk getLowestChunkAt(int x, int z) {
        int cx = BlockVec.chunkOf(x);
        int cz = BlockVec.chunkOf(z);

        int v = (int) Math.ceil(generator.evaluateNoise(x, z));
        if (v < World.SEA_LEVEL) v = World.SEA_LEVEL;
        else CommonConstants.LOGGER.warn("Lowest block at {} {} is above sea level: {}", x, z, v);
        int cy = BlockVec.chunkOf(v);
        while (cy > BlockVec.chunkOf(-4096)) {
            ServerChunk chunk = getChunk(cx, cy, cz, GenerationBarrier.ALL);
            if (chunk != null) {
                int lowestBlock = chunk.getLowestWorldBlockAt(BlockVec.localize(x), BlockVec.localize(z));
                if (lowestBlock >= 0) {
                    chunk = topOut(lowestBlock, cy, chunk, cx, cz);
                    return chunk;
                }
            }
            cy--;
        }
        return null;
    }

    private @Nullable ServerChunk topOut(int highestBlock, int cy, ServerChunk chunk, int cx, int cz) {
        if (highestBlock == World.CHUNK_SIZE) {
            while (cy < cy + BlockVec.chunkOf(4096)) {
                ServerChunk oldChunk = chunk;
                chunk = getChunk(cx, cy, cz, GenerationBarrier.ALL);
                if (chunk != null) {
                    highestBlock = chunk.getFirstBlockFromTop();
                    if (highestBlock == -1) {
                        return oldChunk;
                    } else if (highestBlock < World.CHUNK_SIZE) {
                        break;
                    }
                }
                cy++;
            }
        }
        return chunk;
    }

    public void save() {
        regionMap.save();
    }
}
