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

import org.lwjgl.opengl.GL11;

public enum GLShape implements GLEnum {
    Points(GL11.GL_POINTS),
    Lines(GL11.GL_LINES),
    LineStrip(GL11.GL_LINE_STRIP),
    Triangles(GL11.GL_TRIANGLES),
    TriangleStrip(GL11.GL_TRIANGLE_STRIP),
    TriangleFan(GL11.GL_TRIANGLE_FAN),
    Quads(GL11.GL_QUADS),
    QuadsStrip(GL11.GL_QUAD_STRIP),
    Polygon(GL11.GL_POLYGON);

    private final int glValue;

    GLShape(int glValue) {
        this.glValue = glValue;
    }

    public int getGLValue() {
        return glValue;
    }
}
