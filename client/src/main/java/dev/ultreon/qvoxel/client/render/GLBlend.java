package dev.ultreon.qvoxel.client.render;

import static org.lwjgl.opengl.GL11.*;

/**
 * GLBlend is an enumerated type that represents OpenGL blending factors
 * used in rendering operations. Each enumerated constant corresponds to
 * a specific OpenGL constant value.
 * <p>
 * The blending factors define the computational weighting of source and
 * destination colors when performing blending operations.
 * <p>
 * Each constant in the enum maps directly to its respective OpenGL integer
 * value, which can be retrieved using the {@link #getGLValue} method.
 */
public enum GLBlend implements GLEnum {
    OneMinusSrcAlpha(GL_ONE_MINUS_SRC_ALPHA),
    OneMinusSrcColor(GL_ONE_MINUS_SRC_COLOR),
    OneMinusDstAlpha(GL_ONE_MINUS_DST_ALPHA),
    OneMinusDstColor(GL_ONE_MINUS_DST_COLOR),
    SrcAlpha(GL_SRC_ALPHA),
    SrcColor(GL_SRC_COLOR),
    DstAlpha(GL_DST_ALPHA),
    DstColor(GL_DST_COLOR);

    private final int glValue;

    GLBlend(int glValue) {
        this.glValue = glValue;
    }

    @Override
    public int getGLValue() {
        return glValue;
    }
}
