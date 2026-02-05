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

package dev.ultreon.qvoxel.util;

import dev.ultreon.qvoxel.block.state.property.StringSerializable;
import dev.ultreon.qvoxel.world.Axis;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.Random;

public enum Direction implements StringSerializable {
    UP(0, 1, 0), // +Y
    DOWN(0, -1, 0), // -Y
    NORTH(0, 0, -1), // -Z
    WEST(-1, 0, 0), // -X
    SOUTH(0, 0, 1), // +Z
    EAST(1, 0, 0), // +X
    ;

    public static final Direction[] HORIZONTAL = {NORTH, SOUTH, EAST, WEST};
    private final int normalX;
    private final int normalY;
    private final int normalZ;

    Direction(int normalX, int normalY, int normalZ) {
        this.normalX = normalX;
        this.normalY = normalY;
        this.normalZ = normalZ;
    }

    public static Direction random(RNG random) {
        return Direction.values()[random.randint(0, Direction.values().length - 1)];
    }

    public static Direction random(Random random) {
        return Direction.values()[random.nextInt(Direction.values().length)];
    }

    public Direction opposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
            case UP -> DOWN;
            case DOWN -> UP;
        };
    }

    @Override
    public String serialize() {
        return name().toLowerCase();
    }

    public Vector3f getNormal(Vector3f normal) {
        return normal.set(normalX, normalY, normalZ);
    }

    public Axis getAxis() {
        return switch (this) {
            case EAST, WEST -> Axis.X;
            case UP, DOWN -> Axis.Y;
            case NORTH, SOUTH -> Axis.Z;
        };
    }

    public boolean isNegative() {
        /*

  val isNegative: Boolean
    get() = when (this) {
      UP -> false
      EAST -> false
      SOUTH -> false
      DOWN -> true
      WEST -> true
      NORTH -> true
      else -> false
    }

         */
        return switch (this) {
            case UP, EAST, SOUTH -> false;
            default -> true;
        };
    }

    public int getNormalX() {
        return normalX;
    }

    public int getNormalY() {
        return normalY;
    }

    public int getNormalZ() {
        return normalZ;
    }

    public Vector3i getNormal() {
        return new Vector3i(normalX, normalY, normalZ);
    }

    public boolean isPositive() {
        return !isNegative();
    }
}
