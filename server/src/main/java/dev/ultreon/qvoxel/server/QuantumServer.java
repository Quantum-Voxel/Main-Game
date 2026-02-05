/*
 * Copyright 2025. Quinten 'Qubix' Jungblut
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.ultreon.qvoxel.server;

import dev.ultreon.libs.commons.v0.Profiler;
import dev.ultreon.libs.crash.v0.CrashLog;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.PollingExecutorService;
import dev.ultreon.qvoxel.ServerException;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.featureflags.FeatureSet;
import dev.ultreon.qvoxel.network.handler.ClientPacketHandler;
import dev.ultreon.qvoxel.network.handler.ServerPacketHandler;
import dev.ultreon.qvoxel.network.packets.c2s.C2SLoginPacket;
import dev.ultreon.qvoxel.network.packets.s2c.S2CBlockSetPacket;
import dev.ultreon.qvoxel.network.system.IConnection;
import dev.ultreon.qvoxel.registry.RegistryKeys;
import dev.ultreon.qvoxel.resource.ReloadContext;
import dev.ultreon.qvoxel.resource.ResourceManager;
import dev.ultreon.qvoxel.spark.QuantumSparkPlugin;
import dev.ultreon.qvoxel.util.ChunkVec;
import dev.ultreon.qvoxel.util.ExecutorClosedException;
import dev.ultreon.qvoxel.util.JavaRNG;
import dev.ultreon.qvoxel.world.DimensionInfo;
import dev.ultreon.qvoxel.world.gen.biome.Biomes;
import dev.ultreon.qvoxel.world.gen.chunk.*;
import dev.ultreon.qvoxel.world.gen.noise.NoiseConfigs;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

import static dev.ultreon.qvoxel.CommonConstants.id;

/**
 * This is the core server implementation for Quantum Voxel
 */
public abstract class QuantumServer extends PollingExecutorService {
    private static final boolean CHUNK_DEBUG = System.getProperty("quantum.chunk.debug", "false").equals("true");

    /**
     * The target ticks per second (TPS) for the server. This constant represents
     * the desired frequency of server ticks, controlling the rate at which the
     * server updates its state and processes tasks.
     */
    public static final int TPS = 20;

