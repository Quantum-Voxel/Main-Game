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

package dev.ultreon.qvoxel.client.model;

import dev.ultreon.qvoxel.util.Direction;
import dev.ultreon.qvoxel.world.Axis;

public class Light {
    // Each direction now stores 4 corner values (4 ints per direction = 6 directions)
    public static int[][] of(int up, int down, int north, int south, int east, int west) {
        int[][] light = new int[6][4];
        // Initialize all corners with the same value for backward compatibility
        for (int i = 0; i < 4; i++) {
            light[Direction.UP.ordinal()][i] = up;
            light[Direction.DOWN.ordinal()][i] = down;
            light[Direction.NORTH.ordinal()][i] = north;
            light[Direction.SOUTH.ordinal()][i] = south;
            light[Direction.EAST.ordinal()][i] = east;
            light[Direction.WEST.ordinal()][i] = west;
        }
        return light;
    }

    // New method for smooth lighting with 4 corners per face
    public static int[][] ofSmooth(int[] up, int[] down, int[] north, int[] south, int[] east, int[] west) {
        int[][] light = new int[6][4];
        System.arraycopy(up, 0, light[Direction.UP.ordinal()], 0, 4);
        System.arraycopy(down, 0, light[Direction.DOWN.ordinal()], 0, 4);
        System.arraycopy(north, 0, light[Direction.NORTH.ordinal()], 0, 4);
        System.arraycopy(south, 0, light[Direction.SOUTH.ordinal()], 0, 4);
        System.arraycopy(east, 0, light[Direction.EAST.ordinal()], 0, 4);
        System.arraycopy(west, 0, light[Direction.WEST.ordinal()], 0, 4);
        return light;
    }

    @Deprecated
    public static int get(long[] light, Direction direction) {
        Axis axis = direction.getAxis();
        boolean negative = direction.isNegative();
        long axisValue = light[axis.ordinal()];
        return negative ? (int) (axisValue >> 32) : (int) axisValue;
    }

    // Get corner light values for a direction
    public static int[] getCorners(int[][] light, Direction direction) {
        return light[direction.ordinal()];
    }

    // Get a specific corner light value
    public static int getCorner(int[][] light, Direction direction, int corner) {
        return light[direction.ordinal()][corner];
    }

    public static int ofAverage(int[][] light) {
        long sumR = 0, sumG = 0, sumB = 0, sumS = 0;
        int count = 0;

        for (int[] row : light) {
            for (int v : row) {
                sumR += (v) & 0xFF;
                sumG += (v >> 8) & 0xFF;
                sumB += (v >> 16) & 0xFF;
                sumS += (v >> 24) & 0xFF;
                count++;
            }
        }

        if (count == 0) return 0;

        int r = (int) (sumR / count);
        int g = (int) (sumG / count);
        int b = (int) (sumB / count);
        int s = (int) (sumS / count);

        return (r & 0xFF)
                | ((g & 0xFF) << 8)
                | ((b & 0xFF) << 16)
                | ((s & 0xFF) << 24);
    }
}
