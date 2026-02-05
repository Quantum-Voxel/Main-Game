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

package dev.ultreon.qvoxel.client.texture;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.render.GLObject;
import dev.ultreon.qvoxel.client.render.GLUtils;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.gui.Resizer;
import dev.ultreon.qvoxel.client.debug.DebugRenderer;
import dev.ultreon.qvoxel.client.debug.Renderer;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.client.render.pipeline.TextureSource;
import dev.ultreon.qvoxel.client.world.ClientPlayerEntity;
import imgui.ImGui;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.STBImage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11C.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11C.glPixelStorei;

@DebugRenderer(Texture.DebugRenderer.class)
public class Texture implements GLObject, GameAsset, TextureSource {
    private static int activeTexture;
    private static final int[] boundTextures = new int[32];

    static {
        Arrays.fill(boundTextures, -1);
    }

    protected int width;
    protected int height;
    private final TextureFormat format;
    protected int objectId = -1;
    protected boolean deleted;

    public Texture(int objectId, TextureFormat format) {
        this.objectId = objectId;
        this.format = format;

        if (objectId != -1) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, objectId);
            width = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WIDTH);
            height = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_HEIGHT);
        } else {
            width = 0;
            height = 0;
        }
    }

    public Texture(InputStream stream, TextureFormat format) throws IOException {
        this.format = format;
        try {
            objectId = GL11.glGenTextures();
            int error = GL11.glGetError();
            if (error != GL11.GL_NO_ERROR)
                throw new TextureException("Failed to create texture: %s".formatted(GLUtils.getErrorName(error)));

            int[] width = new int[1];
            int[] height = new int[1];
            int[] channels = new int[1];
            int available = stream.available();
            if (available <= 0) {
                throw new TextureException("Texture data is empty");
            }
            ByteBuffer malloc = ByteBuffer.allocateDirect(available);
            byte[] buffer = new byte[available];
            int read = stream.read(buffer);
            if (read <= 0) {
                throw new TextureException("Failed to read texture data");
            }
            malloc.put(buffer, 0, read);
            if (read != malloc.capacity()) {
                throw new TextureException("Failed to read texture data");
            }
            malloc.flip();
            ByteBuffer rawImage = STBImage.stbi_load_from_memory(malloc, width, height, channels, format.getChannels());
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, objectId);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, format.getInternalFormat(), width[0], height[0], 0, format.getFormat(), format.getType(), rawImage);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            this.width = width[0];
            this.height = height[0];
        } catch (Exception e) {
            if (objectId != -1)
                GL11.glDeleteTextures(objectId);
            throw new TextureException("Failed to load texture", e);
        }
    }

    public Texture(Identifier identifier) throws IOException {
        this(QuantumClient.get().resourceManager.openResourceStream(identifier), TextureFormat.RGBA8);
    }

    public Texture(int width, int height, TextureFormat textureFormat, ByteBuffer rawBuffer) {
        this.width = width;
        this.height = height;
        format = textureFormat;

        rawBuffer.order(ByteOrder.LITTLE_ENDIAN);
        rawBuffer.flip();

        objectId = GL11.glGenTextures();

        int error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR)
            throw new TextureException("Failed to create texture: %s".formatted(GLUtils.getErrorName(error)));

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1); // important for BMP
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, objectId);
        error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR)
            throw new TextureException("Failed to create texture: %s".formatted(GLUtils.getErrorName(error)));

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, format.getInternalFormat(), width, height, 0, format.getFormat(), format.getType(), rawBuffer);
        error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR)
            throw new TextureException("Failed to create texture: %s".formatted(GLUtils.getErrorName(error)));

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR)
            throw new TextureException("Failed to create texture: %s".formatted(GLUtils.getErrorName(error)));
    }

    public static Texture draw(int width, int height, Consumer<Painter> o) {
        Painter painter = new Painter(width, height);
        o.accept(painter);
        return painter.createTexture();
    }

    public void use() {
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, objectId);

        int error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR)
            throw new TextureException("Failed to bind texture: %s".formatted(GLUtils.getErrorName(error)));
    }

    public void use(int unit) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, objectId);

        int error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR)
            throw new TextureException("Failed to bind texture: %s".formatted(GLUtils.getErrorName(error)));
    }

    @Override
    public void render(ClientPlayerEntity player, GuiRenderer renderer) {
        renderer.drawTexture(this, 0, 0, width, height);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public TextureFormat getFormat() {
        return format;
    }

    public int getObjectId() {
        return objectId;
    }

    public void delete() {
        if (deleted) return;
        deleted = true;

        if (!GL11.glIsTexture(objectId))
            return;

        GL11.glDeleteTextures(objectId);
        int error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR)
            throw new TextureException("Failed to delete texture: %s".formatted(GLUtils.getErrorName(error)));
    }

    @Override
    public Identifier getAssetId() {
        return CommonConstants.id("textures/%d.png".formatted(objectId));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Texture texture = (Texture) o;
        return objectId == texture.objectId;
    }

    @Override
    public int hashCode() {
        return objectId;
    }

    public static class DebugRenderer implements Renderer<Texture> {
        private final Resizer resizer = new Resizer();

        @Override
        public void render(Texture object, @Nullable Consumer<Texture> setter) {
            resizer.set(object.getWidth(), object.getHeight());
            Vector2f fit = resizer.fit(ImGui.getContentRegionAvail().x, ImGui.getContentRegionAvail().y);
            ImGui.image(object.objectId, fit.x, fit.y, 0, 0, 1, 1);

            if (ImGui.isItemHovered()) {
                ImGui.setTooltip("Width: %d\nHeight: %d\nFormat: %s".formatted(object.getWidth(), object.getHeight(), Objects.requireNonNull(object.getFormat()).name()));
            }
        }
    }
}
