package dev.ultreon.qvoxel.client.render;

import static org.lwjgl.opengl.GL11.*;

public enum GLCullFace implements GLEnum {
    Front(GL_FRONT),
    Back(GL_BACK),
    FrontAndBack(GL_FRONT_AND_BACK);

    private final int glValue;

    GLCullFace(int glValue) {
        this.glValue = glValue;
    }

    @Override
    public int getGLValue() {
        return glValue;
    }
}
