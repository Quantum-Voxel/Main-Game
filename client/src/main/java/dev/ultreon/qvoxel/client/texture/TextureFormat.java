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

package dev.ultreon.qvoxel.client.texture;

import org.lwjgl.opengl.*;

public enum TextureFormat {
    RGBA8(4, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE),
    BGRA8(4, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE),
    RGB8(3, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE),
    RGBA16(4, GL11.GL_RGBA, GL11.GL_UNSIGNED_SHORT),
    BGRA16(4, GL12.GL_BGRA, GL11.GL_UNSIGNED_SHORT),
    RGB16(3, GL11.GL_RGB, GL11.GL_UNSIGNED_SHORT),
    RGBA16F(4, GL30.GL_RGBA, GL11.GL_FLOAT),
    RGBA32F(4, GL30.GL_RGBA, GL11.GL_FLOAT),
    RGB16F(3, GL30.GL_RGB, GL11.GL_FLOAT),
    RGB32F(3, GL30.GL_RGB, GL11.GL_FLOAT),
    LUMINANCE(1, GL11.GL_LUMINANCE, GL11.GL_UNSIGNED_BYTE),
    LUMINANCE_ALPHA(2, GL11.GL_LUMINANCE_ALPHA, GL11.GL_UNSIGNED_BYTE),
    RED(1, GL11.GL_RED, GL11.GL_UNSIGNED_BYTE),
    RED_INTEGER(1, GL30.GL_RED_INTEGER, GL11.GL_INT),
    GREEN(1, GL11.GL_GREEN, GL11.GL_UNSIGNED_BYTE),
    GREEN_INTEGER(1, GL30.GL_GREEN_INTEGER, GL11.GL_INT),
    BLUE(1, GL11.GL_BLUE, GL11.GL_UNSIGNED_BYTE),
    BLUE_INTEGER(1, GL30.GL_BLUE_INTEGER, GL11.GL_INT),
    ALPHA(1, GL11.GL_ALPHA, GL11.GL_UNSIGNED_BYTE),
    ALPHA_INTEGER(1, GL30.GL_ALPHA_INTEGER, GL11.GL_INT),
    ;

    private final int channels;
    private final int glType;
    private final int glTypeSize;

    TextureFormat(int channels, int glType, int glTypeSize) {
        this.channels = channels;
        this.glType = glType;
        this.glTypeSize = glTypeSize;
    }

    public int getChannels() {
        return channels;
    }

    public int getFormat() {
        return glType;
    }

    public int getType() {
        return glTypeSize;
    }

    public int getInternalFormat() {
        return switch (this) {
            case RGBA8, BGRA8 -> GL11.GL_RGBA8;
            case RGB8 -> GL11.GL_RGB8;
            case RGBA16, BGRA16 -> GL11.GL_RGBA16;
            case RGB16 -> GL11.GL_RGB16;
            case RGBA16F -> GL30.GL_RGBA16F;
            case RGBA32F -> GL30.GL_RGBA32F;
            case RGB16F -> GL30.GL_RGB16F;
            case RGB32F -> GL30.GL_RGB32F;
            case LUMINANCE -> GL11.GL_LUMINANCE;
            case LUMINANCE_ALPHA -> GL11.GL_LUMINANCE_ALPHA;
            case RED -> GL11.GL_RED;
            case RED_INTEGER -> GL30.GL_RED_INTEGER;
            case GREEN -> GL11.GL_GREEN;
            case GREEN_INTEGER -> GL30.GL_GREEN_INTEGER;
            case BLUE -> GL11.GL_BLUE;
            case BLUE_INTEGER -> GL30.GL_BLUE_INTEGER;
            case ALPHA -> GL11.GL_ALPHA;
            case ALPHA_INTEGER -> GL30.GL_ALPHA_INTEGER;
        };
    }
}
