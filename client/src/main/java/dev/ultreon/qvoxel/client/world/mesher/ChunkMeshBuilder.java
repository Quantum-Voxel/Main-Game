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

import dev.ultreon.qvoxel.client.render.Mesh;
import dev.ultreon.qvoxel.client.render.MeshData;
import dev.ultreon.qvoxel.client.world.ChunkMesh;
import dev.ultreon.qvoxel.client.world.ClientChunk;
import dev.ultreon.qvoxel.client.world.MeshBuilder;
import dev.ultreon.qvoxel.client.world.RenderType;

import java.util.HashMap;
import java.util.Map;

public class ChunkMeshBuilder {
    private final Map<RenderType, MeshBuilder> builders = new HashMap<>();
    private boolean started = false;
    private final ClientChunk chunk;

    public ChunkMeshBuilder(ClientChunk chunk) {
        this.chunk = chunk;
    }

    public void begin() {
        started = true;
    }

    public void end(Map<RenderType, ChunkMesh> meshes) {
        if (!started) throw new IllegalStateException();

        started = false;
        if (meshes == null) {
            builders.clear();
            return;
        }

        for (Map.Entry<RenderType, MeshBuilder> entry : builders.entrySet()) {
            if (entry == null)
                continue;
            RenderType pass = entry.getKey();
            MeshBuilder builder = entry.getValue();
            MeshData meshData = builder.buildData();
            if (meshData == null) {
                continue;
            }
            Mesh part = new Mesh(meshData.optimize());

            if (meshes.containsKey(pass))
                throw new IllegalStateException("Duplicate render pass " + pass.getName());
            meshes.put(entry.getKey(), new ChunkMesh(pass, part, chunk));
        }

        builders.clear();
    }

    public MeshBuilder get(RenderType pass) {
        if (!started) throw new IllegalStateException();
        MeshBuilder builder = builders.get(pass);
        if (builder == null) {
            builder = new MeshBuilder(pass.attributes());
            builders.put(pass, builder);
        }
        return builder;
    }
}
