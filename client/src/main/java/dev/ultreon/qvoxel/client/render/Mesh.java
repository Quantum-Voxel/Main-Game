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

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.debug.ClientDebugging;
import dev.ultreon.qvoxel.client.debug.DebugRenderer;
import dev.ultreon.qvoxel.client.debug.FrameBufferRenderer;
import dev.ultreon.qvoxel.client.shader.ShaderProgram;
import dev.ultreon.qvoxel.resource.GameComponent;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.function.Consumer;

@DebugRenderer(Mesh.DebugRenderer.class)
public class Mesh implements GameComponent {
    private final int vao;
    private final int vbo;
    private int ebo = -1;
    private int vertexCount;
    private int indexCount;
    private final VertexAttribute[] attributes;
    private final GLShape shape;
    private final boolean isDynamic;

    public Mesh(GLShape shape, int vao, int vbo, int ebo, int vertexCount, int indexCount, boolean isDynamic, VertexAttribute... attributes) {
        this.shape = shape;
        this.vao = vao;
        this.vbo = vbo;
        this.ebo = ebo;
        this.vertexCount = vertexCount;
        this.indexCount = indexCount;
        this.attributes = attributes;
        this.isDynamic = isDynamic;
    }

    public Mesh(GLShape shape, float[] vertices, VertexAttribute... attributes) {
        this(shape, vertices, false, attributes);
    }

    public Mesh(GLShape shape, float[] vertices, boolean isDynamic, VertexAttribute... attributes) {
        this.shape = shape;
        this.isDynamic = isDynamic;
        this.attributes = attributes;
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();

        GL30.glBindVertexArray(vao);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        int componentsPerVertex = 0;
        for (VertexAttribute attribute : attributes) {
            componentsPerVertex += attribute.size();
        }

        vertexCount = vertices.length / componentsPerVertex;
        indexCount = vertexCount;

        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new MeshException("Failed to create mesh");
    }

    public Mesh(GLShape shape, float[] vertices, int[] indices, VertexAttribute... attributes) {
        this(shape, vertices, indices, false, attributes);
    }

    public Mesh(MeshData data) {
        this(data, false);
    }

    public Mesh(MeshData data, boolean isDynamic) {
        this(data.shape(), data.vertexBuffer(), data.indexBuffer(), isDynamic, data.attributes());
    }

    public Mesh(GLShape shape, float[] vertices, int[] indices, boolean isDynamic, VertexAttribute... attributes) {
        this.shape = shape;
        if (attributes.length == 0)
            throw new IllegalArgumentException("Mesh must have at least one attribute");
        if (vertices.length == 0)
            throw new IllegalArgumentException("Mesh must have at least one vertex");
        if (indices != null && indices.length == 0)
            throw new IllegalArgumentException("Mesh must have at least one index or null");

        this.isDynamic = isDynamic;
        this.attributes = attributes;
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        ebo = GL15.glGenBuffers();

        GL30.glBindVertexArray(vao);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);

