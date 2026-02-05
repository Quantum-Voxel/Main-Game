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
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.resource.Resource;
import dev.ultreon.qvoxel.resource.ResourceManager;
import dev.ultreon.qvoxel.util.ResourceNotFoundException;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class ShaderPart {
    private final Identifier id;
    private final GLShaderType glShaderType;
    private String source;
    private int shaderID = -1;

    public ShaderPart(Identifier id, GLShaderType glShaderType) {
        this.id = id;
        this.glShaderType = glShaderType;
    }

    public ShaderPart(String source, Identifier id, GLShaderType glShaderType) {
        this.source = source;
        this.id = id;
        this.glShaderType = glShaderType;

        shaderID = ShaderProgram.loadShader(source, glShaderType.getGLValue());
        if (shaderID == -1) throw new ShaderException("Failed to load shader " + id);

        compile();
    }

    void reload() {
        if (shaderID != -1) GL20.glDeleteShader(shaderID);

        shaderID = ShaderProgram.loadShader(QuantumClient.get().resourceManager, id, glShaderType.getGLValue());
        if (shaderID == -1) throw new ShaderException("Failed to load shader " + id);

        compile();
    }

    public int getShaderID() {
        return shaderID;
    }

    public void compile() {
        if (shaderID == -1) throw new ShaderException("Shader is not loaded");
        GL20.glCompileShader(shaderID);
        if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new ShaderException("Failed to compile shader " + id + ":\n" + GL20.glGetShaderInfoLog(shaderID, 1024).indent(8));
        }
    }

    public String getSource() {
        ResourceManager manager = QuantumClient.get().resourceManager;
        @Nullable Resource resource = manager.getResource(id);
        if (resource == null) throw new ResourceNotFoundException(id);
        byte[] bytes = resource.readBytes();
        if (bytes == null) throw new ResourceNotFoundException(id);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public GLShaderType getType() {
        return glShaderType;
    }
}
