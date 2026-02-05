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

package dev.ultreon.qvoxel.client.render;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.framebuffer.Framebuffer;
import dev.ultreon.qvoxel.client.gui.FontRenderer;
import dev.ultreon.qvoxel.client.gui.Screen;
import dev.ultreon.qvoxel.client.gui.Widget;
import dev.ultreon.qvoxel.client.model.json.BakedModel;
import dev.ultreon.qvoxel.client.render.pipeline.TextureSource;
import dev.ultreon.qvoxel.client.shader.ShaderProgram;
import dev.ultreon.qvoxel.client.texture.Texture;
import dev.ultreon.qvoxel.network.system.DevFlag;
import dev.ultreon.qvoxel.network.system.DeveloperMode;
import dev.ultreon.qvoxel.resource.GameNode;
import dev.ultreon.qvoxel.text.Text;
import dev.ultreon.qvoxel.world.container.Container;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static dev.ultreon.qvoxel.CommonConstants.id;

/**
 * The GuiRenderer class is responsible for rendering graphical user interfaces (GUIs) in the game context.
 * It provides methods to draw shapes, textures, strings, and other elements, as well as manage transformations
 * such as scaling, translation, and rotation. This class also supports advanced features like scissor stacks,
 * shaders, and nine-patch rendering.
 *
 * @see Screen#render(GuiRenderer, int, int, float)
 * @see Widget#render(GuiRenderer, int, int, float)
 */
public class GuiRenderer extends GameNode implements Closeable {
    private final Matrix4f projectionMatrix = new Matrix4f();
    private final Matrix4fStack viewMatrix = new Matrix4fStack(1024);
    private final Matrix4f modelMatrix = new Matrix4f();
    private final Vector4f uvOffset = new Vector4f();
    private final Stack<Scissor> scissorStack = new Stack<>();
    public final ShaderProgram colorShader;
    public final ShaderProgram colorTextureShader;
    private final float[] vertices = {
            // X, Y, Z, U, V //
            0, 0, 0, 0, 0,
            1, 0, 0, 1, 0,
            1, 1, 0, 1, 1,
            0, 1, 0, 0, 1,
    };
    private final int[] indices = {0, 1, 2, 0, 2, 3};
    private final Mesh quadMesh = new Mesh(GLShape.Triangles, vertices, indices, VertexAttributes.POSITION, VertexAttributes.UV);
    private final Color color = new Color();
    private final Color textureColor = new Color(1, 1, 1, 1);
    private final Color tmpColor = new Color();
    private final QuantumClient client = QuantumClient.get();
    private final Vector3f tmp3f = new Vector3f();
    private final List<DebugDraw> debugDraws = new ArrayList<>();
    private final Vector3f tmp3f1 = new Vector3f();
    private FontRenderer font;

    public GuiRenderer(FontRenderer font) {
        this.font = font;
        int scaledWidth = QuantumClient.get().getScaledWidth();
        int scaledHeight = QuantumClient.get().getScaledHeight();
        if (scaledWidth <= 0) {
            scaledWidth = 1;
        }
        if (scaledHeight <= 0) {
            scaledHeight = 1;
        }

        CommonConstants.LOGGER.info("Initializing GUI renderer with dimensions {}x{}", scaledWidth, scaledHeight);

        // Set up projection matrix
        projectionMatrix.identity();
        projectionMatrix.setOrtho(0, scaledWidth, scaledHeight, 0, 0, 1000000);

        viewMatrix.identity();
        viewMatrix.translate(0, 0, -10000);
        viewMatrix.scale(1, 1, 1);

        modelMatrix.identity();
        modelMatrix.translate(0, 0, 0);
        modelMatrix.scale(1, 1, 1);

        colorShader = new ShaderProgram(id("gui/color"), """
                #version 330 core
                
                layout (location = 0) in vec3 Position;
                layout (location = 1) in vec2 UV;
                
                uniform mat4 projection;
                uniform mat4 view;
                uniform mat4 model;
                
                void main() {
                    gl_Position = projection * view * model * vec4(Position, 1);
                }
                """, """
                #version 330 core
                
                uniform vec4 color;
                
                out vec4 fragColor;
                
                void main() {
                    fragColor = color;
                }
                """);
        colorTextureShader = new ShaderProgram(id("gui/color_tex"), """
                #version 330 core
                
                layout (location = 0) in vec3 Position;
                layout (location = 1) in vec2 UV;
                
                uniform mat4 projection;
                uniform mat4 view;
                uniform mat4 model;
                uniform vec4 uvOffset;
                
                out vec2 uv;
                
                void main() {
                    gl_Position = projection * view * model * vec4(Position, 1);
                    uv = UV * uvOffset.zw + uvOffset.xy;
                }
                """, """
                #version 330 core
                
                in vec2 uv;
                
                uniform vec4 color;
                uniform sampler2D colorTexture;
                
                out vec4 fragColor;
                
                void main() {
                    fragColor = color * texture(colorTexture, uv);
                }
                """);

        colorShader.use();
        colorShader.enableAttribute("Position");
        colorShader.setUniform("color", new Vector4f(1, 1, 1, 1));

        colorTextureShader.use();
        colorTextureShader.enableAttribute("Position");
        colorTextureShader.enableAttribute("UV");
        colorTextureShader.setUniform("color", new Vector4f(1, 1, 1, 1));
        colorTextureShader.setUniform("colorTexture", 0);
    }

