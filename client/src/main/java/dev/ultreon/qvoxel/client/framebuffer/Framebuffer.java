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

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.debug.DebugRenderer;
import dev.ultreon.qvoxel.client.debug.ImGuiOverlay;
import dev.ultreon.qvoxel.client.debug.Renderer;
import dev.ultreon.qvoxel.client.render.GLObject;
import dev.ultreon.qvoxel.client.render.GLUtils;
import dev.ultreon.qvoxel.client.texture.Texture;
import dev.ultreon.qvoxel.client.texture.TextureFormat;
import dev.ultreon.qvoxel.resource.GameComponent;
import dev.ultreon.qvoxel.resource.GameNode;
import imgui.ImGui;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL41;

import java.util.*;
import java.util.function.Consumer;

/**
 * Represents a framebuffer used for off-screen rendering in OpenGL.
 * A framebuffer allows rendering to textures or other attachments instead
 * of the default framebuffer (typically the screen buffer).
 * Framebuffers provide flexibility for advanced rendering techniques
 * such as post-processing or shadow mapping.
 * <p>
 * This class manages the creation, binding, and lifecycle of framebuffer objects,
 * as well as their attachments. Attachments can include color, depth, or stencil
 * buffers and are used to store rendering results.
 * <p>
 * Framebuffers work as a stack, and their usage requires explicit calls to
 * start()/end() and finish() for lifecycle management. Ensures compatibility
 * and proper viewport settings during rendering.
 */
@DebugRenderer(Framebuffer.DebugRenderer.class)
public class Framebuffer extends GameNode implements GLObject, GameComponent {
    private static final Deque<Framebuffer> stack = new ArrayDeque<>();

    /**
     * Specifies the maximum number of color attachments that can be supported
     * by a framebuffer in this implementation.
     * <p>
     * This constant is used to define an upper limit on the number of color
     * textures that can be attached to a framebuffer for rendering operations.
     * <p>
     * The value of `MAX_COLOR_ATTACHMENTS` is based on the maximum support
     * provided by the underlying OpenGL implementation and ensures adherence
     * to hardware or software limitations.
     * <p>
     * It is commonly used in methods that manage framebuffer attachments, such
     * as attaching color textures, to prevent exceeding the allowable limit.
     */
    public static final int MAX_COLOR_ATTACHMENTS = 16;
    private int objectId;
    private final List<ColorAttachment> colorAttachments = new ArrayList<>();
    private final List<FrameBufferAttachment> attachments = new ArrayList<>();
    private int width;
    private int height;
    private boolean deleted = false;
    private boolean locked = false;

    /// Constructs a new Framebuffer with the specified width, height, texture format, and depth buffer option.
    /// This constructor initializes the framebuffer by generating a new OpenGL framebuffer object, attaching
    /// a color attachment based on the specified texture format, and optionally attaching a depth buffer. The
    /// dimensions of the framebuffer must be greater than zero, and the texture format must not be null.
    /// If the framebuffer cannot be created due to an OpenGL error, a runtime exception is thrown.
    ///
    /// @param width    The width of the framebuffer. Must be greater than 0.
    /// @param height   The height of the framebuffer. Must be greater than 0.
    /// @param format   The texture format for the framebuffer's color attachment. Must not be null.
    /// @param hasDepth Specifies whether the framebuffer should include a depth buffer.
    /// @throws IllegalArgumentException If the width or height is less than or equal to 0,
    ///                                                                                                    or if the texture format is null.
    /// @throws RuntimeException         If an OpenGL error occurs during the creation of the framebuffer.
    public Framebuffer(int width, int height, TextureFormat format, boolean hasDepth) {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Width and height must be greater than 0");
        if (format == null) throw new IllegalArgumentException("Format must not be null");
        objectId = GL41.glGenFramebuffers();
        int error = GL41.glGetError();
        if (error != GL41.GL_NO_ERROR) {
            throw new RuntimeException("Error when creating framebuffer: " + GLUtils.getErrorName(error));
        }

        this.width = width;
        this.height = height;

        start0();
        attach(new ColorAttachment(format));
        if (hasDepth) {
            attach(new DepthAttachment());
        }
        end();
        finish();
    }

