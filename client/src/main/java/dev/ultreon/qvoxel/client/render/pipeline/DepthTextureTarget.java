package dev.ultreon.qvoxel.client.render.pipeline;

import dev.ultreon.qvoxel.client.framebuffer.FrameBufferAttachment;
import dev.ultreon.qvoxel.client.framebuffer.Framebuffer;
import dev.ultreon.qvoxel.client.render.GLObject;
import dev.ultreon.qvoxel.client.render.GLUtils;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.client.world.ClientPlayerEntity;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL30C.*;

public class DepthTextureTarget implements FrameBufferAttachment, TextureTarget, GLObject {
    private final DepthTextureFormat format;
    private Framebuffer frameBuffer;
    private int objectId;
    private int width;
    private int height;

    public DepthTextureTarget(DepthTextureFormat format) {
        super();
        this.format = format;
    }

    @Override
    public void use() {
        glBindTexture(GL_TEXTURE_2D, objectId);
    }

    @Override
    public void use(int unit) {
        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_2D, objectId);
    }

    @Override
    public void render(ClientPlayerEntity player, GuiRenderer renderer) {
        if (frameBuffer instanceof RenderNode renderNode) {
            renderNode.render(player, renderer);
        }
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void attach(Framebuffer frameBuffer, int unit) {
        this.frameBuffer = frameBuffer;


        objectId = glGenTextures();
        if (objectId <= 0)
            throw new RuntimeException("Could not generate texture ID!");

        glBindTexture(GL_TEXTURE_2D, objectId);
        int error = glGetError();
        if (error != GL_NO_ERROR)
            throw new RuntimeException("Error when binding texture: " + GLUtils.getErrorName(error));

        glTexImage2D(GL_TEXTURE_2D, 0, format.getInternalFormat(), getWidth(), getHeight(), 0, format.getFormat(), format.getType(), 0);
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
            throw new RuntimeException("Error when attaching depth texture to framebuffer: " + GLUtils.getErrorName(error));

        width = frameBuffer.getWidth();
        height = frameBuffer.getHeight();
    }

    @Override
    public void detach() {
        frameBuffer = null;
        if (objectId > 0) {
            glDeleteTextures(objectId);
            objectId = 0;
        }
    }

    @Override
    public int getAttachment() {
        return GL_DEPTH_ATTACHMENT;
    }

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;

        Framebuffer buffer = frameBuffer;

        glDeleteTextures(objectId);
        attach(buffer, 0);
    }

    @Override
    public int getObjectId() {
        return objectId;
    }

    @Override
    public void delete() {
        detach();
        glDeleteTextures(objectId);
        frameBuffer = null;
    }
}
