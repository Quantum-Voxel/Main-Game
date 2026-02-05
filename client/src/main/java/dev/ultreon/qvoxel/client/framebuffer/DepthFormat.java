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

package dev.ultreon.qvoxel.client.framebuffer;

import dev.ultreon.qvoxel.client.render.GLEnum;
import org.lwjgl.opengl.GL41;

public enum DepthFormat implements GLEnum {
    DEPTH_24(24, GL41.GL_DEPTH_COMPONENT24),
    DEPTH_32(32, GL41.GL_DEPTH_COMPONENT32),
    DEPTH_24_STENCIL_8(24, GL41.GL_DEPTH24_STENCIL8),
    DEPTH_32_STENCIL_8(32, GL41.GL_DEPTH32F_STENCIL8),
    ;

    private final int bits;
    private final int glValue;

    DepthFormat(int bits, int glValue) {
        this.bits = bits;
        this.glValue = glValue;
    }

    public int getBits() {
        return bits;
    }

    @Override
    public int getGLValue() {
        return glValue;
    }
}
