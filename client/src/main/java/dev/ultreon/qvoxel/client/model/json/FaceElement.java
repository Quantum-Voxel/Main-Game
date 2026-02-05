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

package dev.ultreon.qvoxel.client.model.json;

import dev.ultreon.qvoxel.util.Direction;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("SpellCheckingInspection")
public record FaceElement(String texture, UVs uvs, int rotation, int tintindex, Direction cullface) {
    public FaceElement(String texture, UVs uvs, int rotation, int tintindex,
                       String cullface) {
        Direction direction;
        if (cullface != null)
            direction = switch (cullface) {
                case "north" -> Direction.NORTH;
                case "south" -> Direction.SOUTH;
                case "east" -> Direction.EAST;
                case "west" -> Direction.WEST;
                case "up" -> Direction.UP;
                case "down" -> Direction.DOWN;
                default -> throw new UnsupportedOperationException("Invalid cullface");
            };
        else direction = null;
        this(texture, uvs, rotation, tintindex, direction);
    }

    @Override
    public @NotNull String toString() {
        return "FaceElement[" +
                "texture=" + texture + ", " +
                "uvs=" + uvs + ", " +
                "rotation=" + rotation + ", " +
                "tintindex=" + tintindex + ", " +
                "cullface=" + cullface + ']';
    }

}
