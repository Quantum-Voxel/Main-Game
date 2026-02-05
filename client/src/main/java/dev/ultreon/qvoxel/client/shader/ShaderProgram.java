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

package dev.ultreon.qvoxel.client.shader;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.render.GLObject;
import dev.ultreon.qvoxel.client.render.GLUtils;
import dev.ultreon.qvoxel.client.debug.DebugRenderer;
import dev.ultreon.qvoxel.client.debug.Renderer;
import dev.ultreon.qvoxel.client.render.Color;
import dev.ultreon.qvoxel.resource.ReloadContext;
import dev.ultreon.qvoxel.resource.Resource;
import dev.ultreon.qvoxel.resource.ResourceManager;
import dev.ultreon.qvoxel.util.ResourceNotFoundException;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@DebugRenderer(ShaderProgram.DebugRenderer.class)
public class ShaderProgram implements GLObject, Reloadable {
    private static int usedProgram;
    private int programID;
    private boolean deleted = false;
    private final ShaderPart[] parts;
    private final float[] tmp3x3 = new float[9];
    private final float[] tmp4x4 = new float[16];
    private String[] attrs;
    private final String name;
    private final Object2IntMap<String> uniforms = new Object2IntOpenHashMap<>();

    public ShaderProgram(String name, Identifier vertex, Identifier fragment) {
        this(name, new ShaderPart(vertex, GLShaderType.Vertex), new ShaderPart(fragment, GLShaderType.Fragment));
    }

    public ShaderProgram(Identifier name, String vertexSource, String fragmentSource) {
        this(name.toString(), new ShaderPart(vertexSource, name.mapPath(s -> s + ".vert"), GLShaderType.Vertex), new ShaderPart(fragmentSource, name.mapPath(s -> s + ".frag"), GLShaderType.Fragment));

        programID = GL20.glCreateProgram();

        for (ShaderPart part : parts) {
            GL20.glAttachShader(programID, part.getShaderID());
            if (GL11.glGetError() != GL11.GL_NO_ERROR)
                throw new ShaderException("Failed to attach shader to shader program");
        }

        link();
    }

    private void link() {
        GL20.glLinkProgram(programID);
        if (GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS) == 0) {
            String log = GL20.glGetProgramInfoLog(programID, GL20.glGetProgrami(programID, GL20.GL_INFO_LOG_LENGTH));
            for (ShaderPart part : parts) {
                GL20.glDetachShader(programID, part.getShaderID());
                GL20.glDeleteShader(part.getShaderID());
            }
            GL20.glDeleteProgram(programID);
            throw new ShaderException("Failed to link shader program: " + log);
        }

        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to link shader program");

        discoverAttributes();
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to discover shader attributes");

        discoverUniforms();
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to discover shader uniforms");

        GL20.glValidateProgram(programID);

