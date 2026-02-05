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

import java.util.Collections;
import java.util.List;

public class VoxelShape {
    private final List<BoundingBox> boundingBoxes;

    public VoxelShape(List<BoundingBox> boundingBoxes) {
        this.boundingBoxes = boundingBoxes;
    }

    public List<BoundingBox> getBoxes() {
        return Collections.unmodifiableList(boundingBoxes);
    }

    public VoxelShape addBox(double x1, double y1, double z1, double x2, double y2, double z2) {
        boundingBoxes.add(new BoundingBox(x1, y1, z1, x2, y2, z2));
        return this;
    }

    public VoxelShape addBox(BoundingBox boundingBox) {
        boundingBoxes.add(boundingBox);
        return this;
    }
}
