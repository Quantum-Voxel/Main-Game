package dev.ultreon.qvoxel.devutils;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.debug.ImGuiOverlay;
import dev.ultreon.qvoxel.client.framebuffer.Framebuffer;
import dev.ultreon.qvoxel.client.gui.Resizer;
import dev.ultreon.qvoxel.client.render.GLUtils;
import dev.ultreon.qvoxel.client.render.Mesh;
import dev.ultreon.qvoxel.client.shader.GLShaderType;
import dev.ultreon.qvoxel.client.shader.ShaderPart;
import dev.ultreon.qvoxel.client.shader.ShaderProgram;
import dev.ultreon.qvoxel.client.texture.TextureAtlas;
import dev.ultreon.qvoxel.client.texture.TextureFormat;
import imgui.ImGui;
import imgui.extension.texteditor.TextEditor;
import imgui.extension.texteditor.TextEditorCoordinates;
import imgui.flag.ImGuiCond;
import imgui.type.ImBoolean;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

import static dev.ultreon.qvoxel.client.debug.ImGuiOverlay.*;

public class ImGuiHandler {
    private static final ImBoolean PAUSED = new ImBoolean(false);

    private static final ImBoolean showTextureList = new ImBoolean(false);
    private static final ImBoolean showTextureNode = new ImBoolean(false);

    private static final ImBoolean showTextureAtlasList = new ImBoolean(false);
    private static final ImBoolean showTextureAtlasNode = new ImBoolean(false);

    private static final ImBoolean showMeshList = new ImBoolean(false);
    private static final ImBoolean showMeshNode = new ImBoolean(false);

    private static final ImBoolean showShaderList = new ImBoolean(false);

    private static final ImBoolean showShaderProgramList = new ImBoolean(false);
    private static final ImBoolean showShaderProgramNode = new ImBoolean(false);

    private static final ImBoolean showModelList = new ImBoolean(false);
    private static final ImBoolean showModelNode = new ImBoolean(false);

    private static final Resizer resizer = new Resizer();

    private static Integer texture;
    private static TextureAtlas textureAtlas;
    private static ShaderProgram shaderProgram;
    private static QuantumClient client;
    private static final Vector4f uv = new Vector4f(0, 0, 1, 1);
    private static int texWidth, texHeight;
    private static Mesh mesh;
    private static final Matrix4f transform = new Matrix4f();
    private static final Matrix4f projection = new Matrix4f();
    private static final Matrix4f view = new Matrix4f();
    private static final Vector3f pos = new Vector3f(0, 0, -24);
    private static final Vector3f rot = new Vector3f(0, 0, 0);
    private static final ShaderProgram meshShader = new ShaderProgram(
            "Debug Mesh Shader",
            new ShaderPart(new Identifier("qvoxel_devutils", "shaders/dev/mesh.vert"), GLShaderType.Vertex),
            new ShaderPart(new Identifier("qvoxel_devutils", "shaders/dev/mesh.geom"), GLShaderType.Geometry),
            new ShaderPart(new Identifier("qvoxel_devutils", "shaders/dev/mesh.frag"), GLShaderType.Fragment)
    );
    private static Mesh selectedMesh;
    private static float meshX = 0, meshY = 0, meshW = 0, meshH = 0;
    private static boolean renderMesh;
    private static final Framebuffer MESH_FRAMEBUFFER = new Framebuffer(1024, 1024, TextureFormat.RGBA8, true);

    static {
        projection.setPerspective((float) Math.toRadians(70), (float) 1, 0.01f, 1000f);
        view.identity();
        view.scale(1);
        view.translate(pos);
        view.rotateXYZ(rot);
    }

    public static void renderMenuBar() {
        if (ImGui.beginMenu("Developer Tools##ImGuiHandler::MixinProvider[DevTools]")) {
            ImGui.menuItem("Texture List", "", showTextureList);
            ImGui.menuItem("Texture Atlas List", "", showTextureAtlasList);
            ImGui.menuItem("Mesh List", "", showMeshList);
            ImGui.menuItem("Shader List", "", showShaderList);
            ImGui.menuItem("Shader Program List", "", showShaderProgramList);
            ImGui.menuItem("Model List", "", showModelList);
            if (ImGui.menuItem((PAUSED.get() ? "Resume" : "Pause") + "##ImGuiHandler::MixinProvider[Pause]")) {
                PAUSED.set(!PAUSED.get());
            }
            ImGui.endMenu();
        }
    }

    public static void renderPreGame() {
    }

