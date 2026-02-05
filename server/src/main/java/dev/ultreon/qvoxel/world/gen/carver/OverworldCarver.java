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

package dev.ultreon.qvoxel.world.gen.carver;

import de.articdive.jnoise.core.api.pipeline.NoiseSource;
import dev.ultreon.qvoxel.block.Blocks;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.world.BuilderChunk;
import dev.ultreon.qvoxel.world.Heightmap;
import dev.ultreon.qvoxel.world.HeightmapType;
import dev.ultreon.qvoxel.world.World;
import dev.ultreon.qvoxel.world.gen.CanyonNoise;
import dev.ultreon.qvoxel.world.gen.HillinessNoise;
import dev.ultreon.qvoxel.world.gen.OceanicNoise;
import dev.ultreon.qvoxel.world.gen.noise.DerivativeTunnelClosingCaveCarver;
import dev.ultreon.qvoxel.world.gen.noise.DomainWarping;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongLists;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;

/**
 * The OverworldCarver class is responsible for carving terrain within a chunk.
 * It uses various noise sources and domain warping to determine terrain features like caves and surface height.
 */
public class OverworldCarver implements Carver, NoiseSource {
    private final @NotNull BlockState stoneState = Blocks.STONE.getDefaultState();
    private final NoiseSource worldNoise;

    public static long totalDurations = 0L;
    public static LongList durations = LongLists.synchronize(new LongArrayList());
    private final HillinessNoise hillinessNoise;
    private final CanyonNoise canyonNoise;
    private final OceanicNoise oceanicNoise;
    private final DomainWarping worldDomainWarp;
    private final DerivativeTunnelClosingCaveCarver cave = new DerivativeTunnelClosingCaveCarver();

    public OverworldCarver(NoiseSource terrainNoise, long seed, NoiseSource temperatureNoise) {
        worldNoise = terrainNoise;
        worldDomainWarp = new DomainWarping(seed + 330, 8, 8);

        hillinessNoise = new HillinessNoise(seed + 230, 3.1, 128.0);
        canyonNoise = new CanyonNoise(seed + 270, 1.0, 128.0, temperatureNoise, this);
        oceanicNoise = new OceanicNoise(seed + 300, 3.1, 48);
    }

    @Override
    public double carve(BuilderChunk chunk, int x, int z) {
        long start = System.currentTimeMillis();
        BlockVec offset = chunk.blockStart;

        // Use world coordinates consistently for noise sampling
        x = offset.x + x;
        z = offset.z + z;
        double worldX = x;
        double worldZ = z;

        Vector2d warped = worldDomainWarp.generateDomainOffset(worldX, worldZ);
        worldX += warped.x;
        worldZ += warped.y;
        double groundPos = evaluateNoise(worldX, worldZ);

        // Write to the heightmaps (local indices are correct here)
        int localX = BlockVec.localize(x);
        int localZ = BlockVec.localize(z);

        int pos = (int) groundPos;
        Heightmap terrain = chunk.getWorld().heightMapAt(x, z, HeightmapType.TERRAIN);
        Heightmap oceanFloor = chunk.getWorld().heightMapAt(x, z, HeightmapType.OCEAN_FLOOR);
        Heightmap surfaceHeight = chunk.getWorld().heightMapAt(x, z, HeightmapType.WORLD_SURFACE);
        Heightmap motionHeight = chunk.getWorld().heightMapAt(x, z, HeightmapType.MOTION_BLOCKING);
        Heightmap motionNoLeaves = chunk.getWorld().heightMapAt(x, z, HeightmapType.MOTION_BLOCKING_NO_LEAVES);
        Heightmap lightHeight = chunk.getWorld().heightMapAt(x, z, HeightmapType.LIGHT_BLOCKING);
        terrain.set(localX, localZ, pos);
        oceanFloor.set(localX, localZ, pos);
        surfaceHeight.set(localX, localZ, Math.max(pos, World.SEA_LEVEL));
        motionHeight.set(localX, localZ, Math.max(pos, World.SEA_LEVEL));
        motionNoLeaves.set(localX, localZ, Math.max(pos, World.SEA_LEVEL));
        lightHeight.set(localX, localZ, Math.max(pos, World.SEA_LEVEL));

        // Carve the world into shape.
        for (int y = offset.y + World.CHUNK_SIZE - 1; y >= offset.y; y--) {
            int localY = BlockVec.localize(y);
            if (y <= groundPos) {
                if (y <= World.SEA_LEVEL) {
                    if (y < groundPos - 7) {
                        chunk.set(localX, localY, localZ, stoneState);
                    } else {
                        chunk.set(localX, localY, localZ, stoneState);
                    }
                } else {
                    chunk.set(localX, localY, localZ, stoneState);
                }
            } else if (y <= World.SEA_LEVEL) {
                if (oceanFloor.get(localX, localZ) == y) oceanFloor.set(localX, localZ, (short) (y - 1));
                chunk.set(localX, localY, localZ, Blocks.WATER.getDefaultState());
            }
        }

        long end = System.currentTimeMillis();
        long duration = end - start;

        synchronized (this) {
            if (durations.size() > 100) {
                Long l = durations.removeFirst();
                totalDurations -= l;
            }
            totalDurations += duration;
            durations.add(duration);
        }

        return groundPos;
    }

    @Override
    public boolean isAir(int x, int y, int z) {
        // This method appears to be called with world coordinates; keep it consistent.
        double hilliness = hillinessNoise.evaluateNoise(x, z) - 2.0f;
        int groundPos = (int) Math.floor((evaluateNoise(x, z) - 64) * (hilliness / 4.0f + 0.5f) + 64);
        return y > World.SEA_LEVEL;
    }

    @Override
    public double evaluateNoise(double x) {
        throw new UnsupportedOperationException("Requires 2D noise");
    }

    @Override
    public double evaluateNoise(double x, double y) {
        // This method appears to be called with world coordinates; keep it consistent.
        double hilliness = hillinessNoise.evaluateNoise(x, y) - 2.0f;
        double oceanicness = oceanicNoise.evaluateNoise(x, y);
        double height = worldNoise.evaluateNoise(x, y); // now expects world coords from callers;
        return (Math.max((float) height, 1f) - 64) * (hilliness / 4.0f + 0.5f) + 64 - oceanicness;
    }

    @Override
    public double evaluateNoise(double x, double y, double z) {
        throw new UnsupportedOperationException("Requires 2D noise");
    }

    @Override
    public double evaluateNoise(double x, double y, double z, double w) {
        throw new UnsupportedOperationException("Requires 2D noise");
    }
}
