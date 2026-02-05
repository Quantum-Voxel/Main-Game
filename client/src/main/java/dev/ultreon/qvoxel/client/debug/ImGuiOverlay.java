package dev.ultreon.qvoxel.client.debug;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.block.state.property.StatePropertyKey;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.debug.renderers.*;
import dev.ultreon.qvoxel.client.gui.ContainerWidget;
import dev.ultreon.qvoxel.client.gui.Widget;
import dev.ultreon.qvoxel.client.render.Color;
import dev.ultreon.qvoxel.client.render.Window;
import dev.ultreon.qvoxel.client.texture.Texture;
import dev.ultreon.qvoxel.client.world.ClientWorld;
import dev.ultreon.qvoxel.debug.HiddenNode;
import dev.ultreon.qvoxel.debug.HideInNodeView;
import dev.ultreon.qvoxel.debug.ShowInNodeView;
import dev.ultreon.qvoxel.featureflags.FeatureSet;
import dev.ultreon.qvoxel.network.system.DevFlag;
import dev.ultreon.qvoxel.network.system.DevPipe;
import dev.ultreon.qvoxel.network.system.DeveloperMode;
import dev.ultreon.qvoxel.resource.*;
import dev.ultreon.qvoxel.server.QuantumServer;
import dev.ultreon.qvoxel.server.WorldStorage;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.util.ChunkVec;
import imgui.*;
import imgui.extension.imguifiledialog.ImGuiFileDialog;
import imgui.extension.imguifiledialog.flag.ImGuiFileDialogFlags;
import imgui.extension.implot.ImPlot;
import imgui.extension.implot.ImPlotContext;
import imgui.extension.texteditor.TextEditor;
import imgui.extension.texteditor.TextEditorCoordinates;
import imgui.extension.texteditor.TextEditorLanguageDefinition;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.sdl.SDLKeycode;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * ImGui overlay for Quantum Voxel
 */
@SuppressWarnings("t")
public class ImGuiOverlay {
    public static final ImFloat I_GAMMA = new ImFloat(1.5f);
    public static final ImFloat U_CAP = new ImFloat(0.45f);
    public static final ImFloat U_RADIUS = new ImFloat(0.45f);
    public static final ImFloat U_INTENSITY = new ImFloat(1.5f);
    public static final ImFloat U_MULTIPLIER = new ImFloat(1000.0f);
    public static final ImFloat U_DEPTH_TOLERANCE = new ImFloat(0.0001f);
    public static final ImInt U_ATLAS_SIZE = new ImInt(512);
    public static final ImInt MODEL_VIEWER_LIST_INDEX = new ImInt(0);
    public static final ImBoolean SHOW_RENDER_PIPELINE = new ImBoolean(false);
    private static boolean crashHooked = false;
    private static Throwable crash = null;
    public static final Consumer<Throwable> CRASH_HOOK = t -> {
        crash = t;
    };
    public static final ImInt SHADER_DEBUG_STATE = new ImInt(0);
    public static String NET_PIPE_OUT = "";
    public static final DevPipe DEV_PIPE = (tag, message) -> {
        if (tag.equals("NetLog")) {
            String s = NET_PIPE_OUT;
            NET_PIPE_OUT += message + "\n";
            if (NET_PIPE_OUT.length() > 1024) {
                NET_PIPE_OUT = NET_PIPE_OUT.substring(NET_PIPE_OUT.indexOf("\n") + 1);
            }
        } else {
            CommonConstants.LOGGER.warn("Unhandled DevPipe: {}", tag);
        }
    };
    private static final ImBoolean SHOW_IM_GUI = new ImBoolean(false);
    private static final ImBoolean SHOW_PLAYER_UTILS = new ImBoolean(false);
    private static final ImBoolean SHOW_GUI_UTILS = new ImBoolean(false);
    private static final ImBoolean SHOW_UTILS = new ImBoolean(false);
    private static final ImBoolean SHOW_SHADER_EDITOR = new ImBoolean(false);
    private static final ImBoolean SHOW_SKYBOX_EDITOR = new ImBoolean(false);
    private static final ImBoolean SHOW_MODEL_VIEWER = new ImBoolean(false);
    private static final ImBoolean SHOW_HIDDEN_FIELDS = new ImBoolean(false);
    private static final ImBoolean SHOW_CHUNK_SECTION_BORDERS = new ImBoolean(false);
    private static final ImBoolean SHOW_CHUNK_DEBUGGER = new ImBoolean(false);
    private static final ImBoolean SHOW_PROFILER = new ImBoolean(false);
    private static final ImBoolean SHOW_OCCLUSION_DEBUG = new ImBoolean(false);
    private static final ImBoolean SHOW_NETWORK_LOGGING = new ImBoolean(false);

    private static final ImBoolean SHOW_ABOUT = new ImBoolean(false);
    private static final ImBoolean SHOW_METRICS = new ImBoolean(false);
    private static final ImBoolean SHOW_STACK_TOOL = new ImBoolean(false);
    private static final ImBoolean SHOW_STYLE_EDITOR = new ImBoolean(false);
    private static final ImBoolean SHOW_JSHELL = new ImBoolean(false);
    private static final ImBoolean SHOW_CLASS_ATTACHER = new ImBoolean(false);

    protected static final String[] keys = {"A", "B", "C"};
    protected static final Double[] values = {0.1, 0.3, 0.6};
    private static final Vector3f TRANSLATE_TMP = new Vector3f();
    private static final Vector3f SCALE_TMP = new Vector3f();
    private static final Quaternionf ROTATE_TMP = new Quaternionf();
    public static final boolean[] MOUSE_DOWN = new boolean[5];

    private static ImGuiImplGl3 imGuiGl3;
    private static boolean isImplCreated;
    private static boolean isContextCreated;
    private static boolean triggerLoadWorld;
    private static ImPlotContext imPlotCtx;
    private static String[] modelViewerList = new String[0];

    @SuppressWarnings("GDXJavaStaticResource")
    private static Object selected = null;
    private static final ScreenCoordinates screenCoords = new ScreenCoordinates();
    private static final ImInt rotType = new ImInt(0);
    public static final Map<Identifier, TextEditor> textEditors = new HashMap<>();
    public static TextEditorLanguageDefinition glsl;
    public static final Map<Identifier, TextEditorCoordinates> textEditorPos = new HashMap<>();
    private static boolean firstLoop = true;
    private static final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private static final PrintStream ps = new PrintStream(baos);
    private static final ImString inputBuffer = new ImString(512);
    private static final ImString inputBuffer1 = new ImString(512);
    private static int selectedIndex;
    private static boolean focusInput;
    private static final List<String> filteredClasses = new ArrayList<>();
    private static Class<?> selectedClass;
    private static long nextProfilerCollect;
    private static List<Thread> threads;
    private static final Vector3f tmp3f = new Vector3f();
    private static final Map<Class<?>, Renderer<?>> renderers = new HashMap<>();
    private static boolean extensionsEnabled = true;
    private static boolean extensionsChecked = false;

    static {
        registerRenderer(Map.class, new MapRenderer());
        registerRenderer(Class.class, new ClassRenderer());
        registerRenderer(Number.class, new NumberRenderer());
        registerRenderer(Boolean.class, new BooleanRenderer());
        registerRenderer(String.class, new StringRenderer());
        registerRenderer(Vector4f.class, new Vector4fRenderer());
        registerRenderer(Vector3f.class, new Vector3fRenderer());
        registerRenderer(Vector2f.class, new Vector2fRenderer());
        registerRenderer(Vector4i.class, new Vector4iRenderer());
        registerRenderer(Vector3i.class, new Vector3iRenderer());
        registerRenderer(Vector2i.class, new Vector2iRenderer());
        registerRenderer(Vector4d.class, new Vector4dRenderer());
        registerRenderer(Vector3d.class, new Vector3dRenderer());
        registerRenderer(Vector2d.class, new Vector2dRenderer());
        registerRenderer(Quaternionf.class, new QuaternionfRenderer());
        registerRenderer(Matrix4f.class, new Matrix4fRenderer());
    }

