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

package dev.ultreon.qvoxel.client.world.mesher;

import dev.ultreon.qvoxel.block.BoundingBox;
import dev.ultreon.qvoxel.block.ModelEffect;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.model.AOArray;
import dev.ultreon.qvoxel.client.model.FaceCull;
import dev.ultreon.qvoxel.client.model.OpaqueFaces;
import dev.ultreon.qvoxel.client.world.BlockRenderTypeRegistry;
import dev.ultreon.qvoxel.client.world.ClientChunk;
import dev.ultreon.qvoxel.client.world.RenderType;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.util.ChunkVec;
import dev.ultreon.qvoxel.util.Direction;
import dev.ultreon.qvoxel.world.Axis;
import dev.ultreon.qvoxel.world.World;

import java.util.Random;

public class FaceCullMesher implements Mesher {
    private final ClientChunk chunk;

    public FaceCullMesher(ClientChunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public boolean buildMesh(BoundingBox bounds, OpaqueFaces opaqueFaces, UseCondition condition, ChunkMeshBuilder builder1) {
        boolean flag = false;
        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int y = 0; y < World.CHUNK_SIZE; y++) {
                for (int z = 0; z < World.CHUNK_SIZE; z++) {
                    flag |= loadBlockInto(bounds, opaqueFaces, builder1, x, y, z);
                }
            }
        }