    /// Constructs a new Framebuffer with the specified width and height.
    ///
    /// This constructor initializes the framebuffer by generating a new OpenGL
    /// framebuffer object and checks for errors during the process. If a framebuffer
    /// cannot be created, an exception is thrown. The specified dimensions must
    /// be greater than zero.
    ///
    /// @param width  The width of the framebuffer. Must be greater than 0.
    /// @param height The height of the framebuffer. Must be greater than 0.
    /// @throws IllegalArgumentException If the width or height is less than or equal to 0.
    /// @throws RuntimeException         If an OpenGL error occurs while creating the framebuffer.
    protected Framebuffer(int width, int height) {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Width and height must be greater than 0");
        objectId = GL41.glGenFramebuffers();
        int error = GL41.glGetError();
        if (error != GL41.GL_NO_ERROR) {
            throw new RuntimeException("Error when creating framebuffer: " + GLUtils.getErrorName(error));
        }

        this.width = width;
        this.height = height;
    }

    /// Executes a runnable function within the context of the framebuffer.
    /// This method ensures that the framebuffer context is properly started
    /// and ended in a safe and consistent manner.
    ///
    /// @param func The function to be executed within the framebuffer context. This must not be null, and it should
    ///                                                  perform operations that require an active framebuffer context.
    /// @throws IllegalStateException If the framebuffer is improperly initialized, has no attachments, or fails to
    ///                                                                                           bind correctly.
    /// @throws RuntimeException      If any OpenGL error occurs during the start or end of the framebuffer context.
    protected final void withinContext(Runnable func) {
        start0();
        func.run();
        end();
    }

    /// Starts rendering to the framebuffer. This method initializes the framebuffer rendering context
    /// and prepares it for rendering operations by binding the framebuffer and setting its viewport dimensions.
    ///
    /// Preconditions:
    /// - The framebuffer must have at least one attachment.
    /// - The framebuffer must be locked using the [#finish()] method.
    ///
    /// Postconditions:
    /// - The framebuffer is bound and ready for rendering operations.
    /// - The OpenGL viewport is configured to match the framebuffer's dimensions.
    ///
    /// @throws IllegalStateException if the framebuffer has no attachments or is not locked.
    /// @throws RuntimeException      if there is an OpenGL error or the framebuffer is incomplete.
    public void start() {
        if (attachments.isEmpty()) {
            throw new IllegalStateException("No attachments attached to this framebuffer");
        }

        if (!locked) {
            throw new IllegalStateException("Framebuffer isn't locked in. Call finish() first (" + getName() + ")");
        }

        int lastObjectId = getCurrentObjectId();
        start0();

        // Save current viewport
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);

        // Set viewport to framebuffer dimensions
        GL11.glViewport(0, 0, width, height);