    public void resize(int scaledWidth, int scaledHeight) {
        if (scaledWidth <= 0) {
            scaledWidth = 1;
        }
        if (scaledHeight <= 0) {
            scaledHeight = 1;
        }

        projectionMatrix.identity();
        projectionMatrix.setOrtho(0, scaledWidth, scaledHeight, 0, 0, 1000000);

        viewMatrix.identity();
        viewMatrix.translate(0, 0, -1000);
        viewMatrix.scale(1, 1, 1);

        modelMatrix.identity();
        modelMatrix.translate(0, 0, 0);
        modelMatrix.scale(1, 1, 1);
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4fStack getViewMatrix() {
        return viewMatrix;
    }

    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    public void fillRect(int x, int y, int width, int height, int colorArgb) {
        modelMatrix.identity();
        modelMatrix.translation(x, y, 0);
        modelMatrix.scale(width, height, 1);

        colorShader.use();
        tmpColor.setARGB(colorArgb);
        colorShader.setUniform("color", tmpColor);
        colorShader.setUniform("projection", projectionMatrix);
        colorShader.setUniform("model", modelMatrix);
        colorShader.setUniform("view", viewMatrix);
        quadMesh.render(colorShader);
    }

    public void rect(int x, int y, int width, int height, ShaderProgram program) {
        modelMatrix.identity();
        modelMatrix.translation(x, y, 0);
        modelMatrix.scale(width, height, 1);

        program.setUniform("projection", getProjectionMatrix());
        program.setUniform("model", getModelMatrix());
        program.setUniform("view", getViewMatrix());
        quadMesh.render(program);
    }

    public void drawRect(int x, int y, int width, int height, int colorArgb) {
        fillRect(x, y, width, 1, colorArgb);
        fillRect(x, y + height - 1, width, 1, colorArgb);
        fillRect(x, y, 1, height, colorArgb);
        fillRect(x + width - 1, y, 1, height, colorArgb);
    }

    public void drawTexture(Identifier id, int x, int y, int width, int height) {
        Texture texture = QuantumClient.get().getTextureManager().getTexture(id);
        if (texture == null) {
            return;
        }

        modelMatrix.identity();
        modelMatrix.translation(x, y, 0);
        modelMatrix.scale(width, height, 1);

        uvOffset.set(0, 0, 1, 1);

        colorTextureShader.use();
        colorTextureShader.setUniform("color", textureColor);
        colorTextureShader.setUniform("colorTexture", 0);
        colorTextureShader.setUniform("projection", projectionMatrix);
        colorTextureShader.setUniform("model", modelMatrix);
        colorTextureShader.setUniform("view", viewMatrix);
        colorTextureShader.setUniform("uvOffset", uvOffset);
        texture.use();
        quadMesh.render(colorTextureShader);
    }

    public void drawTexture(Identifier id, int x, int y, int width, int height, int texU, int texV, int texUWidth, int texVHeight) {
        Texture texture = QuantumClient.get().getTextureManager().getTexture(id);
        if (texture == null) {
            return;
        }

        modelMatrix.identity();
        modelMatrix.translation(x, y, 0);
        modelMatrix.scale(width, height, 1);

        uvOffset.set((float) texU / texture.getWidth(), (float) texV / texture.getHeight(), (float) texUWidth / texture.getWidth(), (float) texVHeight / texture.getHeight());

        colorTextureShader.use();
        colorTextureShader.setUniform("color", textureColor);
        colorTextureShader.setUniform("colorTexture", 0);
        colorTextureShader.setUniform("projection", projectionMatrix);
        colorTextureShader.setUniform("model", modelMatrix);
        colorTextureShader.setUniform("view", viewMatrix);
        colorTextureShader.setUniform("uvOffset", uvOffset);
        texture.use();
        quadMesh.render(colorTextureShader);
    }

    public void drawTexture(Identifier id, int x, int y, int width, int height, int texU, int texV, int texUWidth, int texVHeight, int textureWidth, int textureHeight) {
        Texture texture = QuantumClient.get().getTextureManager().getTexture(id);
        if (texture == null) {
            return;
        }

        modelMatrix.identity();
        modelMatrix.translation(x, y, 0);
        modelMatrix.scale(width, height, 1);

        uvOffset.set((float) texU / textureWidth, (float) texV / textureHeight, (float) texUWidth / textureWidth, (float) texVHeight / textureHeight);

        colorTextureShader.use();
        colorTextureShader.setUniform("color", textureColor);
        colorTextureShader.setUniform("colorTexture", 0);
        colorTextureShader.setUniform("projection", projectionMatrix);
        colorTextureShader.setUniform("model", modelMatrix);
        colorTextureShader.setUniform("view", viewMatrix);
        colorTextureShader.setUniform("uvOffset", uvOffset);
        texture.use();
        quadMesh.render(colorTextureShader);
    }

    public void drawTexture(TextureSource texture, int x, int y, int width, int height, int texU, int texV, int texUWidth, int texVHeight) {
        modelMatrix.identity();
        modelMatrix.translation(x, y, 0);
        modelMatrix.scale(width, height, 1);

        uvOffset.set((float) texU / texture.getWidth(), (float) texV / texture.getHeight(), (float) texUWidth / texture.getWidth(), (float) texVHeight / texture.getHeight());

        colorTextureShader.use();
        colorTextureShader.setUniform("color", textureColor);
        colorTextureShader.setUniform("colorTexture", 0);
        colorTextureShader.setUniform("projection", projectionMatrix);
        colorTextureShader.setUniform("model", modelMatrix);
        colorTextureShader.setUniform("view", viewMatrix);
        colorTextureShader.setUniform("uvOffset", uvOffset);
        texture.use();
        quadMesh.render(colorTextureShader);
    }

    public void drawTexture(TextureSource texture, int x, int y, int width, int height, int texU, int texV, int texUWidth, int texVHeight, int textureWidth, int textureHeight) {
        modelMatrix.identity();
        modelMatrix.translation(x, y, 0);
        modelMatrix.scale(width, height, 1);

        uvOffset.set((float) texU / textureWidth, (float) texV / textureHeight, (float) texUWidth / textureWidth, (float) texVHeight / textureHeight);

        colorTextureShader.use();
        colorTextureShader.setUniform("color", textureColor);
        colorTextureShader.setUniform("colorTexture", 0);
        colorTextureShader.setUniform("projection", projectionMatrix);
        colorTextureShader.setUniform("model", modelMatrix);
        colorTextureShader.setUniform("view", viewMatrix);
        colorTextureShader.setUniform("uvOffset", uvOffset);
        texture.use();
        quadMesh.render(colorTextureShader);
    }

    public void setTextureColor(int colorArgb) {
        textureColor.setARGB(colorArgb);
    }

    public void close() {
        viewMatrix.clear();
        modelMatrix.identity();
        projectionMatrix.identity();

        colorShader.delete();
        colorTextureShader.delete();
        quadMesh.delete();
    }

    public Tessellator getTessellator() {
        return Tessellator.getInstance();
    }

    public Color getTextureColor() {
        return textureColor;
    }

    public int getHeight() {
        return QuantumClient.get().getScaledHeight();
    }

    public void renderModel(BakedModel model) {
        uvOffset.set(0, 0, 1, 1);

        var itemGuiShader = client.shaders.getItemGuiProgram();
        itemGuiShader.use();
        itemGuiShader.setUniform("color", textureColor);
        itemGuiShader.setUniform("colorTexture", 0);
        itemGuiShader.setUniform("projection", projectionMatrix);
        itemGuiShader.setUniform("model", modelMatrix);
        itemGuiShader.setUniform("view", viewMatrix);
        itemGuiShader.setUniform("uvOffset", uvOffset);
        model.render(itemGuiShader);
    }

    public void drawTexture(TextureSource texture, int cursorX, int cursorY, int width, int height) {
        modelMatrix.identity();
        modelMatrix.translation(cursorX, cursorY, 0);
        modelMatrix.scale(width, height, 1);

        uvOffset.set(0, 0, 1, 1);

        colorTextureShader.use();
        colorTextureShader.setUniform("color", textureColor);
        colorTextureShader.setUniform("colorTexture", 0);
        colorTextureShader.setUniform("projection", projectionMatrix);
        colorTextureShader.setUniform("model", modelMatrix);
        colorTextureShader.setUniform("view", viewMatrix);
        colorTextureShader.setUniform("uvOffset", uvOffset);
        texture.use();
        quadMesh.render(colorTextureShader);
    }

    public void drawFramebufferTexture(TextureSource texture, int cursorX, int cursorY, int width, int height) {
        modelMatrix.identity();
        modelMatrix.translation(cursorX, cursorY, 0);
        modelMatrix.scale(width, height, 1);

        uvOffset.set(0, 1, 1, -1);

        colorTextureShader.use();
        colorTextureShader.setUniform("color", textureColor);
        colorTextureShader.setUniform("colorTexture", 0);
        colorTextureShader.setUniform("projection", projectionMatrix);
        colorTextureShader.setUniform("model", modelMatrix);
        colorTextureShader.setUniform("view", viewMatrix);
        colorTextureShader.setUniform("uvOffset", uvOffset);
        texture.use();
        quadMesh.render(colorTextureShader);
    }

    public void pushMatrix() {
        viewMatrix.pushMatrix();
    }

    public void popMatrix() {
        viewMatrix.popMatrix();
    }

    public void translate(Vector3f position) {
        viewMatrix.translate(position);
    }

    public void scale(Vector3f scale) {
        viewMatrix.scale(scale);
    }

    public void rotate(float angle, Vector3f axis) {
        viewMatrix.rotate(angle, axis);
    }

    public void mulMatrix(Matrix4f matrix) {
        viewMatrix.mul(matrix);
    }

    public void translate(float x, float y, float z) {
        viewMatrix.translate(x, y, z);
    }

    public void scale(float x, float y, float z) {
        viewMatrix.scale(x, y, z);
    }

    public void rotate(float angle, float x, float y, float z) {
        viewMatrix.rotate(angle, x, y, z);
    }

    public void drawString(String message, int x, int y, int color) {
        drawString(message, x, y, color, true);
    }

    public void drawString(Text message, int x, int y, int color) {
        drawString(message, x, y, color, true);
    }

    public void drawString(String message, int x, int y, int color, boolean shadow) {
        if (shadow) {
            font.renderTextWithShadow(this, message, x, y + 1, tmpColor.setARGB(color));
            return;
        }
        font.renderText(this, message, x, y, tmpColor.setARGB(color));
    }

    public void drawString(Text message, int x, int y, int color, boolean shadow) {
        if (shadow) {
            font.renderTextWithShadow(this, message, x, y + 1, tmpColor.setARGB(color));
            return;
        }

        font.renderText(this, message, x, y, tmpColor.setARGB(color));
    }

    public void drawCenteredString(String translate, int x, int y, int color) {
        if (translate == null) return;
        try (Stream<String> line = translate.lines()) {
            for (String lineStr : line.toList()) {
                int textX = font.widthOf(lineStr) / 2;
                drawString(lineStr, x - textX, y, color);
                y += font.lineHeight;
            }
        }
    }

    public void drawLabel(int x, int y, int width, int height) {
        drawLabel(x, y, width, height, true);
    }

    public void drawLabel(int x, int y, int width, int height, boolean shadow) {
        if (shadow) {
            fillRect(x + 5, y + 5, width, height, 0x80000000);
            fillRect(x + 6, y + height + 5, width - 2, 1, 0x80000000);
            fillRect(x + 6, y + 4, width - 2, 1, 0x80000000);
        }

        fillRect(x - 1, y, width + 2, height, 0xff202020);
        fillRect(x, y - 1, width, height + 2, 0xff202020);
        fillRect(x, y, width, height, 0xff404040);
        fillRect(x + 1, y + 1, width - 2, height - 2, 0xff202020);
    }


    public void drawDisplay(int x, int y, int width, int height) {
        fillRect(x - 1, y, width + 2, height, 0xff202020);
        fillRect(x, y - 1, width, height + 2, 0xff202020);
        fillRect(x, y, width, height, 0xff404040);
        fillRect(x + 1, y + 1, width - 2, height - 2, 0xff202020);
    }

    public void drawLightDisplay(int x, int y, int width, int height) {
        fillRect(x - 1, y, width + 2, height, 0xffd0d0d0);
        fillRect(x, y - 1, width, height + 2, 0xffd0d0d0);
        fillRect(x, y, width, height, 0xffffffff);
        fillRect(x + 1, y + 1, width - 2, height - 2, 0xffd0d0d0);
    }

    public int drawColorDisplay(int x, int y, int width, int height, int color) {
        tmpColor.setARGB(color);
        tmpColor.a = 1f;
        tmpColor.clampBrightness(0f, 0.8f).brighten(1.2f);
        color = tmpColor.toARGB();
        int lightColor = tmpColor.brighten(1.4f).toARGB();
        fillRect(x - 1, y, width + 2, height, color);
        fillRect(x, y - 1, width, height + 2, color);
        fillRect(x, y, width, height, lightColor);
        fillRect(x + 1, y + 1, width - 2, height - 2, color);
        return color;
    }

    public int getStringWidth(String message) {
        return font.widthOf(message);
    }

    public int getFontHeight() {
        return font.lineHeight;
    }

    public void drawNinePatch(Identifier texture, int x, int y, int inset) {
        Texture ninePatch = QuantumClient.get().getTextureManager().getTexture(texture);
        drawNinePatch(texture, x, y, ninePatch.getWidth(), ninePatch.getHeight(), 0, 0, ninePatch.getWidth(), ninePatch.getHeight(), ninePatch.getWidth(), ninePatch.getHeight(), inset);
    }

    public void drawNinePatch(Identifier texture, int x, int y, int width, int height, int inset) {
        Texture ninePatch = QuantumClient.get().getTextureManager().getTexture(texture);
        drawNinePatch(texture, x, y, width, height, ninePatch.getWidth(), ninePatch.getHeight(), inset);
    }

    public void drawNinePatch(Identifier texture, int x, int y, int width, int height, int uWidth, int vHeight, int inset) {
        Texture ninePatch = QuantumClient.get().getTextureManager().getTexture(texture);
        drawNinePatch(texture, x, y, width, height, 0, 0, uWidth, vHeight, ninePatch.getWidth(), ninePatch.getHeight(), inset);
    }

    public void drawNinePatch(Identifier texture, int x, int y, int width, int height, int u, int v, int uWidth, int vHeight, int inset) {
        Texture ninePatch = QuantumClient.get().getTextureManager().getTexture(texture);
        drawNinePatch(texture, x, y, width, height, u, v, uWidth, vHeight, ninePatch.getWidth(), ninePatch.getHeight(), inset);
    }

    public void drawNinePatch(Identifier texture, int x, int y, int width, int height, int u, int v, int uWidth, int vHeight, int texWidth, int texHeight, int inset) {
        Texture ninePatch = QuantumClient.get().getTextureManager().getTexture(texture);
        if (ninePatch == null) {
            return;
        }

        // Compute destinations and clamped center sizes
        int rightX = x + width - inset;
        int bottomY = y + height - inset;
        int centerW = Math.max(0, width - 2 * inset);
        int centerH = Math.max(0, height - 2 * inset);
        int centerUWidth = Math.max(0, uWidth - 2 * inset);
        int centerVHeight = Math.max(0, vHeight - 2 * inset);

        // Top
        drawTexture(texture, x, y, inset, inset, u, v, inset, inset, texWidth, texHeight); // Top Left
        drawTexture(texture, x + inset, y, centerW, inset, u + inset, v, centerUWidth, inset, texWidth, texHeight); // Top
        drawTexture(texture, rightX, y, inset, inset, u + uWidth - inset, v, inset, inset, texWidth, texHeight); // Top Right

        // Middle
        drawTexture(texture, x, y + inset, inset, centerH, u, v + inset, inset, centerVHeight, texWidth, texHeight); // Left
        drawTexture(texture, x + inset, y + inset, centerW, centerH, u + inset, v + inset, centerUWidth, centerVHeight, texWidth, texHeight); // Middle
        drawTexture(texture, rightX, y + inset, inset, centerH, u + uWidth - inset, v + inset, inset, centerVHeight, texWidth, texHeight); // Right

        // Bottom
        drawTexture(texture, x, bottomY, inset, inset, u, v + vHeight - inset, inset, inset, texWidth, texHeight); // Bottom Left
        drawTexture(texture, x + inset, bottomY, centerW, inset, u + inset, v + vHeight - inset, centerUWidth, inset, texWidth, texHeight); // Bottom
        drawTexture(texture, rightX, bottomY, inset, inset, u + uWidth - inset, v + vHeight - inset, inset, inset, texWidth, texHeight); // Bottom Right
    }

    public void drawString(String message, int x, int y) {
        drawString(message, x, y, 0xffffffff);
    }

    public void drawString(Text text, int x, int y) {
        drawString(text, x, y, 0xffffffff);
    }

    public void drawCenteredString(Text text, int x, int y, int color) {
        drawString(text, x - font.widthOf(text) / 2, y, color);
    }

    public boolean pushScissors(int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            return false;
        }

        viewMatrix.getTranslation(tmp3f);
        x += (int) tmp3f.x;
        y += (int) tmp3f.y;
        int scale = client.getScale();
        int windowWidth = client.getWindow().getWidth();
        int windowHeight = client.getWindow().getHeight();
        Scissor item = new Scissor(x * scale, windowHeight - y * scale - height * scale, width * scale, height * scale);
        if (item.x >= windowWidth || item.y >= windowHeight || item.x + item.width <= 0 || item.y + item.height <= 0) {
            return false;
        }

        if (scissorStack.isEmpty()) {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
        }

        scissorStack.push(item);
        GL11.glScissor(item.x, item.y, item.width, item.height);

        debugDraws.add(new DebugDraw(x, y, width, height, 0x80FFFFFF, false));
        return true;
    }