        if (indices != null) {
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);
        }

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        int error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR)
            throw new MeshException("Failed to create mesh: " + GLUtils.getErrorName(error));

        error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR)
            throw new MeshException("Failed to setup mesh vertex attributes: " + GLUtils.getErrorName(error));

        int componentsPerVertex = 0;
        for (VertexAttribute attribute : attributes) {
            componentsPerVertex += attribute.size();
        }

        vertexCount = vertices.length / componentsPerVertex;
        indexCount = indices != null ? indices.length : 0;
    }

    public Mesh(GLShape shape, FloatBuffer vertices, IntBuffer indices, boolean isDynamic, VertexAttribute... attributes) {
        vertices.clear();
        indices.clear();
        this.shape = shape;
        if (attributes.length == 0)
            throw new IllegalArgumentException("Mesh must have at least one attribute");
        if (vertices.limit() == 0)
            throw new IllegalArgumentException("Mesh must have at least one vertex");
        if (indices != null && indices.limit() == 0)
            throw new IllegalArgumentException("Mesh must have at least one index or null");

        this.isDynamic = isDynamic;
        this.attributes = attributes;
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        ebo = GL15.glGenBuffers();

        GL30.glBindVertexArray(vao);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);

        if (indices != null) {
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);
        }

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        int error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR)
            throw new MeshException("Failed to create mesh: " + GLUtils.getErrorName(error));

        error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR)
            throw new MeshException("Failed to setup mesh vertex attributes: " + GLUtils.getErrorName(error));

        int componentsPerVertex = 0;
        for (VertexAttribute attribute : attributes) {
            componentsPerVertex += attribute.size();
        }

        vertexCount = vertices.limit() / componentsPerVertex;
        indexCount = indices != null ? indices.limit() : 0;
    }

    public static Mesh cube() {
        return new Mesh(GLShape.Triangles,
                new float[]{
                        // Position (XYZ) | Texture coordinates (UV) | Normal vector (XYZ)

                        // Front
                        /**/ 0, 1, 1, /**/ 0, 1, /**/ 0, 0, 1, /**/
                        /**/ 1, 1, 1, /**/ 1, 1, /**/ 0, 0, 1, /**/
                        /**/ 1, 0, 1, /**/ 1, 0, /**/ 0, 0, 1, /**/
                        /**/ 0, 0, 1, /**/ 0, 0, /**/ 0, 0, 1, /**/

                        // Back
                        /**/ 0, 0, 0, /**/ 0, 0, /**/ 0, 0, -1, /**/
                        /**/ 1, 0, 0, /**/ 1, 0, /**/ 0, 0, -1, /**/
                        /**/ 1, 1, 0, /**/ 1, 1, /**/ 0, 0, -1, /**/
                        /**/ 0, 1, 0, /**/ 0, 1, /**/ 0, 0, -1, /**/

                        // East
                        /**/ 1, 0, 0, /**/ 0, 0, /**/ 1, 0, 0, /**/
                        /**/ 1, 0, 1, /**/ 1, 0, /**/ 1, 0, 0, /**/
                        /**/ 1, 1, 1, /**/ 1, 1, /**/ 1, 0, 0, /**/
                        /**/ 1, 1, 0, /**/ 0, 1, /**/ 1, 0, 0, /**/

                        // West
                        /**/ 0, 0, 0, /**/ 0, 0, /**/ -1, 0, 0, /**/
                        /**/ 0, 1, 0, /**/ 0, 1, /**/ -1, 0, 0, /**/
                        /**/ 0, 1, 1, /**/ 1, 1, /**/ -1, 0, 0, /**/
                        /**/ 0, 0, 1, /**/ 1, 0, /**/ -1, 0, 0, /**/

                        // Top
                        /**/ 0, 1, 0, /**/ 0, 0, /**/ 0, 1, 0, /**/
                        /**/ 1, 1, 0, /**/ 1, 0, /**/ 0, 1, 0, /**/
                        /**/ 1, 1, 1, /**/ 1, 1, /**/ 0, 1, 0, /**/
                        /**/ 0, 1, 1, /**/ 0, 1, /**/ 0, 1, 0, /**/

                        // Bottom
                        /**/ 0, 0, 1, /**/ 0, 1, /**/ 0, -1, 0, /**/
                        /**/ 1, 0, 1, /**/ 1, 1, /**/ 0, -1, 0, /**/
                        /**/ 1, 0, 0, /**/ 1, 0, /**/ 0, -1, 0, /**/
                        /**/ 0, 0, 0, /**/ 0, 0, /**/ 0, -1, 0, /**/
                },
                new int[]{
                        // Indices
                        0, 1, 2, 2, 3, 0,
                        4, 5, 6, 6, 7, 4,
                        8, 9, 10, 10, 11, 8,
                        12, 13, 14, 14, 15, 12,
                        16, 17, 18, 18, 19, 16,
                        20, 21, 22, 22, 23, 20,
                },
                VertexAttributes.POSITION, VertexAttributes.UV, VertexAttributes.NORMAL
        );
    }

    public static Mesh face() {
        return new Mesh(GLShape.Triangles,
                new float[]{
                        // Position (XYZ) | Texture coordinates (UV) | Normal vector (XYZ)

                        /**/ 0, 1, 1, /**/ 0, 1, /**/ 0, 0, 1, /**/
                        /**/ 1, 1, 1, /**/ 1, 1, /**/ 0, 0, 1, /**/
                        /**/ 1, 0, 1, /**/ 1, 0, /**/ 0, 0, 1, /**/
                        /**/ 0, 0, 1, /**/ 0, 0, /**/ 0, 0, 1, /**/
                },
                new int[]{
                        // Indices
                        0, 1, 2, 2, 3, 0
                },
                VertexAttributes.POSITION, VertexAttributes.UV, VertexAttributes.NORMAL
        );
    }

    public void setData(float[] vertices, int[] indices) {
        GL30.glBindVertexArray(vao);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, vertices);

        if (ebo != -1 && indices != null) {
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);
            GL15.glBufferSubData(GL15.GL_ELEMENT_ARRAY_BUFFER, 0, indices);
        }

        GL30.glBindVertexArray(0);

        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new MeshException("Failed to set mesh data");

        int componentsPerVertex = 0;
        for (VertexAttribute attribute : attributes) {
            componentsPerVertex += attribute.size();
        }
        vertexCount = vertices.length / componentsPerVertex;
        indexCount = indices != null ? indices.length : vertexCount;
    }

    public void render(ShaderProgram shader) {
        GL30.glBindVertexArray(vao);
        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new MeshException("Failed to bind vertex array");

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        if (ebo != -1) {
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);
        }

        // Compute stride (bytes per vertex) and set up attributes with correct offsets
        int strideBytes = 0;
        for (VertexAttribute attribute : attributes) {
            strideBytes += attribute.size() * attribute.type().getBytes();
        }
        int offsetBytes = 0;

        for (VertexAttribute attribute : attributes) {
            int location = GL20.glGetAttribLocation(shader.getProgramID(), attribute.name());
            if (location == -1) {
                GL11.glGetError(); // ignore missing attribute
                offsetBytes += attribute.size() * attribute.type().getBytes();
                continue;
            }
            GL20.glVertexAttribPointer(
                    location,
                    attribute.size(),
                    attribute.type().getGLValue(),
                    attribute.normalized(),
                    strideBytes,
                    offsetBytes
            );
            GL20.glEnableVertexAttribArray(location);
            offsetBytes += attribute.size() * attribute.type().getBytes();
            if (GL11.glGetError() != GL11.GL_NO_ERROR)
                throw new MeshException("Failed to enable attribute " + attribute.name());
        }

        if (ebo != -1 && indexCount > 0) {
            GL11.glDrawElements(shape.getGLValue(), indexCount, GL11.GL_UNSIGNED_INT, 0);
        } else {
            GL11.glDrawArrays(shape.getGLValue(), 0, vertexCount);
        }

        int error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR)
            throw new MeshException("Failed to render mesh: " + GLUtils.getErrorName(error));

        for (VertexAttribute attribute : attributes) {
            int location = GL20.glGetAttribLocation(shader.getProgramID(), attribute.name());
            if (location == -1) {
                GL11.glGetError(); // ignore missing attribute
                continue;
            }
            GL20.glDisableVertexAttribArray(location);
            if (GL11.glGetError() != GL11.GL_NO_ERROR)
                throw new MeshException("Failed to disable attribute " + attribute.name());
        }

        GL30.glBindVertexArray(0);

        if (GL11.glGetError() != GL11.GL_NO_ERROR)
            throw new MeshException("Failed to render mesh");

    }

    public void delete() {
        if (GL30.glIsVertexArray(vao)) GL30.glDeleteVertexArrays(vao);
        if (GL15.glIsBuffer(vbo)) GL15.glDeleteBuffers(vbo);
        if (GL15.glIsBuffer(ebo)) GL15.glDeleteBuffers(ebo);
    }

    public void setVertices(float[] vertices, int vertexCount) {
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, isDynamic ? GL15.GL_DYNAMIC_DRAW : GL15.GL_STATIC_DRAW);
        GL30.glBindVertexArray(0);
        int error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR)
            throw new MeshException("Failed to set mesh vertices: " + GLUtils.getErrorName(error));

        this.vertexCount = vertexCount;
    }

    public void setIndices(int[] indices, int indexCount) {
        GL30.glBindVertexArray(vao);

        // Create EBO lazily if this mesh was constructed without indices
        if (ebo == -1) {
            ebo = GL15.glGenBuffers();
            if (ebo == 0) {
                GL30.glBindVertexArray(0);
                throw new MeshException("Failed to create element buffer object (EBO)");
            }
        }

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, isDynamic ? GL15.GL_DYNAMIC_DRAW : GL15.GL_STATIC_DRAW);
        GL30.glBindVertexArray(0);

        int error = GL11.glGetError();
        if (error != GL11.GL_NO_ERROR)
            throw new MeshException("Failed to set mesh indices: " + GLUtils.getErrorName(error));

        this.indexCount = indexCount;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Mesh mesh = (Mesh) o;
        return vao == mesh.vao && vbo == mesh.vbo && ebo == mesh.ebo;
    }

    @Override
    public int hashCode() {
        int result = vao;
        result = 31 * result + vbo;
        result = 31 * result + ebo;
        return result;
    }

    public int getVaoId() {
        return vao;
    }

    public int getVboId() {
        return vbo;
    }

    public int getEboId() {
        return ebo;
    }

    public int getIndexCount() {
        return indexCount;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void transform(Matrix4f transform) {
        if (transform == null)
            throw new IllegalArgumentException("Transform matrix cannot be null");

        transform.identity();
        transform.translate(0.5f, 0.5f, 0.5f);
        transform.scale(1, 1, 1);
    }

    public GLShape getShape() {
        return shape;
    }

    public static class DebugRenderer extends FrameBufferRenderer<Mesh> {
        @Override
        public void offRender(Mesh object, Matrix4f projection, Matrix4f view, Matrix4f model) {
            ShaderProgram meshShader = ClientDebugging.meshShader;
            if (meshShader == null) return;

            meshShader.enableAttribute("Position");
            meshShader.use();
            meshShader.setUniform("projection", projection);
            meshShader.setUniform("view", view);
            meshShader.setUniform("model", model);

            try {
                object.render(meshShader);
            } catch (Exception e) {
                CommonConstants.LOGGER.error("Failed to render mesh for debug renderer", e);
            }
        }

        @Override
        public void render(Mesh mesh, @Nullable Consumer<Mesh> setter) {
            super.render(mesh, setter);
        }

        @Override
        protected Vector3f getOrigin(Vector3f vector3f) {
            return vector3f.set(0, 0, 0);
        }

        @Override
        protected float getSize() {
            return 1;
        }
    }
}
