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

package dev.ultreon.qvoxel.client;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.activity.ActivityType;
import dev.ultreon.gameprovider.quantum.PlatformOS;
import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.libs.commons.v0.Profiler;
import dev.ultreon.libs.crash.v0.ApplicationCrash;
import dev.ultreon.libs.crash.v0.CrashCategory;
import dev.ultreon.libs.crash.v0.CrashLog;
import dev.ultreon.libs.translations.v1.Language;
import dev.ultreon.libs.translations.v1.LanguageManager;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.CommonInit;
import dev.ultreon.qvoxel.PollingExecutorService;
import dev.ultreon.qvoxel.block.Block;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.client.debug.ImGuiOverlay;
import dev.ultreon.qvoxel.client.framebuffer.Framebuffer;
import dev.ultreon.qvoxel.client.gui.*;
import dev.ultreon.qvoxel.client.gui.screen.*;
import dev.ultreon.qvoxel.client.input.Mouse;
import dev.ultreon.qvoxel.client.model.*;
import dev.ultreon.qvoxel.client.model.json.JsonModel;
import dev.ultreon.qvoxel.client.network.ClientConnection;
import dev.ultreon.qvoxel.client.network.LoginClientPacketHandlerImpl;
import dev.ultreon.qvoxel.client.particle.ParticleEmitters;
import dev.ultreon.qvoxel.client.particle.ParticleSystem;
import dev.ultreon.qvoxel.client.registry.ClientSyncRegistries;
import dev.ultreon.qvoxel.client.registry.SoundRegistry;
import dev.ultreon.qvoxel.client.render.*;
import dev.ultreon.qvoxel.client.sound.Sound;
import dev.ultreon.qvoxel.client.sound.SoundSource;
import dev.ultreon.qvoxel.client.sound.SoundSystem;
import dev.ultreon.qvoxel.client.spark.QuantumClientSparkPlugin;
import dev.ultreon.qvoxel.client.texture.AtlasContainer;
import dev.ultreon.qvoxel.client.texture.TextureAtlas;
import dev.ultreon.qvoxel.client.texture.TextureFormat;
import dev.ultreon.qvoxel.client.texture.TextureManager;
import dev.ultreon.qvoxel.client.world.*;
import dev.ultreon.qvoxel.featureflags.FeatureSet;
import dev.ultreon.qvoxel.item.BlockItem;
import dev.ultreon.qvoxel.item.Item;
import dev.ultreon.qvoxel.item.Items;
import dev.ultreon.qvoxel.menu.ContainerMenu;
import dev.ultreon.qvoxel.network.Connection;
import dev.ultreon.qvoxel.network.packets.c2s.C2SLoginPacket;
import dev.ultreon.qvoxel.network.system.DevFlag;
import dev.ultreon.qvoxel.network.system.DeveloperMode;
import dev.ultreon.qvoxel.network.system.PacketStages;
import dev.ultreon.qvoxel.registry.Registries;
import dev.ultreon.qvoxel.resource.ReloadContext;
import dev.ultreon.qvoxel.resource.Resource;
import dev.ultreon.qvoxel.resource.ResourceManager;
import dev.ultreon.qvoxel.server.*;
import dev.ultreon.qvoxel.sound.SoundEvent;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.world.World;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.local.LocalIoHandler;
import io.netty.channel.nio.NioIoHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.assimp.AILogStream;
import org.lwjgl.assimp.Assimp;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;
import org.lwjgl.sdl.*;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static dev.ultreon.qvoxel.CommonConstants.id;

/**
 * Quantum Voxel's main class.
 */
public final class QuantumClient extends PollingExecutorService implements AutoCloseable {
    /**
     * A shared {@code EventLoopGroup} instance used to handle remote communication
     * tasks within the client. This group is configured to use a number of threads
     * matching the available processors as an NIO-based I/O handler.
     */
    public static final EventLoopGroup REMOTE_EVENT_GROUP = new MultiThreadIoEventLoopGroup(Runtime.getRuntime().availableProcessors(), NioIoHandler.newFactory());

    /**
     * A shared {@code EventLoopGroup} instance used to handle remote communication
     * tasks within the client. This group is configured to use a number of threads
     * matching the available processors as an I/O handler for local channels.
     *
     * @see Connection#isSingleplayer()
     */
    public static final EventLoopGroup LOCAL_EVENT_GROUP = new MultiThreadIoEventLoopGroup(Math.max(Runtime.getRuntime().availableProcessors() / 2, 1), LocalIoHandler.newFactory());
    private static final int MAX_WIDTH = 960;
    private static final int MAX_HEIGHT = 480;
    private static final long TICK_NANOS = 1000000000L / QuantumServer.TPS;
    private static final Logger SDL_LOGGER = LoggerFactory.getLogger("SDL");
    private static final Logger ASSIMP_LOGGER = LoggerFactory.getLogger("Assimp");
    private static final Logger GL_LOGGER = LoggerFactory.getLogger("OpenGL");
    private static final Logger GL_API_LOGGER = LoggerFactory.getLogger("OpenGL:API");
    private static final Logger GL_APP_LOGGER = LoggerFactory.getLogger("OpenGL:Application");
    private static final Logger GL_SC_LOGGER = LoggerFactory.getLogger("OpenGL:ShaderCompiler");
    private static final Logger GL_TP_LOGGER = LoggerFactory.getLogger("OpenGL:ThirdParty");
    private static final Logger GL_WS_LOGGER = LoggerFactory.getLogger("OpenGL:WindowSystem");

    private static int fps = 0;
    private static QuantumClient instance;
    private static final List<Runnable> onClose = new ArrayList<>();

