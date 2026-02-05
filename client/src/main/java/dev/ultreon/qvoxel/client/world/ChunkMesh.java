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

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.debug.DebugRenderer;
import dev.ultreon.qvoxel.client.debug.FrameBufferRenderer;
import dev.ultreon.qvoxel.client.render.GLUtils;
import dev.ultreon.qvoxel.client.render.Mesh;
import dev.ultreon.qvoxel.client.shader.ShaderProgram;
import dev.ultreon.qvoxel.resource.GameObject;
import dev.ultreon.qvoxel.world.World;
import imgui.type.ImBoolean;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import static org.lwjgl.opengl.GL15.*;

@DebugRenderer(ChunkMesh.DebugRenderer.class)
public class ChunkMesh extends GameObject implements AutoCloseable, Renderable {
    private final RenderType renderType;
    private Mesh mesh;
    private final ClientChunk chunk;
    private final Vector3f tmpF = new Vector3f();
    private final Vector3d tmp = new Vector3d();
    private final int queryId;
    private final Matrix4f oldMatrix = new Matrix4f();

    public ChunkMesh(RenderType renderType, Mesh mesh, ClientChunk chunk) {
        this.renderType = renderType;
        this.mesh = mesh;
        this.chunk = chunk;
        queryId = glGenQueries();
    }

    public void close() {
        if (mesh != null) mesh.delete();
        mesh = null;

        if (GL15.glIsQuery(queryId)) {
            glDeleteQueries(queryId);
        }

        GL11.glGetError();
    }

    public RenderType getRenderType() {
        return renderType;
    }

    public ClientChunk getChunk() {
        return chunk;
    }

    @Override
    public void render(Camera view, Matrix4fStack transform, Vector3d globalPos, RenderBuffer buffer, float partialTicks) {
        if (mesh == null) return;

        oldMatrix.set(modelMatrix);
        modelMatrix.set(transform);

        transform.pushMatrix();
        tmpF.set(tmp.set(chunk.vec.x, chunk.vec.y, chunk.vec.z)
                .mul(World.CHUNK_SIZE)
                .sub(globalPos));
        transform.translate(tmpF.x, tmpF.y, tmpF.z);


        ShaderProgram shaderProgram = renderType.shaderProgram();
        if (shaderProgram == null) {
            transform.popMatrix();
            return;
        }

        shaderProgram.use();
        if (shaderProgram.hasUniform("colorTexture")) {
            shaderProgram.setUniform("colorTexture", 0);
        }

        // TODO: Re-enable these textures when implementing advanced rendering
//        if (shaderProgram.hasUniform("lightTexture")) {
//            QuantumClient.get().lightTexture.use(1);
//            shaderProgram.setUniform("lightTexture", 1);
//        }
//        if (shaderProgram.hasUniform("normalTexture")) {
//            QuantumClient.get().normalTextureAtlas.use(2);
//            shaderProgram.setUniform("normalTexture", 2);
//        }
//        if (shaderProgram.hasUniform("depthTexture")) {
//            QuantumClient.get().depthTexture.use(3);
//            shaderProgram.setUniform("depthTexture", 3);
//        }

        // Required uniforms
        shaderProgram.setUniform("projectionMatrix", view.getProjectionMatrix());
        shaderProgram.setUniform("viewMatrix", view.getViewMatrix());
        shaderProgram.setUniform("modelMatrix", transform);

        // Optional uniforms
        if (shaderProgram.hasUniform("SkyLight"))
            shaderProgram.setUniform("SkyLight", chunk.getWorld().getSkyLight());
        if (shaderProgram.hasUniform("MinLight"))
            shaderProgram.setUniform("MinLight", CommonConstants.MIN_LIGHT);
        if (shaderProgram.hasUniform("atlasSize"))
            shaderProgram.setUniform("atlasSize", QuantumClient.get().blockTextureAtlas.getSize());
        if (shaderProgram.hasUniform("cameraPos"))
            shaderProgram.setUniform("cameraPos", view.getPosition());
        if (shaderProgram.hasUniform("chunkPos"))
            shaderProgram.setUniform("chunkPos", chunk.vec);
        if (shaderProgram.hasUniform("time"))
            shaderProgram.setUniform("time", QuantumClient.get().getTotalTimeSeconds());
        if (shaderProgram.hasUniform("partialTicks"))
            shaderProgram.setUniform("partialTicks", partialTicks);
        shaderProgram.enableAttribute("Position");
        shaderProgram.enableAttribute("UV");
        shaderProgram.enableAttribute("Normal");
        mesh.render(shaderProgram);
        shaderProgram.disableAttribute("Position");
        shaderProgram.disableAttribute("UV");
        shaderProgram.disableAttribute("Normal");

        transform.popMatrix();

        int error = glGetError();
        if (error != GL_NO_ERROR) {
            throw new RuntimeException("Failed to render chunk mesh: %s".formatted(GLUtils.getErrorName(error)));
        }
    }

    public String renderDebug(Matrix4f projection, Matrix4f view, Matrix4f model, boolean shadows) {
        if (mesh == null) return null;

        ShaderProgram shaderProgram = renderType.shaderProgram();
        shaderProgram.use();
        shaderProgram.setUniform("colorTexture", 0);
        shaderProgram.setUniform("projectionMatrix", projection);
        shaderProgram.setUniform("viewMatrix", view);
        shaderProgram.setUniform("modelMatrix", model);
        if (shaderProgram.hasUniform("SkyLight"))
            shaderProgram.setUniform("SkyLight", chunk.getWorld().getSkyLight());
        if (shaderProgram.hasUniform("MinLight"))
            shaderProgram.setUniform("MinLight", CommonConstants.MIN_LIGHT);
        if (shaderProgram.hasUniform("atlasSize"))
            shaderProgram.setUniform("atlasSize", QuantumClient.get().blockTextureAtlas.getSize());
        if (shaderProgram.hasUniform("chunkPos"))
            shaderProgram.setUniform("chunkPos", chunk.vec);
        if (shaderProgram.hasUniform("time"))
            shaderProgram.setUniform("time", QuantumClient.get().getTotalTimeSeconds());
        shaderProgram.enableAttribute("Position");
        shaderProgram.enableAttribute("UV");
        shaderProgram.enableAttribute("Normal");
        shaderProgram.enableAttribute("AO");
        shaderProgram.enableAttribute("Light");
        mesh.render(shaderProgram);
        shaderProgram.disableAttribute("Position");
        shaderProgram.disableAttribute("UV");
        shaderProgram.disableAttribute("Normal");
        shaderProgram.disableAttribute("AO");
        shaderProgram.disableAttribute("Light");

        int error = glGetError();
        if (error != GL_NO_ERROR) {
            return "Failed to render chunk mesh debug: %s".formatted(GLUtils.getErrorName(error));
        }
        return null;
    }

    public static class DebugRenderer extends FrameBufferRenderer<ChunkMesh> {
        private final ImBoolean shadows = new ImBoolean(false);

        @Override
        public void offRender(ChunkMesh object, Matrix4f projection, Matrix4f view, Matrix4f model) {
            QuantumClient.get().blockTextureAtlas.use();
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glCullFace(GL11.GL_BACK);
            GL11.glFrontFace(GL11.GL_CW);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            object.renderDebug(projection, view, model, shadows.get());
        }


        @Override
        protected Vector3f getOrigin(Vector3f vector3f) {
            return vector3f.set(0, 0, 0);
        }

        @Override
        protected float getSize() {
            return World.CHUNK_SIZE;
        }
    }
}
