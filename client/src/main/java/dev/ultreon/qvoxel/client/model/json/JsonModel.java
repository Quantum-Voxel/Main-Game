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

import dev.ultreon.libs.collections.v0.tables.HashTable;
import dev.ultreon.libs.collections.v0.tables.Table;
import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.block.BoundingBox;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.block.state.property.BlockDataEntry;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.model.*;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.client.texture.TextureAtlas;
import dev.ultreon.qvoxel.client.world.MeshBuilder;
import dev.ultreon.qvoxel.client.world.RenderType;
import dev.ultreon.qvoxel.util.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class JsonModel implements BlockModel, ItemModel {
    public final Map<String, Identifier> textureElements;
    public final List<ModelElement> modelElements;
    public final boolean ambientOcclusion;
    public final Display display;
    private final Identifier id;
    private BakedModel model;
    private final Table<String, BlockDataEntry<?>, JsonModel> overrides;
    public static final Vector3f SCALE = new Vector3f(-16, -16, 16).mul(0.6f);
    private BlockState block;
    private final Vector3f rotation = new Vector3f(15, 0, 0);
    private ByteBuffer data;

    public JsonModel(Identifier id, Map<String, Identifier> textureElements, List<ModelElement> modelElements, boolean ambientOcclusion, Display display, Table<String, BlockDataEntry<?>, JsonModel> overrides) {
        this.textureElements = textureElements;
        this.modelElements = modelElements;
        this.ambientOcclusion = ambientOcclusion;
        this.display = display;
        this.overrides = overrides;
        this.id = id;
    }

    public static JsonModel cubeOf(CubeModel model, @Nullable Identifier buriedTexture) {
        Map<String, Identifier> elements = new java.util.HashMap<>();
        elements.put("top", model.top());
        elements.put("bottom", model.bottom());
        elements.put("left", model.left());
        elements.put("right", model.right());
        elements.put("front", model.front());
        elements.put("back", model.back());
        if (buriedTexture != null) elements.put("buried", buriedTexture);
        return new JsonModel(
                model.resourceId(),
                elements,
                List.of(
                        new ModelElement(
                                Map.of(
                                        Direction.UP, new FaceElement("#top", new UVs(0, 0, 16, 16, 16, 16), 0, 0, "up"),
                                        Direction.DOWN, new FaceElement("#bottom", new UVs(0, 0, 16, 16, 16, 16), 0, 0, "down"),
                                        Direction.NORTH, new FaceElement("#front", new UVs(0, 0, 16, 16, 16, 16), 0, 0, "north"),
                                        Direction.SOUTH, new FaceElement("#back", new UVs(0, 0, 16, 16, 16, 16), 0, 0, "south"),
                                        Direction.EAST, new FaceElement("#left", new UVs(0, 0, 16, 16, 16, 16), 0, 0, "east"),
                                        Direction.WEST, new FaceElement("#right", new UVs(0, 0, 16, 16, 16, 16), 0, 0, "west")
                                ),
                                true,
                                ElementRotation.ZERO,
                                new Vector3f(0, 0, 0),
                                new Vector3f(16, 16, 16)
                        )
                ),
                true,
                new Display(model.pass() == null ? "opaque" : model.pass()),
                new HashTable<>()
        );
    }

    @Override
    public void bakeInto(BoundingBox bounds, OpaqueFaces opaqueFaces, MeshBuilder builder, float x, float y, float z, int cull, AOArray ao, int[][] light) {
        for (ModelElement modelElement : modelElements) {
            modelElement.bakeInto(bounds, opaqueFaces, builder, textureElements, x, y, z, cull, ao, light, QuantumClient.get().blockTextureAtlas);
        }
    }

    @Override
    public void load(QuantumClient client) {
        if (model != null) {
            model.close();
        }
        for (int i = 0, modelElementsSize = modelElements.size(); i < modelElementsSize; i++) {
            ModelElement modelElement = modelElements.get(i);
            model = new BakedModel(modelElement.bake(i, textureElements));
        }
    }

    @Override
    public Identifier resourceId() {
        return id;
    }

    @Override
    public boolean isCustom() {
        return true;
    }

    @Override
    public @Nullable BlockState getBlock() {
        return block;
    }

    @Override
    public BakedModel getModel() {
        if (model == null) throw new IllegalStateException("Model not loaded: " + resourceId());
        return model;
    }

    @Override
    public void close() {
        if (model != null) model.close();
        model = null;
    }

    @Override
    public Vector3f getItemScale() {
        return SCALE;
    }

    @Override
    public boolean hasAO() {
        return ambientOcclusion;
    }

    @Override
    public RenderType getRenderPass() {
        return RenderType.byName(display.renderPass);
    }

    @Override
    public Collection<Identifier> getAllTextures() {
        return textureElements.values();
    }

    @Override
    public void renderItem(GuiRenderer renderer, int x, int y) {
        if (model == null) {
            throw new IllegalStateException("Model not loaded: " + resourceId());
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Matrix4f modelMatrix = renderer.getModelMatrix();
        modelMatrix.identity();
        modelMatrix.translate(x + 15, y - 12, 0);
        modelMatrix.scale(SCALE.x, SCALE.y, SCALE.z);
        modelMatrix.rotateX(Math.toRadians(-25));
        modelMatrix.rotateY(Math.toRadians(45));

        renderer.renderModel(model);
    }

    public Table<String, BlockDataEntry<?>, JsonModel> getOverrides() {
        return overrides;
    }

    public void setBlock(BlockState block) {
        this.block = block;
    }

    @Override
    public Identifier getBuriedTexture() {
        Identifier namespaceID = textureElements.get("buried");
        if (block != null && namespaceID != null) {
            return namespaceID;
        }
        return null;
    }

    @Override
    public TextureAtlas.AtlasRegion getParticle() {
        Identifier particle = textureElements.get("particle");
        if (particle != null) {
            return QuantumClient.get().blockTextureAtlas.getRegion(particle.mapPath(s -> "textures/" + s + ".png"));
        }
        return null;
    }

    @Override
    public void preload(QuantumClient client) {

    }
}