    private final Window window;
    private final QuantumClientSparkPlugin sparkPlugin = new QuantumClientSparkPlugin();
    public final ResourceManager resourceManager = new ResourceManager("assets");
    private GuiRenderer guiRenderer;
    private final OverlayManager overlayManager = new OverlayManager();
    public final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(8);
    public final List<ClientPlayerEntity> players = new CopyOnWriteArrayList<>();
    public final FontRenderer font;
    public final FontRenderer smallFont;
    private final AtlasContainer atlasContainer;
    public ModelLoader modelLoader;
    public final SoundSystem soundSystem;
    public final Shaders shaders = new Shaders();
    public NotificationOverlay notifications;
    private final AILogStream logStream;
    public boolean mouseEjected;
    @Nullable
    private ClientWorld world;
    public TextureAtlas blockTextureAtlas;
    public TextureAtlas itemTextureAtlas;
    public TextureAtlas particleTextureAtlas;
    public ClientSyncRegistries registries = new ClientSyncRegistries(this);
    public int renderDistance = 256;
    public boolean diagonalFontShadow = false;
    public boolean hideHud;
    public ItemRenderer itemRenderer = new ItemRenderer(this);
    public long deltaTime;
    public ParticleSystem particleSystem = new ParticleSystem(this);
    public ExecutorService modelExecutor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(), r -> new Thread(r, "Model Executor")
    );
    public Framebuffer targetFbo;
    private boolean running = true;
    private int scaledWidth;
    private int scaledHeight;
    private int scale = 0;
    private int selectedScale;
    private long lastTime;
    private int frames;
    private int tickFrames;
    private float partialTick;
    private long frameId;
    private final TextureManager textureManager;
    private float yaw;
    private float pitch;
    private long lastTickTime;
    private final Vector3d tmpV3d = new Vector3d();
    private long accumulatorNanos;
    private final ModelManager modelManager = new ModelManager(this);
    private final BlockVec tmpBV = new BlockVec();
    private float bobStrength = 0.03f;
    private float swayStrength = 0.01f;
    private IntegratedServer integratedServer;
    private final Stack<Screen> screenStack = new Stack<>();
    private boolean disconnecting;
    private final Object disconnectLock = new Object();
    private final RenderBufferSource renderBuffers = new RenderBufferSource();
    private int breakCooldown;
    private int useCooldown;
    private boolean breaking;
    private boolean using;
    private final DebugInfoRenderer debugInfoRenderer = new DebugInfoRenderer(this);
    private final SoundRegistry soundRegistry = new SoundRegistry();
    private long lastRenderTime;
    private final List<AutoCloseable> closeables = new ArrayList<>();
    private GraphicsMode currentGraphicsMode = GraphicsMode.DEFAULT;
    private final long startTimeMillis = System.currentTimeMillis();
    private int desiredWidth = -1, desiredHeight = -1;
    private final WorldManager worldManager = new WorldManager();
    private boolean wasJumping;
    private long lastJump;
    private final List<Marker> markers = new CopyOnWriteArrayList<>();
    private final SequencedMap<Integer, Mouse> mice = new LinkedHashMap<>();
    private final SequencedMap<Integer, Keyboard> keyboards = new LinkedHashMap<>();
    private Mouse mainMouse;
    private final Clipboard clipboard = new Clipboard();
    private final LoadingOverlay loadingOverlay = new LoadingOverlay(this);
    private boolean debugKey;
    private Core core;

    @SuppressWarnings("D")
    public QuantumClient() {
        super(Thread.currentThread(), new Profiler());
        instance = this;

        FabricLoader.getInstance().invokeEntrypoints("client", ClientModInitializer.class, ClientModInitializer::onInitializeClient);
        FabricLoader.getInstance().invokeEntrypoints("main", ModInitializer.class, ModInitializer::onInitialize);

        connectDiscordIPC();

        logStream = AILogStream.calloc();
        logStream.set((message, user) -> {
            String msg = MemoryUtil.memUTF8(message);
            String[] split = msg.split(": ", 2);
            String[] split1 = split[0].split(",");
            switch (split1[0]) {
                case "Debug" -> ASSIMP_LOGGER.debug(split[1]);
                case "Info" -> ASSIMP_LOGGER.info(split[1]);
                case "Warn" -> ASSIMP_LOGGER.warn(split[1]);
                case "Error" -> ASSIMP_LOGGER.error(split[1]);
                default -> ASSIMP_LOGGER.trace(split[1]);
            }
        }, BufferUtils.createByteBuffer(0));
        Assimp.aiAttachLogStream(logStream);
        Assimp.aiEnableVerboseLogging(true);

        // Check for RenderDoc
        if (System.getProperty("renderdoc.path") != null) {
            String property = System.getProperty("renderdoc.path");
            if (property.endsWith(".dll")) {
                System.load(property);
            } else if (property.endsWith(".so")) {
                if (property.startsWith("lib"))
                    System.loadLibrary(property.substring(3, property.length() - 3));
                else System.loadLibrary(property.substring(0, property.length() - 3));
            } else if (property.endsWith(".dylib")) {
                if (property.startsWith("lib"))
                    System.loadLibrary(property.substring(3, property.length() - 3));
                else System.loadLibrary(property.substring(0, property.length() - 6));
            } else {
                System.loadLibrary(property);
            }
        }

        window = new Window(this, 1280, 640, "Quantum Voxel v" + FabricLoader.getInstance().getModContainer(CommonConstants.NAMESPACE).map(v -> v.getMetadata().getVersion().getFriendlyString()).orElse("UNKNOWN"));
        try {
//            List<Path> rootPaths = FabricLoader.getInstance().getModContainer(CommonConstants.NAMESPACE).orElseThrow().getRootPaths();
//            for (Path rootPath : rootPaths) {
//                CommonConstants.LOGGER.info("Importing resources from {}", rootPath);
//                resourceManager.importPackage(rootPath);
//            }
            if (false) {
                throw new IOException("Failed to import resources");
            }
            throw new NoSuchElementException("Failed to import resources, no root paths found");
        } catch (NoSuchElementException e) {
            CommonConstants.LOGGER.error("Failed to import resources", e);
            try {
                URI uri = QuantumClient.class.getProtectionDomain().getCodeSource().getLocation().toURI();
                CommonConstants.LOGGER.info("Importing resources from {}", uri);
                resourceManager.importPackage(uri);

                for (String classPath : System.getProperty("java.class.path").split(System.getProperty("path.separator"))) {
                    CommonConstants.LOGGER.info("Importing resources from {}", classPath);
                    try {
                        resourceManager.importPackage(Path.of(classPath));
                    } catch (Exception e1) {
                        CommonConstants.LOGGER.error("Failed to import resources from {}", classPath, e1);
                    }
                }
            } catch (IOException | URISyntaxException ex) {
                CommonConstants.LOGGER.error("Failed to import resources", ex);
                System.exit(1);
            }
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to import resources");
            System.exit(1);
        }

        try {
            resourceManager.discover();
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to discover resources", e);
            System.exit(1);
        }

        soundSystem = new SoundSystem();
        CommonInit.init();

        ParticleEmitters.init(particleSystem);

        for (SoundEvent soundEvent : Registries.SOUND_EVENT.values()) {
            soundRegistry.register(soundEvent);
        }

        if (window.getCapabilities().OpenGL43) {
            GL_LOGGER.info("Enabling GL Debug messages");
            GL43.glDebugMessageCallback((source, type, id, severity, length, message, userParam) -> {
                switch (severity) {
                    case GL43.GL_DEBUG_SEVERITY_HIGH -> findLogger(source).error(MemoryUtil.memUTF8(message, length));
                    case GL43.GL_DEBUG_SEVERITY_MEDIUM -> findLogger(source).warn(MemoryUtil.memUTF8(message, length));
                    case GL43.GL_DEBUG_SEVERITY_LOW -> findLogger(source).info(MemoryUtil.memUTF8(message, length));
                    case GL43.GL_DEBUG_SEVERITY_NOTIFICATION ->
                            findLogger(source).debug(MemoryUtil.memUTF8(message, length));
                    default -> findLogger(source).trace(MemoryUtil.memUTF8(message));
                }
            }, 0);
        }

        font = new FontRenderer(id("font/quantium.otf"), 9, 11);
        smallFont = new FontRenderer(id("font/quantium_small.otf"), 5, 6);
        guiRenderer = new GuiRenderer(font);
        textureManager = new TextureManager();
        Resource resource = resourceManager.getResource(id("lang/en_us.json"));
        if (resource == null) {
            CommonConstants.LOGGER.error("Failed to load language file");
            System.exit(1);
        }
        try (Reader reader = resource.openReader()) {
            LanguageManager.INSTANCE.load(Locale.of("en", "us"), id("en_us"), reader);
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to load language file", e);
            System.exit(1);
        }

        addComponent(atlasContainer = new AtlasContainer());

        Overlays.init(overlayManager);
        add("Overlays", overlayManager);
        add("Debug Info", debugInfoRenderer);
        add("Sound Registry", soundRegistry);
        add("Particle System", particleSystem);
        add("Texture Manager", textureManager);
        add("Font Renderer", font);
        add("Render Buffers", renderBuffers);
        add("GUI Renderer", guiRenderer);

        notifications = Overlays.NOTIFICATIONS;

        ImGuiOverlay.preInitImGui();
        ImGuiOverlay.setupImGui();

        onResize(window.getWidth(), window.getHeight());

        ClientCommands.register("sparkc", (sender, args) -> {
            sparkPlugin.executeCommand(args);
        });
        DeveloperMode.devPipe = (tag, message) -> {
            switch (tag) {
                case "NetLog" -> {
                    // Implement Network logging here
                }
                case "ImGui" -> {
                    try {
                        ImGuiOverlay.setShowingImGui(Boolean.parseBoolean(message));
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            }
        };

        guiRenderer = new GuiRenderer(font);


        loadingOverlay.registerReloadProcess("textures", textureManager);
        loadingOverlay.registerReloadProcess("models", modelManager);
        loadingOverlay.registerReloadProcess("shaders", shaders);
        loadingOverlay.registerReloadProcess("render-types", RenderType::reloadAll);
        loadingOverlay.registerReloadProcess("block-render-types", BlockRenderTypeRegistry::reload);

        reloadResources().thenRunAsync(() -> {
            if (DeveloperMode.enabled && DeveloperMode.isDevFlagEnabled(DevFlag.ImGui)) {
                ImGuiOverlay.setShowingImGui(true);
            }

            sparkPlugin.enable();

            showScreen(new TitleScreen());
        }, this);
    }

    private void connectDiscordIPC() {
        try (CreateParams params = new CreateParams()) {
            params.setClientID(1179401719902384138L);
            params.setFlags(CreateParams.getDefaultFlags());

            core = new Core(params);

            if (core.overlayManager().isEnabled()) {
                boolean locked = core.overlayManager().isLocked();
                core.overlayManager().setLocked(!locked, result -> {
                    boolean newLocked = core.overlayManager().isLocked();
                });
            }
            try (Activity activity = new Activity()) {
                activity.setDetails("Loading");
                activity.setType(ActivityType.PLAYING);
                activity.timestamps().setStart(Instant.ofEpochMilli(startTimeMillis));
                activity.assets().setLargeImage("icon");
                activity.assets().setLargeText(FabricLoader.getInstance().getRawGameVersion());

                core.activityManager().updateActivity(activity);
            }
        } catch (Exception e) {
            CommonConstants.LOGGER.error("Failed!", e);
        }
    }

    private Logger findLogger(int source) {
        return switch (source) {
            case GL43.GL_DEBUG_SOURCE_API -> GL_API_LOGGER;
            case GL43.GL_DEBUG_SOURCE_APPLICATION -> GL_APP_LOGGER;
            case GL43.GL_DEBUG_SOURCE_SHADER_COMPILER -> GL_SC_LOGGER;
            case GL43.GL_DEBUG_SOURCE_THIRD_PARTY -> GL_TP_LOGGER;
            case GL43.GL_DEBUG_SOURCE_WINDOW_SYSTEM -> GL_WS_LOGGER;
            default -> GL_LOGGER;
        };
    }

    public static void crash(CrashLog log) {
        ApplicationCrash crash = log.createCrash();
        crash(crash);
    }

    public static boolean isRenderThread() {
        return Thread.currentThread() == QuantumClient.get().thread;
    }

    public static void setCrashHook(CrashHook o) {

    }

    public static void onClose(Runnable destroy) {
        onClose.add(destroy);
    }

    private static void uncaughtException(Thread thread, Throwable throwable) {
        CrashLog log = new CrashLog("Uncaught exception in thread \"" + thread.getName() + "\"", throwable);
        CrashCategory category = new CrashCategory("Thread Info");
        category.add("Thread Name", thread.getName());
        category.add("Thread ID", thread.threadId());
        category.add("Thread State", thread.getState());
        category.add("Thread Priority", thread.getPriority());
        log.addCategory(category);

        CrashCategory groupCategory = new CrashCategory("Thread Group Info");
        groupCategory.add("Thread Group Name", thread.getThreadGroup().getName());
        groupCategory.add("Thread Group Parent", thread.getThreadGroup().getParent());
        groupCategory.add("Thread Group Active Count", thread.getThreadGroup().activeCount());
        log.addCategory(groupCategory);
    }

    public static Path getGameDir() {
        try {
            return FabricLoader.getInstance().getGameDir();
        } catch (IllegalStateException e) {
            return Path.of(".");
        }
    }

    @Deprecated
    public WorldStorage createNewWorld(WorldSaveInfo info) throws StorageException {
        return createNewWorld(info, Path.of("worlds/world"));
    }

    public WorldStorage createNewWorld(WorldSaveInfo info, Path path) throws StorageException {
        WorldStorage storage = new WorldStorage(path);
        try {
            storage.saveInfo(info);
        } catch (IOException e) {
            throw new StorageException("Failed to save world info", e);
        }
        return storage;
    }

    public void openWorld(WorldStorage storage, FeatureSet features) {
        CommonConstants.LOGGER.info("Opening world: {}", storage.getName());

        if (getWorld() != null) {
            CommonConstants.LOGGER.warn("Tried to open world while already in one");
            return;
        }
        showScreen(new LoadingScreen("Loading World", "Starting up server connection..."));
        integratedServer = new IntegratedServer(this, storage, features);

        QuantumClient.invoke(() -> {
            Iterator<Mouse> miceIterator = mice.sequencedValues().iterator();
            Iterator<Keyboard> keyboardIterator = keyboards.sequencedValues().iterator();
            for (int count = 0; count < Math.max(availablePlayers(), 1); count++) {
                Mouse mouse = miceIterator.hasNext() ? miceIterator.next() : null;
                Keyboard keyboard = keyboardIterator.hasNext() ? keyboardIterator.next() : null;
                ClientPlayerEntity player = newPlayer("Player" + count);
                player.keyboard = keyboard;
                player.mouse = mouse;
                CompletableFuture<ClientConnection> connect = integratedServer.connect(player);
                connect.thenAcceptAsync(connection1 -> CommonConstants.LOGGER.info("Connected to {}", connection1.getChannel().remoteAddress()));
            }
        }).exceptionally(throwable -> {
            CommonConstants.LOGGER.error("Failed to open world:", throwable);
            quitGameThen(() -> {
                clearGuiLayers();
                showScreen(new TitleScreen());
            });
            return null;
        });
    }

    private int availablePlayers() {
        return Math.min(mice.size(), keyboards.size());
    }

    private @NotNull ClientPlayerEntity newPlayer(String username) {
        ClientPlayerEntity player;
        List<ClientPlayerEntity> players = this.players;
        int newPlayerCount = players.size() + 1;
        int widthEach = window.getWidth() / newPlayerCount;
        int x = resizePlayerViews();
        int viewWidth = widthEach - 4;
        int viewHeight = window.getHeight();
        player = new ClientPlayerEntity(new Framebuffer(viewWidth, viewHeight, TextureFormat.RGB8, true), username, null);
        player.playerViewX = x + 2;
        WorldRenderer worldRenderer = new WorldRenderer(getGraphicsMode(), player, viewWidth, viewHeight);
        player.setWorldRenderer(worldRenderer);
        players.add(player);
        return player;
    }

    private int resizePlayerViews() {
        int size = players.size() + 1;
        int width = window.getWidth() / size;
        int x = 0;
        for (ClientPlayerEntity oldPlayer : players) {
            oldPlayer.resizePlayerView(width - 4, window.getHeight());
            oldPlayer.playerViewX = x + 2;
            x += width;
        }
        return x;
    }

    public void connectToServer(String uri) {
        pushGuiLayer(new MessageScreen(Language.translate("quantum.screen.connecting.server"), ""));
        CommonConstants.LOGGER.info("Connecting to server {}", uri);

        ClientPlayerEntity player = newPlayer("Player1");
        var result = ClientConnection.connectToServer(uri, player);
        result.thenApplyAsync(connection -> {
            player.connection = connection;
            connection.moveTo(PacketStages.LOGIN.get(), new LoginClientPacketHandlerImpl(connection, player));
            connection.send(new C2SLoginPacket("Player" + new Random().nextInt(1000, 10000), renderDistance));
            return connection;
        }, this).exceptionallyAsync((failure) -> {
            if (failure == null) failure = new IOException("Generic I/O error");
            CommonConstants.LOGGER.error("Failed to connect to server", failure);
            showScreen(new TitleScreen());
            pushGuiLayer(new DisconnectScreen("Failed to connect to server:\n" + failure.getMessage(), false));
            close();
            return null;
        }, this);
    }

    public static QuantumClient get() {
        return instance;
    }

    public static CompletableFuture<Void> invoke(Runnable runnable) {
        return get().submit(runnable);
    }

    public static <T> CompletableFuture<T> invoke(Callable<T> callable) {
        return get().submit(callable);
    }

    public static <T> CompletableFuture<T> invoke(Supplier<T> supplier) {
        return get().submit(supplier::get);
    }

    public static void invokeAndWait(Runnable runnable) {
        if (Thread.currentThread() == get().thread) {
            runnable.run();
            return;
        }
        invoke(runnable).join();
    }

    public static <T> T invokeAndWait(Callable<T> callable) {
        return invoke(callable).join();
    }

    public static <T> T invokeAndWait(Supplier<T> supplier) {
        return invoke(supplier).join();
    }

    public void mainloop() {
        while (running) {
            int error = GL11.glGetError();
            if (error != GL11.GL_NO_ERROR)
                CommonConstants.LOGGER.warn("GL Error: {}", GLUtils.getErrorName(error));

            window.update();
            frameId++;
            frames++;
            if (System.currentTimeMillis() - lastTime >= 1000) {
                fps = frames;
                frames = 0;
                lastTime = System.currentTimeMillis();
            }

            if (frameId == 2) {
                window.show();
            }


            pollAll();

            if (desiredWidth != -1 && desiredHeight != -1) {
                onResize(desiredWidth, desiredHeight);
                desiredWidth = -1;
                desiredHeight = -1;
            }

            runTick();

            render();

            if (Framebuffer.isAnyBound()) {
                throw new IllegalStateException("Frame buffer is bound");
            }

            window.flip();
        }
    }

    @ApiStatus.Internal
    public void close() {
        if (!isRenderThread()) throw new GLException(getClass().getSimpleName() + "#close called from illegal thread!");

        if (logStream != null) {
            logStream.free();
        }

        core.close();

        sparkPlugin.disable();

        window.delete();

        safeClose(shaders);

        // Stop ImGui overlay
        ImGuiOverlay.delete();

        // Close registered auto-close objects
        autoClose();

        String closingClientMsg = "Closing client";
        if (DeveloperMode.enabled)
            CommonConstants.LOGGER.info(closingClientMsg, new Throwable("Stacktrace for debugging purposes"));
        else CommonConstants.LOGGER.info(closingClientMsg);

        // Shutdown integrated server
        if (integratedServer != null) {
            CompletableFuture<Void> shutdownFuture = new CompletableFuture<>();
            integratedServer.shutdown(() -> shutdownFuture.complete(null));
            shutdownFuture.join();
            integratedServer = null;
        }

        // Close connection if connected
        for (ClientPlayerEntity player : players) {
            CommonConstants.LOGGER.info("Closing connection to server");
            player.close();
            player.connection.close();
            player.connection = null;
        }

        safeClose(getWorld());

        CommonConstants.LOGGER.info(closingClientMsg);
        safeClose(textureManager);
        safeClose(guiRenderer);
        safeClose(font);
        safeClose(renderBuffers);
        safeClose(Tessellator.getInstance());

        // Terminate GLFW and OpenGL
        SDLInit.SDL_Quit();
        GL.destroy();

        // Unset instance
        shutdown(() -> {
            // Nope
        });

        CommonConstants.LOGGER.info("Closed client");

        // Exit the program, since we are done here
        System.exit(0);
    }

    private void safeClose(@Nullable AutoCloseable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (Exception e) {
            if (DeveloperMode.enabled)
                CommonConstants.LOGGER.error("Failed to close {}", closeable, e);
        }
    }

    private void safeClose(@Nullable GLObject glObject) {
        if (glObject == null) return;
        try {
            glObject.delete();
        } catch (Exception e) {
            CommonConstants.LOGGER.error("Failed to delete {}", glObject, e);
        }
    }

    private void autoClose() {
        for (var closeable : closeables) {
            try {
                closeable.close();
            } catch (Exception e) {
                CommonConstants.LOGGER.error("Failed to close {}", closeable, e);
            }
        }

        for (Runnable runnable : onClose) {
            runnable.run();
        }
    }

    private void runTick() {
        long now = System.nanoTime();
        if (lastTickTime == 0L) {
            lastTickTime = now;
            partialTick = 0f;
            return;
        }

        long delta = now - lastTickTime;
        lastTickTime = now;

        // Accumulate elapsed time while not paused
        accumulatorNanos += delta;

        // Run fixed updates
        while (accumulatorNanos >= TICK_NANOS) {
            tick();
            accumulatorNanos -= TICK_NANOS;
        }

        // Interpolation factor for rendering
        partialTick = (float) Math.min(1.0, (double) accumulatorNanos / (double) TICK_NANOS);
    }

    private boolean isPaused() {
        return screenStack.stream().anyMatch(Screen::doesPauseGame) || window.shouldClose();
    }

    private void tick() {
        screenStack.forEach(Screen::tick);

        soundSystem.update();

        for (Marker marker : List.copyOf(markers)) {
            if (marker.tick())
                markers.remove(marker);
        }

        ClientWorld currentWorld = world;
        if (currentWorld == null) return;
        for (ClientPlayerEntity currentPlayer : players) {
            if (currentPlayer.connection != null) {
                currentPlayer.connection.tick();
            }

            Keyboard keyboard = currentPlayer.keyboard;
            if (keyboard != null) {
                currentPlayer.forward = keyboard.isKeyPressed(SDLKeycode.SDLK_W);
                currentPlayer.backward = keyboard.isKeyPressed(SDLKeycode.SDLK_S);
                currentPlayer.left = keyboard.isKeyPressed(SDLKeycode.SDLK_A);
                currentPlayer.right = keyboard.isKeyPressed(SDLKeycode.SDLK_D);
                currentPlayer.jumping = keyboard.isKeyPressed(SDLKeycode.SDLK_SPACE);
                currentPlayer.down = keyboard.isKeyPressed(SDLKeycode.SDLK_LSHIFT);
                currentPlayer.running = keyboard.isKeyPressed(SDLKeycode.SDLK_LCTRL) && !currentPlayer.isInWater();
            }

            currentPlayer.setCrouching(currentPlayer.down);
            if (!currentPlayer.jumping && wasJumping) {
                lastJump = lastTickTime;
            } else if (currentPlayer.jumping && !wasJumping && (lastTickTime - lastJump) < 200000000L) {
                currentPlayer.setFlying(!currentPlayer.isFlying());
            }

            wasJumping = currentPlayer.jumping;

            if (breaking && breakCooldown-- <= 0) {
                currentPlayer.breakBlock();
                breakCooldown = 5;
            }

            if (using && useCooldown-- <= 0) {
                currentPlayer.useItem();
                useCooldown = 5;
            }

            currentPlayer.tick();
        }

        currentWorld.tick();
    }

    public void onResize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        if (selectedScale == 0)
            scale = maxScaleFor(width, height);
        else scale = Math.clamp(selectedScale, 1, maxScaleFor(width, height));

        scaledWidth = width / scale;
        scaledHeight = height / scale;
        overlayManager.resize(scaledWidth, scaledHeight);

        guiRenderer.resize(scaledWidth, scaledHeight);
        for (Screen screen : screenStack) {
            if (screen != null)
                screen.resize(scaledWidth, scaledHeight);
        }

        resizePlayerViews();

        if (targetFbo != null) {
            targetFbo.delete();
            remove(targetFbo);
        }

        targetFbo = new Framebuffer(width, height, TextureFormat.RGB8, true);
        add("Target Framebuffer", targetFbo);
        GL11.glViewport(0, 0, (int) (width * window.xScale), (int) (height * window.yScale));
    }

    private int maxScaleFor(int width, int height) {
        int maxScale = 1;
        while (width / maxScale >= MAX_WIDTH && height / maxScale >= MAX_HEIGHT) {
            maxScale++;
        }
        return maxScale;
    }

    public void setScale(int scale) {
        selectedScale = scale;
    }

    public int getScale() {
        return scale;
    }

    public int getSelectedScale() {
        return selectedScale;
    }

    public int getScaledWidth() {
        return scaledWidth;
    }

    public int getScaledHeight() {
        return scaledHeight;
    }

    public void render() {
        long currentTime = System.currentTimeMillis();
        deltaTime = currentTime - lastRenderTime;
        lastRenderTime = currentTime;

        boolean imGui = ImGuiOverlay.isShown();
        if (imGui)
            targetFbo.start();

        // Prepare to render the current frame
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
        int error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR) {
            throw new GLException("Error when clearing: " + GLUtils.getErrorName(error));
        }

        if (loadingOverlay.isOpen()) {
            loadingOverlay.render(null, guiRenderer, partialTick);
        } else {
            // Render GUI
            guiRenderer.pushMatrix();
            guiRenderer.translate(0, 0, -500000); // Render with a z-index way back.

            int oSw = scaledWidth;
            int oSh = scaledHeight;

            // Render all player views
            for (ClientPlayerEntity player : players) {
                if (player == null || player.getWorld() == null) {
                    continue;
                }
                // Prepare to render GUI stuff
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_CULL_FACE);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glDepthMask(true);
                GL11.glColorMask(true, true, true, true);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                WorldRenderer worldRenderer = player.getWorldRenderer();
                Mouse mouse = player.mouse;
                Keyboard keyboard = player.keyboard;

                Framebuffer playerView = player.getPlayerView();
                playerView.start();
                int viewWidth = playerView.getWidth();
                int viewHeight = playerView.getHeight();

                scaledWidth = viewWidth / scale;
                scaledHeight = viewHeight / scale;

                // Prepare to render the current frame
                GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
                GL11.glGetError();
                if (mouse != null && keyboard != null && window.isMouseCaptured()) {
                    double sensitivity = 0.5;
                    double rotationChangeX = mouse.getDeltaX() * sensitivity * (0.6 + Math.pow(sensitivity, 3) * 0.2);
                    double rotationChangeY = mouse.getDeltaY() * sensitivity * (0.6 + Math.pow(sensitivity, 3) * 0.2);

                    player.rotate((float) rotationChangeX, (float) rotationChangeY);
                }

                cameraStuff(player);

                // Render world
                if (worldRenderer != null) {
                    worldRenderer.render(partialTick);
                }

                // Prepare to render GUI stuff
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GL11.glDepthMask(false);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                // Render overlays
                overlayManager.resize(viewWidth / scale / players.size(), viewHeight / scale);
                overlayManager.render(player, guiRenderer, partialTick);
                playerView.end();

                if (worldRenderer != null) {
                    worldRenderer.getCamera().finish();
                }
                if (keyboard != null && keyboard.isKeyJustPressed(SDLKeycode.SDLK_E)) {
                    player.openInventory();
                }
            }

            // Prepare to render GUI stuff
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            scaledWidth = oSw;
            scaledHeight = oSh;

            for (ClientPlayerEntity player : players) {
                Framebuffer playerView = player.getPlayerView();
                guiRenderer.drawFramebufferTexture(playerView.get(0), player.playerViewX / scale, player.playerViewY / scale, playerView.getWidth() / scale, playerView.getHeight() / scale);
            }

            guiRenderer.translate(0, 0, 1000);

            ArrayList<Screen> screens = new ArrayList<>(screenStack);
            int mouseX = (int) (window.getMouseX() / scale);
            int mouseY = (int) (window.getMouseY() / scale);

            // Render layered screens
            for (int i = 0, screensSize = screens.size(); i < screensSize; i++) {
                Screen screen = screens.get(i);
                if (screen != null) {
                    // Render the screen
                    guiRenderer.pushMatrix();

                    screen.render(guiRenderer, i == screensSize - 1 ? mouseX : Integer.MIN_VALUE, i == screensSize - 1 ? mouseY : Integer.MIN_VALUE, partialTick);
                    guiRenderer.postRender();

                    guiRenderer.popMatrix();

                    // Translate to the screen's relativePos to layer the next screen on top
                    guiRenderer.translate(0, 0, 1000);

                    error = GL11.glGetError();
                    if (error != GL11.GL_NO_ERROR) {
                        throw new GLException("Error when rendering screen " + screen.getClass().getSimpleName() + ": " + GLUtils.getErrorName(error));
                    }
                }
            }

            int markerIndex = 0;
            if (DeveloperMode.isDevFlagEnabled(DevFlag.GuiDebug)) {
                for (Marker marker : markers) {
                    int x = marker.x();
                    int y = marker.y();

                    if (x > scaledWidth || y > scaledHeight || x < 0 || y < 0) {
                        if (marker.ticks() / 5 % 2 == 0) {
                            guiRenderer.drawString("Marker [OOB}: " + x + ", " + y, 10, 10 + markerIndex * 11, marker.color() | 0xFF000000);
                        }
                    } else {
                        guiRenderer.drawString("Marker: " + x + ", " + y, 10, 10 + markerIndex * 11, marker.color() | 0xFF000000);
                    }
                    markerIndex++;
                    if (marker.ticks() / 5 % 2 == 0) {
                        guiRenderer.fillRect(x - 4, y, 9, 1, marker.color() | 0xFF000000);
                        guiRenderer.fillRect(x, y - 4, 1, 9, marker.color() | 0xFF000000);
                        guiRenderer.drawString(x + ", " + y, x + 3, y + 9, marker.color() | 0xFF000000);
                    }
                }
            }

            guiRenderer.popMatrix();

            guiRenderer.pushMatrix();
            guiRenderer.scale(1f / getScale(), 1f / getScale(), 1);
            if (!window.isMouseCaptured()) {
                for (Mouse mouse : mice.values()) {
                    mouse.render(guiRenderer);
                }
            }
            guiRenderer.popMatrix();

            debugInfoRenderer.render(guiRenderer);

            for (Keyboard keyboard : keyboards.values()) {
                keyboard.update();
            }
        }

        // Finalize rendering the current frame
        GL11.glDisable(GL11.GL_BLEND);

        error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR) {
            throw new GLException("Error when rendering: " + GLUtils.getErrorName(error));
        }
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        if (imGui) {
            targetFbo.end();
            ImGuiOverlay.renderImGui(this);
        }

        for (Mouse keyboard : mice.values()) {
            keyboard.postUpdate();
        }
    }

    private void renderDebugInfo(WorldRenderer worldRenderer) {

    }

    private void cameraStuff(ClientPlayerEntity player) {
        WorldRenderer worldRenderer = player.getWorldRenderer();
        World world = player.getWorld();
        if (world == null || worldRenderer == null) return;

        Camera camera = worldRenderer.getCamera();
        camera.farPlane = renderDistance;
        camera.position.set(0, player.getEyeHeight(), 0);
        camera.roll = 0;

        double interpWalk = player.prevWalkDistance + (player.walkDistance - player.prevWalkDistance) * partialTick;
        float freq = 0.6662f;

        float bob = (float) Math.sin(interpWalk * freq) * bobStrength;
        float sway = (float) Math.cos(interpWalk * freq) * swayStrength;

        camera.position.y += bob;
        camera.roll += sway * 5f;

        camera.update();
    }

    public void onKeyRelease(int which, int key, int scancode, int mods) {
        if (debugKey) {
            if (key == SDLKeycode.SDLK_F4) {
                debugKey = false;
            }
            return;
        }

        if (key == SDLKeycode.SDLK_F9 && (mods == SDLKeycode.SDL_KMOD_SHIFT || System.getProperty("os.name").toLowerCase().contains("linux"))) {
            window.setVSync(!window.isVSync());
        }

        Keyboard keyboard = keyboards.get(which);
        if (keyboard == null) return;
        keyboard.onRelease(key);

        Screen screen = getScreen();
        if (screen != null) {
            screen.onKeyRelease(key, scancode, mods);
        }
    }

    public void onKeyPress(int which, int key, int scancode, int mods) {
        if (key == SDLKeycode.SDLK_F4) {
            debugKey = true;
            return;
        }
        if (debugKey) {
            if (key == SDLKeycode.SDLK_R) {
                reloadResources();
            }
            return;
        }
        if (key == SDLKeycode.SDLK_F11) {
            window.setFullscreen(!window.isFullscreen());
        }


        if (!keyboards.containsKey(which))
            addKeyboard(window.createKeyboard(which));

        Keyboard keyboard = keyboards.get(which);
        keyboard.onPress(key);

        if (DeveloperMode.enabled) {
            if (key == SDLKeycode.SDLK_F6) {
                cycleGraphicMode();
            }
        }
        if (key == SDLKeycode.SDLK_F3) {
            debugInfoRenderer.enabled = !debugInfoRenderer.enabled;
        }
        if (key == SDLKeycode.SDLK_F12 && DeveloperMode.enabled) {
            DeveloperMode.setDevFlagEnabled(DevFlag.ImGui, !DeveloperMode.isDevFlagEnabled(DevFlag.ImGui));
        }

        Screen screen = getScreen();
        if (screen != null) {
            screen.onKeyPress(key, scancode, mods);
        } else if (key == SDLKeycode.SDLK_ESCAPE) {
            window.releaseMouse();
            if (world != null) {
                showScreen(new PauseScreen());
            }
        } else if (key == SDLKeycode.SDLK_F5) {
            for (ClientPlayerEntity player : players) {
                WorldRenderer worldRenderer = player.getWorldRenderer();
                if (worldRenderer != null) {
                    worldRenderer.reloadChunks();
                }
            }
        } else if (key == SDLKeycode.SDLK_C) {
            showScreen(new ChatScreen());
        } else if (key >= SDLKeycode.SDLK_1 && key <= SDLKeycode.SDLK_9) {
            for (ClientPlayerEntity player : players) {
                if (player.keyboard.keyboardID() == which) {
                    if (player != null) player.selectItem(key - SDLKeycode.SDLK_1);
                }
            }
        }
    }

    public void onCharTyped(int codepoint) {
        Screen screen = getScreen();
        if (screen != null) {
            screen.onCharTyped((char) codepoint);
        }
    }

    private void cycleGraphicMode() {
        for (ClientPlayerEntity player : players) {
            if (player.getWorldRenderer() == null) return;

            GraphicsMode current = getGraphicsMode();
            GraphicsMode[] modes = GraphicsMode.values();
            int ordinal = current.ordinal();
            if (ordinal < 0 || ordinal >= modes.length) {
                ordinal = 0;
            }
            int nextOrdinal = (ordinal + 1) % modes.length;
            GraphicsMode nextMode = modes[nextOrdinal];
            CommonConstants.LOGGER.info("Switching graphics mode from {} to {}", current, nextMode);
            setGraphicsMode(player, nextMode);
        }
    }

    public void setGraphicsMode(ClientPlayerEntity player, GraphicsMode nextMode) {
        invoke(() -> {
            WorldRenderer worldRenderer = player.getWorldRenderer();
            GraphicsMode original = getGraphicsMode();
            try {
                currentGraphicsMode = nextMode;
                player.setWorldRenderer(new WorldRenderer(currentGraphicsMode, player, player.getPlayerView().getWidth(), player.getPlayerView().getHeight()));
            } catch (Exception e) {
                CommonConstants.LOGGER.error("Failed to switch graphics mode", e);
                player.setWorldRenderer(worldRenderer);
                currentGraphicsMode = original;
                return;
            }
            try {
                if (worldRenderer != null) {
                    worldRenderer.close();
                }
            } catch (Exception e) {
                CommonConstants.LOGGER.error("Failed to close old world renderer", e);
            }
        }).exceptionally(throwable -> {
            CommonConstants.LOGGER.error("An serious error occurred while switching graphics mode", throwable);
            return null;
        });
    }

    public GraphicsMode getGraphicsMode() {
        return currentGraphicsMode;
    }

    public void onKeyRepeat(int key, int scancode, int mods) {
        Screen screen = getScreen();
        if (screen != null) {
            screen.onKeyRepeat(key, scancode, mods);
        }
    }

    public void onMouseScroll(int which, double scrollX, double scrollY) {
        Screen screen = getScreen();
        if (screen != null) {
            screen.onMouseScroll(scrollX, scrollY);
        } else {
            for (ClientPlayerEntity player : players) {
                Mouse mouse = player.mouse;
                if (mouse != null && mouse.mouseID() == which) {
                    player.scrollItem(scrollX + scrollY);
                }
            }
        }
    }

    public void onMouseButtonRelease(int which, int button, int mods) {
        if (getWorld() != null && getScreen() == null && button == SDLMouse.SDL_BUTTON_LEFT) {
            window.setMouseCaptured(true);
        }
        Screen screen = getScreen();
        if (screen != null) {
            Mouse usingMouse = getMainMouse();
            if (usingMouse == null || which != mainMouse.mouseID() || !usingMouse.isPressed(button)) return;
            float mouseX = usingMouse.getX();
            float mouseY = usingMouse.getY();
            if (button == SDLMouse.SDL_BUTTON_X1) {
                popGuiLayer();
            }
            screen.onMouseButtonRelease((int) mouseX, (int) mouseY, button, mods);
            usingMouse.onRelease(button);
        } else if (button == SDLMouse.SDL_BUTTON_LEFT) {
            breaking = false;
        } else if (button == SDLMouse.SDL_BUTTON_RIGHT) {
            using = false;
        }
    }

    @ApiStatus.Internal
    public void popGuiLayer() {
        Screen oldScreen = getScreen();
        if (screenStack.size() > 1) {
            if (oldScreen != null) {
                oldScreen.onClose();
            }
            Screen old = screenStack.pop();
            if (old != null) old.onClose();
            Screen screen = screenStack.isEmpty() ? null : screenStack.peek();
            if (screen != null) {
                screen.onOpen();
            }
        } else if (world != null) {
            if (oldScreen != null) {
                oldScreen.onClose();
            }
            showScreen(null);
        }
    }

    @ApiStatus.Internal
    public void pushGuiLayer(Screen screen) {
        if (screen != null) {
            screen.onOpen();
            window.releaseMouse();
        }
        screenStack.push(screen);
    }

    public void replaceGuiLayer(Screen screen) {
        if (screen != null) {
            screen.onOpen();
            window.releaseMouse();
        }
        if (!screenStack.isEmpty()) {
            Screen old = screenStack.pop();
            if (old != null) old.onClose();
        }
        screenStack.push(screen);
    }

    public void clearGuiLayers() {
        while (!screenStack.isEmpty()) {
            Screen old = screenStack.pop();
            if (old != null) old.onClose();
        }
    }

    public void onMouseButtonPress(int which, float mouseX, float mouseY, int button, int mods) {
        if (which == 0) {
            if (!window.confineCursor()) return;
            mouseEjected = false;
            if (mainMouse != null) {
                mainMouse.onEnter(mouseX, mouseY);
                return;
            }
        }

        Screen screen = getScreen();
        if (screen != null) {
            Mouse usingMouse = getMainMouse();
            if (usingMouse == null || which != mainMouse.mouseID()) return;
            mouseX = usingMouse.getX();
            mouseY = usingMouse.getY();
            usingMouse.onPress(button);
            screen.onMouseButtonPress((int) mouseX / getScale(), (int) mouseY / getScale(), button, mods);
        } else if (button == SDLMouse.SDL_BUTTON_LEFT) {
            breaking = true;
            breakCooldown = 0;
        } else if (button == SDLMouse.SDL_BUTTON_RIGHT) {
            using = true;
            useCooldown = 0;
        }
    }

    public boolean onWindowClose() {
        CommonConstants.LOGGER.info("Closing window");
        running = false;
        return true;
    }

    public float getPartialTick() {
        return partialTick;
    }

    public static int getFps() {
        return fps;
    }

    public Window getWindow() {
        return window;
    }

    public static void crash(Throwable throwable) {
        CrashLog crashLog = new CrashLog("Generic Crash", throwable);
        crash(crashLog);
    }

    private static void crash(ApplicationCrash crash) {
        String string = crash.getCrashLog().toString();
        CommonConstants.LOGGER.error(string);
        if (instance != null) {
            QuantumClient.invokeAndWait(instance::close);
        }
        System.exit(1);
    }

    public TextureManager getTextureManager() {
        return textureManager;
    }

    public void onMouseMove(int which, float mouseX, float mouseY) {
        if (which == 0) return;

        if (!mice.containsKey(which))
            addMouse(window.createMouse(which));
        Mouse mouse = mice.get(which);
        if (mouse != null) {
            if (mainMouse == null)
                makeMainMouse(mouse);
            mouse.onMove(mouseX, mouseY);
        }

        Screen screen = getScreen();
        if (screen != null) {
            Mouse usingMouse = mainMouse;
            if (usingMouse != null)
                screen.onMouseMove((int) usingMouse.getX(), (int) usingMouse.getY());
        }
    }

    public Mouse getMainMouse() {
        return mainMouse;
    }

    private void makeMainMouse(Mouse mouse) {
        if (mainMouse != null)
            mainMouse.removeMain();
        mainMouse = mouse;
        if (mainMouse != null)
            mainMouse.makeMain();
    }

    public void onWindowFocus() {

    }

    public void onWindowUnfocus() {

    }

    public BlockModel getBlockModel(BlockState state) {
        return modelManager.getBlockModel(state);
    }

    public float getBobStrength() {
        return bobStrength;
    }

    public void setBobStrength(float bobStrength) {
        this.bobStrength = bobStrength;
    }

    public float getSwayStrength() {
        return swayStrength;
    }

    public void setSwayStrength(float swayStrength) {
        this.swayStrength = swayStrength;
    }

    public void showScreen(Screen nextScreen) {
        if (nextScreen == null) {
            clearGuiLayers();

            if (getWorld() == null) {
                pushGuiLayer(new TitleScreen());
                try (Activity activity = new Activity()) {
                    activity.setDetails("In the menus");
                    activity.setType(ActivityType.PLAYING);
                    activity.timestamps().setStart(Instant.ofEpochMilli(startTimeMillis));
                    activity.assets().setLargeImage("icon");
                    activity.assets().setLargeText(FabricLoader.getInstance().getRawGameVersion());

                    core.activityManager().updateActivity(activity);
                }
            } else {
                window.captureMouse();
                try (Activity activity = new Activity()) {
                    activity.setState(players.size() <= 1 ? "Singleplayer" : "Splitscreen");
                    activity.setDetails(integratedServer != null ? "Playing in their world" : "Playing on a remote world");
                    activity.setType(ActivityType.PLAYING);
                    activity.timestamps().setStart(Instant.ofEpochMilli(startTimeMillis));
                    activity.assets().setLargeImage("icon");
                    activity.assets().setLargeText(FabricLoader.getInstance().getRawGameVersion());

                    core.activityManager().updateActivity(activity);
                }
            }
            return;
        }
        if (!nextScreen.isModal()) {
            clearGuiLayers();

            if (nextScreen.isModal())
                pushGuiLayer(new TitleScreen());
            nextScreen.onOpen();
            pushGuiLayer(nextScreen);
        } else {
            if (screenStack.isEmpty()) {
                pushGuiLayer(new TitleScreen());
            }
            pushGuiLayer(nextScreen);
        }
        nextScreen.onOpen();

        try (Activity activity = new Activity()) {
            activity.setDetails("In the menus");
            activity.setType(ActivityType.PLAYING);
            activity.timestamps().setStart(Instant.ofEpochMilli(startTimeMillis));
            activity.assets().setLargeImage("icon");
            activity.assets().setLargeText(FabricLoader.getInstance().getRawGameVersion());

            core.activityManager().updateActivity(activity);
        }
    }

    public void onDisconnect(String message, boolean isMemory) {
        if (getScreen() instanceof DisconnectScreen) {
            return;
        }
        synchronized (disconnectLock) {
            if (disconnecting) return;
            disconnecting = true;
        }
        invokeAndWait(() -> {
            showScreen(new DisconnectingScreen("Disconnecting...", isMemory ? "Got disconnected from world" : "Got disconnected from server"));
            quitGameThen(() -> {
                if (isMemory) {
                    CommonConstants.LOGGER.info("Disconnected from integrated server: {}", message);
                    showScreen(new DisconnectScreen(message, true));
                } else {
                    CommonConstants.LOGGER.info("Disconnected from server: {}", message);
                    showScreen(new DisconnectScreen(message, false));
                }
                disconnecting = false;
            });
        });
    }

    public IntegratedServer getIntegratedServer() {
        return integratedServer;
    }

    static void main(String[] args) {
        if (PlatformOS.isLinux) {
            // Nope!
        }

        Thread.currentThread().setName("Render Thread");
        Thread.currentThread().setUncaughtExceptionHandler(QuantumClient::uncaughtException);
        Thread.setDefaultUncaughtExceptionHandler(QuantumClient::uncaughtException);

        SDLHints.SDL_SetHint(SDLHints.SDL_HINT_MOUSE_RELATIVE_WARP_MOTION, "0");
        SDLHints.SDL_SetHint(SDLHints.SDL_HINT_WINDOWS_RAW_KEYBOARD, "1");
        SDLHints.SDL_SetHint(SDLHints.SDL_HINT_MOUSE_AUTO_CAPTURE, "0");
        SDLHints.SDL_SetHint(SDLHints.SDL_HINT_MOUSE_FOCUS_CLICKTHROUGH, "1");
        SDLHints.SDL_SetHint(SDLHints.SDL_HINT_MOUSE_TOUCH_EVENTS, "0");

        if (!SDLInit.SDL_Init(SDLInit.SDL_INIT_VIDEO | SDLInit.SDL_INIT_EVENTS | SDLInit.SDL_INIT_AUDIO | SDLInit.SDL_INIT_GAMEPAD)) {
            throw new SDLInitException("Unable to initialize SDL");
        }

        SDLLog.SDL_SetLogOutputFunction((userdata, category, priority, message) -> {
            String s = MemoryUtil.memUTF8(message);
            switch (priority) {
                case SDLLog.SDL_LOG_PRIORITY_TRACE,
                     SDLLog.SDL_LOG_PRIORITY_VERBOSE -> SDL_LOGGER.trace(s);
                case SDLLog.SDL_LOG_PRIORITY_DEBUG -> SDL_LOGGER.debug(s);
                case SDLLog.SDL_LOG_PRIORITY_INFO -> SDL_LOGGER.info(s);
                case SDLLog.SDL_LOG_PRIORITY_WARN -> SDL_LOGGER.warn(s);
                case SDLLog.SDL_LOG_PRIORITY_ERROR,
                     SDLLog.SDL_LOG_PRIORITY_CRITICAL -> SDL_LOGGER.error(s);
            }
        }, 0);
        SDLLog.SDL_SetLogPriorities(SDLLog.SDL_LOG_PRIORITY_TRACE);

        QuantumClient client = null;
        try {
            client = new QuantumClient();
            client.mainloop();
        } catch (Throwable throwable) {
            crash(throwable);
            System.exit(1);
        } finally {
            CommonConstants.LOGGER.info("Finished main loop");
        }

        client.close();
        System.exit(0);
    }

    public Screen getScreen() {
        return screenStack.isEmpty() ? null : screenStack.peek();
    }

    public int getScreenIndex(Screen screen) {
        return screenStack.size() - screenStack.indexOf(screen);
    }

    public void resumeGame() {
        clearGuiLayers();
        window.captureMouse();
    }

    public void pauseGame() {
        showScreen(new PauseScreen());
    }

    public void quitGameThen(Runnable runnable) {
        for (ClientPlayerEntity player : players) {
            if (player.connection != null) {
                player.connection.close();
                player.connection = null;
            }
        }
        if (integratedServer != null) {
            CompletableFuture<Void> shutdownFuture = new CompletableFuture<>();
            integratedServer.shutdown(() -> {
                CommonConstants.LOGGER.info("Closed integrated server");
                shutdownFuture.complete(null);
            });
            shutdownFuture.join();
            integratedServer = null;
        }
        invokeAndWait(() -> {
            ClientWorld theWorld = getWorld();
            if (theWorld != null) {
                theWorld.close();
            }
            setWorld(null);
            players.clear();
            clearGuiLayers();
            runnable.run();
        });
    }

    public void quitGame() {
        quitGameThen(() -> showScreen(new TitleScreen()));
    }

    public RenderBufferSource getRenderBuffers() {
        return renderBuffers;
    }

    public void openMenu(ContainerMenu menu) {
        ScreenFactory screenFactory = MenuScreenManager.getScreenFactory(menu);
        Screen screen = screenFactory.create(menu);
        if (screen != null) {
            pushGuiLayer(screen);
        } else {
            CommonConstants.LOGGER.warn("No screen factory found for menu {}", menu.getClass().getSimpleName());
        }
    }

    public void closeMenu() {
        if (screenStack.isEmpty()) {
            CommonConstants.LOGGER.warn("Tried to close menu when there was no menu open");
            return;
        }
        int size = screenStack.size();
        int index = size - 1;
        boolean found = false;
        for (; index >= 0; index--) {
            Screen screen = screenStack.get(index);
            if (screen instanceof MenuScreen) {
                found = true;
                break;
            }
        }

        if (!found) {
            CommonConstants.LOGGER.warn("Tried to close menu when there was no menu open");
            return;
        }

        for (int i = size - 1; i > index; i--) {
            Screen screen = screenStack.pop();
            if (screen != null) screen.onClose();
        }
    }

    public ContainerMenu getMenu() {
        if (screenStack.isEmpty()) {
            return null;
        }
        int size = screenStack.size();
        int index = size - 1;
        for (; index >= 0; index--) {
            Screen screen = screenStack.get(index);
            if (screen instanceof MenuScreen menuScreen) {
                return menuScreen.getMenu();
            }
        }

        return null;
    }

    public ItemModel getItemModel(Item item) {
        return modelManager.getItemModel(item);
    }

    public void playSound(SoundEvent soundEvent, float volume, float pitch, Vector3f position, Vector3f velocity) {
        Sound sound = soundRegistry.get(soundEvent);
        if (sound == null) return;

        SoundSource allocate = soundSystem.getWorldGroup().allocate();
        if (allocate == null) return;

        allocate.set(sound)
                .setGain(volume)
                .setPitch(pitch)
                .setPosition(position)
                .setVelocity(velocity)
                .play();
    }

    public void playSound(SoundEvent soundEvent, float volume) {
        playSound(soundEvent, volume, ((float) Math.random() * 2 - 1f) * 0.1f + 1f);
    }

    public void playSound(SoundEvent soundEvent, float volume, float pitch) {
        Sound sound = soundRegistry.get(soundEvent);
        if (sound == null) return;

        SoundSource allocate = soundSystem.getWorldGroup().allocate();
        if (allocate == null) return;

        allocate.set(sound)
                .setGain(volume)
                .setPitch(1)
                .setPosition(0.0f, 0.0f, 0.0f)
                .setVelocity(0.0f, 0.0f, 0.0f)
                .play();
    }

    public void onWindowContentScaleChange(float xscale, float yscale) {

    }

    public @Nullable ClientWorld getWorld() {
        return world;
    }

    public void setWorld(@Nullable ClientWorld world) {
        if (world == null && this.world != null) {
            this.world.close();
            remove(this.world);
            this.world = null;
        } else if (world != null) {
            for (ClientPlayerEntity player : players) {
                player.setWorld(world);
            }
            if (this.world == null) {
                add("World", world);
                this.world = world;
            } else {
                remove(this.world);
                this.world = world;
                add("World", world);
            }
        }
    }

    public GuiRenderer getGuiRenderer() {
        return guiRenderer;
    }

    public float getTotalTimeSeconds() {
        return (System.currentTimeMillis() - startTimeMillis) / 1000f;
    }

    public void onDeferResize(int width, int height) {
        desiredWidth = width;
        desiredHeight = height;
    }

    public FeatureSet getFeatures() {
        if (integratedServer == null) return FeatureSet.NONE;
        return integratedServer.getFeatures();
    }

    public WorldStorage createNewWorld() {
        return createNewWorld(new WorldSaveInfo(
                new Random().nextLong(),
                1,
                GameMode.SURVIVAL,
                null,
                "Developer World",
                0xFF00000,
                LocalDateTime.now()
        ));
    }

    @Deprecated
    public WorldStorage createNewWorld(String name) {
        return createNewWorld(new WorldSaveInfo(
                new Random().nextLong(),
                1,
                GameMode.SURVIVAL,
                null,
                name,
                0xFFFF0000,
                LocalDateTime.now()
        ));
    }

    public WorldStorage createNewWorld(@NotNull String name, Path path) {
        return createNewWorld(new WorldSaveInfo(
                new Random().nextLong(),
                1,
                GameMode.SURVIVAL,
                null,
                name,
                0xFFFF0000,
                LocalDateTime.now()
        ), path);
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public void openWorld(WorldStorage selected) {
        openWorld(selected, FeatureSet.NONE);
    }

    public OverlayManager getOverlays() {
        return overlayManager;
    }

    public void addMarker(int x, int y, int ticks, int color) {
        if (DeveloperMode.isDevFlagEnabled(DevFlag.GuiDebug)) {
            markers.add(new Marker(x, y, ticks, color));
        }
    }

    public void removeMouse(int which) {
        mice.remove(which);
        if (mainMouse.mouseID() == which) {
            mainMouse.removeMain();
            mainMouse = null;
        }
    }

    public void addMouse(Mouse mouse) {
        CommonConstants.LOGGER.info("Mouse connected: {}", mouse.name());
        this.mice.put(mouse.mouseID(), mouse);
        for (ClientPlayerEntity player : players) {
            if (player.mouse == null) {
                player.mouse = mouse;
                return;
            }
        }
    }

    public void addKeyboard(Keyboard keyboard) {
        CommonConstants.LOGGER.info("Keyboard connected: {}", keyboard.name());
        this.keyboards.put(keyboard.keyboardID(), keyboard);
        for (ClientPlayerEntity player : players) {
            if (player.keyboard == null) {
                player.keyboard = keyboard;
                return;
            }
        }
    }

    public Mouse getMouse(int mouseID) {
        return mice.get(mouseID);
    }

    public boolean isKeyDown(int keycode) {
        for (Keyboard keyboard : keyboards.values()) {
            if (keyboard.isKeyPressed(keycode)) {
                return true;
            }
        }
        return false;
    }

    public void removeKeyboard(int which) {
        this.keyboards.remove(which);
    }

    public void startTextInput() {
        window.startTextInput();
    }

    public void stopTextInput() {
        window.stopTextInput();
    }

    public void setInputRect(int x, int y, int width, int height, int cursorX) {
        window.setTextInputRect(x, y, width, height, cursorX);
    }

    public Clipboard getClipboard() {
        return clipboard;
    }

    public void onMouseEnter(float x, float y) {
        Mouse mouse = mainMouse;
        if (!window.confineCursor()) return;
        mouseEjected = false;
        if (mouse == null) return;
        mouse.onEnter(x, y);
    }

    public QuantumClientSparkPlugin getSparkPlugin() {
        return sparkPlugin;
    }

    private void reload(ReloadContext context) {
        resourceManager.reload();

        loadingOverlay.reload(context);
    }

    public void loadModels(ReloadContext context) {
        for (BlockModel model : modelManager.getBlockModels()) {
            context.log("Load block model: " + model.resourceId());
            context.submitSafe(() -> {
                model.preload(this);
                Collection<Identifier> allTextures = model.getAllTextures();
                for (Identifier texture : allTextures) {
                    blockTextureAtlas.addTexture(texture.mapPath(path -> "textures/" + path + ".png"));
                }
                model.load(this);
            }).join();
        }

        for (ItemModel model : modelManager.getItemModels()) {
            context.log("Loading item model: " + model.resourceId());
            context.submitSafe(() -> {
                model.preload(this);
                Collection<Identifier> allTextures = model.getAllTextures();
                for (Identifier texture : allTextures) {
                    itemTextureAtlas.addTexture(texture.mapPath(path -> "textures/" + path + ".png"));
                }
                model.load(this);
            }).join();
        }
    }

    public void discoverModels(ReloadContext context) {
        modelLoader = new ModelLoader(resourceManager);
        for (Block block : Registries.BLOCK.values()) {
            if (block.isAir()) continue;

            context.log("Loading models for block: " + block.getId());

            context.submitSafe(() -> {
                try {
                    BlockModel load = modelLoader.load(block);
                    if (load == null) {
                        return;
                    }

                    modelManager.registerBlockModel(block.getDefaultState(), load);
                } catch (IOException e) {
                    CommonConstants.LOGGER.error("Failed to load model for block {}", block.getId(), e);
                }
            }).join();
        }

        for (Item item : Registries.ITEM.values()) {
            if (item == Items.AIR) continue;

            context.log("Loading models for item: " + item.getId());

            context.submitSafe(() -> {
                try {
                    ItemModel load = modelLoader.load(item);
                    if (load == null) {
                        if (item instanceof BlockItem blockItem) {
                            BlockModel blockModel = modelManager.getBlockModel(blockItem.getBlock().getDefaultState());
                            if (blockModel instanceof JsonModel jsonModel) {
                                modelManager.registerItemModel(item, jsonModel);
                            } else if (blockModel instanceof AssimpBlockModel assimpBlockModel) {
                                modelManager.registerItemModel(item, new AssimpBlockItemModel(assimpBlockModel));
                            } else {
                                CommonConstants.LOGGER.error("Failed to load model for block item {}", blockItem.getBlock().getId());
                            }
                            return;
                        }
                        modelManager.registerItemModel(
                                item, new FlatItemModel(
                                        item.getId().mapPath(path -> "items/" + path),
                                        item.getId().mapPath(path -> "textures/items/" + path + ".png")
                                )
                        );
                        return;
                    }
                    modelManager.registerItemModel(item, load);
                } catch (IOException e) {
                    CommonConstants.LOGGER.error("Failed to load model for item {}", item.getId(), e);
                }
            }).join();
        }
    }

    public void resetAtlas(ReloadContext context) {
        context.log("Deleting atlases...");

        if (blockTextureAtlas != null) {
            atlasContainer.removeAtlas(blockTextureAtlas);
            blockTextureAtlas.delete();
        }
        if (blockTextureAtlas != null) {
            atlasContainer.removeAtlas(itemTextureAtlas);
            itemTextureAtlas.delete();
        }
        if (blockTextureAtlas != null) {
            atlasContainer.removeAtlas(particleTextureAtlas);
            particleTextureAtlas.delete();
        }

        context.log("Creating atlases...");

        blockTextureAtlas = new TextureAtlas(8192);
        itemTextureAtlas = new TextureAtlas(8192);
        particleTextureAtlas = new TextureAtlas(8192);

        atlasContainer.addAtlas(blockTextureAtlas);
        atlasContainer.addAtlas(itemTextureAtlas);
        atlasContainer.addAtlas(particleTextureAtlas);
    }

    public CompletableFuture<Void> reloadResources() {
        loadingOverlay.open();
        ReloadContext context = new ReloadContext(this, resourceManager);
        return CompletableFuture.runAsync(() -> {
            reload(context);
            loadingOverlay.finish();
            context.await();
            context.finish();
        }).thenRunAsync(() -> {
            for (ClientPlayerEntity player : players) {
                player.getWorldRenderer().reloadChunks();
            }
            loadingOverlay.close();
        }, this).exceptionally(throwable -> {
            crash(throwable);
            return null;
        });
    }
}
