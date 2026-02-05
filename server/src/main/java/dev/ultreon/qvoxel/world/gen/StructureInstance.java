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

import dev.ultreon.qvoxel.world.Chunk;
import dev.ultreon.qvoxel.world.structure.Structure;
import dev.ultreon.qvoxel.util.BlockVec;
import org.joml.Vector3i;

import java.util.Objects;

public final class StructureInstance {
    private final BlockVec pos;
    private final Structure structure;

    public StructureInstance(
            BlockVec pos,
            Structure structure
    ) {
        this.pos = pos;
        this.structure = structure;
    }

    public boolean contains(int x, int y, int z) {
        BlockVec size = structure.getSize();
        return x >= pos.x && x < pos.x + size.x && y >= pos.y && y < pos.y + size.y && z >= pos.z && z < pos.z + size.z;
    }

    public void placeSlice(Chunk recordingChunk) {
        Vector3i center = structure.getCenter().copy().add(pos);
        structure.placeSlice(recordingChunk, center.x, center.y, center.z);
    }

    public BlockVec pos() {
        return pos;
    }

    public Structure structure() {
        return structure;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        var that = (StructureInstance) obj;
        return Objects.equals(pos, that.pos) &&
                Objects.equals(structure, that.structure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, structure);
    }

    @Override
    public String toString() {
        return "StructureInstance[" +
                "pos=" + pos + ", " +
                "structure=" + structure + ']';
    }

}