    public static void renderWindows(QuantumClient client) {
        ImGuiHandler.client = client;
        if (showTextureList.get()) {
            showTextureListWindow();
        }

        if (showTextureNode.get()) {
            showTextureNodeWindow();
        }
        if (showTextureAtlasList.get()) {
            showTextureAtlasListWindow();
        }
        if (showTextureAtlasNode.get()) {
            showTextureAtlasNodeWindow();
        }
        if (showMeshList.get()) {
            showMeshListWindow();
        }
        if (showMeshNode.get()) {
            showMeshNodeWindow();
        }
        if (showShaderProgramList.get()) {
            showShaderProgramListWindow();
        }
        if (showShaderProgramNode.get()) {
            showShaderProgramNodeWindow();
        }
        if (showModelList.get()) {
            showModelListWindow();
        }
        if (showModelNode.get()) {
            showModelNodeWindow();
        }

        int error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR) {
            ImGuiOverlay.showError("OpenGL Error: " + GLUtils.getErrorName(error));
        }
    }

    private static void showMeshListWindow() {
        if (ImGui.begin("Mesh List")) {
            ImGui.text("Mesh List");
            ImGui.textColored(ImGui.getColorU32(1, 0.5f, 0, 1), "WARNING: This is not yet implemented!");
            int vertexCount = 0;
            int indexCount = 0;
            if (ImGui.beginListBox("##Meshes{}::MixinProvider", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY())) {
                for (Mesh mesh : DebugRegistries.MESHES.stream().sorted(Comparator.comparing(Mesh::getVaoId)).toList()) {
                    if (ImGui.selectable("Mesh ID: " + mesh.getVaoId(), ImGuiHandler.mesh == mesh)) {
                        showMeshNode.set(true);
                        ImGuiHandler.mesh = mesh;
                    }
                    if (ImGui.isItemHovered()) {
                        ImGui.beginTooltip();
                        ImGui.text("Vertex Array Object ID: " + mesh.getVaoId());
                        ImGui.text("Vertex Buffer Object ID: " + mesh.getVboId());
                        ImGui.text("Element Buffer Object ID: " + mesh.getEboId());
                        ImGui.text("Index Count: " + mesh.getIndexCount());
                        ImGui.text("Vertex Count: " + mesh.getVertexCount());

                        selectedMesh = mesh;

                        ImGui.endTooltip();
                    }

                    vertexCount += mesh.getVertexCount();
                    indexCount += mesh.getIndexCount();
                }
                ImGui.endListBox();
            }

            ImGui.text("Total Vertex Count: " + vertexCount);
            ImGui.text("Total Index Count: " + indexCount);
        }
        ImGui.end();
    }

    private static void showMeshNodeWindow() {
        ImGui.setNextWindowSize(256, 128, ImGuiCond.Once);
        if (ImGui.begin("Mesh Node", showMeshNode)) {
            ImGui.text("Mesh Node");

            selectedMesh = selectedMesh == null ? mesh : selectedMesh;

            resizer.set(MESH_FRAMEBUFFER.getWidth(), MESH_FRAMEBUFFER.getHeight());
            Vector2f fit = resizer.fit(ImGui.getContentRegionAvail().x, ImGui.getContentRegionAvail().y);
            ImGui.image(MESH_FRAMEBUFFER.get(0).getObjectId(), fit.x, fit.y, 0, 1, 1, 0);

            renderMesh = true;
        }
        ImGui.end();
    }

    private static void showSource(ShaderProgram shader, String source, GLShaderType type) {
        Identifier location = new Identifier("mixinprovider/text_editor/" + shader.getObjectId() + "/" + type.name());
        TextEditor textEditor = textEditors.get(location);
        if (textEditor == null) {
            textEditor = new TextEditor();
            textEditors.put(location, textEditor);
        }

        textEditor.setText(source);
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
                ImGui.setClipboardText(source);
            }
        }
    }

    private static void showShaderProgramListWindow() {
        if (ImGui.begin("Shader Program List")) {
            ImGui.text("Shader Program List");
            ImGui.textColored(ImGui.getColorU32(1, 0.5f, 0, 1), "WARNING: This is not yet implemented!");
            if (ImGui.beginListBox("##ShaderPrograms{}::MixinProvider", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY())) {
                for (ShaderProgram shaderProgram : DebugRegistries.SHADER_PROGRAMS) {
                    if (ImGui.selectable("Shader Program ID: " + shaderProgram.hashCode(), ImGuiHandler.shaderProgram == shaderProgram)) {
                        showShaderProgramNode.set(true);
                        ImGuiHandler.shaderProgram = shaderProgram;
                    }
                    if (ImGui.isItemHovered()) {
                        ImGui.beginTooltip();
                        ImGui.text("Shader Program ID: " + shaderProgram.getObjectId());
                        ImGui.endTooltip();
                    }
                }
                ImGui.endListBox();
            }
        }
        ImGui.end();
    }

    private static void showShaderProgramNodeWindow() {
        ImGui.setNextWindowSize(256, 128, ImGuiCond.Once);
        if (ImGui.begin("Shader Program Node", showShaderProgramNode)) {
            ImGui.text("Shader Program Node");
            ImGui.textColored(ImGui.getColorU32(1, 0.5f, 0, 1), "WARNING: This is not yet implemented!");

            if (shaderProgram != null) {
                ShaderPart[] shaderParts = shaderProgram.getShaderParts();
                for (int i = 0; i < shaderParts.length; i++) {
                    ShaderPart shaderPart = shaderParts[i];
                    showSource(shaderProgram, shaderPart.getSource(), shaderPart.getType());
                    if (i < shaderParts.length - 1)
                        ImGui.separator();
                }
            } else {
                ImGui.text("Shader Program has been disposed");
                if (ImGui.button("Close")) {
                    showShaderProgramNode.set(false);
                }
            }
        }
        ImGui.end();
    }

    private static void showModelListWindow() {
        if (ImGui.begin("Model List")) {
            ImGui.text("Model List");
            ImGui.textColored(ImGui.getColorU32(1, 0.5f, 0, 1), "WARNING: This is not yet implemented!");
            if (ImGui.beginListBox("##Models{}::MixinProvider", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY())) {
                ImGui.endListBox();
            }
        }
        ImGui.end();
    }

    private static void showModelNodeWindow() {
        ImGui.setNextWindowSize(256, 128, ImGuiCond.Once);
        if (ImGui.begin("Model Node", showModelNode)) {
            ImGui.text("Model Node");
            ImGui.textColored(ImGui.getColorU32(1, 0.5f, 0, 1), "WARNING: This is not yet implemented!");
        }
        ImGui.end();
    }

    private static void showTextureAtlasListWindow() {
        if (ImGui.begin("Texture Atlas List", showTextureAtlasList)) {
            ImGui.text("Texture Atlas List");
            if (ImGui.beginListBox("##TextureAtlas{}::MixinProvider", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY())) {
                for (TextureAtlas textureAtlas : DebugRegistries.TEXTURE_ATLASES) {
                    if (ImGui.selectable("Texture Atlas (" + textureAtlas.getRegions().size() + "x)", ImGuiHandler.textureAtlas == textureAtlas)) {
                        showTextureAtlasNode.set(true);
                        ImGuiHandler.textureAtlas = textureAtlas;
                    }
                    if (ImGui.isItemHovered()) {
                        ImGui.beginTooltip();
                        ImGui.text("Textures: " + textureAtlas.getTextures().size());
                        ImGui.text("Regions: " + textureAtlas.getRegions().size());
                        ImGui.endTooltip();
                    }
                }
                ImGui.endListBox();
            }
        }
        ImGui.end();
    }

    private static void showTextureAtlasNodeWindow() {
        ImGui.setNextWindowSize(256, 128, ImGuiCond.Once);
        if (ImGui.begin("Texture Atlas Node", showTextureAtlasNode)) {
            try {
                if (textureAtlas != null) {
                    if (textureAtlas.getTextures().isEmpty()) {
                        textureAtlas = null;
                    }

                    if (ImGui.beginListBox("##TextureAtlas{}::MixinProvider::Pages", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY())) {
                        try {
                            Collection<TextureAtlas.AtlasRegion> textures = textureAtlas.getRegions();
                            for (TextureAtlas.AtlasRegion region : textures) {
                                if (ImGui.selectable("Texture ID: " + region.texture().getObjectId() + " (" + region.width() + "x" + region.height() + ")",
                                        ImGuiHandler.texture != null
                                                && Objects.equals(ImGuiHandler.texture, region.texture().getObjectId())
                                                && ImGuiHandler.uv.x == region.getU()
                                                && ImGuiHandler.uv.y == region.getV()
                                                && ImGuiHandler.uv.z == region.getU2()
                                                && ImGuiHandler.uv.w == region.getV2()
                                )) {
                                    showTextureNode.set(true);
                                    ImGuiHandler.texture = region.texture().getObjectId();
                                    ImGuiHandler.uv.set(region.getU(), region.getV(), region.getU2(), region.getV2());
                                    texWidth = (int) region.width();
                                    texHeight = (int) region.height();
                                }
                                if (ImGui.isItemHovered()) {
                                    ImGui.beginTooltip();
                                    ImGui.text("Texture: " + region.texture().getObjectId() + " (" + region.width() + "x" + region.height() + "+" + region.x() + "x" + region.y() + ")");
                                    resizer.set((int) region.width(), (int) region.height());
                                    Vector2f fit = resizer.fit(256, 128);
                                    ImGui.image(region.textureAtlas().getObjectId(), fit.x, fit.y, region.getU(), region.getV(), region.getU2(), region.getV2());
                                    ImGui.endTooltip();
                                }
                            }
                        } finally {
                            ImGui.endListBox();
                        }
                    }
                }
            } catch (Exception e) {
                textureAtlas = null;
            }

            if (textureAtlas == null) {
                ImGui.text("Texture Atlas has been disposed");
                if (ImGui.button("Close")) {
                    showTextureAtlasNode.set(false);
                }
            }
        }
        ImGui.end();
    }

    private static void showTextureNodeWindow() {
        ImGui.setNextWindowSize(256, 128, ImGuiCond.Once);
        if (ImGui.begin("Texture Node", showTextureNode)) {
            try {
                if (texture != null) {
                    ImGui.text("Texture: " + texture + " (" + texWidth + "x" + texHeight + ")");
                    resizer.set(texWidth, (int) texHeight);
                    Vector2f fit = resizer.fit(ImGui.getContentRegionAvail().x, ImGui.getContentRegionAvail().y);
                    ImGui.image(texture, fit.x, fit.y, uv.x, uv.y, uv.z, uv.w);
                }
            } catch (Exception e) {
                texture = null;
            }

            if (texture == null) {
                ImGui.text("Texture has been disposed");
                if (ImGui.button("Close")) {
                    showTextureNode.set(false);
                }
            }
        }
        ImGui.end();
    }

    private static void showTextureListWindow() {
        if (ImGui.begin("Texture List")) {
            ImGui.text("Texture List");
            if (ImGui.beginListBox("##Textures{}::MixinProvider", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY())) {
                int width;
                int height;
                for (int texture : DebugRegistries.TEXTURES.intStream().sorted().toArray()) {
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
                    width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
                    height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);

                    if (ImGui.selectable("Texture ID: " + texture + " (" + width + "x" + height + ")",
                            ImGuiHandler.texture != null
                                    && ImGuiHandler.texture == (int) texture
                                    && ImGuiHandler.uv.x == 0
                                    && ImGuiHandler.uv.y == 0
                                    && ImGuiHandler.uv.z == 1
                                    && ImGuiHandler.uv.w == 1
                    )) {
                        showTextureNode.set(true);
                        ImGuiHandler.texture = texture;
                        ImGuiHandler.uv.set(0, 0, 1, 1);
                        texWidth = width;
                        texHeight = height;
                    }
                    if (ImGui.isItemHovered()) {
                        ImGui.beginTooltip();
                        ImGui.text("Texture: " + texture);
                        resizer.set(width, height);
                        Vector2f fit = resizer.fit(256, 128);
                        ImGui.image(texture, fit.x, fit.y);
                        ImGui.endTooltip();
                    }
                }
                ImGui.endListBox();
            }
        }
        ImGui.end();
    }

    public static boolean isPaused() {
        return PAUSED.get();
    }

    public static QuantumClient getClient() {
        return client;
    }

    public static void renderPostGame() {
        MESH_FRAMEBUFFER.start();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LESS);
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glViewport(0, 0, MESH_FRAMEBUFFER.getWidth(), MESH_FRAMEBUFFER.getHeight());
        GL11.glDisable(GL11.GL_CULL_FACE);

        meshShader.enableAttribute("Position");
        meshShader.use();

        projection.identity();
        projection.setPerspective((float) Math.toRadians(70), 1f, 0.01f, 1000f);

        rot.x = 0;
        rot.z = 0;
        rot.y = (float) org.joml.Math.toRadians(System.currentTimeMillis() / 100.0 % 360.0);


        view.identity();
        view.translate(pos);

        transform.identity();
        transform.translate(0, 0, 0);
        transform.scale(0.25f, 0.25f, 0.25f);
        transform.rotateXYZ(rot);

        meshShader.setUniform("projection", projection);
        meshShader.setUniform("view", view);
        meshShader.setUniform("model", transform);

        try {
            if (selectedMesh != null && renderMesh) {
                selectedMesh.render(meshShader);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        MESH_FRAMEBUFFER.end();

        renderMesh = false;
    }
}
