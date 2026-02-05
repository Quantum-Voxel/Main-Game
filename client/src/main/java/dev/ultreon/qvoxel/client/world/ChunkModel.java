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
import dev.ultreon.qvoxel.block.BoundingBox;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.debug.DebugRenderer;
import dev.ultreon.qvoxel.client.debug.FrameBufferRenderer;
import dev.ultreon.qvoxel.client.model.OpaqueFaces;
import dev.ultreon.qvoxel.client.world.mesher.ChunkMeshBuilder;
import dev.ultreon.qvoxel.client.world.mesher.FaceCullMesher;
import dev.ultreon.qvoxel.resource.GameObject;
import dev.ultreon.qvoxel.server.ServerOfflineException;
import dev.ultreon.qvoxel.util.ExecutorClosedException;
import dev.ultreon.qvoxel.world.World;
import imgui.ImGui;
import imgui.type.ImBoolean;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@DebugRenderer(ChunkModel.DebugRenderer.class)
public class ChunkModel extends GameObject implements AutoCloseable {
    static int concurrentBuilds = 0;
    private final Map<RenderType, ChunkMesh> meshes = new HashMap<>();
    private final ClientChunk chunk;
    private final OpaqueFaces opaqueFaces = new OpaqueFaces();
    private boolean built, deleting, building;
    private boolean unloading;
    private boolean deleted;

    public ChunkModel(ClientChunk chunk) {
        this.chunk = chunk;
        built = false;

        addComponent(opaqueFaces);
    }

    @Override
    public void close() {
        if (deleted) return;
        deleted = true;
        deleting = true;
        for (ChunkMesh mesh : meshes.values()) {
            mesh.close();
        }
    }

    public void rebuild() {
        if (!QuantumClient.isRenderThread()) throw new IllegalStateException("Not on render thread");
        if (unloading) return;

        building = true;
        concurrentBuilds++;
        CompletableFuture.runAsync(() -> {
            var builder = new ChunkMeshBuilder(chunk);
            var mesher = new FaceCullMesher(chunk);
            var opaqueFaces = new OpaqueFaces();
            builder.begin();
            BoundingBox boundingBox = new BoundingBox();
            mesher.buildMesh(boundingBox, opaqueFaces, (block, _, pass) -> {
                if (block != null) {
                    return BlockRenderTypeRegistry.getRenderType(block.getBlock()).equals(pass);
                }
                return false;
            }, builder);

            // Finish off in the render thread
            QuantumClient.invoke(() -> {
                if (unloading) {
                    // Chunk is being unloaded, don't build the mesh,
                    // and unset both the building flag and the unloading flag
                    building = false;
                    unloading = false;
                    concurrentBuilds--;
                    return;
                }
                if (deleting) {
                    builder.end(null);
                    concurrentBuilds--;
                    return; // Don't need to unset the building flag, the chunk is unused anyway
                }

                for (RenderType renderType : List.copyOf(meshes.keySet())) {
                    ChunkMesh remove = meshes.remove(renderType);
                    remove.close();
                    remove(remove);
                }

                // Build the mesh and add it to the map
                builder.end(meshes);
                meshes.values().forEach(mesh -> add(mesh.getRenderType().getName(), mesh));
                this.opaqueFaces.set(opaqueFaces);
                built = true;
                building = false;
                concurrentBuilds--;
            }).exceptionally(throwable -> {
                if (throwable instanceof ExecutorClosedException || throwable instanceof ServerOfflineException) {
                    // Ignore
                    return null;
                }
                CommonConstants.LOGGER.error("Failed to finish building chunk mesh", throwable);
                return null;
            });
        }, QuantumClient.get().modelExecutor).exceptionally(throwable -> {
            CommonConstants.LOGGER.error("Failed to build chunk mesh", throwable);
            return null;
        });

        chunk.done();
    }

    public boolean render(RenderBufferSource source) {
        boolean rendered = false;
        for (ChunkMesh mesh : meshes.values()) {
            source.get(mesh.getRenderType()).render(mesh);
            rendered = true;
        }

        return rendered;
    }

    public boolean isBuilt() {
        return built;
    }

    public void unload() {
        if (building) {
            unloading = true;
            return;
        }

        for (ChunkMesh mesh : meshes.values()) {
            mesh.close();
        }
        built = false;
        meshes.clear();
    }

    private String renderDebug(Matrix4f projection, Matrix4f view, Matrix4f model, boolean shadows) {
        if (meshes.isEmpty())
            return null;
        StringBuilder errorBuilder = new StringBuilder();
        boolean error = false;

        QuantumClient.get().blockTextureAtlas.use();

        for (ChunkMesh mesh : meshes.values()) {
            String s = mesh.renderDebug(projection, view, model, shadows);
            if (s != null) {
                errorBuilder.append(s).append("\n");
                error = true;
            }
        }

        return error ? errorBuilder.toString() : "";
    }

    public static class DebugRenderer extends FrameBufferRenderer<ChunkModel> {

        private boolean rendered;
        private String error;
        private final ImBoolean shadows = new ImBoolean(false);

        @Override
        public void offRender(ChunkModel object, Matrix4f projection, Matrix4f view, Matrix4f model) {
            if (!object.isBuilt()) {
                rendered = false;
                error = null;
                return;
            }
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glCullFace(GL11.GL_BACK);
            GL11.glFrontFace(GL11.GL_CW);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            String s = object.renderDebug(projection, view, model, shadows.get());
            error = null;
            if (s != null) {
                if (!s.isBlank()) error = s;
                rendered = true;
            } else {
                rendered = false;
            }
        }

        @Override
        public void render(ChunkModel object, @Nullable Consumer<ChunkModel> setter) {
            super.render(object, setter);
            if (!rendered) {
                ImGui.text("Chunk not built yet");
            }
            if (error != null) {
                ImGui.textColored(1, 0.25f, 0.25f, 1, error);
            }
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
