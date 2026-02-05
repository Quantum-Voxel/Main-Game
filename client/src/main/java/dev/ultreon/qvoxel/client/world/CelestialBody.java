/*
 * Copyright 2025. Quinten 'Qubix' Jungblut
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.ultreon.qvoxel.client.world;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.render.Mesh;
import dev.ultreon.qvoxel.client.shader.ShaderProgram;
import dev.ultreon.qvoxel.client.texture.Texture;
import dev.ultreon.qvoxel.resource.GameObject;

public class CelestialBody extends GameObject {
    private final Identifier id;
    private final Identifier textureId;

    public CelestialBody(Identifier id) {
        this.id = id;
        textureId = id.mapPath(s -> "textures/environment/" + s + ".png");
    }

    public void render(ShaderProgram program, Mesh mesh) {
        Texture texture = QuantumClient.get().getTextureManager().getTexture(textureId);
        texture.use();

        setUniforms(program);

        mesh.render(program);
    }

    protected void setUniforms(ShaderProgram program) {
        program.setUniform("colorTexture", 0);
        program.setUniform("modelMatrix", modelMatrix);
    }

    public Identifier getId() {
        return id;
    }
}
