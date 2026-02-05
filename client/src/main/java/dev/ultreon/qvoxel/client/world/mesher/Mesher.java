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

package dev.ultreon.qvoxel.client.world.mesher;

import dev.ultreon.qvoxel.block.BlockLike;
import dev.ultreon.qvoxel.block.BoundingBox;
import dev.ultreon.qvoxel.client.model.BlockModel;
import dev.ultreon.qvoxel.client.model.OpaqueFaces;
import dev.ultreon.qvoxel.client.world.RenderType;
import org.jetbrains.annotations.Nullable;

/**
 * Turns an array of voxels into OpenGL vertices
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
public interface Mesher {
    /**
     * Builds a mesh based on the given condition and MeshPartBuilder.
     *
     * @param bounds
     * @param opaqueFaces
     * @param condition   The condition to determine which blocks should be used for the mesh.
     * @param builder     The MeshPartBuilder to construct the mesh.
     * @return
     */
    boolean buildMesh(BoundingBox bounds, OpaqueFaces opaqueFaces, UseCondition condition, ChunkMeshBuilder builder);

    /**
     * Determines whether a block should be used in the mesh.
     */
    interface UseCondition {
        /**
         * @param block Block to check
         * @param model
         * @param pass
         * @return True if the block should be used in this mesh
         */
        boolean shouldUse(@Nullable BlockLike block, BlockModel model, RenderType pass);
    }
}
