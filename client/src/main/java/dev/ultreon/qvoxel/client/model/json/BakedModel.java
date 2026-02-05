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

package dev.ultreon.qvoxel.client.model.json;

import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.client.render.Mesh;
import dev.ultreon.qvoxel.client.render.MeshData;
import dev.ultreon.qvoxel.client.shader.ShaderProgram;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public record BakedModel(Mesh mesh) implements AutoCloseable {

    public BakedModel(MeshData data) {
        this(new Mesh(data));
    }

    public void render(ShaderProgram shader) {
        mesh.render(shader);
    }

    public void close() {
        mesh.delete();
    }

    public void render(GuiRenderer renderer, Matrix4f projectionMatrix, Matrix4fStack viewMatrix, Matrix4f modelMatrix) {
        renderer.customRender(_ -> {
            var shader = QuantumClient.get().shaders.getItemGuiProgram();
            shader.setUniform("projection", projectionMatrix);
            shader.setUniform("view", viewMatrix);
            shader.setUniform("model", modelMatrix);
            render(shader);
        });
    }
}