    public void popScissors() {
        if (scissorStack.isEmpty()) {
            return;
        }

        scissorStack.pop();
        if (scissorStack.isEmpty()) {
            GL11.glScissor(0, 0, client.getWindow().getWidth(), client.getWindow().getHeight());
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        } else {
            Scissor peek = scissorStack.peek();
            GL11.glScissor(peek.x, peek.y, peek.width, peek.height);
        }
    }

    public void clearScissors() {
        scissorStack.clear();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public void customRender(Consumer<ShaderProgram> render) {
        colorTextureShader.use();
        render.accept(colorTextureShader);
    }

    public void start() {
        colorTextureShader.use();
    }

    public void end() {
    }

    public void postRender() {
        clearScissors();
        if (DeveloperMode.isDevFlagEnabled(DevFlag.GuiDebug)) {
            for (DebugDraw debugDraw : debugDraws) {
                debugDraw(debugDraw.x(), debugDraw.y(), debugDraw.width(), debugDraw.height(), debugDraw.colorArgb(), debugDraw.filled());
            }
        }
        debugDraws.clear();
    }

    private void debugDraw(int x, int y, int width, int height, int colorArgb, boolean filled) {
        if (filled) {
            fillRect(x, y, width, height, colorArgb);
            return;
        }
        fillRect(x, y, width, 2, colorArgb);
        fillRect(x, y + height - 2, width, 2, colorArgb);
        fillRect(x, y, 2, height, colorArgb);
        fillRect(x + width - 2, y, 2, height, colorArgb);
    }

    public void debugDraw(Widget child) {
        if (!child.isVisible) {
            debugDraw(new DebugDraw((int) child.position.x, (int) child.position.y, child.size.x, child.size.y, 0x80ff0000));
            return;
        }
        if (child instanceof Container) {
            debugDraw(new DebugDraw((int) child.position.x, (int) child.position.y, child.size.x, child.size.y, 0x80ffff00));
            return;
        }
        debugDraw(new DebugDraw((int) child.position.x, (int) child.position.y, child.size.x, child.size.y, 0x80ff00ff));
    }

    public void debugDraw(DebugDraw debugDraw) {
        debugDraws.add(debugDraw.translate(viewMatrix.getTranslation(tmp3f), viewMatrix.getScale(tmp3f1)));
    }

    public void debugDump(String name) {
        Framebuffer current = Framebuffer.getCurrent();

        int[] rgba = new int[current.getWidth() * current.getHeight()];
        GL11.glReadPixels(0, 0, current.getWidth(), current.getHeight(), GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, rgba);

        BufferedImage image = new BufferedImage(current.getWidth(), current.getHeight(), BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, current.getWidth(), current.getHeight(), rgba, 0, 0);

        try {
            ImageIO.write(image, "PNG", new File(name + ".png"));
        } catch (IOException e) {
            CommonConstants.LOGGER.warn("Debug dump failed: ", e);
        }
    }

    public void setFont(FontRenderer font) {
        this.font = font;
    }
}
