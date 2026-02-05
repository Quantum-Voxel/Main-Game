package dev.ultreon.qvoxel.client.render;

import dev.ultreon.qvoxel.client.shader.GLShaderType;

import java.util.BitSet;
import java.util.Stack;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class RenderSystem {
    private static final BitSet vertexArrays = new BitSet();
    private static final BitSet buffers = new BitSet();
    private static final BitSet textures = new BitSet();
    private static final BitSet shaders = new BitSet();
    private static final BitSet programs = new BitSet();
    private static final BitSet framebuffers = new BitSet();
    private static final BitSet renderBuffers = new BitSet();

    private static final Stack<GLState> stateStack = new Stack<>();

    static {
        stateStack.push(new GLState());
    }

    public static int genVertexArray() {
        int i = glGenVertexArrays();
        vertexArrays.set(i);
        return i;
    }

    public static void deleteVertexArray(int array) {
        if (!vertexArrays.get(array))
            return;

        vertexArrays.clear(array);
        glDeleteVertexArrays(array);
    }

    public static int genBuffer() {
        int i = glGenBuffers();
        buffers.set(i);
        return i;
    }

    public static void deleteBuffer(int buffer) {
        if (!buffers.get(buffer))
            return;

        buffers.clear(buffer);
        glDeleteBuffers(buffer);
    }

    public static int genTexture() {
        int i = glGenTextures();
        textures.set(i);
        return i;
    }

    public static void deleteTexture(int texture) {
        if (!textures.get(texture))
            return;

        textures.clear(texture);
        glDeleteTextures(texture);
    }

    public static int createShader(GLShaderType type) {
        int i = glCreateShader(type.getGLValue());
        shaders.set(i);
        return i;
    }

    public static void deleteShader(int shader) {
        if (!shaders.get(shader))
            return;

        shaders.clear(shader);
        glDeleteShader(shader);
    }

    public static int createProgram() {
        int i = glCreateProgram();
        programs.set(i);
        return i;
    }

    public static void deleteProgram(int program) {
        if (!programs.get(program))
            return;

        programs.clear(program);
        glDeleteProgram(program);
    }

    public static int genFramebuffer() {
        int i = glGenFramebuffers();
        framebuffers.set(i);
        return i;
    }

    public static void deleteFramebuffer(int framebuffer) {
        if (!framebuffers.get(framebuffer))
            return;

        framebuffers.clear(framebuffer);
        glDeleteFramebuffers(framebuffer);
    }

    public static int genRenderBuffer() {
        int i = glGenRenderbuffers();
        renderBuffers.set(i);
        return i;
    }

    public static void deleteRenderBuffer(int renderBuffer) {
        if (!renderBuffers.get(renderBuffer))
            return;

        renderBuffers.clear(renderBuffer);
        glDeleteRenderbuffers(renderBuffer);
    }

    public static void pushState() {
        stateStack.push(new GLState(stateStack.peek()));
    }

    public static void popState() {
        GLState pop = stateStack.pop();
        stateStack.peek().apply(pop);
    }
}
