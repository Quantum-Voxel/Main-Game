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

import dev.ultreon.qvoxel.block.BoundingBox;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.util.ChunkVec;
import dev.ultreon.qvoxel.world.structure.Structure;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static java.util.List.copyOf;

public class StructureData {
    private final List<BoundingBox> bounds = new ArrayList<>();
    private final List<Structure> structures = new ArrayList<>();

    private final Map<ChunkVec, List<StructureInstance>> structureInstances = new HashMap<>();

    public void addStructurePoint(int x, int y, int z, Structure structure) {
        synchronized (this) {
            BlockVec center = structure.getCenter();
            BlockVec size = structure.getSize();
            BoundingBox boundingBox = new BoundingBox(x + center.x, y + center.y, z + center.z, x + center.x + size.x, y + center.y + size.y, z + center.z + size.z);
            bounds.add(boundingBox);
            structures.add(structure);

            BlockVec start = new BlockVec(boundingBox.min());
            ChunkVec startChunk = start.chunk();

            BlockVec end = new BlockVec(boundingBox.max());
            ChunkVec endChunk = end.chunk();

            for (int cx = startChunk.x; cx <= endChunk.x; cx++) {
                for (int cy = startChunk.y; cy <= endChunk.y; cy++) {
                    for (int cz = startChunk.z; cz <= endChunk.z; cz++) {
                        ChunkVec chunkVec = new ChunkVec(cx, cy, cz);
                        structureInstances.computeIfAbsent(chunkVec, k -> new ArrayList<>()).add(new StructureInstance(
                                start,
                                structure
                        ));
                    }
                }
            }
        }
    }

    public final @Nullable Structure getStructureAt(int x, int y, int z) {
        synchronized (this) {
            for (int i = 0; i < bounds.size(); i++) {
                BoundingBox box = bounds.get(i);
                if (box.contains(x, y, z)) {
                    return structures.get(i);
                }
            }
        }

        return null;
    }

    public final List<BoundingBox> getStructures() {
        return copyOf(bounds);
    }

    public Collection<StructureInstance> getStructuresAt(ChunkVec vec) {
        List<StructureInstance> list = structureInstances.get(vec);
        if (list == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableCollection(list);
    }
}
