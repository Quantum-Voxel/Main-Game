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

import dev.ultreon.qvoxel.resource.GameComponent;
import dev.ultreon.qvoxel.util.Direction;
import dev.ultreon.qvoxel.world.World;

import java.util.BitSet;

public class OpaqueFaces implements GameComponent {
    private final BitSet[] opaqueFaces = new BitSet[Direction.values().length];

    public OpaqueFaces() {
        for (int i = 0; i < opaqueFaces.length; i++) {
            opaqueFaces[i] = new BitSet();
        }
    }

    public void add(int x, int y, int z, Direction direction) {
        switch (direction) {
            case UP:
            case DOWN:
                opaqueFaces[direction.ordinal()].set(x * World.CHUNK_SIZE + z);
                break;
            case NORTH:
            case SOUTH:
                opaqueFaces[direction.ordinal()].set(x * World.CHUNK_SIZE + y);
                break;
            case EAST:
            case WEST:
                opaqueFaces[direction.ordinal()].set(y * World.CHUNK_SIZE + z);
                break;
            default:
                throw new IllegalArgumentException("Invalid direction: " + direction);
        }
    }

    public boolean isFull(Direction direction) {
        BitSet bitSet = opaqueFaces[direction.ordinal()];
        return bitSet != null && bitSet.nextClearBit(0) == (World.CHUNK_SIZE - 1) * World.CHUNK_SIZE + World.CHUNK_SIZE - 1;
    }

    public void clear() {
        for (BitSet bitSet : opaqueFaces) {
            bitSet.clear();
        }
    }

    public void set(OpaqueFaces opaqueFaces) {
        System.arraycopy(opaqueFaces.opaqueFaces, 0, this.opaqueFaces, 0, this.opaqueFaces.length);
    }
}
