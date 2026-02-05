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

package dev.ultreon.qvoxel.client.shader;

import dev.ultreon.qvoxel.client.render.GLEnum;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;

public enum GLShaderType implements GLEnum {
    Vertex(GL20.GL_VERTEX_SHADER),
    Fragment(GL20.GL_FRAGMENT_SHADER),
    Geometry(GL32.GL_GEOMETRY_SHADER),
    TesselationControl(GL40.GL_TESS_CONTROL_SHADER),
    TesselationEvaluation(GL40.GL_TESS_EVALUATION_SHADER),
    Compute(GL43.GL_COMPUTE_SHADER),
    ;

    private final int glValue;

    GLShaderType(int glValue) {
        this.glValue = glValue;
    }

    public int getGLValue() {
        return glValue;
    }
}
