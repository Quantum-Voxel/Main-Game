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
import dev.ultreon.qvoxel.client.render.GLUtils;
import imgui.ImGui;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static org.lwjgl.opengl.GL41.*;

@DebugRenderer(DepthAttachment.DebugRenderer.class)
public class DepthAttachment implements FrameBufferAttachment {
    private final DepthFormat depthFormat;
    private int renderBuffer;
    private Framebuffer frameBuffer;
    private boolean deleted;

    public DepthAttachment(DepthFormat depthFormat) {
        this.depthFormat = depthFormat;
        renderBuffer = glGenRenderbuffers();
    }

    public DepthAttachment() {
        this(DepthFormat.DEPTH_24);
    }

    public DepthFormat getDepthFormat() {
        return depthFormat;
    }

    @Override
    public void attach(Framebuffer frameBuffer, int data) {
        if (this.frameBuffer != null)
            throw new IllegalStateException("Depth attachment already attached to framebuffer " + frameBuffer.getName() + "!");
        if (Framebuffer.getCurrent() != frameBuffer)
            throw new IllegalStateException("Cannot attach texture to a framebuffer that is not the current one!");

        this.frameBuffer = frameBuffer;
        glBindRenderbuffer(GL_RENDERBUFFER, renderBuffer);
        int error = glGetError();
        if (error != GL_NO_ERROR) {
            throw new RuntimeException("Error when binding depth buffer: " + GLUtils.getErrorName(error));
        }

        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT32, frameBuffer.getWidth(), frameBuffer.getHeight());
        error = glGetError();
        if (error != GL_NO_ERROR) {
            throw new RuntimeException("Error when allocating depth buffer: " + GLUtils.getErrorName(error));
        }
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, renderBuffer);

        error = glGetError();
        if (error != GL_NO_ERROR) {
            throw new RuntimeException("Error when attaching depth buffer to framebuffer: " + GLUtils.getErrorName(error));
        }
    }

    @Override
    public void detach() {
        if (frameBuffer == null)
            throw new IllegalStateException("Depth attachment not attached to any framebuffer!");
        if (Framebuffer.getCurrent() != frameBuffer)
            throw new IllegalStateException("Cannot attach texture to a framebuffer that is not the current one!");

        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, 0);
        renderBuffer = 0;
    }

    @Override
    public int getAttachment() {
        return GL_DEPTH_ATTACHMENT;
    }

    @Override
    public void resize(int width, int height) {
        Framebuffer buffer = frameBuffer;
        delete();

        renderBuffer = glGenRenderbuffers();
        attach(buffer, 0);
    }

    @Override
    public int getObjectId() {
        return renderBuffer;
    }

    @Override
    public void delete() {
        if (deleted) return;
        deleted = true;
        frameBuffer = null;

        if (!glIsRenderbuffer(renderBuffer))
            return;
        glDeleteRenderbuffers(renderBuffer);
    }

    public static class DebugRenderer implements Renderer<DepthAttachment> {
        @Override
        public void render(DepthAttachment object, @Nullable Consumer<DepthAttachment> setter) {
            ImGui.text("Framebuffer depth attachment");
            ImGui.text("Format: " + object.getDepthFormat());
            ImGui.text("Attachment: " + object.getAttachment());
            ImGui.text("Object ID: " + object.getObjectId());
        }
    }
}