    public static void registerRenderer(Class<?> cls, Renderer<?> renderer) {
        renderers.put(cls, renderer);
    }

    /**
     * Configures and initializes the ImGui context, along with associated components,
     * for use within the application. This method sets up ImGui's context, enables
     * various features, initializes fonts, and prepares necessary rendering systems
     * for graphical user interface display.
     */
    @ApiStatus.Internal
    public static void setupImGui() {
        CommonConstants.LOGGER.info("Setting up ImGui");

        synchronized (ImGuiOverlay.class) {
            ImGui.createContext();
            ImGuiOverlay.imPlotCtx = ImPlot.createContext();
            ImGuiOverlay.isContextCreated = true;
        }
        final ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.getFonts().addFontDefault();

        // This enables FreeType font renderer, which is disabled by default.
        io.getFonts().setFreeTypeRenderer(true);


        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);  // Enable Keyboard Controls
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);      // Enable Docking
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);    // Enable Multi-Viewport / Platform Windows

        long windowHandle = QuantumClient.get().getWindow().getObjectId();

        QuantumClient.invokeAndWait(() -> {
            ImGuiOverlay.imGuiGl3.init("#version 460");

            glsl = TextEditorLanguageDefinition.GLSL();
        });
    }

    /**
     * Pre-initializes the ImGui library by setting up and associating the
     * required implementations for GLFW and OpenGL, ensuring they are ready
     * for use. This method marks the internal implementation as created
     * and prepares the overlay for further setup or rendering phases.
     */
    public static void preInitImGui() {
        CommonConstants.LOGGER.info("Pre-initializing ImGui");
        synchronized (ImGuiOverlay.class) {
            ImGuiOverlay.imGuiGl3 = new ImGuiImplGl3();
            ImGuiOverlay.isImplCreated = true;
        }
    }

    /**
     * Determines if the chunk section borders are set to be shown by the ImGui overlay.
     *
     * @return true if the chunk section borders are shown, false otherwise
     */
    public static boolean isChunkSectionBordersShown() {
        return ImGuiOverlay.SHOW_CHUNK_SECTION_BORDERS.get();
    }

    /**
     * Renders the game's debugging overlay using ImGui.
     *
     * @param client the instance of {@code QuantumClient} that provides the context
     *               and data required for the ImGui rendering process.
     */
    @ApiStatus.Internal
    public static void renderImGui(QuantumClient client) {
        if (!ImGuiOverlay.SHOW_IM_GUI.get()) return;
        if (QuantumClient.get().getWindow().isMouseCaptured()) {
            ImGui.getIO().setMousePos(Float.MAX_VALUE, Float.MAX_VALUE);
        }

        newFrame();
        process(client);
        endFrame();
    }

    private static void newFrame() {
        imGuiGl3.newFrame();
        Window window = QuantumClient.get().getWindow();
        ImGui.getIO().setDisplaySize(window.getWidth(), window.getHeight());
        ImGui.getIO().setDisplayFramebufferScale(1, 1);
        ImGui.getIO().setMousePos((float) window.getMouseX(), (float) window.getMouseY());
        ImGui.newFrame();
    }

    private static void endFrame() {
        ImGui.render();
        ImDrawData drawData = ImGui.getDrawData();
        ImGuiOverlay.imGuiGl3.renderDrawData(drawData);

        ImGuiOverlay.handleInput();
    }

    private static void process(QuantumClient client) {
        GL11.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX(), ImGui.getMainViewport().getPosY() + 18);
        ImGui.setNextWindowSize(ImGui.getMainViewport().getSizeX(), ImGui.getMainViewport().getSizeY() - 18);
        ImGui.setNextWindowCollapsed(false);

        ImGui.getStyle().setWindowPadding(0, 0);
        ImGui.getStyle().setWindowBorderSize(0);

        ImGui.begin("MainDockingArea", ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoBackground | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoScrollbar);
        int id = ImGui.getID("MainDockingArea");
        int dockSpace = ImGui.dockSpace(id);
        ImGui.end();

        if (firstLoop) {
            firstLoop = false;

            ImInt gameDockId = new ImInt(dockSpace);

            ImInt sceneDock = new ImInt(imgui.internal.ImGui.dockBuilderSplitNode(gameDockId.get(), ImGuiDir.Left, 0.15f, null, gameDockId));
            ImInt nodeDock = new ImInt(imgui.internal.ImGui.dockBuilderSplitNode(gameDockId.get(), ImGuiDir.Right, 0.3f, null, gameDockId));
            ImInt assetDock = new ImInt(imgui.internal.ImGui.dockBuilderSplitNode(gameDockId.get(), ImGuiDir.Down, 0.3f, null, gameDockId));
            imgui.internal.ImGui.dockBuilderDockWindow("Node View", nodeDock.get());
            imgui.internal.ImGui.dockBuilderDockWindow("Node Tree", sceneDock.get());
            imgui.internal.ImGui.dockBuilderDockWindow("Asset View", assetDock.get());
            imgui.internal.ImGui.dockBuilderDockWindow("Game", gameDockId.get());
            imgui.internal.ImGui.dockBuilderFinish(gameDockId.get());
        }


        ImGui.getStyle().setWindowPadding(8, 8);
        ImGui.getStyle().setWindowBorderSize(1);
        renderWindows(client);

        ImGui.setNextWindowPos(ImGui.getMainViewport().getPos());
        ImGui.setNextWindowSize(ImGui.getMainViewport().getSizeX(), 18);
        ImGui.setNextWindowCollapsed(true);

        if (QuantumClient.get().getWindow().isMouseCaptured()) {
            ImGui.getIO().setMouseDown(new boolean[5]);
            ImGui.getIO().setMousePos(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        ImGuiOverlay.renderDisplay();
        if (ImGui.begin("MenuBar", ImGuiWindowFlags.NoMove |
                ImGuiWindowFlags.NoCollapse |
                ImGuiWindowFlags.AlwaysAutoResize |
                ImGuiWindowFlags.NoTitleBar |
                ImGuiWindowFlags.MenuBar |
                ImGuiWindowFlags.NoDocking |
                ImGuiWindowFlags.NoDecoration |
                ImGuiInputTextFlags.AllowTabInput)) {
            ImGuiOverlay.renderMenuBar();
        }
        ImGui.end();

        ImGuiOverlay.handleTriggers();
    }

    private static void renderDisplay() {
        if (ImGuiFileDialog.display("Main::loadWorld", ImGuiFileDialogFlags.None, 200, 400, 800, 600)) {
            if (ImGuiFileDialog.isOk()) {
                Path filePathName = Path.of(ImGuiFileDialog.getFilePathName());
                QuantumClient.invoke(() -> QuantumClient.get().openWorld(new WorldStorage(filePathName), FeatureSet.ALL));
            }
            ImGuiFileDialog.close();
        }
    }

    private static void handleTriggers() {
        if (ImGuiOverlay.triggerLoadWorld) {
            ImGuiOverlay.triggerLoadWorld = false;
            ImGuiFileDialog.openModal("Main::loadWorld", "Choose Folder", null, QuantumClient.getGameDir().toAbsolutePath().toString(), "", 1, 7, ImGuiFileDialogFlags.None);
        }
    }

    private static void renderWindows(QuantumClient client) {
        showSceneView();
        showAssetView(client);
        showNodeView(client);
        showGame(client);

        if (ImGuiOverlay.SHOW_ABOUT.get()) ImGui.showAboutWindow();
        if (ImGuiOverlay.SHOW_METRICS.get()) ImGui.showMetricsWindow();
        if (ImGuiOverlay.SHOW_STACK_TOOL.get()) ImGui.showStackToolWindow();
        if (ImGuiOverlay.SHOW_STYLE_EDITOR.get()) ImGui.showStyleEditor();

        if (ImGuiOverlay.SHOW_SHADER_EDITOR.get()) ImGuiOverlay.showShaderEditor();
        if (ImGuiOverlay.SHOW_MODEL_VIEWER.get()) ImGuiOverlay.showModelViewer();
        if (ImGuiOverlay.SHOW_CLASS_ATTACHER.get()) showClassAttacher();
        if (ImGuiOverlay.SHOW_NETWORK_LOGGING.get()) ImGuiOverlay.showNetworkLogging();
    }

    private static void showNetworkLogging() {
        if (ImGui.begin("Network Logging")) {
            ImGui.beginChild("##network_log_area", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY(), true);
            ImGui.text(NET_PIPE_OUT);
            ImGui.endChild();
        }
        ImGui.end();
    }

    private static void showClassAttacher() {
        ImGui.begin("Class Attacher");

        // Input Field
        ImGui.setNextItemWidth(ImGui.getContentRegionAvailX());
        ImGui.inputText("##class_input", inputBuffer1, ImGuiInputTextFlags.AutoSelectAll);

        if (focusInput) {
            ImGui.setKeyboardFocusHere(-1);
            focusInput = false;
        }

        updateFiltered();

        // Suggestions
        if (!filteredClasses.isEmpty()) {
            ImGui.beginChild("##suggestions", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY(), true);
            for (int i = 0; i < filteredClasses.size(); i++) {
                boolean isSelected = i == selectedIndex;
                if (ImGui.selectable(filteredClasses.get(i), isSelected)) {
                    inputBuffer1.set(filteredClasses.get(i));
                    filteredClasses.clear(); // Hide suggestions after selection
                    selectedIndex = -1;
                }
                if (isSelected && ImGui.isItemFocused()) {
                    if (InputSystem.isKeyJustPressed(SDLKeycode.SDLK_BACKSPACE)) { // Backspace
                        inputBuffer1.set("");
                        filteredClasses.clear();
                    }
                }
            }

            ImGui.endChild();

            // Keyboard navigation
            if (InputSystem.isKeyJustPressed(SDLKeycode.SDLK_DOWN)) { // Down
                selectedIndex = (selectedIndex + 1) % filteredClasses.size();
            }
            if (InputSystem.isKeyJustPressed(SDLKeycode.SDLK_UP)) { // Up
                selectedIndex = (selectedIndex - 1 + filteredClasses.size()) % filteredClasses.size();
            }
            if (InputSystem.isKeyJustPressed(SDLKeycode.SDLK_RETURN)) { // Enter
                if (selectedIndex >= 0 && selectedIndex < filteredClasses.size()) {
                    String value = filteredClasses.get(selectedIndex);
                    inputBuffer1.set(value);
                    try {
                        selectedClass = Class.forName(value);
                    } catch (ClassNotFoundException e) {
                        CommonConstants.LOGGER.error("Unable to load already loaded class " + value, e);
                    }
                    filteredClasses.clear();
                    selectedIndex = -1;
                }
            }
        }

        ImGui.end();
    }

    private static void updateFiltered() {
        // TODO: Implement loaded classes filtering :D
//        Class<?>[] allClasses = GamePlatform.get().getLoadedClasses();
//        filteredClasses.clear();
//        if (!inputBuffer1.isEmpty()) {
//            List<String> toSort = new ArrayList<>();
//            for (Class<?> cls : allClasses) {
//                String name = cls.getName();
//                if (name.startsWith("[")) continue;
//                if (name.toLowerCase().contains(inputBuffer1.get().toLowerCase())) {
//                    toSort.add(name);
//                }
//            }
//            toSort.sort(null);
//            List<String> list = new ArrayList<>();
//            long limit = 100;
//            for (String cls : toSort) {
//                if (limit-- == 0) break;
//                list.add(cls);
//            }
//            filteredClasses.addAll(
//                    list
//            );
//        }
    }

//    private static void showJShell(Context jshell) {
//        if (ImGui.begin("Game Shell")) {
//            // Output area
//            ImGui.begin("ConsoleOutput");
//            float scrollY = ImGui.getScrollY();
//            ImGui.textWrapped(baos.toString());
//            if (scrollY == 1.0f) ImGui.setScrollHereY(1.0f); // Auto-scroll
//            ImGui.end();
//
//            // Input field
//            ImGui.separator();
//            ImGui.text("Enter Java expression:");
//            ImGui.inputTextMultiline("##input", inputBuffer, 512);
//            if (ImGui.isItemFocused() && InputSystem.isKeyJustPressed(GLFW.GLFW_KEY_ENTER) && InputSystem.isKeyJustPressed(GLFW.GLFW_KEY_ALT_LEFT) && !inputBuffer.get().isEmpty()) {
//                evaluateShell(jshell);
//            }
//
//            // Evaluate button
//            if (ImGui.button("Evaluate")) {
//                evaluateShell(jshell);
//            }
//
//            ImGui.sameLine();
//            if (ImGui.button("Clear Output")) {
//                baos.reset();
//            }
//        }
//        ImGui.end();
//    }
//
//    private static void evaluateShell(Context jshell) {
//        try {
//            // Get JS bindings
//            Value js = jshell.getBindings("js");
//
//            // Assign host class
//            Class<?> selClass = selectedClass;
//            if (selClass != null)
//                js.putMember("HostClass", jshell.eval("js", "Java.type('" + selClass.getCanonicalName() + "');"));
//
//            // Assign host object
//            Object sel = selected;
//            if (sel != null)
//                js.putMember("hostObject", Value.asValue(sel));
//
//            // Evaluate JS expression
//            Value java = jshell.eval("js", inputBuffer.get());
//            try {
//                String string = java.as(Object.class).toString() + "\n";
//                baos.write(string.getBytes());
//            } catch (IOException e) {
//                CommonConstants.LOGGER.error("Unable to write to output stream", e);
//            }
//        } catch (Exception e) {
//            e.printStackTrace(ps);
//        }
//    }

    private static void showGame(QuantumClient ignoredClient) {
        ImGuiStyle style = ImGui.getStyle();

        // Save the original padding if needed
        ImVec2 originalPadding = new ImVec2(style.getWindowPadding());

        // Set the padding to 0 (or any value you need)
        style.setWindowPadding(0, 0);
        if (ImGui.begin("Game", ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoMove)) {
            screenCoords.mouseX = (int) ((ImGui.getMousePosX() - ImGui.getCursorPosX() - ImGui.getWindowPosX()) * ImGui.getWindowDpiScale());
            screenCoords.mouseY = (int) ((ImGui.getMousePosY() - ImGui.getCursorPosY() - ImGui.getWindowPosY()) * ImGui.getWindowDpiScale());
            float contentRegionAvailX = ImGui.getContentRegionAvailX();
            float contentRegionAvailY = ImGui.getContentRegionAvailY();
            if (screenCoords.width != (int) (contentRegionAvailX * ImGui.getWindowDpiScale()) || screenCoords.height != (int) (contentRegionAvailY * ImGui.getWindowDpiScale())) {
                screenCoords.width = (int) (contentRegionAvailX * ImGui.getWindowDpiScale());
                screenCoords.height = (int) (contentRegionAvailY * ImGui.getWindowDpiScale());

                // Update the game viewport
                QuantumClient.get().onDeferResize((int) contentRegionAvailX, (int) contentRegionAvailY);
            } else {
                screenCoords.width = (int) (contentRegionAvailX * ImGui.getWindowDpiScale());
                screenCoords.height = (int) (contentRegionAvailY * ImGui.getWindowDpiScale());
            }

            ImGui.image(QuantumClient.get().targetFbo.get(0).getObjectId(), contentRegionAvailX, contentRegionAvailY, 0, 1, 1, 0, 1, 1, 1, 1);
        }
        ImGui.end();

        // Restore the original padding
        style.setWindowPadding(originalPadding.x, originalPadding.y);
    }

    private static void showAssetView(QuantumClient client) {
        if (ImGui.begin("Asset View", ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoMove)) {
            // Show a list of all assets
            ResourceManager resourceManager = client.resourceManager;
            if (ImGui.treeNodeEx("Assets", ImGuiTreeNodeFlags.Framed | ImGuiTreeNodeFlags.OpenOnArrow | (resourceManager == null ? ImGuiTreeNodeFlags.Leaf : 0)) && resourceManager != null) {
                for (ResourceCategory category : resourceManager.getResourceCategories()) {
                    if (ImGui.treeNodeEx(category.getName(), ImGuiTreeNodeFlags.Framed | ImGuiTreeNodeFlags.OpenOnArrow)) {
                        for (Map.Entry<Identifier, StaticResource> entry : category.mapEntries().entrySet()) {
                            StaticResource resource = entry.getValue();
                            Identifier location = entry.getKey();
                            if (ImGui.treeNodeEx(location.toString(), ImGuiTreeNodeFlags.Framed | ImGuiTreeNodeFlags.OpenOnArrow)) {
                                if (location.path().endsWith(".png")) {
                                    Texture texture = QuantumClient.get().getTextureManager().getTexture(location);
                                    ImGui.image(texture.getObjectId(), 64, 64, 0, 0, 1, 1);
                                } else if (location.path().endsWith(".frag")) {
                                    byte[] bytes = resource.readBytes();
                                    if (bytes != null) {
                                        String shader = new String(bytes, StandardCharsets.UTF_8);
                                        TextEditor textEditor = textEditors.get(location);
                                        if (textEditor == null) {
                                            textEditor = new TextEditor();
                                            textEditors.put(location, textEditor);
                                        }

                                        textEditor.setText(shader);
                                        textEditor.setReadOnly(true);
                                        textEditor.setLanguageDefinition(glsl);
                                        textEditor.setColorizerEnable(true);
                                        textEditor.setShowWhitespaces(false);

                                        TextEditorCoordinates coordinates = textEditorPos.get(location);
                                        if (coordinates != null) textEditor.setCursorPosition(coordinates);

                                        float v = textEditor.getTotalLines() * ImGui.getFont().getFontSize() + 16;
                                        textEditor.render("Shader Editor - " + location, ImGui.getContentRegionAvailX(), v);

                                        if (textEditor.isCursorPositionChanged()) {
                                            textEditorPos.put(location, textEditor.getCursorPosition());
                                        }


                                        if (ImGui.isItemHovered()) {
                                            ImGui.setTooltip("Click to copy to clipboard");
                                            if (ImGui.isItemClicked()) {
                                                ImGui.setClipboardText(shader);
                                            }
                                        }
                                    }
                                }
                                ImGui.treePop();
                            } else {
                                TextEditor remove = textEditors.remove(location);

                                if (remove != null) {
                                    remove.destroy();
                                }
                            }
                        }
                        ImGui.treePop();
                    }
                }
                ImGui.treePop();
            }
        }
        ImGui.end();
    }

    private static void showNodeView(QuantumClient ignoredClient) {
        Object sel = selected;
        if (ImGui.begin("Node View", ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoMove)) {
            if (sel != null) {
                ImGui.pushID(sel.hashCode());
                if (ImGui.treeNodeEx("Game Object", ImGuiTreeNodeFlags.Framed | ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.SpanAvailWidth | ImGuiTreeNodeFlags.DefaultOpen)) {
                    renderComponent(sel);
                    ImGui.treePop();
                }

                if (sel instanceof GameNode) {
                    for (GameComponent component : ((GameNode) sel).getComponents()) {
                        if (ImGui.treeNodeEx(component.getClass().getName(), ImGuiTreeNodeFlags.Framed | ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.SpanAvailWidth, component instanceof GameComponent ? component.getClass().getSimpleName() : component.getClass().getSimpleName() + (DeveloperMode.enabled ? ".java" : ".class"))) {
                            renderComponent(component);
                            ImGui.treePop();
                        }
                    }
                }
                ImGui.popID();
            }
        }
        ImGui.end();
    }

    public static void renderComponent(final @Nullable Object component) {
        if (component == null) return;
        Class<?> clazz = component.getClass();
        renderDebugRenderer(component, null);

        if (ImGui.treeNodeEx("Fields", ImGuiTreeNodeFlags.OpenOnArrow | ImGuiTreeNodeFlags.SpanAvailWidth)) {
            Set<Field> fields = new HashSet<>();
            Stack<Class<?>> stack = new Stack<>();
            while (true) {
                for (Field field : component.getClass().getDeclaredFields()) {
                    processField(component, field, fields);
                }
                for (Field field : component.getClass().getFields()) {
                    processField(component, field, fields);
                }
                for (Class<?> anInterface : clazz.getInterfaces()) {
                    if (!stack.contains(anInterface) && anInterface != Object.class)
                        stack.push(anInterface);
                }
                if (clazz.getSuperclass() != null && !stack.contains(clazz.getSuperclass()) && !clazz.getSuperclass().equals(Object.class))
                    stack.push(clazz.getSuperclass());
                if (stack.isEmpty()) break;
                clazz = stack.pop();
            }
            ImGui.treePop();
        }
    }

    private static void processField(@NotNull Object component, Field field, Set<Field> fields) {
        if (fields.contains(field)) return;
        fields.add(field);
        if ((!Modifier.isPublic(field.getModifiers()) && !field.isAnnotationPresent(ShowInNodeView.class)
                || field.isSynthetic()
                || field.isAnnotationPresent(HiddenNode.class))
                && !SHOW_HIDDEN_FIELDS.get()
                || Modifier.isStatic(field.getModifiers()))
            return;

        boolean readOnly = Modifier.isFinal(field.getModifiers());

        Runnable runnable = renderObject(component, field, readOnly);
        ImGui.pushID(field.getName());
        if (ImGui.treeNodeEx(field.getName(), ImGuiTreeNodeFlags.DefaultOpen | ImGuiTreeNodeFlags.SpanAvailWidth)) {
            if (runnable != null) {
                runnable.run();
            }
            ImGui.treePop();
        }
        ImGui.popID();
    }

    @SuppressWarnings("unchecked")
    public static @Nullable Runnable renderObject(@Nullable Object component, Field field, boolean readOnly) {
        try {
            field.setAccessible(true);
            Object object = field.get(component);
            if (object == null) return null;
            Class<?> clazz = object.getClass();
            if (true) {
                return () -> {
                    renderDebugRenderer(object, o -> {
                        try {
                            field.set(component, o);
                        } catch (Throwable e) {
                            CommonConstants.LOGGER.error("Unable to set field {}", field.getName(), e);
                        }
                    });
                };
            }

            if (field.getType().isPrimitive()) return () -> {
                if (object instanceof Number number) {
                    num(component, field, readOnly, number, object);
                } else if (object instanceof Boolean) {

                } else {
                    ImGui.text(String.valueOf(object));
                }
            };
            if (object instanceof Texture texture) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), "Texture")) {
                        ImGui.image(texture.getObjectId(), ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailX());
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof String s) {
                return () -> {
                    ImString ims = new ImString(s);
                    if (ImGui.inputText(field.getName(), ims, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                        try {
                            field.set(component, ims.get());
                        } catch (IllegalAccessException e) {
                            // ignore
                        }
                    }
                };
            } else if (object instanceof Identifier Identifier) {
                return () -> {
                    ImString s = new ImString(Identifier.location());
                    ImString n = new ImString(Identifier.path());

                    try {
                        ImGui.sameLine();
                        ImGui.setNextItemWidth((ImGui.getWindowSizeX() - ImGui.getCursorPosX()) / 2 - 5);

                        if (ImGui.inputText(field.getName(), s, readOnly ? ImGuiInputTextFlags.ReadOnly : 0)) {
                            field.set(component, new Identifier(s.get(), n.get()));
                        }
                        ImGui.sameLine();
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - ImGui.getCursorPosX() - 5);
                        if (ImGui.inputText(" : ", n, readOnly ? ImGuiInputTextFlags.ReadOnly : 0)) {
                            field.set(component, new Identifier(s.get(), n.get()));
                        }
                    } catch (Exception e) {
                        CommonConstants.LOGGER.error("Unable to set namespace id", e);
                    }
                };
            } else if (object instanceof Enum<?>) {
                return () -> {
                    if (!readOnly) {
                        if (ImGui.beginCombo(field.getName(), object.toString())) {
                            //noinspection rawtypes
                            for (Object enumValue : EnumSet.allOf((Class<? extends Enum>) field.getType())) {
                                if (ImGui.selectable(enumValue.toString(), object.equals(enumValue))) {
                                    try {
                                        field.set(component, enumValue);
                                    } catch (IllegalAccessException e) {
                                        // ignore
                                    }
                                }
                            }

                            ImGui.endCombo();
                        }
                    } else {
                        ImGui.text(object.toString());
                    }
                };
            } else if (object instanceof Color color) {
                return () -> {
                    float[] c = new float[4];
                    c[0] = color.r;
                    c[1] = color.g;
                    c[2] = color.b;
                    c[3] = color.a;
                    if (ImGui.colorEdit4(field.getName(), c)) {
                        try {
                            field.set(component, color.set(c[0], c[1], c[2], c[3]));
                        } catch (IllegalAccessException e) {
                            // ignore
                        }
                    }
                };
            } else if (object instanceof Vector3f vec3) {
                return () -> {
                    float[] v = new float[3];
                    v[0] = vec3.x;
                    v[1] = vec3.y;
                    v[2] = vec3.z;
                    if (ImGui.inputFloat3(field.getName(), v)) {
                        try {
                            field.set(component, vec3.set(v[0], v[1], v[2]));
                        } catch (IllegalAccessException e) {
                            // ignore
                        }
                    }
                };
            } else if (object instanceof Vector2f vec3) {
                return () -> {
                    float[] v = new float[2];
                    v[0] = vec3.x;
                    v[1] = vec3.y;
                    if (ImGui.inputFloat2(field.getName(), v)) {
                        try {
                            field.set(component, vec3.set(v[0], v[1]));
                        } catch (IllegalAccessException e) {
                            // ignore
                        }
                    }
                };
            } else if (object instanceof Vector4f vec4) {
                return () -> {
                    float[] v = new float[4];
                    v[0] = vec4.x;
                    v[1] = vec4.y;
                    v[2] = vec4.z;
                    v[3] = vec4.w;
                    if (ImGui.inputFloat4(field.getName(), v)) {
                        try {
                            field.set(component, vec4.set(v[0], v[1], v[2], v[3]));
                        } catch (IllegalAccessException e) {
                            // ignore
                        }
                    }
                };
            } else if (object instanceof UUID uuid) {
                return () -> {
                    ImString text = new ImString(uuid.toString());
                    if (ImGui.inputText(field.getName(), text, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                        try {
                            field.set(component, UUID.fromString(text.get()));
                        } catch (IllegalArgumentException | IllegalAccessException ignored) {
                            // ignore
                        }
                    }
                };
            } else if (object instanceof GameNode gameObject) {
                return () -> {
                    if (ImGui.treeNode(System.identityHashCode(gameObject), gameObject.getName() == null ? gameObject.toString() : gameObject.getName())) {
                        renderComponent(gameObject);
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof List<?> list) {
                return () -> {
                    ImInt selected = new ImInt(-1);
                    List<String> result = new ArrayList<>();
                    for (Object o : list) {
                        String string = o.toString();
                        result.add(string);
                    }
                    ImGui.listBox("##List" + field.hashCode(), selected, result.toArray(new String[0]));
                    ImGui.sameLine(200);
                    ImGui.setNextItemWidth(ImGui.getWindowSizeX() - ImGui.getCursorPosX() - 110);
                    if (ImGui.treeNode(field.getName())) {
                        if (selected.get() >= 0 && selected.get() < list.size()) {
                            renderComponent(list.get(selected.get()));
                        }
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof Map.Entry<?, ?> entry) {
                return () -> {
                    ImGui.setNextItemWidth((ImGui.getWindowSizeX() - 200) / 2 - 5);
                    ImGui.text(entry.getKey().toString());
                    ImGui.sameLine((ImGui.getWindowSizeX() - 200) / 2 + 200);
                    ImGui.setNextItemWidth((ImGui.getWindowSizeX() - 200) / 2 - 5);
                    renderComponent(entry.getValue());
                };
            } else if (object instanceof AtomicReference<?> reference) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        renderComponent(reference.get());
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof AtomicBoolean atomicBoolean) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        ImBoolean b = new ImBoolean(atomicBoolean.get());
                        if (ImGui.checkbox(field.getName(), b)) {
                            atomicBoolean.set(b.get());
                        }
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof AtomicLong atomicLong) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        ImString text = new ImString(atomicLong.get() + "");
                        if (ImGui.inputText(field.getName(), text)) {
                            try {
                                atomicLong.set(Long.parseLong(text.get()));
                            } catch (NumberFormatException ignored) {

                            }
                        }
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof AtomicInteger atomicInteger) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        ImInt i = new ImInt(atomicInteger.get());
                        if (ImGui.inputInt(field.getName(), i)) {
                            atomicInteger.set(i.get());
                        }
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof BlockVec vec) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        int[] i = new int[]{vec.x, vec.y, vec.z};
                        if (ImGui.inputInt3(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                            vec.set(i[0], i[1], i[2]);
                        }
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof ChunkVec vec) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        int[] i = new int[]{vec.x, vec.y, vec.z};
                        if (ImGui.inputInt3(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                            vec.set(i[0], i[1], i[2]);
                        }
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof Vector3i vec) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        int[] i = new int[]{vec.x, vec.y, vec.z};
                        if (ImGui.inputInt3(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                            vec.set(i[0], i[1], i[2]);
                        }
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof Vector3f vec) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        float[] i = new float[]{vec.x, vec.y, vec.z};
                        if (ImGui.inputFloat3(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                            vec.set(i[0], i[1], i[2]);
                        }
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof Vector2i vec) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        int[] i = new int[]{vec.x, vec.y};
                        if (ImGui.inputInt2(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                            vec.set(i[0], i[1]);
                        }
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof Vector2f vec) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        float[] i = new float[]{vec.x, vec.y};
                        if (ImGui.inputFloat2(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                            vec.set(i[0], i[1]);
                        }
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof Vector4i vec) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        int[] i = new int[]{vec.x, vec.y, vec.z, vec.w};
                        if (ImGui.inputInt4(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                            vec.set(i[0], i[1], i[2], i[3]);
                        }
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof Vector4f vec) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                        float[] i = new float[]{vec.x, vec.y, vec.z, vec.w};
                        if (ImGui.inputFloat4(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                            vec.set(i[0], i[1], i[2], i[3]);
                        }
                        ImGui.treePop();
                    }
                };
            } else if (object instanceof BlockState state) {
                return () -> {
                    if (ImGui.treeNode(field.hashCode(), field.getName())) {
                        ImGui.text("ID: " + state.getBlock().getId());

                        if (ImGui.beginTable("##BlockState[" + System.identityHashCode(state), 2, ImGuiTableFlags.Borders)) {
                            ImGui.tableHeadersRow();
                            ImGui.tableSetColumnIndex(0);
                            ImGui.text("Key");
                            ImGui.tableSetColumnIndex(1);
                            ImGui.text("Value");

                            ImGui.tableNextRow();
                            for (StatePropertyKey<?> key : state.getBlock().getDefinition().getKeys()) {
                                ImGui.tableSetColumnIndex(0);
                                ImGui.text(key.getName());
                                ImGui.tableSetColumnIndex(1);
                                ImGui.text(String.valueOf(state.get(state.getDefinition().keyByName(key.getName()))));
                            }
                        }
                        ImGui.endTable();

                        ImGui.treePop();
                    }
                };
            } else if (object instanceof Quaternionf quat) {
                return () -> {
                    ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                    ImGui.combo("Rotation Type", rotType, "Euler\0Quaternionf\0");
                    ImGui.sameLine(180);

                    if (rotType.get() == 0) {
                        if (ImGui.treeNode(field.hashCode(), field.getName())) {
                            ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                            Vector3f eulerAnglesXYZ = quat.getEulerAnglesXYZ(tmp3f);
                            float[] i = new float[]{eulerAnglesXYZ.x, eulerAnglesXYZ.y, eulerAnglesXYZ.z};
                            if (ImGui.inputFloat3(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                                quat.identity();
                                quat.rotateXYZ(i[0], i[1], i[2]);
                            }
                            ImGui.treePop();
                        }
                    } else if (rotType.get() == 1) {
                        if (ImGui.treeNode(field.hashCode(), field.getName())) {
                            ImGui.setNextItemWidth(ImGui.getWindowSizeX() - 200);
                            float[] i = new float[]{quat.x, quat.y, quat.z, quat.w};
                            if (ImGui.inputFloat4(field.getName(), i, readOnly ? ImGuiInputTextFlags.ReadOnly : 0) && !readOnly) {
                                quat.set(i[0], i[1], i[2], i[3]);
                            }
                            ImGui.treePop();
                        }
                    }
                };
            }
            if (isAnnotationPresent(field.getType(), ShowInNodeView.class) && ImGui.treeNode(field.hashCode(), field.getName())) {
                return () -> {
                    renderComponent(object);
                    ImGui.treePop();
                };
            } else {
                return null;
            }
        } catch (Throwable e) {
            ImGui.textColored(1f, 0.5f, 0.5f, 1f, e.getMessage());
        }

        return null;
    }

    private static boolean hasDebugRenderer(Class<?> clazz) {
        boolean present = clazz.isAnnotationPresent(DebugRenderer.class);
        Stack<Class<?>> stack = new Stack<>();
        stack.push(clazz);
        while (!clazz.equals(Object.class)) {
            clazz = stack.pop();
            if (present) {
                return true;
            }
            if (clazz.getSuperclass() != null) {
                stack.push(clazz.getSuperclass());
            }
            for (Class<?> i : clazz.getInterfaces()) {
                stack.push(i);
            }
        }
        return present;
    }

    @Contract(pure = true)
    private static <T extends Annotation> boolean isAnnotationPresent(@NotNull Class<?> type, Class<T> anno) {
        if (type.isAnnotationPresent(anno)) {
            return true;
        }
//        while (type != Object.class) {
//
//            type = type.getSuperclass();
//        }

        return false;
    }

    private static void num(@Nullable Object component, Field field, boolean readOnly, Number number, Object object) {

    }

    private static void showSceneView() {
        if (ImGui.begin("Node Tree", ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoMove)) {
            // Recursively render the scene view
            if (ImGui.treeNode(System.identityHashCode(QuantumClient.get()), "Main")) {
                renderGameNode(QuantumClient.get());
                ImGui.treePop();
            }
//            if (ImGui.treeNode(System.identityHashCode(QuantumClient.get().profiler), "Profiler")) {
//                QuantumClient.get().profiler.setProfiling(true);
//                renderProfiler(QuantumClient.get().profiler);
//                ImGui.treePop();
//            } else {
//                QuantumClient.get().profiler.setProfiling(false);
//            }


//            if (ImGui.treeNode(1, "Foreground")) {
//                if (QuantumClient.get().screen != null) {
//                    renderUINode(QuantumClient.get().screen);
//                }
//                ImGui.treePop();
//            }

            if (ImGui.treeNode(2, "Selected Class")) {
                if (selectedClass != null) {
                    renderClass(selectedClass);
                }
                ImGui.treePop();
            }

            ImGui.getWindowSizeX();
        } else {
//            QuantumClient.get().profiler.setProfiling(false);
        }
        ImGui.end();
    }

