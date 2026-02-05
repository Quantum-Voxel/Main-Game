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

package dev.ultreon.qvoxel.client.world;

import dev.ultreon.qvoxel.client.render.GraphicsException;
import dev.ultreon.qvoxel.client.shader.ShaderProgram;
import org.joml.Matrix4fStack;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class RenderBuffer {
    private final RenderType renderType;
    private final List<Renderable> renderables = new ArrayList<>();
    private boolean opened;
    private Camera camera;
    private Matrix4fStack matrixStack;
    private Vector3d origin;
    private float partialTicks;

    public RenderBuffer(RenderType renderType) {
        this.renderType = renderType;
    }

    public RenderBuffer open(Camera camera, Matrix4fStack matrixStack, Vector3d origin, float partialTicks) {
        this.camera = camera;
        this.matrixStack = matrixStack;
        this.origin = origin;
        this.partialTicks = partialTicks;
        if (opened) throw new GraphicsException("Render buffer is already started");

        opened = true;
        renderables.clear();
        return this;
    }

    public void render(Renderable mesh) {
        if (!opened) throw new GraphicsException("Render buffer is not started");

        renderables.add(mesh);
    }

    public RenderType getRenderType() {
        return renderType;
    }

    public void close() {
        if (!opened) throw new GraphicsException("Render buffer is not started");

        renderType.prepare();
        for (Renderable mesh : renderables)
            mesh.render(camera, matrixStack, origin, this, partialTicks);
        renderType.finish();

        renderables.clear();

        matrixStack = null;
        camera = null;

        opened = false;
    }

    public void setShaderProgram(ShaderProgram solidShader) {
        renderType.setShaderProgram(solidShader);
    }
}
