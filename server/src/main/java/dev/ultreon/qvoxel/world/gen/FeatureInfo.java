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

package dev.ultreon.qvoxel.world.gen;

import dev.ultreon.qvoxel.util.ChunkVec;
import dev.ultreon.qvoxel.world.BlockPoint;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class FeatureInfo {
    private final List<BlockPoint> points;
    private @Nullable Set<ChunkVec> coveringChunks = null;

    public FeatureInfo(List<BlockPoint> points) {
        this.points = points;
    }

    public List<BlockPoint> points() {
        return Collections.unmodifiableList(points);
    }

    public Set<ChunkVec> coveringChunks() {
        if (coveringChunks == null) {
            coveringChunks = new HashSet<>();
            for (var point : points) {
                coveringChunks.add(point.vec().chunk());
            }
        }

        return coveringChunks;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        var that = (FeatureInfo) obj;
        return Objects.equals(points, that.points);
    }

    @Override
    public int hashCode() {
        return Objects.hash(points);
    }

    @Override
    public String toString() {
        return "FeatureInfo[" +
                "points=" + points + ']';
    }

}
