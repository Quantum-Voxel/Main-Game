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

import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record RecordedChange(int x, int y, int z, BlockState block) {
    public MapType save() {
        MapType mapType = new MapType();
        mapType.putInt("x", x);
        mapType.putInt("y", y);
        mapType.putInt("z", z);
        mapType.put("block", block.save());
        return mapType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecordedChange that = (RecordedChange) o;
        return x == that.x && y == that.y && z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public @NotNull String toString() {
        return "RecordedChange{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", block=" + block +
                '}';
    }
}