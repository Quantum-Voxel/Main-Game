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

public class FaceCull {
    public static int of(boolean top, boolean bottom, boolean front, boolean left, boolean back, boolean right) {
        return (bottom ? 1 : 0) | (top ? 2 : 0) | (front ? 4 : 0) | (left ? 8 : 0) | (back ? 16 : 0) | (right ? 32 : 0);
    }

    public static boolean culls(Direction direction, int cull) {
        if (direction == null) return false;
        return (cull & 1 << direction.opposite().ordinal()) != 0;
    }
}
