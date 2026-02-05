package dev.ultreon.qvoxel.client.render.pipeline;

import static org.lwjgl.opengl.GL11C.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL30C.GL_DEPTH_COMPONENT24;
import static org.lwjgl.opengl.GL30C.GL_DEPTH_STENCIL;

public enum DepthTextureFormat {
    DEPTH24_STENCIL8(GL_DEPTH_COMPONENT, GL_DEPTH_STENCIL, GL_FLOAT),
    DEPTH24(GL_DEPTH_COMPONENT24, GL_DEPTH_COMPONENT, GL_FLOAT);

    private final int glFormat;
    private final int glInternalFormat;
    private final int glType;

    DepthTextureFormat(int glFormat, int glInternalFormat, int glType) {
        this.glFormat = glFormat;
        this.glInternalFormat = glInternalFormat;
        this.glType = glType;
    }

    public int getFormat() {
        return glFormat;
    }

    public int getInternalFormat() {
        return glInternalFormat;
    }

    public int getType() {
        return glType;
    }
}