        if (GL20.glGetProgrami(programID, GL20.GL_VALIDATE_STATUS) == 0) {
            String log = GL20.glGetProgramInfoLog(programID, GL20.glGetProgrami(programID, GL20.GL_INFO_LOG_LENGTH));
            CommonConstants.LOGGER.warn("Warning validating shader program {}: {}", programID, log);
        }
        if (GL11.glGetError() != GL11.GL_NO_ERROR) {
            throw new ShaderException("Failed to validate shader program");
        }
    }

    public ShaderProgram(String name, ShaderPart... parts) {
        this.name = name;
        this.parts = parts;
    }

    private String[] discoverAttributes() {
        int activeAttributeCount = GL20.glGetProgrami(programID, GL20.GL_ACTIVE_ATTRIBUTES);
        String[] attributes = new String[activeAttributeCount];
        int[] types = new int[activeAttributeCount];
        int[] sizes = new int[activeAttributeCount];

        int[] len = new int[1];
        int[] size = new int[1];
        int[] type = new int[1];
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer name = stack.malloc(256);
            for (int i = 0; i < attributes.length; i++) {
                GL20.glGetActiveAttrib(programID, i, len, size, type, name);
                byte[] bytes = new byte[len[0]];
                name.get(bytes);
                attributes[i] = new String(bytes, StandardCharsets.UTF_8);
                types[i] = type[0];
                sizes[i] = size[0];
            }
        }
        attrs = attributes;
        return attributes;
    }

    private String[] discoverUniforms() {
        int activeAttributeCount = GL20.glGetProgrami(programID, GL20.GL_ACTIVE_UNIFORMS);
        uniforms.clear();
        String[] attributes = new String[activeAttributeCount];
        int[] types = new int[activeAttributeCount];
        int[] sizes = new int[activeAttributeCount];

        int[] len = new int[1];
        int[] size = new int[1];
        int[] type = new int[1];
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer name = stack.malloc(256);
            for (int i = 0; i < attributes.length; i++) {
                GL20.glGetActiveUniform(programID, i, len, size, type, name);
                byte[] bytes = new byte[len[0]];
                name.get(bytes);
                attributes[i] = new String(bytes, StandardCharsets.UTF_8);
                types[i] = type[0];
                sizes[i] = size[0];
                int location = GL20.glGetUniformLocation(programID, attributes[i]);
                uniforms.put(new String(bytes), location);
            }
        }
        return attributes;
    }

    public int getAttribLocation(String name) {
        return GL20.glGetAttribLocation(programID, name);
    }

    public void enableAttribute(String name) {
        GL20.glEnableVertexAttribArray(getAttribLocation(name));
        GL11.glGetError(); // Quirk: macOSX returns GL_INVALID_OPERATION here if the attribute doesn't exist
    }

    public void disableAttribute(String name) {
        GL20.glDisableVertexAttribArray(getAttribLocation(name));
        GL11.glGetError(); // Quirk: macOSX returns GL_INVALID_OPERATION here if the attribute is not enabled or doesn't exist'
    }

    public void enableAttributes(String... attributes) {
        for (String attr : attributes) {
            int attribLocation = getAttribLocation(attr);
            GL20.glEnableVertexAttribArray(attribLocation);
            if (GL11.glGetError() != GL11.GL_NO_ERROR)
                throw new ShaderException("Failed to enable attribute " + attr);
        }
    }

    public void disableAttributes(String... attributes) {
        for (String attr : attributes) {
            GL20.glDisableVertexAttribArray(getAttribLocation(attr));
            if (GL11.glGetError() != GL11.GL_NO_ERROR)
                throw new ShaderException("Failed to disable attribute " + attr);
        }
    }

    public String[] getAttributes() {
        if (attrs == null) discoverAttributes();
        return attrs;
    }

    public void use() {
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("OpenGL is in an error state before using shader program");

        if (deleted)
            throw new ShaderException("Shader program has been deleted");

        if (programID <= 0)
            throw new ShaderException("Shader program is not initialized");

        if (usedProgram == programID) return;

        usedProgram = programID;

        GL20.glUseProgram(programID);
        int error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR) {
            try {
                String log = getInfoLog();
                CommonConstants.LOGGER.error("Shader Program Info Log:\n{}", log);
            } catch (Exception e) {
                CommonConstants.LOGGER.error("Failed to get shader program info log: {}", e.getMessage());
            }
            CommonConstants.LOGGER.error("[GL] Failed to use shader program {}: {} ({})", programID, GLUtils.getErrorName(error), error);
        }
    }

    public String getInfoLog() {
        int maxLength = GL20.glGetProgrami(programID, GL20.GL_INFO_LOG_LENGTH);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to get shader program info log length");

        String log = GL20.glGetProgramInfoLog(programID, maxLength);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to get shader program info log");

        return log;
    }

    public int getProgramID() {
        return programID;
    }

    @Override
    public int getObjectId() {
        return programID;
    }

    @Override
    public void delete() {
        if (deleted) return;
        deleted = true;

        for (ShaderPart part : parts) {
            int shaderID = part.getShaderID();
            if (!GL20.glIsShader(shaderID)) continue;
            GL20.glDeleteShader(shaderID);
            int error = GL11.glGetError();
            if (error != GL11.GL_NO_ERROR)
                CommonConstants.LOGGER.warn("Failed to delete shader part: {} ({})", shaderID, GLUtils.getErrorName(error));
        }

        if (!GL20.glIsProgram(programID)) return;
        GL20.glDeleteProgram(programID);
        int error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR)
            CommonConstants.LOGGER.warn("Failed to delete shader program: {} ({})", programID, GLUtils.getErrorName(error));
    }

    public int getUniform(String name) {
        if (deleted)
            throw new ShaderException("Shader program has been deleted");

        if (uniforms.containsKey(name)) return uniforms.getInt(name);

        int location = GL20.glGetUniformLocation(programID, name);
        if (location == -1)
            throw new ShaderException("Uniform " + name + " not found in " + this.name + " (program ID " + programID + ")");
        uniforms.put(name, location);
        return location;
    }

    public boolean hasUniform(String name) {
        if (uniforms.containsKey(name)) return true;

        int location = GL20.glGetUniformLocation(programID, name);
        if (location == -1) return false;
        uniforms.put(name, location);
        return true;
    }

    public void setUniform(String name, int value) {
        GL20.glUniform1i(getUniform(name), value);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to set uniform " + name);
    }

    public void setUniform(String name, float value) {
        GL20.glUniform1f(getUniform(name), value);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to set uniform " + name);
    }

    public void setUniform(String name, boolean value) {
        GL20.glUniform1i(getUniform(name), value ? 1 : 0);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to set uniform " + name);
    }

    public void setUniform(String name, float[] values) {
        GL20.glUniform1fv(getUniform(name), values);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to set uniform " + name);
    }

    public void setUniform(String name, int[] values) {
        GL20.glUniform1iv(getUniform(name), values);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to set uniform " + name);
    }

    public void setUniform(String name, Matrix4f value) {
        GL20.glUniformMatrix4fv(getUniform(name), false, value.get(tmp4x4));
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to set uniform " + name + " in shader " + this.name);
    }

    public void setUniform(String name, Matrix4f[] values) {
        float[] tmp = new float[16 * values.length];
        for (int i = 0; i < values.length; i++) {
            values[i].get(tmp, i * 16);
        }
        GL20.glUniformMatrix4fv(getUniform(name), false, tmp);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to set uniform " + name);
    }

    public void setUniform(String name, Vector2f value) {
        GL20.glUniform2f(getUniform(name), value.x, value.y);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to set uniform " + name);
    }

    public void setUniform(String name, Vector2f[] values) {
        float[] tmp = new float[2 * values.length];
        for (int i = 0; i < values.length; i++) {
            tmp[i * 2] = values[i].x;
            tmp[i * 2 + 1] = values[i].y;
        }
        GL20.glUniform2fv(getUniform(name), tmp);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to set uniform " + name);
    }

    public void setUniform(String name, Vector3f value) {
        GL20.glUniform3f(getUniform(name), value.x, value.y, value.z);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to set uniform " + name);
    }

    public void setUniform(String name, Vector3f[] values) {
        float[] tmp = new float[3 * values.length];
        for (int i = 0; i < values.length; i++) {
            tmp[i * 3] = values[i].x;
            tmp[i * 3 + 1] = values[i].y;
            tmp[i * 3 + 2] = values[i].z;
        }

        GL20.glUniform3fv(getUniform(name), tmp);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to set uniform " + name);
    }

    public void setUniform(String name, Vector4f value) {
        GL20.glUniform4f(getUniform(name), value.x, value.y, value.z, value.w);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to set uniform " + name);
    }

    public void setUniform(String name, Vector4f[] values) {
        float[] tmp = new float[4 * values.length];
        for (int i = 0; i < values.length; i++) {
            tmp[i * 4] = values[i].x;
            tmp[i * 4 + 1] = values[i].y;
            tmp[i * 4 + 2] = values[i].z;
            tmp[i * 4 + 3] = values[i].w;
        }
        GL20.glUniform4fv(getUniform(name), tmp);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to set uniform " + name);
    }

    public void setUniform(String name, Quaternionf value) {
        GL20.glUniform4f(getUniform(name), value.x, value.y, value.z, value.w);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to set uniform " + name);
    }

    public void setUniform(String name, Quaternionf[] values) {
        float[] tmp = new float[4 * values.length];
        for (int i = 0; i < values.length; i++) {
            tmp[i * 4] = values[i].x;
            tmp[i * 4 + 1] = values[i].y;
            tmp[i * 4 + 2] = values[i].z;
            tmp[i * 4 + 3] = values[i].w;
        }
        GL20.glUniform4fv(getUniform(name), tmp);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to set uniform " + name);
    }

    public void setUniform(String name, Vector2i value) {
        GL20.glUniform2i(getUniform(name), value.x, value.y);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to set uniform " + name);
    }

    public void setUniform(String name, Vector3i value) {
        GL20.glUniform3i(getUniform(name), value.x, value.y, value.z);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to set uniform " + name);
    }

    public void setUniform(String name, Vector4i value) {
        GL20.glUniform4i(getUniform(name), value.x, value.y, value.z, value.w);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to set uniform " + name);
    }

    public void setUniform(String name, Vector2d value) {
        GL40.glUniform2d(getUniform(name), value.x, value.y);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to set uniform " + name);
    }

    public void setUniform(String name, Vector3d value) {
        GL40.glUniform3d(getUniform(name), value.x, value.y, value.z);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to set uniform " + name);
    }

    public void setUniform(String name, Vector4d value) {
        GL40.glUniform4d(getUniform(name), value.x, value.y, value.z, value.w);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to set uniform " + name);
    }

    public void setUniform(String name, Matrix3f value) {
        GL20.glUniformMatrix3fv(getUniform(name), false, value.get(tmp3x3));
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to set uniform " + name);
    }

    public void setUniform(String name, Matrix3f[] values) {
        float[] tmp = new float[9 * values.length];
        for (int i = 0; i < values.length; i++) {
            values[i].get(tmp, i * 9);
        }
        GL20.glUniformMatrix3fv(getUniform(name), false, tmp);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to set uniform " + name);
    }

    public void setUniform(String name, Color value) {
        GL20.glUniform4f(getUniform(name), value.r, value.g, value.b, value.a);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to set uniform " + name);
    }

    public static int loadShader(ResourceManager manager, Identifier location, int glShaderType) {
        @Nullable Resource resource = manager.getResource(location);
        if (resource == null) throw new ResourceNotFoundException(location);
        byte[] bytes = resource.readBytes();
        if (bytes == null) throw new ResourceNotFoundException(location);
        String source = new String(bytes, StandardCharsets.UTF_8);
        int shaderID = GL20.glCreateShader(glShaderType);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to create shader");

        if (shaderID == 0) throw new ShaderException("Failed to create shader");
        if (shaderID > 0) GL20.glShaderSource(shaderID, source);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to set shader source");
        return shaderID;
    }

    public static int loadShader(String source, int glShaderType) {
        int shaderID = GL20.glCreateShader(glShaderType);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to create shader");

        if (shaderID == 0) throw new ShaderException("Failed to create shader");
        if (shaderID > 0) GL20.glShaderSource(shaderID, source);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new ShaderException("Failed to set shader source");
        return shaderID;
    }

    public ShaderPart[] getShaderParts() {
        return parts;
    }

    @Override
    public CompletableFuture<?> reload(ReloadContext context) {

        return context.submitSafe(() -> {
            context.log("Loading shader: " + name);

            if (GL20.glIsProgram(programID)) {
                GL20.glDeleteProgram(programID);
            }

            programID = GL20.glCreateProgram();
            for (ShaderPart part : parts) {
                part.reload();
                GL20.glAttachShader(programID, part.getShaderID());
                if (GL11.glGetError() != GL11.GL_NO_ERROR)
                    throw new ShaderException("Failed to attach shader to shader program");
            }
            link();
        });
    }

    public static class DebugRenderer implements Renderer<ShaderProgram> {
        @Override
        public void render(ShaderProgram object, @Nullable Consumer<ShaderProgram> setter) {
            ImString title = new ImString(object.getInfoLog());
            ImGui.inputTextMultiline("Information Log", title, -1, 100, ImGuiInputTextFlags.ReadOnly);
            if (ImGui.treeNode("Attributes")) {
                for (String attr : object.getAttributes()) {
                    ImGui.text(attr);
                }
                ImGui.treePop();
            }
            if (ImGui.treeNode("Shaders")) {
                ImGui.pushID("Shaders");
                for (ShaderPart part : object.getShaderParts()) {
                    if (ImGui.treeNode(part.getType() + " " + part.getShaderID())) {
                        ImGui.pushID(part.getShaderID());
                        ImGui.text(part.getSource());
                        ImGui.popID();
                        ImGui.treePop();
                    }
                }
                ImGui.popID();
                ImGui.treePop();
            }
        }
    }
}
