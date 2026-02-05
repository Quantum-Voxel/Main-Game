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

import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.client.world.RenderType;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.util.Direction;
import dev.ultreon.qvoxel.world.Chunk;

import java.util.Arrays;

public record AOArray(float[][] value) {

    public static AOArray of(AO of, AO of1, AO of2, AO of3, AO of4, AO of5) {
        return new AOArray(new float[][]{
                of.corners(),
                of1.corners(),
                of2.corners(),
                of3.corners(),
                of4.corners(),
                of5.corners()
        });
    }

    public AO aoForSide(Direction side) {
        return new AO(value[side.ordinal()]);
    }

    public AO get(int index) {
        return new AO(value[index]);
    }

    @Deprecated
    public AOArray copy() {
        float[][] copied = new float[value.length][4];
        for (int i = 0; i < value.length; i++) {
            copied[i] = Arrays.copyOf(value[i], 4);
        }
        return new AOArray(copied);
    }

    public record AO(float[] corners) {
        public float corner00() {
            return corners[0];
        }

        public float corner01() {
            return corners[1];
        }

        public float corner10() {
            return corners[2];
        }

        public float corner11() {
            return corners[3];
        }

        public static AO of(float c00, float c01, float c10, float c11) {
            return new AO(new float[]{c00, c01, c10, c11});
        }

        public static AO none() {
            return of(1f, 1f, 1f, 1f);
        }
    }

    public static AOArray calculate(Chunk chunk, int x, int y, int z, RenderType renderType) {
        BlockState block = chunk.getSafe(x, y, z);
        if (!block.ambientOcclusion()) return AOArray.of(
                AO.none(),
                AO.none(),
                AO.none(),
                AO.none(),
                AO.none(),
                AO.none()
        );

        float[][] array = new float[6][4];

        for (Direction dir : Direction.values()) {
            float[] corners = new float[4];

            // Determine the 3 neighbors for each corner
            BlockVec p = new BlockVec(x + dir.getNormal().x, y + dir.getNormal().y, z + dir.getNormal().z);

            switch (dir.getAxis()) {
                case Y -> {
                    // Corners: NW, SW, NE, SE
                    corners[0] = smoothAO(chunk, p.x - 1, p.y, p.z - 1, p.x - 1, p.y, p.z, p.x, p.y, p.z - 1);
                    corners[1] = smoothAO(chunk, p.x - 1, p.y, p.z + 1, p.x - 1, p.y, p.z, p.x, p.y, p.z + 1);
                    corners[2] = smoothAO(chunk, p.x + 1, p.y, p.z - 1, p.x + 1, p.y, p.z, p.x, p.y, p.z - 1);
                    corners[3] = smoothAO(chunk, p.x + 1, p.y, p.z + 1, p.x + 1, p.y, p.z, p.x, p.y, p.z + 1);
                    if (!dir.isNegative()) {
                        // flip top face
                        float tmp = corners[0];
                        corners[0] = corners[2];
                        corners[2] = tmp;
                        tmp = corners[1];
                        corners[1] = corners[3];
                        corners[3] = tmp;
                    }
                }
                case X -> {
                    // Corners: ND, NU, SD, SU
                    corners[0] = smoothAO(chunk, p.x, p.y - 1, p.z - 1, p.x, p.y - 1, p.z, p.x, p.y, p.z - 1);
                    corners[1] = smoothAO(chunk, p.x, p.y + 1, p.z - 1, p.x, p.y + 1, p.z, p.x, p.y, p.z - 1);
                    corners[2] = smoothAO(chunk, p.x, p.y - 1, p.z + 1, p.x, p.y - 1, p.z, p.x, p.y, p.z + 1);
                    corners[3] = smoothAO(chunk, p.x, p.y + 1, p.z + 1, p.x, p.y + 1, p.z, p.x, p.y, p.z + 1);
                    if (dir.isNegative()) {
                        float tmp = corners[0];
                        corners[0] = corners[2];
                        corners[2] = tmp;
                        tmp = corners[1];
                        corners[1] = corners[3];
                        corners[3] = tmp;
                    }
                }
                case Z -> {
                    // Corners: WD, WU, ED, EU
                    corners[0] = smoothAO(chunk, p.x - 1, p.y - 1, p.z, p.x - 1, p.y, p.z, p.x, p.y - 1, p.z);
                    corners[1] = smoothAO(chunk, p.x - 1, p.y + 1, p.z, p.x - 1, p.y, p.z, p.x, p.y + 1, p.z);
                    corners[2] = smoothAO(chunk, p.x + 1, p.y - 1, p.z, p.x + 1, p.y, p.z, p.x, p.y - 1, p.z);
                    corners[3] = smoothAO(chunk, p.x + 1, p.y + 1, p.z, p.x + 1, p.y, p.z, p.x, p.y + 1, p.z);
                    if (dir.isNegative()) {
                        float tmp = corners[0];
                        corners[0] = corners[2];
                        corners[2] = tmp;
                        tmp = corners[1];
                        corners[1] = corners[3];
                        corners[3] = tmp;
                    }
                }
            }

            array[dir.ordinal()] = corners;
        }

        return new AOArray(array);
    }

    /**
     * Smooth AO for a single corner using three neighbors:
     * Each neighbor reduces brightness fractionally.
     */
    private static float smoothAO(Chunk chunk,
                                  int cornerX, int cornerY, int cornerZ,
                                  int sideX, int sideY, int sideZ,
                                  int adjX, int adjY, int adjZ) {
        float ao = 1f;
        ao -= chunk.getSafe(cornerX, cornerY, cornerZ).ambientOcclusion() ? 0.3f : 0f;
        ao -= chunk.getSafe(sideX, sideY, sideZ).ambientOcclusion() ? 0.3f : 0f;
        ao -= chunk.getSafe(adjX, adjY, adjZ).ambientOcclusion() ? 0.3f : 0f;
        return Math.max(0f, ao);
    }
}
