package dev.ultreon.qvoxel.client.render.pipeline;

import com.google.gson.JsonObject;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.debug.DebugRenderer;
import dev.ultreon.qvoxel.client.debug.ImGuiEx;
import dev.ultreon.qvoxel.client.debug.ImGuiOverlay;
import dev.ultreon.qvoxel.client.debug.Renderer;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.client.world.ClientPlayerEntity;
import dev.ultreon.qvoxel.resource.GameNode;
import imgui.ImGui;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The RenderPipeline class represents a configurable rendering pipeline responsible for managing
 * and coordinating RenderNode instances to produce image output. It provides functionality
 * for creating, connecting, and managing nodes and their configurations, as well as rendering
 * and resizing operations. The pipeline is designed to work with a given GuiRenderer and
 * adjustable rendering dimensions.
 */
@DebugRenderer(RenderPipeline.DebugRenderer.class)
public class RenderPipeline extends GameNode implements AutoCloseable {
    private final GuiRenderer renderer;
    private final int width;
    private final int height;
    private final List<RenderNode> nodes = new ArrayList<>();
    private RenderNode outputNode = null;
    private JsonObject data = new JsonObject();
    private JsonObject defaults = new JsonObject();
    private Object2IntOpenHashMap<String> minValues = new Object2IntOpenHashMap<>();
    private Object2IntOpenHashMap<String> maxValues = new Object2IntOpenHashMap<>();
    private boolean closed;

    public RenderPipeline(GuiRenderer renderer, int width, int height) {
        this.renderer = renderer;
        this.width = width;
        this.height = height;
    }

    public RenderNode createNode(String name) {
        return createNode(name, false);
    }

    public RenderNode createNode(String name, boolean hasDepth) {
        RenderNode renderNode = new RenderNode(width, height, hasDepth);
        add(name, renderNode);
        nodes.add(renderNode);
        return renderNode;
    }

    public void addNode(String name, RenderNode node) {
        nodes.add(node);
        add(name, node);
    }

    public void connect(RenderNode from, RenderNode to, String inputName, String outputName) {
        to.setShaderInput(inputName, from.getShaderOutput(outputName));
    }

    public void setOutputNode(RenderNode node) {
        outputNode = node;
    }

    public void render(ClientPlayerEntity player, float partialTicks) {
        if (outputNode == null) {
            throw new IllegalStateException("Output node is not set.");
        }

        outputNode.render(player, renderer);

        renderer.start();
        int scaledWidth = QuantumClient.get().getScaledWidth();
        int scaledHeight = QuantumClient.get().getScaledHeight();
        renderer.drawTexture(outputNode.get(0), 0, scaledHeight, scaledWidth, -scaledHeight);
        renderer.end();

        for (RenderNode node : nodes) {
            node.finish();
        }
    }

    public void resize(int width, int height) {
        for (RenderNode node : nodes) {
            node.resize(width, height);
        }
    }

    @Override
    public void close() {
        for (RenderNode node : nodes) {
            node.delete();
        }
        nodes.clear();
        outputNode = null;
        closed = true;
    }

    public boolean getBoolean(String reflectionsEnabled) {
        if (data.has(reflectionsEnabled) && data.get(reflectionsEnabled).isJsonPrimitive() && data.get(reflectionsEnabled).getAsJsonPrimitive().isBoolean()) {
            return data.get(reflectionsEnabled).getAsBoolean();
        }
        return defaults.get(reflectionsEnabled).getAsBoolean();
    }

    public void setBoolean(String key, boolean value) {
        data.addProperty(key, value);
    }

    public int getInt(String key) {
        if (data.has(key) && data.get(key).isJsonPrimitive() && data.get(key).getAsJsonPrimitive().isNumber()) {
            return Math.max(minValues.getInt(key), Math.min(maxValues.getInt(key), data.get(key).getAsInt()));
        }
        return defaults.get(key).getAsInt();
    }

    public void setInt(String key, int value) {
        data.addProperty(key, Math.max(minValues.getInt(key), Math.min(maxValues.getInt(key), value)));
    }

    public void registerBoolean(String reflectionsEnabled, boolean defaultValue) {
        data.addProperty(reflectionsEnabled, defaultValue);
        defaults.addProperty(reflectionsEnabled, defaultValue);
    }

    public void registerInt(String key, int defaultValue, int min, int max) {
        data.addProperty(key, defaultValue);
        defaults.addProperty(key, defaultValue);
        minValues.put(key, min);
        maxValues.put(key, max);
    }

    public Collection<String> getConfigKeys() {
        return data.entrySet().stream().map(Map.Entry::getKey).toList();
    }

    public void verify() {
        if (outputNode == null) {
            throw new IllegalStateException("Output node is not set");
        }
    }

    /**
     * Debug renderer for {@link RenderPipeline} for use within the {@linkplain ImGuiOverlay ImGui Overlay}.
     */
    public static class DebugRenderer implements Renderer<RenderPipeline> {
        public DebugRenderer() {

        }

        @Override
        public void render(RenderPipeline object, @Nullable Consumer<RenderPipeline> setter) {
            for (String key : object.getConfigKeys()) {
                if (object.defaults.get(key).isJsonPrimitive() && object.defaults.get(key).getAsJsonPrimitive().isBoolean()) {
                    ImGuiEx.editBool(key, key, () -> object.getBoolean(key), v -> object.setBoolean(key, v));
                } else if (object.defaults.get(key).isJsonPrimitive() && object.defaults.get(key).getAsJsonPrimitive().isNumber()) {
                    ImGuiEx.editInt(key, key, () -> object.getInt(key), v -> object.setInt(key, v));
                }
            }

            ImGui.separator();

            for (RenderNode node : object.nodes) {
                ImGuiOverlay.renderObject(node);
                ImGui.separator();
            }
        }
    }
}
