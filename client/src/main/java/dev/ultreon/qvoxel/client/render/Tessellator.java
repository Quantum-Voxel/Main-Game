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

package dev.ultreon.qvoxel.client.render;

import dev.ultreon.qvoxel.client.model.RawMeshData;
import dev.ultreon.qvoxel.client.shader.ShaderProgram;
import dev.ultreon.qvoxel.client.world.RenderBuffer;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

public class Tessellator implements AutoCloseable {
    private static Tessellator instance;
    private int cursor;
    private final float[] vertex = new float[3];
    private final float[] texCoord = new float[2];
    private final float[] normal = new float[3];
    private final float[] color = new float[4];
    private final int[] light = new int[1];
    private final byte[] state = new byte[1];
    private float[] vertices = new float[0];
    private int[] indices = new int[0];
    private final Mesh mesh = new Mesh(GLShape.Triangles, new float[5120], VertexAttributes.POSITION, VertexAttributes.UV);
    private final Matrix4f modelMatrix = new Matrix4f();
    private final Vector4f uvOffset = new Vector4f();
    private List<VertexAttribute> attributes;
    private int vertexSize;
    private int indexSize;
    private int vertexCount;
    private int verticesPerObject;
    private GLShape shape;
    private int indexCount;

    private Tessellator() {

    }

    public static Tessellator getInstance() {
        if (instance == null) {
            instance = new Tessellator();
        }
        return instance;
    }

    public Tessellator begin(GLShape shape, VertexAttribute... attributes) {
        this.shape = shape;
        this.attributes = List.of(attributes);
        vertexSize = 0;
        for (VertexAttribute attribute : attributes) {
            vertexSize += attribute.size();
        }
        // Fix: correct verticesPerObject per shape and prevent fall-through
        switch (shape) {
            case Triangles: {
                indexSize = 3;
                verticesPerObject = 3;
                break;
            }
            case Quads: {
                indexSize = 6;
                verticesPerObject = 4;
                break;
            }
            case Lines: {
                indexSize = 2;
                verticesPerObject = 2;
                break;
            }
            case Points: {
                indexSize = 1;
                verticesPerObject = 1;
                break;
            }
            default:
                throw new GraphicsException("Unsupported shape: " + shape);
        }
        cursor = 0;
        return this;
    }

    public Tessellator addVertex(float x, float y, float z) {
        // Add vertex coordinates
        vertex[0] = x;
        vertex[1] = y;
        vertex[2] = z;
        return this;
    }

    public Tessellator addVertex(Vector3f vector) {
        return addVertex(vector.x, vector.y, vector.z);
    }

    public Tessellator setUV(float u, float v) {
        // Add texture coordinates
        texCoord[0] = u;
        texCoord[1] = v;
        return this;
    }

    public Tessellator setColor(float r, float g, float b, float a) {
        // Add color
        color[0] = r;
        color[1] = g;
        color[2] = b;
        color[3] = a;
        return this;
    }

    public Tessellator setColor(Color color) {
        return setColor(color.r, color.g, color.b, color.a);
    }

    public Tessellator setNormal(float x, float y, float z) {
        // Add normal vector
        normal[0] = x;
        normal[1] = y;
        normal[2] = z;
        return this;
    }

    public Tessellator setNormal(Vector3f vector) {
        return setNormal(vector.x, vector.y, vector.z);
    }

    public Tessellator setLight(int light) {
        this.light[0] = light;
        return this;
    }

    public Tessellator setLight(float red, float green, float blue, float sky) {
        int redInt = (int) (red * 255);
        int greenInt = (int) (green * 255);
        int blueInt = (int) (blue * 255);
        int skyInt = (int) (sky * 255);
        light[0] = redInt << 24 | greenInt << 16 | blueInt << 8 | skyInt;
        return this;
    }

    public Tessellator setLight(float red, float green, float blue) {
        return setLight(red, green, blue, 1);
    }

    public Tessellator setLight(Color color) {
        return setLight(color.r, color.g, color.b, color.a);
    }

    public Tessellator setLight(int skyLight, int blockLight) {
        light[0] = (byte) (skyLight << 4 | blockLight);
        return this;
    }

    public Tessellator setState(byte state) {
        this.state[0] = state;
        return this;
    }

    public Tessellator setState(int state) {
        this.state[0] = (byte) state;
        return this;
    }

