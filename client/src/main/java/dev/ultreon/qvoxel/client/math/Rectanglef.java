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

import org.joml.Vector2f;

import java.util.Objects;

public class Rectanglef {
    public float x, y, width, height;

    public Rectanglef(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(float x, float y) {
        return x >= this.x && x <= this.x + width && y >= this.y && y <= this.y + height;
    }

    public boolean contains(Vector2f point) {
        return contains(point.x, point.y);
    }

    public Vector2f getCenter(Vector2f out) {
        return out.set(x + width / 2, y + height / 2);
    }

    public Vector2f getTopLeft(Vector2f out) {
        return out.set(x, y);
    }

    public Vector2f getBottomRight(Vector2f out) {
        return out.set(x + width, y + height);
    }

    public String toString() {
        return width + "x" + height + " @ " + x + ", " + y;
    }

    @Deprecated
    public Rectanglef copy() {
        return new Rectanglef(x, y, width, height);
    }

    public void translate(float x, float y) {
        this.x += x;
        this.y += y;
    }

    public void translate(Vector2f vector) {
        translate(vector.x, vector.y);
    }

    public void scale(float scale) {
        width *= scale;
        height *= scale;
    }

    public void scale(float x, float y) {
        width *= x;
        height *= y;
    }

    public void scale(Vector2f scale) {
        scale(scale.x, scale.y);
    }

    public void set(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public static Rectanglef of(float x, float y, float width, float height) {
        return new Rectanglef(x, y, width, height);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Rectanglef that = (Rectanglef) o;
        return Float.compare(x, that.x) == 0 && Float.compare(y, that.y) == 0 && Float.compare(width, that.width) == 0 && Float.compare(height, that.height) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, width, height);
    }
}
