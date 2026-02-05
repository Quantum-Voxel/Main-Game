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

import dev.ultreon.qvoxel.block.BoundingBox;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.util.BlockVec;
import org.joml.Vector3d;

@SuppressWarnings("D")
public class WorldCollider {
    private final World world;
    private final Vector3d tmpVec = new Vector3d();

    public WorldCollider(World world) {
        this.world = world;
    }

    /**
     * Move an AABB with collision resolution.
     *
     * @param box Entity's bounding box
     * @param dx  Desired motion on X
     * @param dy  Desired motion on Y
     * @param dz  Desired motion on Z
     * @return Final moved AABB
     */
    public BoundingBox move(BoundingBox box, double dx, double dy, double dz) {
        // Step 1: move on X
        if (dx != 0) {
            dx = clipAxis(box, dx, dy, dz, Axis.X);
            box = box.offset(dx, 0, 0);
        }

        // Step 2: move on Y
        if (dy != 0) {
            dy = clipAxis(box, dx, dy, dz, Axis.Y);
            box = box.offset(0, dy, 0);
        }

        // Step 3: move on Z
        if (dz != 0) {
            dz = clipAxis(box, dx, dy, dz, Axis.Z);
            box = box.offset(0, 0, dz);
        }

        return box;
    }

    public Vector3d move(BoundingBox boundingBox, Vector3d vec, double dx, double dy, double dz) {
        tmpVec.set(vec).sub(center(boundingBox.min().x, boundingBox.max().x), center(boundingBox.min().y, boundingBox.max().y), center(boundingBox.min().z, boundingBox.max().z));
        BoundingBox offset = boundingBox.offset(vec.x, vec.y, vec.z);
        BoundingBox moved = move(offset, dx, dy, dz);
        return tmpVec.add(center(moved.min().x, moved.max().x), center(moved.min().y, moved.max().y), center(moved.min().z, moved.max().z));
    }

    private double center(double v, double v1) {
        return v + (v1 - v) / 2;
    }

    /**
     * Clip motion along a given axis based on world collisions.
     */
    private double clipAxis(BoundingBox box, double dx, double dy, double dz, Axis axis) {
        // Expand region to cover potential collisions
        int minX = (int) Math.floor(box.min().x + (axis == Axis.X ? Math.min(0, dx) : 0));
        int minY = (int) Math.floor(box.min().y + (axis == Axis.Y ? Math.min(0, dy) : 0));
        int minZ = (int) Math.floor(box.min().z + (axis == Axis.Z ? Math.min(0, dz) : 0));
        int maxX = (int) Math.floor(box.max().x + (axis == Axis.X ? Math.max(0, dx) : 0));
        int maxY = (int) Math.floor(box.max().y + (axis == Axis.Y ? Math.max(0, dy) : 0));
        int maxZ = (int) Math.floor(box.max().z + (axis == Axis.Z ? Math.max(0, dz) : 0));

        double clipped = axis == Axis.X ? dx : axis == Axis.Y ? dy : dz;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockVec pos = new BlockVec(x, y, z);
                    BlockState state = world.get(pos);
                    if (state == null || state.isAir()) continue;

                    for (BoundingBox local : state.getBoundingBoxes(world, pos)) {
                        BoundingBox blockBox = local.offset(x, y, z);

                        switch (axis) {
                            case X -> {
                                if (box.max().y > blockBox.min().y && box.min().y < blockBox.max().y &&
                                        box.max().z > blockBox.min().z && box.min().z < blockBox.max().z) {
                                    if (clipped > 0 && box.max().x <= blockBox.min().x)
                                        clipped = Math.min(clipped, blockBox.min().x - box.max().x);
                                    else if (clipped < 0 && box.min().x >= blockBox.max().x)
                                        clipped = Math.max(clipped, blockBox.max().x - box.min().x);
                                }
                            }
                            case Y -> {
                                if (box.max().x > blockBox.min().x && box.min().x < blockBox.max().x &&
                                        box.max().z > blockBox.min().z && box.min().z < blockBox.max().z) {
                                    if (clipped > 0 && box.max().y <= blockBox.min().y)
                                        clipped = Math.min(clipped, blockBox.min().y - box.max().y);
                                    else if (clipped < 0 && box.min().y >= blockBox.max().y)
                                        clipped = Math.max(clipped, blockBox.max().y - box.min().y);
                                }
                            }
                            case Z -> {
                                if (box.max().x > blockBox.min().x && box.min().x < blockBox.max().x &&
                                        box.max().y > blockBox.min().y && box.min().y < blockBox.max().y) {
                                    if (clipped > 0 && box.max().z <= blockBox.min().z)
                                        clipped = Math.min(clipped, blockBox.min().z - box.max().z);
                                    else if (clipped < 0 && box.min().z >= blockBox.max().z)
                                        clipped = Math.max(clipped, blockBox.max().z - box.min().z);
                                }
                            }
                        }
                    }
                }
            }
        }

        return clipped;
    }
}