    public Tessellator endVertex() {
        // Store vertex data
        int offset = vertices.length;
        float[] newVertices = new float[offset + vertexSize];
        System.arraycopy(vertices, 0, newVertices, 0, offset);
        for (VertexAttribute attribute : attributes) {
            if (attribute == VertexAttributes.POSITION) {
                System.arraycopy(vertex, 0, newVertices, offset, 3);
                offset += 3;
            }
            if (attribute == VertexAttributes.UV) {
                System.arraycopy(texCoord, 0, newVertices, offset, 2);
                offset += 2;
            }
            if (attribute == VertexAttributes.COLOR) {
                System.arraycopy(color, 0, newVertices, offset, 4);
                offset += 4;
            }
            if (attribute == VertexAttributes.NORMAL) {
                System.arraycopy(normal, 0, newVertices, offset, 3);
                offset += 3;
            }
            if (attribute == VertexAttributes.LIGHT) {
                newVertices[offset] = Float.intBitsToFloat(light[0] & 0xFF);
                offset += 1;
            }
            if (attribute == VertexAttributes.STATE)
                newVertices[offset + 1] = Float.intBitsToFloat(state[0] & 0xFF);
        }

        vertices = newVertices;
        vertexCount++;

        // Fix: emit indices whenever a primitive worth of vertices has been submitted
        if (vertexCount % verticesPerObject == 0) {
            switch (shape) {
                case Triangles -> {
                    int[] indices = new int[this.indices.length + 3];
                    System.arraycopy(this.indices, 0, indices, 0, this.indices.length);
                    int base = vertexCount - 3;
                    indices[indexCount] = base;
                    indices[indexCount + 1] = base + 1;
                    indices[indexCount + 2] = base + 2;
                    this.indices = indices;
                    indexCount += 3;
                }
                case Quads -> {
                    int[] indices = new int[this.indices.length + 6];
                    System.arraycopy(this.indices, 0, indices, 0, this.indices.length);
                    int base = vertexCount - 4;
                    indices[indexCount] = base;
                    indices[indexCount + 1] = base + 1;
                    indices[indexCount + 2] = base + 2;
                    indices[indexCount + 3] = base + 2;
                    indices[indexCount + 4] = base + 3;
                    indices[indexCount + 5] = base;
                    this.indices = indices;
                    indexCount += 6;
                }
                case Lines -> {
                }
                case Points -> {
                    int[] indices = new int[this.indices.length + 1];
                    System.arraycopy(this.indices, 0, indices, 0, this.indices.length);
                    int base = vertexCount - 1;
                    indices[indexCount] = base;
                    this.indices = indices;
                    indexCount += 1;
                }
                default -> throw new GraphicsException("Unsupported shape: " + shape);
            }
        }

        // Fix: advance cursor by actual vertexSize instead of hardcoded 5
        cursor += vertexSize;
        return this;
    }

    public void draw(GuiRenderer renderer, Color color) {
        modelMatrix.identity();
        modelMatrix.translate(0, 0, 0);
        modelMatrix.scale(1, 1, 1);

        uvOffset.set(0, 0, 1, 1);

        renderer.colorTextureShader.use();
        for (VertexAttribute attribute : attributes) {
            renderer.colorTextureShader.enableAttribute(attribute.name());
        }
        renderer.colorTextureShader.setUniform("color", color);
        renderer.colorTextureShader.setUniform("colorTexture", 0);
        renderer.colorTextureShader.setUniform("projection", renderer.getProjectionMatrix());
        renderer.colorTextureShader.setUniform("view", renderer.getViewMatrix());
        renderer.colorTextureShader.setUniform("model", modelMatrix);
        renderer.colorTextureShader.setUniform("uvOffset", uvOffset);

        mesh.setVertices(vertices, vertices.length);
        mesh.setIndices(indices, indices.length);
        mesh.render(renderer.colorTextureShader);

        for (VertexAttribute attribute : attributes) {
            renderer.colorTextureShader.disableAttribute(attribute.name());
        }

        vertices = new float[0];
        indices = new int[0];

        vertexCount = 0;
        indexCount = 0;
        cursor = 0;
    }

    @Override
    public void close() {
        mesh.delete();
    }

    @Deprecated
    public void draw(RenderBuffer source) {
        final float[] vertices = this.vertices;
        final int[] indices = this.indices;

        source.render((camera, matrixStack, origin, buffer, partialTicks) -> {
            ShaderProgram shader = source.getRenderType().shaderProgram();
            if (shader == null) return;

            modelMatrix.identity();
            modelMatrix.translate(0, 0, 0);
            modelMatrix.scale(1, 1, 1);

            for (VertexAttribute attribute : attributes) {
                shader.enableAttribute(attribute.name());
            }

            shader.setUniform("projection", camera.getProjectionMatrix());
            shader.setUniform("view", camera.getViewMatrix());
            shader.setUniform("model", modelMatrix);
            mesh.setVertices(vertices, vertices.length);
            mesh.setIndices(indices, indices.length);
            mesh.render(shader);

            for (VertexAttribute attribute : attributes) {
                shader.disableAttribute(attribute.name());
            }
        });


        this.vertices = new float[0];
        this.indices = new int[0];

        vertexCount = 0;
        indexCount = 0;
        cursor = 0;
    }

    public Mesh end() {
        if (vertexCount == 0) return null;
        if (indexCount == 0) indices = null;

        Mesh createdMesh = indices == null ?
                new Mesh(shape, vertices, attributes.toArray(VertexAttribute[]::new)) :
                new Mesh(shape, vertices, indices, attributes.toArray(VertexAttribute[]::new));
        vertices = new float[0];
        indices = new int[0];

        vertexCount = 0;
        indexCount = 0;
        cursor = 0;

        return createdMesh;
    }

    public MeshData endData() {
        if (vertexCount == 0) return null;
        if (indexCount == 0) {
            throw new IllegalArgumentException("No indices!");
        }

        MeshData createdMesh = new MeshData(shape, vertices, indices, attributes.toArray(VertexAttribute[]::new));
        vertices = new float[0];
        indices = new int[0];

        vertexCount = 0;
        indexCount = 0;
        cursor = 0;

        return createdMesh;
    }

    public RawMeshData endRawData() {
        if (vertexCount == 0) return null;
        if (indexCount == 0) {
            throw new IllegalArgumentException("No indices!");
        }

        RawMeshData createdMesh = new RawMeshData(shape, new FloatArrayList(vertices), new IntArrayList(indices), attributes.toArray(VertexAttribute[]::new));
        vertices = new float[0];
        indices = new int[0];

        vertexCount = 0;
        indexCount = 0;
        cursor = 0;

        return createdMesh;
    }
}