        int i = GL30.glCheckFramebufferStatus(GL41.GL_FRAMEBUFFER);
        if (i != GL41.GL_FRAMEBUFFER_COMPLETE) {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, lastObjectId);
            // Restore original viewport on error
            GL11.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
            throw new RuntimeException("Error when binding framebuffer '" + getName() + "': " + switch (i) {
                case GL41.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> "Incompatible attachments";
                case GL41.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> "No attachments attached";
                case GL41.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER -> "Incompatible draw buffers";
                case GL41.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER -> "Incompatible read buffers";
                case GL41.GL_FRAMEBUFFER_UNSUPPORTED -> "Unsupported combination of formats";
                case GL41.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE -> "Incompatible multisampling";
                case GL41.GL_FRAMEBUFFER_UNDEFINED -> "Undefined behavior";
                default -> "Unknown error";
            });
        }

        int error = GL41.glGetError();
        if (error != GL41.GL_NO_ERROR) {
            throw new RuntimeException("Error when binding framebuffer: " + GLUtils.getErrorName(error));
        }
    }

    protected void start0() {
        stack.push(this);

        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, objectId);

        int error = GL41.glGetError();
        if (error != GL41.GL_NO_ERROR) {
            throw new RuntimeException("Error when binding framebuffer: " + GLUtils.getErrorName(error));
        }
    }

    /// Ends the active framebuffer rendering session and restores the previous state of the framebuffer.
    ///
    /// This method must be called after a corresponding `start()` or `start0()` method
    /// invocation to properly unbind the framebuffer. Failing to call this method in the correct order
    /// will result in an [IllegalStateException].
    ///
    /// @throws IllegalStateException if there is a mismatch with the active framebuffer context.
    /// @throws RuntimeException      if there is an OpenGL error when unbinding the framebuffer
    ///                                                                                                                                                                                                                   or resetting the viewport.
    public void end() {
        if (stack.peek() != this)
            throw new IllegalStateException("start()/end() mismatch, make sure to call end() in the reverse order of start()");

        stack.pop();

        int priorFrameBufferId = getCurrentObjectId();
        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, priorFrameBufferId);
        int error = GL41.glGetError();
        if (error != GL41.GL_NO_ERROR)
            throw new RuntimeException("Error when unbinding framebuffer: " + GLUtils.getErrorName(error));
        if (priorFrameBufferId == 0)
            GL11.glViewport(0, 0, QuantumClient.get().getWindow().getWidth(), QuantumClient.get().getWindow().getHeight());

        error = GL41.glGetError();
        if (error != GL41.GL_NO_ERROR)
            throw new RuntimeException("Error when unbinding framebuffer: " + GLUtils.getErrorName(error));
    }

    private int getCurrentObjectId() {
        if (stack.isEmpty()) {
            return 0;
        }
        return stack.peek().objectId;
    }

    /// Gets a color attachment texture at the specified index.
    ///
    /// @param textureIndex Index of the color attachment to retrieve
    /// @return The texture for the specified color attachment
    /// @throws IndexOutOfBoundsException if the index is out of bounds
    public Texture get(int textureIndex) {
        if (textureIndex < 0 || textureIndex >= colorAttachments.size()) {
            throw new IndexOutOfBoundsException("Color attachment index " + textureIndex + " is out of bounds (size: " + colorAttachments.size() + ")");
        }
        return colorAttachments.get(textureIndex);
    }

    /// Gets all color attachments of this framebuffer.
    ///
    /// @return Unmodifiable list of color attachments
    public List<ColorAttachment> getColorAttachments() {
        return Collections.unmodifiableList(colorAttachments);
    }

    /// Attaches a [FrameBufferAttachment] to the framebuffer. This can include
    /// a color attachment or a depth/stencil attachment. If it is a color attachment,
    /// it will be assigned the next available color attachment slot, provided the
    /// maximum number of color attachments has not been exceeded.
    ///
    /// @param attachment The framebuffer attachment to attach. This can be an instance of a color attachment or depth/stencil attachment. Must not be null.
    /// @throws IllegalArgumentException if the attachment is null.
    /// @throws IllegalStateException    if the attachment is a color attachment and the
    ///                                                                                                                                                                                                                                        maximum number of color attachments has been exceeded.
    public void attach(FrameBufferAttachment attachment) {
        if (attachment == null) {
            throw new IllegalArgumentException("Attachment cannot be null");
        }

        if (attachment instanceof ColorAttachment colorAttachment) {
            if (colorAttachments.size() >= MAX_COLOR_ATTACHMENTS) {
                throw new IllegalStateException("Maximum number of color attachments (" + MAX_COLOR_ATTACHMENTS + ") exceeded");
            }
            attachment.attach(this, colorAttachments.size());
            colorAttachments.add(colorAttachment);
        } else {
            // For depth/stencil attachments
            attachment.attach(this, 0);
        }
        attachments.add(attachment);
    }

    public void finish() {
        if (locked) {
            return;
        }

        withinContext(() -> {
            if (colorAttachments.isEmpty()) {
                throw new IllegalStateException("No attachments attached to framebuffer " + getName());
            }

            int[] attachments = new int[colorAttachments.size()];
            for (int i = 0; i < colorAttachments.size(); i++) {
                int attachment = colorAttachments.get(i).getAttachment();
                if (ArrayUtils.contains(attachments, attachment))
                    throw new IllegalStateException("Duplicate attachment: " + attachment);
                if (attachment >= GL30.GL_COLOR_ATTACHMENT0 && attachment <= GL30.GL_COLOR_ATTACHMENT31)
                    attachments[i] = attachment;
                else throw new IllegalStateException("Invalid attachment: " + attachment);
            }
            GL31.glDrawBuffers(attachments);
            int error = GL41.glGetError();
            if (error != GL41.GL_NO_ERROR)
                throw new RuntimeException("Error when setting draw buffers for " + getName() + ": " + GLUtils.getErrorName(error));

            locked = true;
        });
    }

    @Override
    public int getObjectId() {
        return objectId;
    }

    @Override
    public void delete() {
        if (deleted) {
            CommonConstants.LOGGER.warn("Tried to delete already deleted framebuffer: {}", getName());
            return;
        }
        deleted = true;

        GL41.glGetError();

        for (FrameBufferAttachment attachment : attachments) {
            try {
                attachment.delete();
            } catch (Exception e) {
                // Ignore
            }

            GL41.glGetError();
        }
        GL41.glDeleteFramebuffers(objectId);
        GL41.glGetError();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /// Retrieves the current framebuffer from the stack.
    /// If the stack is empty, this method returns null.
    ///
    /// @return the current framebuffer from the stack, or null if the stack is empty
    public static Framebuffer getCurrent() {
        if (stack.isEmpty()) {
            return null;
        }
        return stack.peek();
    }

    /// Checks if any framebuffer is currently bound in the stack.
    ///
    /// @return `true` if the framebuffer stack is not empty, indicating that a framebuffer is bound;
    ///         `false` otherwise.
    public static boolean isAnyBound() {
        return !stack.isEmpty();
    }

    /// Clears the current framebuffer stack.
    ///
    /// @throws IllegalStateException if any framebuffer in the stack cannot be ended properly.
    public static void clear() {
        while (!stack.isEmpty()) {
            stack.pop().end();
        }
    }

    /// Resizes the framebuffer to the specified width and height.
    /// This method updates the dimensions of the framebuffer, regenerates
    /// attachments, and ensures proper OpenGL framebuffer configuration.
    ///
    /// @param width  The new width of the framebuffer. Must be greater than 0.
    /// @param height The new height of the framebuffer. Must be greater than 0.
    /// @throws IllegalArgumentException If the width or height is less than or equal to 0.
    /// @throws RuntimeException         If an OpenGL error occurs while creating the framebuffer or resizing its attachments.
    public void resize(int width, int height) {
        locked = false;

        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Width and height must be greater than 0");
        objectId = GL41.glGenFramebuffers();
        int error = GL41.glGetError();
        if (error != GL41.GL_NO_ERROR) {
            throw new RuntimeException("Error when creating framebuffer: " + GLUtils.getErrorName(error));
        }

        this.width = width;
        this.height = height;

        start0();
        for (FrameBufferAttachment attachment : attachments) {
            attachment.resize(width, height);
        }
        finish();

        locked = true;
    }

    /// Binds the framebuffer represented by this object to the OpenGL pipeline for rendering operations.
    ///
    /// This method uses the framebuffer's OpenGL object ID to bind it as the currently active framebuffer.
    /// It also checks for any OpenGL errors after the binding procedure and throws a runtime exception if
    /// an error is detected.
    ///
    /// @throws RuntimeException if an OpenGL error occurs during the binding operation. The exception message
    ///                                                   includes the name of the encountered error.
    public void bind() {
        GL41.glBindFramebuffer(GL41.GL_FRAMEBUFFER, objectId);
        int error = GL41.glGetError();
        if (error != GL41.GL_NO_ERROR)
            throw new RuntimeException("Error when binding framebuffer: " + GLUtils.getErrorName(error));
    }

    /// A debug renderer used to render details about a [Framebuffer] and its
    /// attachments to the [ImGuiOverlay]
    ///
    /// This class implements the [Renderer] interface for [Framebuffer].
    public static class DebugRenderer implements Renderer<Framebuffer> {
        @Override
        public void render(Framebuffer object, @Nullable Consumer<Framebuffer> setter) {
            if (object.attachments.isEmpty()) {
                return;
            }

            for (FrameBufferAttachment attachment : object.attachments) {
                ImGuiOverlay.renderObject(attachment);
                ImGui.separator();
            }
        }
    }
}