    /**
     * Thread group for the {@linkplain #generatorService world generator service}.
     */
    public static final ThreadGroup GENERATOR_SERVICE = new ThreadGroup("Generator Service");
    private static QuantumServer current;
    private final ServerRegistries registries = new ServerRegistries(this);
    private final NoiseConfigs noiseConfigs = new NoiseConfigs(this);
    private final Biomes biomes = new Biomes(this);
    private final List<AutoCloseable> closeables = new ArrayList<>();
    private final ResourceManager resourceManager;
    private final FeatureSet features;
    private boolean loading;
    private int spawnX;
    private int spawnZ;
    private long lastTick = System.currentTimeMillis();
    private final DimensionManager dimManager = new DimensionManager(this);
    private final WorldStorage storage;
    private final PlayerManager playerManager = new PlayerManager();
    private final ExecutorService generatorService = Executors.newFixedThreadPool(8, r -> {
        Thread thread = new Thread(GENERATOR_SERVICE, r);
        thread.setName("Generator Service");
        thread.setDaemon(false);
        return thread;
    });
    private final ScheduledExecutorService saveService = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("Save Service");
        thread.setDaemon(false);
        return thread;
    });
    private final ScheduledExecutorService refreshService = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("Refresh Service");
        thread.setDaemon(false);
        return thread;
    });
    private boolean shuttingDown;
    private static int currentTps;
    private Thread refreshTask;

    protected QuantumServer(WorldStorage storage, FeatureSet features) {
        super(new Profiler());

        add("Player Manager", playerManager);

        this.features = features;
        current = this;

        this.storage = storage;

        long seed = getSeed();
        JavaRNG.GLOBAL.setSeed(seed);
        CommonConstants.LOGGER.info("Seed: {}", seed);

        setSpawnX(JavaRNG.GLOBAL.nextInt(2000) - 1000);
        setSpawnZ(JavaRNG.GLOBAL.nextInt(2000) - 1000);

        resourceManager = new ResourceManager("data");
        resourceManager.importAll();

        addComponent(registries);

        add("Resource Manager", resourceManager);

        reload();
        loadRegistries();

        add("Dimension Manager", dimManager);
        dimManager.loadWorlds(seed);
        CommonConstants.LOGGER.info("Loaded {} dimensions", dimManager.getWorlds().size());

        add("Biomes", biomes);
        biomes.init();

        loading = true;
    }

    /**
     * Executes the given {@link Runnable} on the server thread and waits for its completion.
     * If the server is offline or the execution fails due to a closed executor, a
     * {@link ServerOfflineException} is thrown.
     *
     * @param runnable the task to execute. It should contain the logic to be run on the server thread.
     *                 Must not be null.
     * @throws ServerOfflineException if the server is not running or the executor has been closed.
     */
    public static void invokeAndWait(Runnable runnable) throws ServerOfflineException {
        try {
            invoke(runnable).join();
        } catch (ExecutorClosedException e) {
            throw new ServerOfflineException();
        } catch (CompletionException e) {
            if (e.getCause() instanceof ExecutorClosedException) {
                throw new ServerOfflineException();
            }
            throw e;
        }
    }

    /**
     * Executes the given {@link Callable} on the server thread and waits for its completion.
     * This method ensures the task is performed on the server thread and blocks until the task
     * completes, returning the result of the computation. If the server is offline or the executor
     * is closed, a {@link ServerOfflineException} is thrown.
     *
     * @param callable the task to be executed, which returns a result on completion. It must not be null.
     * @return the result of the computation performed by the given callable.
     * @throws ServerOfflineException if the server is offline or the executor has been closed.
     */
    public static <T> T invokeAndWait(Callable<T> callable) throws ServerOfflineException {
        try {
            return invoke(callable).join();
        } catch (ExecutorClosedException e) {
            throw new ServerOfflineException();
        } catch (CompletionException e) {
            if (e.getCause() instanceof ExecutorClosedException) {
                throw new ServerOfflineException();
            }
            throw e;
        }
    }

    private void loadRegistries() {
        var chunkGenRegistry = registries.get(RegistryKeys.CHUNK_GENERATOR);
        chunkGenRegistry.register(ChunkGenerator.OVERWORLD, CHUNK_DEBUG ? new DebugGenerator(registries.biomes()) : new OverworldGenerator(registries.biomes()));
        chunkGenRegistry.register(ChunkGenerator.TEST, new TestGenerator(registries.biomes()));
        chunkGenRegistry.register(ChunkGenerator.FLOATING_ISLANDS, new SpaceGenerator(registries.biomes()));

        var dimRegistry = registries.get(RegistryKeys.DIMENSION);

        dimRegistry.register(DimensionInfo.OVERWORLD, new DimensionInfo(
                id("overworld"),
                Optional.empty(),
                ChunkGenerator.OVERWORLD
        ));

        dimRegistry.register(DimensionInfo.TEST, new DimensionInfo(
                id("test"),
                Optional.empty(),
                ChunkGenerator.TEST
        ));

        dimRegistry.register(DimensionInfo.SPACE, new DimensionInfo(
                id("space"),
                Optional.empty(),
                ChunkGenerator.FLOATING_ISLANDS
        ));

        dimManager.load(registries);
    }

    private void reload() {
        ReloadContext context = ReloadContext.create(this, resourceManager);
        registries.stream().forEach(registry -> registry.reload(context));
    }

    /**
     * Retrieves the resource manager associated with the server.
     *
     * @return the ResourceManager instance managing resources for this server
     */
    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    protected void run() {
        saveService.scheduleAtFixedRate(this::save, 0, 5, TimeUnit.MINUTES);
        refreshService.scheduleAtFixedRate(this::refreshChunks, 0, 5, TimeUnit.SECONDS);

        int ticks = 0;
        long lastSecond = System.currentTimeMillis();
        while (!isShutdown()) {
            long now = System.currentTimeMillis();
            if (now - lastTick >= 1000f / TPS) {
                try {
                    runTick();
                } catch (OutOfMemoryError | Exception t) {
                    CommonConstants.LOGGER.error("Failed to run tick", t);
                    CrashLog log = new CrashLog("Failed to run tick", t);
                    crash(log);
                }
                ticks++;
                if (now - lastSecond >= 1000) {
                    lastSecond = now;
                    currentTps = ticks;
                    ticks = 0;
                }
                lastTick = now;
            }
        }

        CommonConstants.LOGGER.info("Shutting down...");
        close();
    }

    private void refreshChunks() {
        for (ServerPlayerEntity player : playerManager) {
            player.refreshChunks();

            if (Thread.interrupted()) {
                break;
            }
        }
    }

    protected void save() {
        try {
            CommonConstants.LOGGER.info("Saving worlds...");
            for (var world : dimManager.getWorlds().values()) {
                world.save();
            }
        } catch (Exception e) {
            CommonConstants.LOGGER.error("Failed to save worlds", e);
        }
    }

    protected void close() {
        if (!isShutdown()) {
            shutdown(() -> {
            });
        }

        refreshTask.interrupt();

        for (AutoCloseable closeable : closeables) {
            try {
                closeable.close();
            } catch (Exception e) {
                CommonConstants.LOGGER.error("Failed to close {}", closeable.getClass().getSimpleName(), e);
            }
        }
    }

    protected void runTick() {
        if (tasks.size() > 100)
            CommonConstants.LOGGER.warn("Server is running {} tasks, this is probably not normal", tasks.size());

        pollAll();
        hostLoad();

        if (loading) {
            for (var player : playerManager) {
                player.tick();
            }
            return;
        }

        for (ServerWorld world : dimManager.getWorlds().values()) {
            world.tick();
        }

        for (var player : playerManager) {
            player.tick();
        }
    }

    protected void hostLoad() {

    }

    protected void done() {
        loading = false;
    }

    public static QuantumServer get() {
        return current;
    }

    public static CompletableFuture<Void> invoke(Runnable runnable) throws ServerOfflineException {
        QuantumServer quantumServer = get();
        if (quantumServer == null) {
            throw new ServerOfflineException();
        }
        return quantumServer.submit(runnable);
    }

    public static <T> CompletableFuture<T> invoke(Callable<T> callable) throws ServerOfflineException {
        QuantumServer quantumServer = get();
        if (quantumServer == null) {
            throw new ServerOfflineException();
        }
        return quantumServer.submit(callable);
    }

    public void onChunkLoad(int loaded, int total, ChunkVec vec) {
        // Nope!
    }

    protected abstract long getSeed();

    @Deprecated
    public ServerWorld getWorld() {
        return dimManager.getWorld(DimensionInfo.OVERWORLD);
    }

    public Biomes getBiomes() {
        return biomes;
    }

    public ServerRegistries getRegistries() {
        return registries;
    }

    @Override
    public void shutdown(Runnable finalizer) {
        onDisconnectMessage("Shutting down integrated server");
        shuttingDown = true;

        super.shutdown(finalizer);

        CompletableFuture<Void> terminationFuture = CompletableFuture.runAsync(() -> {
            try {
                CommonConstants.LOGGER.info("Waiting for generator service to terminate...");
                onDisconnectMessage("Waiting for chunk generator to terminate...");
                if (!generatorService.awaitTermination(15, TimeUnit.SECONDS)) {
                    CommonConstants.LOGGER.error("Generator service did not terminate in time");
                    Runtime.getRuntime().halt(13);
                }
                CommonConstants.LOGGER.info("Generator service terminated");
            } catch (OutOfMemoryError | Exception t) {
                CommonConstants.LOGGER.error("Failed to wait for generator service to terminate", t);
                if (t instanceof InterruptedException)
                    Thread.currentThread().interrupt();
            }
        });
        generatorService.shutdown();
        terminationFuture.join();

        CommonConstants.LOGGER.info("Closing server...");
        onDisconnectMessage("Closing server...");
        dimManager.close();
        playerManager.close();
        registries.close();
        current = null;

        finalizer.run();
        CommonConstants.LOGGER.info("Server closed");
    }

    protected void onDisconnectMessage(String message) {

    }

    public abstract void crash(CrashLog crashLog);

    public NoiseConfigs getNoiseConfigs() {
        return noiseConfigs;
    }

    public <T extends AutoCloseable> T closeOnClose(T noiseInstance) {
        closeables.add(noiseInstance);
        return noiseInstance;
    }

    public WorldStorage getStorage() {
        return storage;
    }

    public GameMode getDefaultGameMode() {
        return GameMode.SURVIVAL;
    }

    public ServerWorld getDefaultWorld() throws ServerException {
        ServerWorld world = dimManager.getWorld(DimensionInfo.OVERWORLD);
        if (world == null) {
            throw new ServerException("Overworld world is not loaded");
        }
        return world;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public abstract boolean isDedicated();

    public ExecutorService getGeneratorService() {
        return generatorService;
    }

    public ServerPlayerEntity placePlayer(C2SLoginPacket packet, IConnection<ServerPacketHandler, ClientPacketHandler> connection) {
        ServerPlayerEntity player = new ServerPlayerEntity(connection, packet.name(), packet.clientRenderDistance(), this);
        connection.setPlayer(player);
        player.setUsername(packet.name());
        player.setGameMode(getDefaultGameMode());
        UUID uuid = UUID.nameUUIDFromBytes("LocalPlayer:".concat(packet.name()).getBytes(StandardCharsets.UTF_8));
        if (playerManager.getPlayer(uuid) != null)
            connection.disconnect("Player with UUID " + uuid + " already exists");
        player.setUuid(uuid);
        try {
            player.setWorld(getDefaultWorld());
        } catch (ServerException e) {
            connection.disconnect("Failed to transfer player to default world:\n" + e.getMessage());
            return null;
        }
        playerManager.addPlayer(player);
        return player;
    }

    public static int getCurrentTps() {
        return currentTps;
    }

    public boolean isShuttingDown() {
        return shuttingDown;
    }

    public void broadcastBlockChange(ServerWorld serverWorld, int x, int y, int z, BlockState state) {
        for (var player : playerManager) {
            if (player.getWorld() == serverWorld) {
                player.connection.send(new S2CBlockSetPacket(x, y, z, state));
            }
        }
    }

    public boolean isServerThread() {
        return Thread.currentThread() == thread;
    }

    public void onSaveError(String formatted, Exception e) {
        CommonConstants.LOGGER.error(formatted, e);
    }

    public void onSpawnAttempt(int attempts) {
        // Nope!
    }

    public FeatureSet getFeatures() {
        return features;
    }

    public int getSpawnX() {
        return spawnX;
    }

    public void setSpawnX(int spawnX) {
        this.spawnX = spawnX;
    }

    public int getSpawnZ() {
        return spawnZ;
    }

    public void setSpawnZ(int spawnZ) {
        this.spawnZ = spawnZ;
    }

    public void setSpawn(int x, int z) {
        setSpawnX(x);
        setSpawnZ(z);
    }

    public void onChunkError(int x, int y, int z, Throwable error) {

    }

    public boolean isSpawnSet() {
        return this.getPlayerManager().getPlayerCount() > 0;
    }

    public Future<?> saveAsync() {
        return saveService.submit(this::save);
    }

    public abstract QuantumSparkPlugin getSparkPlugin();
}
