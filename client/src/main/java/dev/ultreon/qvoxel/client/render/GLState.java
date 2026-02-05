package dev.ultreon.qvoxel.client.render;

import dev.ultreon.qvoxel.client.math.Rectanglei;
import dev.ultreon.qvoxel.client.shader.ShaderProgram;
import dev.ultreon.qvoxel.client.texture.Texture;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.util.Arrays;

import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE31;

public class GLState {
    private int activeTexture = 0;
    private ShaderProgram boundShader = null;

    private boolean blendEnabled = false;
    private GLBlend srcBlend = GLBlend.SrcAlpha;
    private GLBlend dstBlend = GLBlend.OneMinusSrcAlpha;

    private boolean depthTestEnabled = false;
    private GLComparison depthFunc = GLComparison.Always;

    private boolean cullFaceEnabled = false;
    private GLCullFace cullFace = GLCullFace.Back;

    private boolean redMask = true;
    private boolean greenMask = true;
    private boolean blueMask = true;
    private boolean alphaMask = true;
    private boolean depthMask = true;
    private int stencilMask = 0;

    private boolean scissorEnabled = false;
    private Rectanglei scissor = new Rectanglei(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);

    private final Texture[] textures = new Texture[32];

    public GLState(GLState state) {
        activeTexture = state.activeTexture;
        boundShader = state.boundShader;
        blendEnabled = state.blendEnabled;
        srcBlend = state.srcBlend;
        dstBlend = state.dstBlend;
        depthTestEnabled = state.depthTestEnabled;
        depthFunc = state.depthFunc;
        redMask = state.redMask;
        greenMask = state.greenMask;
        blueMask = state.blueMask;
        alphaMask = state.alphaMask;
        depthMask = state.depthMask;
        stencilMask = state.stencilMask;
        scissorEnabled = state.scissorEnabled;
        scissor = state.scissor;
        cullFaceEnabled = state.cullFaceEnabled;
        cullFace = state.cullFace;
        System.arraycopy(state.textures, 0, textures, 0, textures.length);
    }

    public GLState() {

    }

    public void apply(GLState state) {
        applyBlend(state);
        applyCullFace(state);
        applyShader(state);
        applyDepthTest(state);
        applyBuffer(state);
        applyScissor(state);
        applyTextures(state);
    }

    private void applyCullFace(GLState state) {
        if (cullFaceEnabled && !state.cullFaceEnabled)
            GL11.glEnable(GL11.GL_CULL_FACE);
        if (!cullFaceEnabled && state.cullFaceEnabled)
            GL11.glDisable(GL11.GL_CULL_FACE);

        if (cullFace != state.cullFace)
            GL11.glCullFace(cullFace.getGLValue());
    }

    private void applyShader(GLState state) {
        if (boundShader != state.boundShader && boundShader != null)
            boundShader.use();
    }

    private void applyTextures(GLState state) {
        if (!Arrays.equals(textures, state.textures)) {
            for (int texture = GL_TEXTURE0; texture <= GL_TEXTURE31; texture++) {
                int unit = texture - GL_TEXTURE0;
                Texture next = textures[unit];
                Texture prev = state.textures[unit];

                if (next != prev && next != null) next.use(unit);
            }
        }

        if (activeTexture != state.activeTexture)
            GL20.glActiveTexture(activeTexture);
    }

    private void applyScissor(GLState state) {
        if (scissorEnabled && !state.scissorEnabled)
            GL11.glEnable(GL11.GL_SCISSOR_BOX);
        if (!scissorEnabled && state.scissorEnabled)
            GL11.glDisable(GL11.GL_SCISSOR_BOX);

        if (!scissor.equals(state.scissor))
            GL11.glScissor(scissor.x, scissor.y, scissor.width, scissor.height);
    }

    private void applyBuffer(GLState state) {
        if (redMask != state.redMask || greenMask != state.greenMask || blueMask != state.blueMask || alphaMask != state.alphaMask)
            GL11.glColorMask(redMask, greenMask, blueMask, alphaMask);

        if (depthMask != state.depthMask)
            GL11.glDepthMask(depthMask);

        if (stencilMask != state.stencilMask)
            GL11.glStencilMask(stencilMask);
    }

    private void applyDepthTest(GLState state) {
        if (depthTestEnabled && !state.depthTestEnabled)
            GL11.glEnable(GL11.GL_BLEND);
        if (!depthTestEnabled && state.depthTestEnabled)
            GL11.glDisable(GL11.GL_BLEND);

        if (depthFunc != state.depthFunc)
            GL11.glDepthFunc(depthFunc.getGLValue());
    }

    private void applyBlend(GLState state) {
        if (blendEnabled && !state.blendEnabled)
            GL11.glEnable(GL11.GL_BLEND);
        if (!blendEnabled && state.blendEnabled)
            GL11.glDisable(GL11.GL_BLEND);

        if (srcBlend != state.srcBlend || dstBlend != state.dstBlend)
            GL11.glBlendFunc(srcBlend.getGLValue(), dstBlend.getGLValue());
    }
}
