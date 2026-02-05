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

import dev.ultreon.qvoxel.client.render.Color;
import dev.ultreon.qvoxel.client.render.VertexAttribute;
import dev.ultreon.qvoxel.client.render.VertexAttributes;
import it.unimi.dsi.fastutil.floats.FloatList;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Vertex {
    private final MeshBuilder builder;

    public Vector3f position = new Vector3f();
    public Vector2f uv = new Vector2f();
    public Vector2f localUV = new Vector2f();
    public Vector3f normal = new Vector3f();
    public Vector4f ao = new Vector4f();
    public int light = 0x000000FF; // Red, Green, Blue, Sky
    public Color color = new Color(1, 1, 1, 1);

    public Vertex(MeshBuilder builder) {
        this.builder = builder;
    }

    public Vertex set(Vertex other) {
        position.set(other.position);
        uv.set(other.uv);
        normal.set(other.normal);
        color.set(other.color);

        return this;
    }

    @Deprecated
    public Vertex copy() {
        return new Vertex(builder).set(this);
    }

    public Vertex setPosition(float x, float y, float z) {
        position.set(x, y, z);
        return this;
    }

    public Vertex setUV(float u, float v) {
        uv.set(u, v);
        return this;
    }

    public Vertex setLocalUV(float u, float v) {
        localUV.set(u, v);
        return this;
    }

    public Vertex setNormal(float nx, float ny, float nz) {
        normal.set(nx, ny, nz);
        return this;
    }

    public Vertex setColor(float r, float g, float b, float a) {
        color.set(r, g, b, a);
        return this;
    }

    public Vertex setColor(Color color) {
        return setColor(color.r, color.g, color.b, color.a);
    }

    public Vertex setColor(int color) {
        return setColor(new Color(color));
    }

    public Vertex setAO(float ao00, float ao10, float ao01, float ao11) {
        ao.set(ao00, ao10, ao01, ao11);
        return this;
    }

    public Vertex setAO(Vector4f ao) {
        return setAO(ao.x, ao.y, ao.z, ao.w);
    }

    public Vertex setLight(int light) {
        this.light = light;
        return this;
    }

    public Vertex setLight(float red, float green, float blue, float sky) {
        int redInt = (int) (red * 255);
        int greenInt = (int) (green * 255);
        int blueInt = (int) (blue * 255);
        int skyInt = (int) (sky * 255);
        light = redInt << 24 | greenInt << 16 | blueInt << 8 | skyInt;
        return this;
    }

    public Vertex setLight(Color color) {
        return setLight(color.r, color.g, color.b, color.a);
    }

    public Vertex setPosition(Vector3f position) {
        return setPosition(position.x, position.y, position.z);
    }

    public Vertex setUV(Vector2f uv) {
        return setUV(uv.x, uv.y);
    }

    public Vertex setNormal(Vector3f normal) {
        return setNormal(normal.x, normal.y, normal.z);
    }

    public Vertex translate(Vector3f translation) {
        position.add(translation);
        return this;
    }

    public void write(FloatList vertices, VertexAttribute... attributes) {
        for (VertexAttribute attribute : attributes) {
            if (attribute == VertexAttributes.POSITION) {
                vertices.add(position.x);
                vertices.add(position.y);
                vertices.add(position.z);
            } else if (attribute == VertexAttributes.UV) {
                vertices.add(uv.x);
                vertices.add(uv.y);
            } else if (attribute == VertexAttributes.LOCAL_UV) {
                vertices.add(localUV.x);
                vertices.add(localUV.y);
            } else if (attribute == VertexAttributes.NORMAL) {
                vertices.add(normal.x);
                vertices.add(normal.y);
                vertices.add(normal.z);
            } else if (attribute == VertexAttributes.COLOR) {
                vertices.add(color.r);
                vertices.add(color.g);
                vertices.add(color.b);
                vertices.add(color.a);
            } else if (attribute == VertexAttributes.AO) {
                vertices.add(ao.x);
                vertices.add(ao.y);
                vertices.add(ao.z);
                vertices.add(ao.w);
            } else if (attribute == VertexAttributes.LIGHT) {
                vertices.add(Float.intBitsToFloat(light));
            } else {
                throw new IllegalArgumentException("Unknown vertex attribute: " + attribute);
            }
        }
    }

    public Vector3f getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "Vertex{" +
                "relativePos=" + position +
                ", uv=" + uv +
                ", normal=" + normal +
                ", color=" + color +
                '}';
    }
}
