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

package dev.ultreon.qvoxel.client.math;

import org.joml.Vector2i;

import java.util.Objects;

public class Rectanglei {
    public int x, y, width, height;

    public Rectanglei(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rectanglei(Rectanglei rectangle) {
        this(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    public Rectanglei(Rectanglef rectangle) {
        this((int) rectangle.x, (int) rectangle.y, (int) rectangle.width, (int) rectangle.height);
    }

    public boolean contains(int x, int y) {
        return x >= this.x && x <= this.x + width && y >= this.y && y <= this.y + height;
    }

    public boolean contains(Vector2i point) {
        return contains(point.x, point.y);
    }

    public Vector2i getCenter(Vector2i out) {
        return out.set(x + width / 2, y + height / 2);
    }

    public Vector2i getTopLeft(Vector2i out) {
        return out.set(x, y);
    }

    public Vector2i getBottomRight(Vector2i out) {
        return out.set(x + width, y + height);
    }

    public String toString() {
        return width + "x" + height + " @ " + x + ", " + y;
    }

    @Deprecated
    public Rectanglei copy() {
        return new Rectanglei(x, y, width, height);
    }

    public void translate(int x, int y) {
        this.x += x;
        this.y += y;
    }

    public void translate(Vector2i vector) {
        translate(vector.x, vector.y);
    }

    public void scale(int scale) {
        width *= scale;
        height *= scale;
    }

    public void scale(int x, int y) {
        width *= x;
        height *= y;
    }

    public void scale(Vector2i scale) {
        scale(scale.x, scale.y);
    }

    public void set(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public static Rectanglei of(int x, int y, int width, int height) {
        return new Rectanglei(x, y, width, height);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Rectanglei that = (Rectanglei) o;
        return x == that.x && y == that.y && width == that.width && height == that.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height);
    }
}