//    private static void renderProfiler(Profiler profiler) {
//        if (nextProfilerCollect < System.currentTimeMillis()) {
//            profilerData = profiler.collect();
//            List<Thread> list = new ArrayList<>();
//            for (Thread thread : profilerData.getThreads()) {
//                list.add(thread);
//            }
//            list.sort(Comparator.comparing(Thread::getName));
//            threads = list;
//            nextProfilerCollect = System.currentTimeMillis() + 1000;
//        }
//        for (Thread thread : threads) {
//            if (ImGui.treeNode("ProfilerThread." + thread.getId(), thread.getName())) {
//                ThreadSection.FinishedThreadSection threadSection = profilerData.getThreadSection(thread);
//                if (threadSection != null) {
//                    extracted(thread, threadSection);
//                }
//                ImGui.treePop();
//            }
//        }
//    }
//
//    private static void extracted(Thread thread, ThreadSection.FinishedThreadSection threadSection) {
//        for (Map.Entry<String, Section.FinishedSection> section : threadSection.getData().entrySet()) {
//            String strId = "ProfilerThread." + thread.getId() + "/" + section.getKey();
//            if (ImGui.treeNode(strId, section.getKey() + " (" + section.getValue().getNanos() + "ms)")) {
//                for (Map.Entry<String, Integer> info : section.getValue().getStats().entrySet()) {
//                    ImGuiEx.editInt(info.getKey(), strId + ":" + info.getKey(), info::getValue, v -> {});
//                }
//                Section.FinishedSection finishedSection = section.getValue();
//                extracted(strId, thread, finishedSection);
//                ImGui.treePop();
//            }
//        }
//    }
//
//    private static void extracted(String parentId, Thread thread, Section.FinishedSection threadSection) {
//        for (Map.Entry<String, Section.FinishedSection> section : threadSection.getData().entrySet()) {
//            String strId = parentId + "/" + section.getKey();
//            if (ImGui.treeNode(strId, section.getKey() + " (" + section.getValue().getNanos() / 1000000L + "ms)")) {
//                for (Map.Entry<String, Integer> info : section.getValue().getStats().entrySet()) {
//                    ImGuiEx.editInt(info.getKey(), strId + ":" + info.getKey(), info::getValue, v -> {});
//                }
//                Section.FinishedSection finishedSection = section.getValue();
//                extracted(strId, thread, finishedSection);
//                ImGui.treePop();
//            }
//        }
//    }

    private static final ImVec2 rectMin = new ImVec2();

    private static final ImVec2 rectMax = new ImVec2();
    private static final ImVec2 mousePos = new ImVec2();

    private static void renderClass(Class<?> selectedClass) {
        for (Field field : selectedClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;

            if (field.isAnnotationPresent(ShowInNodeView.class) && !field.isAnnotationPresent(HideInNodeView.class) || SHOW_HIDDEN_FIELDS.get()) {
                ImGui.pushID(field.hashCode());
                try {
                    field.setAccessible(true);
                    Object object = field.get(QuantumClient.get().getScreen());
                    if (object != null) {
                        if (ImGui.treeNode(field.hashCode(), field.getName())) {
                            try {
                                renderObject(object);
                            } finally {
                                ImGui.treePop();
                            }
                        }
                        if (ImGui.isItemClicked(ImGuiMouseButton.Right)) {
                            selected = object;
                        }
                    }
                } catch (Throwable e) {
                    ImGui.textColored(1f, 0.5f, 0.5f, 1f, e.getMessage());
                } finally {
                    ImGui.popID();
                }
            }
        }
    }

    public static void renderObject(Object object) {
        renderDebugRenderer(object, null);

        ImGui.pushID("$");
        if (ImGui.treeNode("Fields")) {
            for (Field field : object.getClass().getFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;

                if (field.isAnnotationPresent(ShowInNodeView.class) && !field.isAnnotationPresent(HideInNodeView.class) || SHOW_HIDDEN_FIELDS.get()) {
                    ImGui.pushID(field.hashCode());
                    try {
                        field.setAccessible(true);
                        if (ImGui.treeNode(field.hashCode(), field.getName() + " (" + field.getType().getSimpleName() + ".class)")) {
                            try {
                                Object component = field.get(object);
                                if (component != null) {
                                    renderObject(component);
                                }
                            } finally {
                                ImGui.treePop();
                            }
                        }
                        if (ImGui.isItemHovered()) {
                            ImGui.beginTooltip();
                            ImGui.textColored(1f, 1f, 0f, 1f, field.getName());
                            try {
                                field.setAccessible(true);
                                Object component = field.get(object);
                                if (component != null) {
                                    ImGui.text(component.getClass().getSimpleName());
                                    ImGui.text("");
                                    ImGui.textColored(.5f, .5f, .5f, 1f, String.valueOf(component));
                                }
                            } catch (Throwable ignored) {
                                ImGui.textColored(.5f, .5f, .5f, 1f, "null");
                            }
                            ImGui.endTooltip();
                        }
                        if (ImGui.isItemClicked(ImGuiMouseButton.Right)) {
                            selected = object;
                        }
                    } catch (Throwable e) {
                        ImGui.textColored(1f, 0.5f, 0.5f, 1f, e.getMessage());
                    } finally {
                        ImGui.popID();
                    }
                }
            }
            ImGui.treePop();
        }
        ImGui.popID();
    }

    @SuppressWarnings("unchecked")
    private static void renderDebugRenderer(Object object, @Nullable Consumer<Object> setter) {
        @SuppressWarnings("rawtypes") Renderer renderer = renderers.get(object.getClass());
        if (renderer != null) {
            renderer.render(object, null);
            return;
        }

        Stack<Class<?>> stack = new Stack<>();
        Class<?> clazz = object.getClass();
        stack.push(object.getClass());
        while (!clazz.equals(Object.class)) {
            if (stack.isEmpty()) {
                break;
            }

            clazz = stack.pop();
            if (renderers.containsKey(clazz)) {
                @SuppressWarnings("rawtypes") Renderer value = renderers.get(clazz);
                renderers.put(object.getClass(), value);
                value.render(object, setter);
                return;
            }
            if (clazz.isAnnotationPresent(DebugRenderer.class)) {
                DebugRenderer debugRenderer = clazz.getAnnotation(DebugRenderer.class);
                try {
                    @SuppressWarnings("rawtypes") Renderer value = debugRenderer.value().getConstructor().newInstance();
                    renderers.put(object.getClass(), value);
                    value.render(object, setter);
                    return;
                } catch (Throwable e) {
                    CommonConstants.LOGGER.error("Failed to instantiate renderer for {}", clazz.getName(), e);
                    @SuppressWarnings("rawtypes") Renderer value = new ErrorRenderer<>(e);
                    renderers.put(object.getClass(), value);
                    value.render(object, setter);
                    return;
                }
            }
            for (Class<?> i : clazz.getInterfaces()) {
                stack.push(i);
            }
            if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
                stack.push(clazz.getSuperclass());
            }
        }

        ErrorRenderer<Object> value = new ErrorRenderer<>(new IllegalStateException("No renderer found for " + object.getClass().getName()));
        renderers.put(object.getClass(), value);
        value.render(object, setter);
    }

    private static void renderGameNode(GameNode object) {
        for (GameNode child : object.getChildren()) {
            if (ImGui.treeNodeEx(System.identityHashCode(child), selected == child ? ImGuiTreeNodeFlags.Selected : ImGuiTreeNodeFlags.OpenOnArrow, child.getName() == null ? child.toString() : child.getName())) {
                ImGui.getItemRectMin(rectMin);
                ImGui.getItemRectMax(rectMax);
                ImGui.getMousePos(mousePos);
                if (ImGui.isItemActive() || ImGui.isItemFocused()) {
                    selected = child;
                }
                if (ImGui.isItemHovered() && child.getDescription() != null)
                    ImGui.setTooltip(child.getDescription());

                renderGameNode(child);

                ImGui.treePop();
            } else {
                ImGui.getItemRectMin(rectMin);
                ImGui.getItemRectMax(rectMax);
                ImGui.getMousePos(mousePos);
                if (ImGui.isItemActive() || ImGui.isItemFocused()) {
                    selected = child;
                }
                if (ImGui.isItemHovered() && child.getDescription() != null)
                    ImGui.setTooltip(child.getDescription());
            }
        }
    }

    private static void renderUINode(Widget widget) {
        if (widget instanceof ContainerWidget container) {
            for (Widget child : container.getWidgets()) {
                if (ImGui.treeNodeEx(System.identityHashCode(child), selected == child ? ImGuiTreeNodeFlags.Selected : ImGuiTreeNodeFlags.OpenOnArrow, child.toString())) {
                    renderUINode(child);

                    ImGui.treePop();
                }
            }
        }
    }

    private static void showModelViewer() {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (ImGui.begin("Model Viewer", ImGuiOverlay.getDefaultFlags())) {
            if (ImGui.button("Reload")) {
                List<String> list = new ArrayList<>();
//                for (EntityType<?> entityType : QuantumClient.get().entityModelManager.getRegistry().keySet()) {
//                    Identifier id = entityType.getId();
//                    String string = Objects.toString(id);
//                    list.add(string);
//                }
//                list.sort(String.CASE_INSENSITIVE_ORDER);
                modelViewerList = list.toArray(new String[0]);
            }

            ImGui.text("Select Model:");
            ImGui.sameLine();
            ImGui.listBox("##ModelViewer::ListBox", MODEL_VIEWER_LIST_INDEX, modelViewerList);

            if (modelViewerList.length == 0) {
                ImGui.text("No models found");
            } else {

                String s = modelViewerList[MODEL_VIEWER_LIST_INDEX.get()];
                Identifier id = new Identifier(s);
//                EntityType<?> entityType = Registries.ENTITY_TYPE.get(id);
//                if (entityType != null) {
//                    Model model = QuantumClient.get().entityModelManager.getFinished(entityType);
//                    if (model != null) {
//                        if (ImGui.treeNode("Model")) {
//                            ImGui.text("Model Name:");
//                            ImGui.sameLine();
//                            ImGui.text(s);
//
//                            if (ImGui.treeNode("Nodes")) {
//                                for (Node node : model.nodes) {
//                                    drawNode(node);
//                                }
//
//                                ImGui.treePop();
//                            }
//
//                            ImGui.treePop();
//                        }
//                    }
//                }
            }

            if (ImGui.button("Close")) {
                ImGuiOverlay.SHOW_MODEL_VIEWER.set(false);
            }

        }
        ImGui.end();
    }

