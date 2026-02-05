package dev.ultreon.qvoxel.client.render;

import org.lwjgl.opengl.GL11;

/**
 * GLComparison is an enumeration that represents OpenGL comparison functions.
 * Each enumerated value corresponds to a specific OpenGL constant,
 * typically used in operations involving depth testing, stencil testing, or similar comparisons.
 *
 * The comparison functions determine how a new incoming value is compared with an existing value,
 * often to establish whether the incoming value should be discarded or rendered.
 *
 * @see GLState#depthFunc
 */
public enum GLComparison implements GLEnum {
    Never(GL11.GL_NEVER),
    Less(GL11.GL_LESS),
    LessEqual(GL11.GL_LEQUAL),
    Equal(GL11.GL_EQUAL),
    NotEqual(GL11.GL_NOTEQUAL),
    GreaterEqual(GL11.GL_GEQUAL),
    Greater(GL11.GL_GREATER),
    Always(GL11.GL_ALWAYS);

    private final int glValue;

    GLComparison(int glValue) {
        this.glValue = glValue;
    }

    @Override
    public int getGLValue() {
        return glValue;
    }
}
