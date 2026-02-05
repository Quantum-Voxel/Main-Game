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

import dev.ultreon.qvoxel.client.model.AssimpModelLoader;
import dev.ultreon.qvoxel.client.model.RawMeshData;
import dev.ultreon.qvoxel.client.model.RawMeshInfo;
import dev.ultreon.qvoxel.client.render.GLShape;
import dev.ultreon.qvoxel.client.render.Mesh;
import dev.ultreon.qvoxel.client.render.MeshData;
import dev.ultreon.qvoxel.client.render.VertexAttribute;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.Arrays;

public class MeshBuilder implements RawMeshInfo {
    private final FloatList vertices = new FloatArrayList();
    private final IntList indices = new IntArrayList();
    private final VertexAttribute[] attributes;
    private int index = 0;
    private int attributeSize;

    public MeshBuilder(VertexAttribute... attributes) {
        this.attributes = attributes;

        for (VertexAttribute attribute : attributes) {
            attributeSize += attribute.size();
        }
    }

    public Vertex vertex() {
        // Only construct; do NOT add yet. Let addVertex(...) control insertion.
        return new Vertex(this);
    }

    public Mesh build() {
        if (indices.isEmpty() || vertices.isEmpty()) {
            discard();
            return null;
        }

        Mesh mesh = new Mesh(GLShape.Triangles, vertices.toFloatArray(), indices.toIntArray(), attributes);
        discard();
        return mesh;
    }

    public RawMeshData buildRaw() {
        if (indices.isEmpty() || vertices.isEmpty()) {
            discard();
            return null;
        }

        RawMeshData mesh = new RawMeshData(GLShape.Triangles, new FloatArrayList(vertices), new IntArrayList(indices), attributes);
        discard();
        return mesh;
    }

    public void discard() {
        this.vertices.clear();
        indices.clear();
        index = 0;
    }

    public MeshData buildData() {
        if (indices.isEmpty() || vertices.isEmpty()) {
            discard();
            return null;
        }

        MeshData mesh = new MeshData(GLShape.Triangles, vertices.toFloatArray(), indices.toIntArray(), attributes);
        discard();
        return mesh;
    }

    public int addVertex(Vertex vertex) {
        vertex.write(vertices, attributes);
        return index++;
    }

    public void addMesh(RawMeshInfo mesh) {
        if (!Arrays.equals(mesh.attributes(), attributes))
            throw new IllegalStateException("Error!");

        int stride = AssimpModelLoader.getStride(attributes);
        int v = vertices.size() / stride; // current vertex count
        vertices.addAll(mesh.vertexList()); // append vertices

        // add indices with correct offset
        int size = mesh.indexList().size();
        for (int i = 0; i < size; i++) {
            indices.add(v + mesh.indexList().getInt(i));
        }

        index += mesh.vertexList().size() / stride;
    }

    public MeshBuilder index(int index) {
        indices.add(index);
        return this;
    }

    public MeshBuilder indices(int... indices) {
        for (int i : indices) {
            this.indices.add(i);
        }
        return this;
    }

    public MeshBuilder face(Vertex v00, Vertex v01, Vertex v10, Vertex v11) {
        int i00 = addVertex(v00);
        int i01 = addVertex(v01);
        int i10 = addVertex(v10);
        int i11 = addVertex(v11);

        // Default to CW triangles (matches GL_CW front).
        indices(
                i00, i11, i01,
                i01, i11, i10
        );

        return this;
    }

    @Override
    public FloatList vertexList() {
        return vertices;
    }

    @Override
    public IntList indexList() {
        return indices;
    }

    @Override
    public VertexAttribute[] attributes() {
        return attributes;
    }

    @Override
    public GLShape shape() {
        return GLShape.Triangles;
    }
}
