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

package dev.ultreon.qvoxel.block;

import dev.ultreon.qvoxel.world.BoundingBoxRaycaster;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.Objects;

/**
 * Axis-Aligned Bounding Box (AABB).
 * Represents a 3D box aligned to axes, used for collision detection.
 */
public final class BoundingBox {
    public static final @NotNull BoundingBox EMPTY = new BoundingBox(0, 0, 0, 0, 0, 0);
    private final Vector3d min;
    private final Vector3d max;

    public BlockCollision userData;
    private final Vector3d cnt = new Vector3d();
    private final Vector3d dim = new Vector3d();

    /**
     * Create a new AABB.
     */
    public BoundingBox(Vector3d min, Vector3d max) {
        this.min = min;
        this.max = max;
    }

    public BoundingBox(double x1, double y1, double z1, double x2, double y2, double z2) {
        this(new Vector3d(x1, y1, z1), new Vector3d(x2, y2, z2));
    }

    public BoundingBox() {
        this(new Vector3d(), new Vector3d());
    }

    public BoundingBox(BoundingBox boundingBox) {
        this(new Vector3d(boundingBox.min), new Vector3d(boundingBox.max));
    }

    /**
     * Offset this bounding box by (x, y, z).
     */
    public BoundingBox offset(double x, double y, double z) {
        return new BoundingBox(
                new Vector3d(min.x + x, min.y + y, min.z + z),
                new Vector3d(max.x + x, max.y + y, max.z + z)
        );
    }

    /**
     * Check if two AABBs intersect.
     */
    public boolean intersects(BoundingBox other) {
        return max.x > other.min.x && min.x < other.max.x &&
                max.y > other.min.y && min.y < other.max.y &&
                max.z > other.min.z && min.z < other.max.z;
    }

    public BoundingBox ext(Vector3f point) {
        return set(
                min.set(BoundingBox.min(min.x, point.x), BoundingBox.min(min.y, point.y), BoundingBox.min(min.z, point.z)),
                max.set(Math.max(max.x, point.x), Math.max(max.y, point.y), Math.max(max.z, point.z))
        );
    }

    private BoundingBox set(Vector3d min, Vector3d max) {
        this.min.set(min);
        this.max.set(max);
        return this;
    }

    private static double min(double a, double b) {
        return Math.min(a, b);
    }

    public Vector3d min() {
        return min;
    }

    public Vector3d max() {
        return max;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        var that = (BoundingBox) obj;
        return Objects.equals(min, that.min) &&
                Objects.equals(max, that.max);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }

    @Override
    public String toString() {
        return "AABB[" +
                "min=" + min + ", " +
                "max=" + max + ']';
    }

    public BoundingBox updateByDelta(double deltaX, double deltaY, double deltaZ) {
        if (deltaX < 0) min.x += deltaX;
        else max.x += deltaX;

        if (deltaY < 0) min.y += deltaY;
        else max.y += deltaY;

        if (deltaZ < 0) min.z += deltaZ;
        else max.z += deltaZ;
        return this;
    }

    public void update() {
        cnt.set(min).add(max).mul(0.5);
        dim.set(max).sub(min);
    }

    public boolean contains(int x, int y, int z) {
        return x >= min.x && x <= max.x &&
                y >= min.y && y <= max.y &&
                z >= min.z && z <= max.z;
    }

    @Deprecated
    public BoundingBox copy() {
        return new BoundingBox(min.x, min.y, min.z, max.x, max.y, max.z);
    }

    public boolean intersects(BoundingBox boundingBox, double x, double y, double z) {
        return boundingBox.max.x > min.x + x && boundingBox.min.x < max.x + x &&
                boundingBox.max.y > min.y + y && boundingBox.min.y < max.y + y &&
                boundingBox.max.z > min.z + z && boundingBox.min.z < max.z + z;
    }


    // Slab method: returns "t" distance along ray of first intersection, or Double.POSITIVE_INFINITY if none
    public double intersectRay(BoundingBoxRaycaster.Ray r, double tMin, double tMax) {
        // X
        double invDx = 1.0 / r.dir.x;
        double t1 = (min.x - r.origin.x) * invDx;
        double t2 = (max.x - r.origin.x) * invDx;
        double txMin = Math.min(t1, t2);
        double txMax = Math.max(t1, t2);

        // Y
        double invDy = 1.0 / r.dir.y;
        double ty1 = (min.y - r.origin.y) * invDy;
        double ty2 = (max.y - r.origin.y) * invDy;
        double tyMin = Math.min(ty1, ty2);
        double tyMax = Math.max(ty1, ty2);

        // Z
        double invDz = 1.0 / r.dir.z;
        double tz1 = (min.z - r.origin.z) * invDz;
        double tz2 = (max.z - r.origin.z) * invDz;
        double tzMin = Math.min(tz1, tz2);
        double tzMax = Math.max(tz1, tz2);

        double low = Math.max(Math.max(txMin, tyMin), Math.max(tzMin, tMin));
        double high = Math.min(Math.min(txMax, tyMax), Math.min(tzMax, tMax));

        if (high >= low && high >= 0) return low >= 0 ? low : high; // return first non-negative t
        return Double.POSITIVE_INFINITY;
    }

}