        return flag;
    }

    private boolean loadBlockInto(
            BoundingBox bounds, OpaqueFaces opaqueFaces, ChunkMeshBuilder meshPartBuilder,
            int x, int y, int z
    ) {
        synchronized (chunk.lock) {
            final var block = chunk.getSafe(x, y, z);
            if (block.isAir()) {
                return false;
            }
            final var model = QuantumClient.get().getBlockModel(block);
            BlockState back = chunk.getSafe(x, y, z - 1);
            BlockState front = chunk.getSafe(x, y, z + 1);
            BlockState left = chunk.getSafe(x - 1, y, z);
            BlockState right = chunk.getSafe(x + 1, y, z);
            BlockState top = chunk.getSafe(x, y + 1, z);
            BlockState bottom = chunk.getSafe(x, y - 1, z);

            // Calculate smooth lighting for each face with 4 corner values
            int[][] light = calculateSmoothLight(chunk, x, y, z);

            ChunkVec vec = chunk.vec;
            long seed = BlockVec.hash64(vec.x * World.CHUNK_SIZE + x, vec.y * World.CHUNK_SIZE + y, vec.z * World.CHUNK_SIZE + z);
            Random random = new Random(seed);

            float dx = 0;
            float dy = 0;
            float dz = 0;
            ModelEffect modelEffect = block.getBlock().getModelEffect();
            boolean isOffXYZ = modelEffect == ModelEffect.OffsetXYZ;
            if (modelEffect == ModelEffect.OffsetXZ || isOffXYZ) {
                dx = random.nextFloat(-0.25f, 0.25f);
                dz = random.nextFloat(-0.25f, 0.25f);
            }
            if (isOffXYZ) {
                dy = random.nextFloat(-0.25f, 0.25f);
            }

            model.bakeInto(bounds, opaqueFaces,
                    meshPartBuilder.get(BlockRenderTypeRegistry.getRenderType(block)), x + dx, y + dy, z + dz, FaceCull.of(
                            shouldMerge(block, top),
                            shouldMerge(block, bottom),
                            shouldMerge(block, front),
                            shouldMerge(block, right),
                            shouldMerge(block, back),
                            shouldMerge(block, left)
                    ), AOArray.calculate(chunk, x, y, z, BlockRenderTypeRegistry.getRenderType(block)), light);
            return true;
        }
    }

    private static boolean shouldMerge(BlockState block, BlockState other) {
        RenderType renderType = BlockRenderTypeRegistry.getRenderType(block);
        if (renderType == RenderType.LEAVES) return false;
        if (renderType == RenderType.WATER && !(other.isAir() && !other.hasCollision())) return true;
        return renderType == BlockRenderTypeRegistry.getRenderType(other)
                && !other.isAir()
                && block.isTransparent() == other.isTransparent()
                && renderType.doesMerging()
                && block.getBlock().culls();
    }

    private static int[][] calculateSmoothLight(ClientChunk chunk, int x, int y, int z) {
        int[][] light = new int[6][4];

        // For each face direction, calculate light at 4 corners by averaging surrounding blocks
        for (Direction dir : Direction.values()) {
            int px = x + dir.getNormal().x;
            int py = y + dir.getNormal().y;
            int pz = z + dir.getNormal().z;

            switch (dir.getAxis()) {
                case Y -> {
                    // Corners: corner00, corner01, corner10, corner11
                    light[dir.ordinal()][0] = averageLight(
                            chunk.getLight(px - 1, py, pz - 1),
                            chunk.getLight(px - 1, py, pz),
                            chunk.getLight(px, py, pz - 1),
                            chunk.getLight(px, py, pz)
                    );
                    light[dir.ordinal()][1] = averageLight(
                            chunk.getLight(px - 1, py, pz + 1),
                            chunk.getLight(px - 1, py, pz),
                            chunk.getLight(px, py, pz + 1),
                            chunk.getLight(px, py, pz)
                    );
                    light[dir.ordinal()][2] = averageLight(
                            chunk.getLight(px + 1, py, pz - 1),
                            chunk.getLight(px + 1, py, pz),
                            chunk.getLight(px, py, pz - 1),
                            chunk.getLight(px, py, pz)
                    );
                    light[dir.ordinal()][3] = averageLight(
                            chunk.getLight(px + 1, py, pz + 1),
                            chunk.getLight(px + 1, py, pz),
                            chunk.getLight(px, py, pz + 1),
                            chunk.getLight(px, py, pz)
                    );
                }
                case X -> {
                    // Corners for X axis faces
                    light[dir.ordinal()][0] = averageLight(
                            chunk.getLight(px, py - 1, pz - 1),
                            chunk.getLight(px, py - 1, pz),
                            chunk.getLight(px, py, pz - 1),
                            chunk.getLight(px, py, pz)
                    );
                    light[dir.ordinal()][1] = averageLight(
                            chunk.getLight(px, py + 1, pz - 1),
                            chunk.getLight(px, py + 1, pz),
                            chunk.getLight(px, py, pz - 1),
                            chunk.getLight(px, py, pz)
                    );
                    light[dir.ordinal()][2] = averageLight(
                            chunk.getLight(px, py - 1, pz + 1),
                            chunk.getLight(px, py - 1, pz),
                            chunk.getLight(px, py, pz + 1),
                            chunk.getLight(px, py, pz)
                    );
                    light[dir.ordinal()][3] = averageLight(
                            chunk.getLight(px, py + 1, pz + 1),
                            chunk.getLight(px, py + 1, pz),
                            chunk.getLight(px, py, pz + 1),
                            chunk.getLight(px, py, pz)
                    );
                }
                case Z -> {
                    // Corners for Z axis faces
                    light[dir.ordinal()][0] = averageLight(
                            chunk.getLight(px - 1, py - 1, pz),
                            chunk.getLight(px - 1, py, pz),
                            chunk.getLight(px, py - 1, pz),
                            chunk.getLight(px, py, pz)
                    );
                    light[dir.ordinal()][1] = averageLight(
                            chunk.getLight(px - 1, py + 1, pz),
                            chunk.getLight(px - 1, py, pz),
                            chunk.getLight(px, py + 1, pz),
                            chunk.getLight(px, py, pz)
                    );
                    light[dir.ordinal()][2] = averageLight(
                            chunk.getLight(px + 1, py - 1, pz),
                            chunk.getLight(px + 1, py, pz),
                            chunk.getLight(px, py - 1, pz),
                            chunk.getLight(px, py, pz)
                    );
                    light[dir.ordinal()][3] = averageLight(
                            chunk.getLight(px + 1, py + 1, pz),
                            chunk.getLight(px + 1, py, pz),
                            chunk.getLight(px, py + 1, pz),
                            chunk.getLight(px, py, pz)
                    );
                }
            }

            // Normalize corner order for Y faces to match vertex layout and avoid diagonal (-,+)/(+,-) flips
            if (dir.getAxis() == Axis.Y) {
                int[] c = light[dir.ordinal()];
                if (dir.isNegative()) {
                    // DOWN: reorder to [1,3,2,0] to keep a stable orientation
                    int c0 = c[0], c1 = c[1], c2 = c[2], c3 = c[3];
                    c[0] = c1;
                    c[1] = c3;
                    c[2] = c2;
                    c[3] = c0;
                } else {
                    // UP: swap mixed-sign corners to stabilize diagonal orientation
                    int t = c[1]; c[1] = c[2]; c[2] = t;
                }
                continue;
            }

            // Normalize corner order for Z faces (X-Y plane)
            if (dir.getAxis() == Axis.Z) {
                int[] c = light[dir.ordinal()];
                if (dir.isNegative()) {
                    // NORTH: reorder to [1,3,2,0]
                    int c0 = c[0], c1 = c[1], c2 = c[2], c3 = c[3];
                    c[0] = c1;
                    c[3] = c3;
                    c[2] = c2;
                    c[1] = c0;
                } else {
                    // SOUTH: swap mixed-sign corners
                    int t = c[1]; c[1] = c[2]; c[2] = t;
                }
                continue;
            }

            // Normalize corner order for X faces (Z-Y plane)
            if (dir.getAxis() == Axis.X) {
                int[] c = light[dir.ordinal()];
                if (dir.isNegative()) {
                    // WEST: reorder to [1,3,2,0]
                    int c0 = c[0], c1 = c[1], c2 = c[2], c3 = c[3];
                    c[0] = c1;
                    c[3] = c3;
                    c[2] = c2;
                    c[1] = c0;
                } else {
                    // EAST: swap mixed-sign corners
                    int t = c[1]; c[1] = c[2]; c[2] = t;
                }
                continue;
            }
        }

        return light;
    }

    private static int averageLight(int l1, int l2, int l3, int l4) {
        // Extract RGBA components and average them
        int r = ((l1 >> 24 & 0xFF) + (l2 >> 24 & 0xFF) + (l3 >> 24 & 0xFF) + (l4 >> 24 & 0xFF)) / 4;
        int g = ((l1 >> 16 & 0xFF) + (l2 >> 16 & 0xFF) + (l3 >> 16 & 0xFF) + (l4 >> 16 & 0xFF)) / 4;
        int b = ((l1 >> 8 & 0xFF) + (l2 >> 8 & 0xFF) + (l3 >> 8 & 0xFF) + (l4 >> 8 & 0xFF)) / 4;
        int s = ((l1 & 0xFF) + (l2 & 0xFF) + (l3 & 0xFF) + (l4 & 0xFF)) / 4;
        return r << 24 | g << 16 | b << 8 | s;
    }
}
