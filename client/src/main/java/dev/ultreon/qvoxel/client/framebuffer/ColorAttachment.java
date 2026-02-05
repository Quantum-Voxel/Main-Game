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

package dev.ultreon.qvoxel.client.framebuffer;

import dev.ultreon.qvoxel.client.debug.DebugRenderer;
import dev.ultreon.qvoxel.client.debug.Renderer;
import dev.ultreon.qvoxel.client.gui.Resizer;
import dev.ultreon.qvoxel.client.render.GLUtils;
import dev.ultreon.qvoxel.client.render.pipeline.TextureTarget;
import dev.ultreon.qvoxel.client.texture.Texture;
import dev.ultreon.qvoxel.client.texture.TextureException;
import dev.ultreon.qvoxel.client.texture.TextureFormat;
import imgui.ImGui;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL11;

import java.util.Objects;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL41.*;

@DebugRenderer(ColorAttachment.DebugRenderer.class)
public class ColorAttachment extends Texture implements FrameBufferAttachment, TextureTarget {
    protected Framebuffer frameBuffer;
    private int unit = -1;

    public ColorAttachment(TextureFormat format) {
        super(-1, format);
        objectId = glGenTextures();
        if (objectId <= 0)
            throw new RuntimeException("Could not generate texture ID!");

        int error = glGetError();
        if (error != GL_NO_ERROR)
            throw new RuntimeException("Error when creating texture: " + GLUtils.getErrorName(error));
    }

    @Override
    public void attach(Framebuffer frameBuffer, int unit) {
        if (this.frameBuffer != null)
            throw new IllegalStateException("Texture attachment already attached to framebuffer " + this.frameBuffer.getObjectId() + "!");
        if (unit < 0 || unit >= Framebuffer.MAX_COLOR_ATTACHMENTS)
            throw new IllegalArgumentException("Invalid color attachment unit: " + unit);
        if (Framebuffer.getCurrent() != frameBuffer)
            throw new IllegalStateException("Cannot attach texture to a framebuffer that is not the current one!");

        TextureFormat format = getFormat();
        this.frameBuffer = frameBuffer;
        width = frameBuffer.getWidth();
        height = frameBuffer.getHeight();
        frameBuffer.bind();

        if (objectId <= 0)
            throw new RuntimeException("Could not generate texture ID!");

        this.unit = unit;
        glBindTexture(GL11.GL_TEXTURE_2D, objectId);
        int error = glGetError();
        if (error != GL_NO_ERROR)
            throw new RuntimeException("Error when binding texture: " + GLUtils.getErrorName(error));

        glTexImage2D(GL11.GL_TEXTURE_2D, 0, format.getInternalFormat(), getWidth(), getHeight(), 0, format.getFormat(), format.getType(), 0);
        error = glGetError();
        if (error != GL_NO_ERROR)
            throw new RuntimeException("Error when creating texture: " + GLUtils.getErrorName(error));

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        error = glGetError();
        if (error != GL_NO_ERROR)
            throw new RuntimeException("Error when setting texture parameters: " + GLUtils.getErrorName(error));

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + unit, GL_TEXTURE_2D, objectId, 0);
        error = glGetError();
        if (error != GL_NO_ERROR)
            throw new RuntimeException("Error when attaching texture to framebuffer: " + GLUtils.getErrorName(error));
    }

    @Override
    public void detach() {
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + unit, GL_TEXTURE_2D, GL_NONE, 0);
        unit = -1;
    }

    public boolean isAttached() {
        return frameBuffer != null && unit != -1;
    }

    @Override
    public int getAttachment() {
        if (unit == -1)
            throw new IllegalStateException("Color attachment not attached to any framebuffer!");
        return GL_COLOR_ATTACHMENT0 + unit;
    }

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;

        Framebuffer buffer = frameBuffer;

        delete();

        objectId = glGenTextures();
        if (objectId <= 0)
            throw new TextureException("Could not generate texture ID!");

        attach(buffer, unit);
    }

    @Override
    public void delete() {
        if (deleted) return;
        deleted = true;

        frameBuffer = null;
        super.delete();
    }

    public static class DebugRenderer implements Renderer<ColorAttachment> {
        private final Resizer resizer = new Resizer();

        @Override
        public void render(ColorAttachment object, @Nullable Consumer<ColorAttachment> setter) {
            ImGui.text("Framebuffer color attachment");

            resizer.set(object.getWidth(), object.getHeight());
            Vector2f fit = resizer.fit(ImGui.getContentRegionAvail().x, ImGui.getContentRegionAvail().y);
            ImGui.image(object.objectId, fit.x, fit.y, 0, 1, 1, 0);

            if (ImGui.isItemHovered()) {
                ImGui.setTooltip("Width: %d\nHeight: %d\nFormat: %s".formatted(object.getWidth(), object.getHeight(), Objects.requireNonNull(object.getFormat()).name()));
            }
        }
    }
}
