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

package dev.ultreon.qvoxel.server;

import dev.ultreon.qvoxel.resource.GameNode;
import dev.ultreon.qvoxel.world.DimensionInfo;
import dev.ultreon.qvoxel.world.gen.chunk.ChunkGenerator;

import java.util.Objects;

public final class Dimension extends GameNode {
    public final DimensionInfo info;
    public final ChunkGenerator generator;

    public Dimension(DimensionInfo info, ChunkGenerator generator) {
        this.info = info;
        this.generator = generator;
    }

    @Override
    public String toString() {
        return "Dimension[" +
                "info=" + info + ", " +
                "generator=" + generator + ']';
    }

    public DimensionInfo info() {
        return info;
    }

    public ChunkGenerator generator() {
        return generator;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Dimension) obj;
        return Objects.equals(this.info, that.info) &&
                Objects.equals(this.generator, that.generator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(info, generator);
    }


}
