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
import dev.ultreon.qvoxel.world.BuilderChunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureData {
    private final Map<ChunkVec, List<FeatureInfo>> featureData = new HashMap<>();

    public void prepareChunk(BuilderChunk chunk) {
        synchronized (this) {
            List<FeatureInfo> featureInfos = featureData.remove(chunk.vec);
            if (featureInfos == null) {
                return;
            }

            // Apply in-chunk points and re-queue out-of-chunk features to their proper chunk buckets if missing.
            Map<ChunkVec, List<FeatureInfo>> toAddPerChunk = new HashMap<>();
            for (FeatureInfo featureInfo : featureInfos) {
                // Apply only points belonging to this chunk (world coords -> local coords)
                featureInfo.points().forEach(point -> {
                    if (point.vec().chunk().equals(chunk.vec)) {
                        chunk.set(point.vec().chunkLocal(), point.state());
                    }
                });

                // Ensure other chunks covered by this feature will also receive it (idempotent add)
                for (ChunkVec otherChunk : featureInfo.coveringChunks()) {
                    if (otherChunk.equals(chunk.vec)) continue;

                    List<FeatureInfo> existingList = featureData.get(otherChunk);
                    if (existingList == null || !existingList.contains(featureInfo)) {
                        toAddPerChunk.computeIfAbsent(otherChunk, __ -> new ArrayList<>()).add(featureInfo);
                    }
                }
            }

            // Re-queue missing feature infos to the appropriate chunks
            for (Map.Entry<ChunkVec, List<FeatureInfo>> entry : toAddPerChunk.entrySet()) {
                featureData.computeIfAbsent(entry.getKey(), __ -> new ArrayList<>()).addAll(entry.getValue());
            }
        }
    }

    public void writeFeature(BuilderChunk origin, FeatureInfo featureInfo) {
        synchronized (this) {
            // Stage feature for all covered chunks
            for (ChunkVec chunkVec : featureInfo.coveringChunks()) {
                featureData.computeIfAbsent(chunkVec, _ -> new ArrayList<>()).add(featureInfo);
            }

            // Apply immediately for the origin chunk only for points within it
            featureInfo.points().forEach(point -> {
                if (point.vec().chunk().equals(origin.vec)) {
                    // Convert world -> local for the origin chunk before writing
                    origin.set(point.vec().x - origin.blockStart.x, point.vec().y - origin.blockStart.y, point.vec().z - origin.blockStart.z, point.state());
                }
            });
        }
    }
}
