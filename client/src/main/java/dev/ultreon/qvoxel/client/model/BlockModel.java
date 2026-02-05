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
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.block.BoundingBox;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.model.json.JsonModel;
import dev.ultreon.qvoxel.client.texture.TextureAtlas;
import dev.ultreon.qvoxel.client.world.MeshBuilder;
import dev.ultreon.qvoxel.client.world.RenderType;
import org.jetbrains.annotations.Nullable;

public interface BlockModel extends Model {
    BlockModel DEFAULT = JsonModel.cubeOf(CubeModel.of(
            CommonConstants.id("block/cube_all"),
            CommonConstants.id("default")
    ), CommonConstants.id("default"));

    void bakeInto(BoundingBox bounds, OpaqueFaces opaqueFaces, MeshBuilder builder, float x, float y, float z, int cull, AOArray ao, int[][] light);

    @Nullable BlockState getBlock();

    boolean hasAO();

    RenderType getRenderPass();

    Identifier getBuriedTexture();

    TextureAtlas.AtlasRegion getParticle();

    void preload(QuantumClient client);
}
