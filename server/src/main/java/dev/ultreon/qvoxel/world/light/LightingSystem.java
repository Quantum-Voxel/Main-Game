/*
 * Copyright 2025. Quinten 'Qubix' Jungblut
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.ultreon.qvoxel.world.light;

import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.server.ServerChunk;
import dev.ultreon.qvoxel.server.ServerWorld;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.util.Direction;
import dev.ultreon.qvoxel.world.BuilderChunk;
import dev.ultreon.qvoxel.world.HeightmapType;
import dev.ultreon.qvoxel.world.World;
import dev.ultreon.qvoxel.world.gen.GenerationBarrier;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;

import static java.lang.Math.max;

public class LightingSystem {
    private final ServerWorld world;

    public LightingSystem(ServerWorld world) {
        this.world = world;
    }

    /* ---------------- SKY INITIALIZATION ---------------- */

    /**
     * Initializes skylight in a given chunk.
     */
    public void initSkyLight(BuilderChunk chunk) {
        int baseX = chunk.vec.x * World.CHUNK_SIZE;
        int baseY = chunk.vec.y * World.CHUNK_SIZE;
        int baseZ = chunk.vec.z * World.CHUNK_SIZE;

        Queue<BlockVec> queue = new ArrayDeque<>();

        // Step 1: Top-down initialization
        for (int lx = 0; lx < World.CHUNK_SIZE; lx++) {
            for (int lz = 0; lz < World.CHUNK_SIZE; lz++) {
                int wx = baseX + lx;
                int wz = baseZ + lz;
                int top = world.getHeight(wx, wz, HeightmapType.LIGHT_BLOCKING);

                int currentLight = 15;
                for (int ly = World.CHUNK_SIZE - 1; ly >= 0; ly--) {
                    int wy = baseY + ly;
                    BlockState state = world.get(wx, wy, wz);

                    if (wy >= top && state.isAir()) {
                        setSkylight(chunk, lx, ly, lz, currentLight);
                    } else {
                        int opacity = state.getLightReduction(); // 0 for air, up to 15 for solid
                        currentLight -= opacity;
                        if (currentLight <= 0) currentLight = 0;
                        setSkylight(chunk, lx, ly, lz, currentLight);
                        if (opacity > 0) break; // stop once we hit solid
                    }

                    if (currentLight > 1) {
                        queue.add(new BlockVec(wx, wy, wz));
                    }
                }
            }
        }

        // Step 2: Flood-fill propagation
        while (!queue.isEmpty()) {
            BlockVec pos = queue.poll();
            int wx = pos.x;
            int wy = pos.y;
            int wz = pos.z;

            int light = world.getLightingSystem().getSkylight(wx, wy, wz);
            if (light <= 1) continue;

            for (Direction dir : Direction.values()) {
                int nx = wx + dir.getNormalX();
                int ny = wy + dir.getNormalY();
                int nz = wz + dir.getNormalZ();

                BlockState neighbor = world.get(nx, ny, nz);
                if (neighbor.blocksLight()) continue;

                int neighborLight = world.getLightingSystem().getSkylight(nx, ny, nz);
                int newLight = light - 1;

                if (newLight > neighborLight) {
                    world.getLightingSystem().setSkylight(world.getChunkAt(nx, ny, nz, GenerationBarrier.PRE_LIGHTING), nx, ny, nz, newLight);
                    queue.add(new BlockVec(nx, ny, nz));
                }
            }
        }
    }

    private int startPropogatingSky(int wx, int wy, int wz) {
        int sky;
        BlockState state = getChunkAt(wx, wy, wz).get(BlockVec.localize(wx), BlockVec.localize(wy), BlockVec.localize(wz));
        int lightReduction = Math.max(state.getLightReduction(), 1);

        propogateSkyLight(wx, wy, wz, 15 - lightReduction);
        sky = 15 - lightReduction;
        return sky;
    }

    private void propogateSkyLight(int startX, int startY, int startZ, int sky) {
        Deque<int[]> queue = new ArrayDeque<>();
        queue.addLast(new int[]{startX, startY, startZ, sky});

        while (!queue.isEmpty()) {
            int[] current = queue.pollFirst();
            int x = current[0];
            int y = current[1];
            int z = current[2];
            int cs = current[3];

            // Clamp to non-negative
            if (cs <= 0) continue;

            // Current light
            int existing = getSkylight(x, y, z);

            // Mix colors by taking max of each channel
            int newSky = Math.max(cs, existing);

            // If mixed light is same as existing, skip
            if (newSky == existing) continue;

            // Set the mixed light
            ServerChunk chunk = getChunkAt(x, y, z);
            setSkylight(chunk, BlockVec.localize(x), BlockVec.localize(y), BlockVec.localize(z), newSky);

            // Spread to neighbors
            int[][] dirs = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
            BlockState state = get(x, y, z);
            int reduction = max(state.getLightReduction(), 1);

            for (int[] dir : dirs) {
                int nx = x + dir[0];
                int ny = y + dir[1];
                int nz = z + dir[2];
                queue.addLast(new int[]{
                        nx, ny, nz,
                        Math.max(0, cs - reduction)
                });
            }
        }
    }

    /* ---------------- BLOCK LIGHTING ---------------- */

    public void addLightSource(int x, int y, int z, int r, int g, int b) {
        floodfillRGB(x, y, z, r, g, b);
    }

    public void removeLightSource(int x, int y, int z) {
        reverseFloodfillRGB(x, y, z);
    }

    private void floodfillRGB(int startX, int startY, int startZ, int r, int g, int b) {
        Deque<int[]> queue = new ArrayDeque<>();
        queue.addLast(new int[]{startX, startY, startZ, r, g, b});

        while (!queue.isEmpty()) {
            int[] current = queue.pollFirst();
            int x = current[0];
            int y = current[1];
            int z = current[2];
            int cr = current[3];
            int cg = current[4];
            int cb = current[5];

            // Clamp to non-negative
            if (cr <= 0 && cg <= 0 && cb <= 0) continue;

            // Current light
            int[] existing = getRGB(x, y, z);
            if (existing == null) continue;

            // Mix colors by taking max of each channel
            int newR = Math.max(cr, existing[0]);
            int newG = Math.max(cg, existing[1]);
            int newB = Math.max(cb, existing[2]);

            // If mixed light is same as existing, skip
            if (newR == existing[0] && newG == existing[1] && newB == existing[2]) continue;

            // Set the mixed light
            setRGB(x, y, z, newR, newG, newB);

            // Spread to neighbors
            int[][] dirs = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
            BlockState state = get(x, y, z);
            int reduction = max(state.getLightReduction(), 1);

            for (int[] dir : dirs) {
                int nx = x + dir[0];
                int ny = y + dir[1];
                int nz = z + dir[2];
                queue.addLast(new int[]{
                        nx, ny, nz,
                        Math.max(0, cr - reduction),
                        Math.max(0, cg - reduction),
                        Math.max(0, cb - reduction)
                });
            }
        }
    }

    private void reverseFloodfillRGB(int x, int y, int z) {
        Deque<int[]> queue = new ArrayDeque<>();
        int[] original = getRGB(x, y, z);
        if (original == null) return;

        setRGB(x, y, z, 0, 0, 0);
        queue.addLast(new int[]{x, y, z});

        Deque<int[]> toAdd = new ArrayDeque<>();

        while (!queue.isEmpty()) {
            int[] current = queue.pollFirst();
            int cx = current[0], cy = current[1], cz = current[2];

            int[][] dirs = {{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};
            for (int[] dir : dirs) {
                int nx = cx + dir[0], ny = cy + dir[1], nz = cz + dir[2];
                int[] neighbor = getRGB(nx, ny, nz);
                if (neighbor != null && (neighbor[0] > 0 || neighbor[1] > 0 || neighbor[2] > 0)) {
                    setRGB(nx, ny, nz, 0, 0, 0);
                    queue.addLast(new int[]{nx, ny, nz});
                }
                if (nx == x && ny == y && nz == z) continue;
                int lightEmission = world.get(nx, ny, nz).getLightEmission();
                if (lightEmission > 0) {
                    toAdd.addLast(new int[]{nx, ny, nz, lightEmission >> 16 & 0xFF, lightEmission >> 8 & 0xFF, lightEmission & 0xFF});
                }
            }
        }

        for (int[] toAddLight : toAdd) {
            floodfillRGB(toAddLight[0], toAddLight[1], toAddLight[2], toAddLight[3], toAddLight[4], toAddLight[5]);
        }
    }

    /* ---------------- SKY LIGHTING ---------------- */

    private int getSkylight(int x, int y, int z) {
        return getChunkAt(x, y, z).getLightMap().getSky(BlockVec.localize(x), BlockVec.localize(y), BlockVec.localize(z));
    }

    private void setSkylight(ServerChunk chunk, int x, int y, int z, int s) {
        chunk.getLightMap().setSky(x, y, z, s);
    }

    /* ---------------- HELPERS ---------------- */

    private int[] getRGB(int x, int y, int z) {
        ServerChunk c = getChunkAt(x, y, z);
        if (c == null) return null;
        var map = c.getLightMap();
        return new int[]{
                map.getRed(BlockVec.localize(x), BlockVec.localize(y), BlockVec.localize(z)),
                map.getGreen(BlockVec.localize(x), BlockVec.localize(y), BlockVec.localize(z)),
                map.getBlue(BlockVec.localize(x), BlockVec.localize(y), BlockVec.localize(z))
        };
    }

    private void setRGB(int x, int y, int z, int r, int g, int b) {
        ServerChunk c = getChunkAt(x, y, z);
        if (c == null) return;
        c.getLightMap().set(BlockVec.localize(x), BlockVec.localize(y), BlockVec.localize(z), r, g, b);
    }

    private ServerChunk getChunkAt(int x, int y, int z) {
        return world.getChunkManager().getChunkAt(x, y, z, GenerationBarrier.PRE_LIGHTING);
    }

    private BlockState get(int x, int y, int z) {
        return world.get(x, y, z);
    }
}
