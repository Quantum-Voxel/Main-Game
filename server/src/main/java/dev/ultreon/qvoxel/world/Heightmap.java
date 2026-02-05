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

package dev.ultreon.qvoxel.world;

public class Heightmap {
    private int[] map;
    private final int width;
    private boolean initialized;

    public Heightmap(int width) {
        map = new int[width * width];
        this.width = width;
    }

    public int[] getMap() {
        return map;
    }

    public int get(int x, int z) {
        if (x < 0 || x >= width || z < 0 || z >= width) throw new IndexOutOfBoundsException();
        return map[z * width + x];
    }

    public void set(int x, int z, int value) {
        map[z * width + x] = value;
    }

    public int getWidth() {
        return width;
    }

    public void load(int[] data) {
        if (data == null) return;
        map = data;
    }

    public int[] save() {
        return map;
    }

    public void init() {
        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
