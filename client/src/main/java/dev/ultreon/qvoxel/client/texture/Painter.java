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

import java.nio.ByteBuffer;

public class Painter {
    private final int width;
    private final int height;
    private final ByteBuffer buffer;

    public Painter(int width, int height) {
        this.width = width;
        this.height = height;
        buffer = ByteBuffer.allocateDirect(width * height * 4);
    }

    public void drawPixel(int x, int y, int colorArgb) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            buffer.putInt((x + y * width) * 4, toRgba(colorArgb));
        } else {
            throw new TextureException("Invalid pixel coordinates");
        }
    }

    public void fillColor(int colorArgb) {
        buffer.clear();
        for (int i = 0; i <= buffer.capacity() - 4; i += 4) {
            buffer.putInt(i, toRgba(colorArgb));
        }
    }

    public void fillRect(int x, int y, int width, int height, int colorArgb) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                drawPixel(x + j, y + i, colorArgb);
            }
        }
    }

    private int toRgba(int colorArgb) {
        int a = colorArgb >> 24 & 0xFF;
        int r = colorArgb >> 16 & 0xFF;
        int g = colorArgb >> 8 & 0xFF;
        int b = colorArgb & 0xFF;

        return r << 24 | g << 16 | b << 8 | a;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Texture createTexture() {
        return new Texture(width, height, TextureFormat.RGBA8, buffer);
    }

    public ByteBuffer getData() {
        return buffer;
    }
}