//    private static void drawNode(Node node) {
//        if (ImGui.treeNode(node.id)) {
//            ImGui.text("Name:");
//            ImGui.sameLine();
//            ImGui.text(node.id);
//
//            ImGui.text("Local Transform:");
//            ImGui.treePush();
//            drawTransform(node.localTransform, node);
//            ImGui.treePop();
//
//            ImGui.text("Global Transform:");
//            ImGui.treePush();
//            drawTransform(node.globalTransform, node);
//            ImGui.treePop();
//
//            for (Node child : node.getChildren()) {
//                drawNode(child);
//            }
//
//            ImGui.treePop();
//        }
//    }

//    private static void drawTransform(Matrix4 node, Node node1) {
//        Vector3f translation = node.getTranslation(TRANSLATE_TMP);
//        drawVec3("Translation:", translation);
//
//        Vector3f scale = node1.localTransform.getScale(SCALE_TMP);
//        drawVec3("Scale:", scale);
//
//        Quaternionf rotation = node1.localTransform.getRotation(ROTATE_TMP);
//        ImGui.text("Rotation:");
//        ImGui.sameLine();
//        ImGui.text("X: " + rotation.x + " Y: " + rotation.y + " Z: " + rotation.z + " W: " + rotation.w);
//    }

    private static void drawVec3(String name, Vector3f vec3) {
        ImGui.text(name);
        ImGui.sameLine();
        ImGui.text("X: " + vec3.x + " Y: " + vec3.y + " Z: " + vec3.z);
    }

    private static void handleInput() {
//        if (GLFW.glfwGetKey(QuantumClient.get().getWindow().getObjectId(), GLFW.GLFW_KEY_LEFT_CONTROL) != GLFW.GLFW_PRESS) return;
//
//        if (GLFW.glfwGetKey(QuantumClient.get().getWindow().getObjectId(), GLFW.GLFW_KEY_O) != GLFW.GLFW_PRESS)
//            ImGuiOverlay.triggerLoadWorld = true;
//        else if (GLFW.glfwGetKey(QuantumClient.get().getWindow().getObjectId(), GLFW.GLFW_KEY_P) != GLFW.GLFW_PRESS))
//            ImGuiOverlay.SHOW_PLAYER_UTILS.set(!ImGuiOverlay.SHOW_PLAYER_UTILS.get());
//        else if (InputSystem.isKeyJustPressed(GLFW.GLFW_KEY_G))
//            ImGuiOverlay.SHOW_GUI_UTILS.set(!ImGuiOverlay.SHOW_GUI_UTILS.get());
//        else if (InputSystem.isKeyJustPressed(GLFW.GLFW_KEY_F4))
//            ImGuiOverlay.SHOW_CHUNK_SECTION_BORDERS.set(!ImGuiOverlay.SHOW_CHUNK_SECTION_BORDERS.get());
    }

    private static void renderMenuBar() {
        if (ImGui.beginMenuBar()) {
            if (ImGui.beginMenu("File")) {
                if (ImGui.menuItem("Load World...", "Ctrl+O")) {
                    ImGuiOverlay.triggerLoadWorld = true;
                }
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("Edit")) {
                ImGui.menuItem("Player Editor", "Ctrl+P", ImGuiOverlay.SHOW_PLAYER_UTILS);
                ImGui.menuItem("Gui Editor", "Ctrl+G", ImGuiOverlay.SHOW_GUI_UTILS);
                ImGui.menuItem("Shader Editor", "", ImGuiOverlay.SHOW_SHADER_EDITOR);
                ImGui.menuItem("Skybox Editor (Deprecated)", "", ImGuiOverlay.SHOW_SKYBOX_EDITOR);
                ImGui.separator();
                if (ImGui.menuItem("Hook Game Crash", "", crashHooked, !crashHooked)) {
                    crashHooked = true;
                    QuantumClient.setCrashHook(caller -> {
                        CRASH_HOOK.accept(caller.getThrowable());
                    });
                }
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("View")) {
                ImGui.menuItem("Utils", null, ImGuiOverlay.SHOW_UTILS);
                ImGui.separator();
                ImGui.menuItem("Chunks", null, ImGuiOverlay.SHOW_CHUNK_DEBUGGER);
                ImGui.menuItem("Chunk Node Borders", "Ctrl+F4", ImGuiOverlay.SHOW_CHUNK_SECTION_BORDERS);
                ImGui.separator();
                ImGui.menuItem("InspectionRoot", "Ctrl+P", ImGuiOverlay.SHOW_PROFILER);
                ImGui.menuItem("Render Pipeline", null, ImGuiOverlay.SHOW_RENDER_PIPELINE);
                if (ImGui.menuItem("GUI Debug", null, DeveloperMode.isDevFlagEnabled(DevFlag.GuiDebug))) {
                    DeveloperMode.setDevFlagEnabled(DevFlag.GuiDebug, !DeveloperMode.isDevFlagEnabled(DevFlag.GuiDebug));
                }
                ImGui.menuItem("Model Viewer", null, ImGuiOverlay.SHOW_MODEL_VIEWER);
                ImGui.menuItem("Network Logging", null, ImGuiOverlay.SHOW_NETWORK_LOGGING);
                ImGui.separator();
                ImGui.menuItem("Show Hidden Fields", null, SHOW_HIDDEN_FIELDS);
                ImGui.menuItem("Show Occlusion Debug", null, SHOW_OCCLUSION_DEBUG);
                ImGui.separator();
                ImGui.menuItem("Classes", null, SHOW_CLASS_ATTACHER);
                ImGui.menuItem("JShell", null, SHOW_JSHELL);
                ImGui.endMenu();
            }

            boolean ext = extensionsEnabled;
            if (!ext) {
                ImGui.beginDisabled();
            }
            if (!extensionsChecked) {
                if (!extensions()) {
                    extensionsEnabled = false;
                }
                extensionsChecked = true;
            }
            if (extensionsEnabled) {
                if (ImGui.beginMenu("Extensions")) {
                    extensions();
                    ImGui.endMenu();
                }
            }
            if (!ext) {
                ImGui.endDisabled();
            }

            if (ImGui.beginMenu("Help")) {
                ImGui.menuItem("About", null, ImGuiOverlay.SHOW_ABOUT);
                ImGui.separator();

                ImGui.menuItem("Metrics", null, ImGuiOverlay.SHOW_METRICS);
                ImGui.menuItem("Stack Tool", null, ImGuiOverlay.SHOW_STACK_TOOL);
                ImGui.menuItem("Style Editor", null, ImGuiOverlay.SHOW_STYLE_EDITOR);
                ImGui.endMenu();
            }

            if (ImGui.beginMenu("Gizmos")) {
                @Nullable ClientWorld clientWorld = QuantumClient.get().getWorld();
                if (clientWorld instanceof ClientWorld world) {
//                    for (String category : world.getGizmoCategories()) {
//                        if (ImGui.menuItem("Gizmo '" + category + "'", null, world.isGimzoCategoryEnabled(category))) {
//                            world.toggleGizmoCategory(category);
//                        }
//                    }
                }
                ImGui.endMenu();
            }
            if (ImGui.beginMenu("Resources")) {
                if (ImGui.menuItem("Reload Resources", "F1+R")) {
//                    QuantumClient.get().reloadResourcesAsync();
                    // TODO: reload resources
                }
                ImGui.endMenu();
            }

            ImGui.text(" FPS: " + QuantumClient.getFps() + " ");
            ImGui.sameLine();
            QuantumServer server = QuantumServer.get();
            if (server != null) {
                ImGui.text(" Server TPS: " + QuantumServer.getCurrentTps() + " ");
                ImGui.sameLine();
            }
            ImGui.text(" Window ID: " + QuantumClient.get().getWindow().getObjectId() + " ");
            ImGui.endMenuBar();
        }
    }

    /**
     * To be injected by mixins :D
     *
     * @return whether extensions are enabled
     */
    public static boolean extensions() {
        return false;
    }

    private static void showShaderEditor() {
        ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
        if (ImGui.begin("Shader Editor", ImGuiOverlay.getDefaultFlags())) {
            if (ImGui.treeNode("Shader::SSAO", "SSAO")) {
                ImGuiEx.editFloat("iGamma", "Shader::SSAO::iGamma", ImGuiOverlay.I_GAMMA::get, ImGuiOverlay.I_GAMMA::set);
                ImGui.treePop();
            }
            if (ImGui.treeNode("Shader::Debug", "Debugging")) {
                ImGuiEx.editInt("State", "Shader::Debug::State", ImGuiOverlay.SHADER_DEBUG_STATE::get, ImGuiOverlay.SHADER_DEBUG_STATE::set);
                ImGui.treePop();
            }
        }

        ImGui.end();
    }

    private static int getDefaultFlags() {
        boolean cursorCaught = QuantumClient.get().getWindow().isMouseCaptured();
        int flags = ImGuiWindowFlags.None;
        if (cursorCaught) flags |= ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoInputs;
        return flags;
    }

    public static boolean isShown() {
        return ImGuiOverlay.SHOW_IM_GUI.get();
    }

    public static void setShowingImGui(boolean value) {
        ImGuiOverlay.SHOW_IM_GUI.set(value);
        QuantumClient.get().getWindow().refresh();
    }

    public static boolean isProfilerShown() {
        return ImGuiOverlay.SHOW_PROFILER.get();
    }

    public static void delete() {
        synchronized (ImGuiOverlay.class) {
            if (ImGuiOverlay.isImplCreated) {
                ImGuiOverlay.imGuiGl3.shutdown();
                ImGuiOverlay.isImplCreated = false;
            }

            if (ImGuiOverlay.isContextCreated) {
                ImGui.destroyContext();
                ImPlot.destroyContext(ImGuiOverlay.imPlotCtx);
                ImGuiOverlay.isContextCreated = false;
            }
        }
    }

    public static double getMouseX() {
        return screenCoords.mouseX;
    }

    public static double getMouseY() {
        return screenCoords.mouseY;
    }

    public static int getGameWidth() {
        return screenCoords.width;
    }

    public static int getGameHeight() {
        return screenCoords.height;
    }

    public static void showError(String s) {
        ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + ImGui.getMainViewport().getSizeX() / 2, ImGui.getMainViewport().getPosY() + ImGui.getMainViewport().getSizeY() / 2, ImGuiCond.Once);
        ImGui.setNextWindowFocus();
        ImGui.begin("OpenGL Error", ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoInputs);
        ImGui.textColored(1f, 0f, 0f, 1f, s);
        ImGui.setWindowPos(ImGui.getMainViewport().getPosX() + ImGui.getMainViewport().getSizeX() / 2 - ImGui.getWindowSizeX() / 2, ImGui.getMainViewport().getPosY() + ImGui.getMainViewport().getSizeY() / 2 - ImGui.getWindowSizeY() / 2);
        ImGui.end();
    }
}
