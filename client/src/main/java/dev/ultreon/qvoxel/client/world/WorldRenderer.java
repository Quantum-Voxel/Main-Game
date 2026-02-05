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
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.model.ModelException;
import dev.ultreon.qvoxel.client.model.entity.PlayerEntityModel;
import dev.ultreon.qvoxel.client.render.*;
import dev.ultreon.qvoxel.client.entity.PlayerEntityRenderer;
import dev.ultreon.qvoxel.client.render.pipeline.RenderNode;
import dev.ultreon.qvoxel.client.render.pipeline.RenderPipeline;
import dev.ultreon.qvoxel.client.shader.ShaderProgram;
import dev.ultreon.qvoxel.resource.GameObject;
import dev.ultreon.qvoxel.world.CollisionType;
import dev.ultreon.qvoxel.world.BlockHitResult;
import dev.ultreon.qvoxel.world.HitResult;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.lwjgl.opengl.GL11;
import dev.ultreon.qvoxel.world.World;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class WorldRenderer extends GameObject implements AutoCloseable {
    private final Mesh mesh;
    public final SkyboxRenderer skyboxRenderer = new SkyboxRenderer(this);
    private final Camera camera;
    private final Matrix4fStack tmpModelMatrix = new Matrix4fStack(1024);
    private final Vector3d tmpPosition = new Vector3d();
    private final RenderPipeline pipeline;
    private final PlayerEntityModel playerModel;
    private final PlayerEntityRenderer playerRenderer;

    private int visibleChunks = 0;
    private final QuantumClient client = QuantumClient.get();
    private final Vector3d tmpPos = new Vector3d();
    private final Mesh celestialMesh;
    private final Color selectionColor = new Color();
    private ShaderProgram solidShader;
    private ShaderProgram transparentShader;
    private ShaderProgram cutoutShader;
    private ShaderProgram waterShader;
    private BlockHitResult lastHitResult;
    private Mesh selectionBox;
    private final ClientPlayerEntity player;

    public WorldRenderer(GraphicsMode mode, ClientPlayerEntity player, int width, int height) {
        pipeline = mode.createPipeline(this, client.getGuiRenderer(), width, height);
        pipeline.verify();
        this.player = player;
        add("Render Pipeline", pipeline);
        mesh = Mesh.cube();
        celestialMesh = Mesh.face();

        playerModel = new PlayerEntityModel(CommonConstants.id("models/entity/player.gltf"));
        playerRenderer = new PlayerEntityRenderer(client);
        try {
            playerModel.load(client.resourceManager);
        } catch (ModelException e) {
            CommonConstants.LOGGER.error("Failed to load player model:", e);
        }

        client.particleSystem.start();
        camera = new Camera(width, height);
    }

    public void render(float partialTicks) {
        pipeline.render(player, partialTicks);
    }

    private void renderInternal(float partialTicks) {
        skyboxRenderer.render(player, partialTicks);

        var worldProgtam = client.shaders.getWorldProgram();
        var lineProgram = client.shaders.getLineProgram();
        var skyboxProgram = client.shaders.getSkyboxProgram();
        var celestialBodyProgram = client.shaders.getCelestialBodyProgram();
        var particlesShaderProgram = client.shaders.getParticlesShaderProgram();

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glFrontFace(GL11.GL_CW);
        GL11.glDepthMask(true);

        try (RenderBufferSource source = client.getRenderBuffers().open(camera, tmpModelMatrix, player.getPosition(tmpPos, partialTicks), partialTicks)) {
            source.get(RenderType.ENTITY_CUTOUT);
            RenderType.SOLID.setShaderProgram(solidShader);
            source.get(RenderType.SOLID);
            RenderType.CUTOUT.setShaderProgram(cutoutShader);
            source.get(RenderType.CUTOUT);
            RenderType.LEAVES.setShaderProgram(cutoutShader);
            source.get(RenderType.LEAVES);
            RenderType.WATER.setShaderProgram(waterShader);
            source.get(RenderType.WATER);
            RenderType.TRANSPARENT.setShaderProgram(transparentShader);
            source.get(RenderType.TRANSPARENT);

            camera.setRotation(player.yawHead, player.pitchHead);

            boolean rebuilt = false;
            visibleChunks = 0;
            List<ClientChunk> chunks = getSortedChunks(player, partialTicks);

            renderChunks(player, chunks, rebuilt, source);

            // Draw an outline cube for each voxel shape in the selected block
            HitResult hit = player.castRay(6.0F);
            if (hit instanceof BlockHitResult selection) {
                buildSelectionBox(selection);

                RenderType.OUTLINE.setShaderProgram(lineProgram);

                RenderBuffer outlineBuffer = source.get(RenderType.OUTLINE);
                renderSelectionBox(selection, outlineBuffer, lineProgram);
            }

            for (ClientPlayerEntity clientPlayerEntity : client.players) {
                if (clientPlayerEntity == player && !player.isInThirdPerson()) {
                    continue;
                }

                playerRenderer.render(clientPlayerEntity, playerModel, source, partialTicks);
            }
        }

        particlesShaderProgram.use();
        particlesShaderProgram.setUniform("texture_sampler", 0);
        particlesShaderProgram.setUniform("projectionMatrix", camera.getProjectionMatrix());

        Vector3d globalPos = player.getPosition(tmpPosition, partialTicks);
        client.particleSystem.render(player, globalPos);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
    }

    private void renderSelectionBox(BlockHitResult selection, RenderBuffer outlineBuffer, ShaderProgram lineProgram) {
        if (selectionBox != null && selection != null) {
            ShaderProgram shader = client.getGuiRenderer().colorShader;
            outlineBuffer.render((view, transform, globalPos, _, _) -> {
                shader.use();
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glDepthMask(false);
                transform.pushMatrix();
                transform.identity();
                transform.scale(1.0f, 1.0f, 1.0f);
                transform.translate((float) (selection.hitBlock().x - globalPos.x), (float) (selection.hitBlock().y - globalPos.y), (float) (selection.hitBlock().z - globalPos.z));
                shader.setUniform("projection", view.getProjectionMatrix());
                shader.setUniform("view", view.getViewMatrix());
                shader.setUniform("model", transform);
                shader.setUniform("color", selectionColor.set(1.0f, 1.0f, 1.0f, ((float) Math.sin(System.currentTimeMillis() / 1000.0) * 0.5f + 0.5f) * 0.25f + 0.375f));

                selectionBox.render(lineProgram);
                transform.popMatrix();

                lineProgram.use();
                GL11.glDepthMask(true);
            });
        }
    }

    private void buildSelectionBox(BlockHitResult selection) {
        if (selection != null && !selection.equals(lastHitResult)) {
            lastHitResult = selection;
            BlockState selectedBlock = selection.state();
            float epsilon = 0.001f;
            if (selectedBlock.getCollision() == CollisionType.SOLID) {
                MeshBuilder builder = new MeshBuilder(VertexAttributes.POS_UV_COLOR);

                for (BoundingBox boundingBox : selectedBlock.getBoundingBoxes(getWorld(), selection.hitBlock())) {
                    drawCube(builder, (float) (boundingBox.min().x) - epsilon, (float) (boundingBox.min().y) - epsilon, (float) (boundingBox.min().z) - epsilon, (float) (boundingBox.max().x) + epsilon, (float) (boundingBox.max().y) + epsilon, (float) (boundingBox.max().z) + epsilon);
                }

                Mesh end = builder.build();
                if (selectionBox != null) {
                    selectionBox.delete();
                    selectionBox = null;
                }
                if (end != null) {
                    selectionBox = end;
                }
            }
        }
    }

    private void renderChunks(ClientPlayerEntity player, List<ClientChunk> list, boolean rebuilt, RenderBufferSource source) {
        for (ClientChunk chunk : list) {
            boolean shouldSkip = false;
            if (!chunk.isVisible(player, camera)) shouldSkip = true;
            else if (!chunk.model.isBuilt() && ChunkModel.concurrentBuilds < 16) {
                if (!rebuilt) {
                    chunk.model.rebuild();
                    rebuilt = true;
                }
                shouldSkip = true;
            }
            if (shouldSkip) continue;

            if (chunk.needRebuild()) {
                chunk.model.rebuild();
            }
            if (chunk.model.render(source)) {
                visibleChunks++;
            }
        }
    }

    private static void drawCube(MeshBuilder builder, float startX, float startY, float startZ, float endX, float endY, float endZ) {
        // Front
        builder.face(
                builder.vertex().setPosition(startX, startY, startZ).setUV(0, 0).setColor(1, 1, 1, 1),
                builder.vertex().setPosition(startX, endY, startZ).setUV(0, 1).setColor(1, 1, 1, 1),
                builder.vertex().setPosition(endX, endY, startZ).setUV(1, 1).setColor(1, 1, 1, 1),
                builder.vertex().setPosition(endX, startY, startZ).setUV(1, 0).setColor(1, 1, 1, 1)
                );

        // Back
        builder.face(
                builder.vertex().setPosition(endX, endY, endZ).setUV(1, 1).setColor(1, 1, 1, 1),
                builder.vertex().setPosition(startX, endY, endZ).setUV(0, 1).setColor(1, 1, 1, 1),
                builder.vertex().setPosition(startX, startY, endZ).setUV(0, 0).setColor(1, 1, 1, 1),
                builder.vertex().setPosition(endX, startY, endZ).setUV(1, 0).setColor(1, 1, 1, 1)
                );

        // Left
        builder.face(
                builder.vertex().setPosition(startX, startY, startZ).setUV(0, 0).setColor(1, 1, 1, 1),
                builder.vertex().setPosition(startX, startY, endZ).setUV(1, 0).setColor(1, 1, 1, 1),
                builder.vertex().setPosition(startX, endY, endZ).setUV(1, 1).setColor(1, 1, 1, 1),
                builder.vertex().setPosition(startX, endY, startZ).setUV(0, 1).setColor(1, 1, 1, 1)
        );

        // Right
        builder.face(
                builder.vertex().setPosition(endX, startY, startZ).setUV(0, 0).setColor(1, 1, 1, 1),
                builder.vertex().setPosition(endX, endY, startZ).setUV(0, 1).setColor(1, 1, 1, 1),
                builder.vertex().setPosition(endX, endY, endZ).setUV(1, 1).setColor(1, 1, 1, 1),
                builder.vertex().setPosition(endX, startY, endZ).setUV(1, 0).setColor(1, 1, 1, 1)
        );

        // Top
        builder.face(
                builder.vertex().setPosition(startX, startY, startZ).setUV(0, 0).setColor(1, 1, 1, 1),
                builder.vertex().setPosition(endX, startY, startZ).setUV(1, 0).setColor(1, 1, 1, 1),
                builder.vertex().setPosition(endX, startY, endZ).setUV(1, 1).setColor(1, 1, 1, 1),
                builder.vertex().setPosition(startX, startY, endZ).setUV(0, 1).setColor(1, 1, 1, 1)
                );

        // Bottom
        builder.face(
                builder.vertex().setPosition(startX, endY, startZ).setUV(0, 1).setColor(1, 1, 1, 1),
                builder.vertex().setPosition(startX, endY, endZ).setUV(1, 1).setColor(1, 1, 1, 1),
                builder.vertex().setPosition(endX, endY, endZ).setUV(1, 0).setColor(1, 1, 1, 1),
                builder.vertex().setPosition(endX, endY, startZ).setUV(0, 0).setColor(1, 1, 1, 1)
                );
    }

    private @NotNull List<ClientChunk> getSortedChunks(ClientPlayerEntity player, float partialTicks) {
        ClientWorld world = getWorld();
        if (world == null) {
            return List.of();
        }
        List<ClientChunk> list = new ArrayList<>(world.getAllChunks());
        list.sort((o1, o2) -> {
            Vector3d position1 = player.getPosition(tmpPosition, partialTicks);
            Vector3d position2 = new Vector3d(o1.vec.x * (double) World.CHUNK_SIZE, o1.vec.y * (double) World.CHUNK_SIZE, o1.vec.z * (double) World.CHUNK_SIZE);
            Vector3d position3 = new Vector3d(o2.vec.x * (double) World.CHUNK_SIZE, o2.vec.y * (double) World.CHUNK_SIZE, o2.vec.z * (double) World.CHUNK_SIZE);
            return Double.compare(position1.distanceSquared(position2), position1.distanceSquared(position3));
        });
        return list;
    }

    public int getVisibleChunks() {
        return visibleChunks;
    }

    public Camera getCamera() {
        return camera;
    }

    public void reloadChunks() {
        ClientWorld world = getWorld();
        for (ClientChunk chunk : world.getAllChunks()) {
            chunk.model.unload();
        }
    }

    public void close() {
        pipeline.close();

        Mesh selectionBox1 = selectionBox;
        if (selectionBox1 != null) {
            selectionBox1.delete();
            selectionBox = null;
        }

        client.particleSystem.stop();
        client.remove(this);

        mesh.delete();
    }

    public Mesh getSkyboxMesh() {
        return mesh;
    }

    public Mesh getCelestialMesh() {
        return celestialMesh;
    }

    public void resize(int scaledWidth, int scaledHeight) {
        camera.resize(scaledWidth, scaledHeight);

        client.setGraphicsMode(player, client.getGraphicsMode());
    }

    public ClientWorld getWorld() {
        return (ClientWorld) player.getWorld();
    }

    public RenderNode createNode() {
        return new RenderNode(client.getWindow().getWidth(), client.getWindow().getHeight(), true) {
            @Override
            public void doRender(ClientPlayerEntity player, GuiRenderer renderer) {
                renderInternal(QuantumClient.get().getPartialTick());
            }
        };
    }

    public RenderNode createDepthlessNode() {
        return new RenderNode(client.getWindow().getWidth(), client.getWindow().getHeight(), false) {
            @Override
            public void doRender(ClientPlayerEntity player, GuiRenderer renderer) {
                renderInternal(QuantumClient.get().getPartialTick());
            }
        };
    }

    public float getSunPosition() {
        ClientWorld world = getWorld();
        if (world == null) return 0f;
        return world.getTimeOfDay() / 24000f;
    }

    public void setSolidShader(ShaderProgram solidShader) {
        this.solidShader = solidShader;
    }

    public ShaderProgram getSolidShader() {
        return solidShader;
    }

    public void setTransparentShader(ShaderProgram transparentShader) {
        this.transparentShader = transparentShader;
    }

    public ShaderProgram getTransparentShader() {
        return transparentShader;
    }

    public void setCutoutShader(ShaderProgram cutoutShader) {
        this.cutoutShader = cutoutShader;
    }

    public ShaderProgram getCutoutShader() {
        return cutoutShader;
    }

    public void setWaterShader(ShaderProgram waterShader) {
        this.waterShader = waterShader;
    }

    public ShaderProgram getWaterShader() {
        return waterShader;
    }

    public ShaderProgram getSkyboxProgram() {
        return client.shaders.getSkyboxProgram();
    }

    public ShaderProgram getCelestialBodyProgram() {
        return client.shaders.getCelestialBodyProgram();
    }

    public void clearChunks() {
        ClientWorld world = getWorld();
        if (world == null) return;
        world.unloadAllChunks();
    }
}
