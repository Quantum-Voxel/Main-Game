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

package dev.ultreon.qvoxel.client.model;

import dev.ultreon.libs.commons.v0.Identifier;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;

import java.util.Objects;
import java.util.Set;

public final class CubeModel {
    private final Identifier top;
    private final Identifier bottom;
    private final Identifier left;
    private final Identifier right;
    private final Identifier front;
    private final Identifier back;
    private final ModelProperties properties;
    private Identifier resourceId;

    private CubeModel(Identifier resourceId, Identifier top, Identifier bottom,
                      Identifier left, Identifier right,
                      Identifier front, Identifier back, ModelProperties properties) {
        this.resourceId = resourceId;
        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        this.front = front;
        this.back = back;
        this.properties = properties;
    }

    public static CubeModel of(Identifier resourceId, Identifier all) {
        return CubeModel.of(resourceId, all, all, all);
    }

    public static CubeModel of(Identifier resourceId, Identifier top, Identifier bottom, Identifier side) {
        return CubeModel.of(resourceId, top, bottom, side, side, side, side);
    }

    public static CubeModel of(Identifier resourceId, Identifier top, Identifier bottom, Identifier side, Identifier front) {
        return CubeModel.of(resourceId, top, bottom, side, side, front, side);
    }

    public static CubeModel of(Identifier resourceId, Identifier top, Identifier bottom, Identifier side, Identifier front, Identifier back) {
        return CubeModel.of(resourceId, top, bottom, side, side, front, back);
    }

    public static CubeModel of(Identifier resourceId, Identifier top, Identifier bottom, Identifier left, Identifier right, Identifier front, Identifier back) {
        return new CubeModel(resourceId, top, bottom, left, right, front, back, ModelProperties.builder().build());
    }

    public static CubeModel of(Identifier resourceId, Identifier all, ModelProperties properties) {
        return CubeModel.of(resourceId, all, all, all, properties);
    }

    public static CubeModel of(Identifier resourceId, Identifier top, Identifier bottom, Identifier side, ModelProperties properties) {
        return CubeModel.of(resourceId, top, bottom, side, side, side, side, properties);
    }

    public static CubeModel of(Identifier resourceId, Identifier top, Identifier bottom, Identifier side, Identifier front, ModelProperties properties) {
        return CubeModel.of(resourceId, top, bottom, side, side, front, side, properties);
    }

    public static CubeModel of(Identifier resourceId, Identifier top, Identifier bottom, Identifier side, Identifier front, Identifier back, ModelProperties properties) {
        return CubeModel.of(resourceId, top, bottom, side, side, front, back, properties);
    }

    public static CubeModel of(Identifier resourceId, Identifier top, Identifier bottom, Identifier left, Identifier right, Identifier front, Identifier back, ModelProperties properties) {
        return new CubeModel(resourceId, top, bottom, left, right, front, back, properties);
    }

    public Identifier top() {
        return top;
    }

    public Identifier bottom() {
        return bottom;
    }

    public Identifier left() {
        return left;
    }

    public Identifier right() {
        return right;
    }

    public Identifier front() {
        return front;
    }

    public Identifier back() {
        return back;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        CubeModel that = (CubeModel) obj;
        return Objects.equals(top, that.top) &&
                Objects.equals(bottom, that.bottom) &&
                Objects.equals(left, that.left) &&
                Objects.equals(right, that.right) &&
                Objects.equals(front, that.front) &&
                Objects.equals(back, that.back);
    }

    @Override
    public int hashCode() {
        return Objects.hash(top, bottom, left, right, front, back);
    }

    @Override
    public String toString() {
        return "CubeModel[" +
                "top=" + top + ", " +
                "bottom=" + bottom + ", " +
                "left=" + left + ", " +
                "right=" + right + ", " +
                "front=" + front + ", " +
                "back=" + back + ']';
    }

    public Set<Identifier> all() {
        return new ReferenceArraySet<>(new Object[]{top, bottom, left, right, front, back});
    }

    public Identifier resourceId() {
        return resourceId;
    }

    public String pass() {
        return properties.renderPass;
    }

    public Identifier buried() {
        return bottom;
    }
}
