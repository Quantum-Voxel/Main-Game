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

import org.joml.Vector2i;

public enum Neighbour8Direction {
    E(0, 1),
    NE(1, 1),
    N(1, 0),
    NW(1, -1),
    W(0, -1),
    SW(-1, -1),
    S(-1, 0),
    SE(-1, 1);

    public final int x;
    public final int y;

    Neighbour8Direction(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2i vec() {
        return new Vector2i(x, y);
    }
}
