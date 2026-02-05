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

package dev.ultreon.qvoxel.client.model;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.model.json.BakedModel;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.client.world.MeshBuilder;
import dev.ultreon.qvoxel.client.world.Vertex;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.List;

public class FlatItemModel implements ItemModel {
    private final Identifier resourceId;
    private final Identifier textureId;
    private BakedModel bakedModel;

    public FlatItemModel(Identifier resourceId, Identifier textureId) {
        this.resourceId = resourceId;
        this.textureId = textureId;
    }

    @Override
    public void load(QuantumClient client) {
        if (bakedModel != null) {
            bakedModel.close();
        }

        MeshBuilder builder = new MeshBuilder();
        Vertex v00a = builder.vertex().setPosition(0, 0, 0).setUV(0, 0).setNormal(0, 1, 0);
        Vertex v01a = builder.vertex().setPosition(16, 0, 0).setUV(16, 0).setNormal(0, 1, 0);
        Vertex v10a = builder.vertex().setPosition(16, 16, 0).setUV(16, 16).setNormal(0, 1, 0);
        Vertex v11a = builder.vertex().setPosition(0, 16, 0).setUV(0, 16).setNormal(0, 1, 0);
        builder.face(v00a, v01a, v11a, v10a);

        Vertex v00b = builder.vertex().setPosition(16, 16, 0).setUV(16, 16).setNormal(0, 1, 0);
        Vertex v01b = builder.vertex().setPosition(0, 16, 0).setUV(0, 16).setNormal(0, 1, 0);
        Vertex v10b = builder.vertex().setPosition(0, 0, 0).setUV(0, 0).setNormal(0, 1, 0);
        Vertex v11b = builder.vertex().setPosition(16, 0, 0).setUV(16, 0).setNormal(0, 1, 0);
        builder.face(v00b, v01b, v11b, v10b);
        bakedModel = new BakedModel(builder.build());
    }

    @Override
    public Identifier resourceId() {
        return resourceId;
    }

    @Override
    public boolean isCustom() {
        return false;
    }

    @Override
    public Collection<Identifier> getAllTextures() {
        return List.of(textureId);
    }

    @Override
    public BakedModel getModel() {
        if (bakedModel == null)
            throw new IllegalStateException("Model not loaded");
        return bakedModel;
    }

    @Override
    public Vector3f getItemScale() {
        return new Vector3f(1, 1, 1);
    }

    @Override
    public void renderItem(GuiRenderer renderer, int x, int y) {
        renderer.drawTexture(textureId, x, y, 16, 16);
    }

    @Override
    public void preload(QuantumClient client) {
        
    }

    @Override
    public void close() throws Exception {
        if (bakedModel != null)
            bakedModel.close();
    }
}
