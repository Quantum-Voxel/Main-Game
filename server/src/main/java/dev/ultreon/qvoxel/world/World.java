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

package dev.ultreon.qvoxel.world;

import dev.ultreon.qvoxel.block.BlockCollision;
import dev.ultreon.qvoxel.block.BoundingBox;
import dev.ultreon.qvoxel.block.state.*;
import dev.ultreon.qvoxel.entity.Entity;
import dev.ultreon.qvoxel.featureflags.FeatureSet;
import dev.ultreon.qvoxel.fluid.FluidState;
import dev.ultreon.qvoxel.fluid.Fluids;
import dev.ultreon.qvoxel.resource.GameObject;
import dev.ultreon.qvoxel.util.*;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an abstract world structure in the game. This class is the base for
 * implementations of game worlds and provides methods for interacting with blocks,
 * chunks, fluid states, entities, and environmental aspects of the world.
 */
public abstract class World extends GameObject {
    public static final int CHUNK_SIZE = 32;
    public static final int CHUNK_SURFACE = CHUNK_SIZE * CHUNK_SIZE;
    public static final int CHUNK_VOLUME = CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE;
    public static final int REGION_SIZE = 32;
    public static final int REGION_SURFACE = REGION_SIZE * REGION_SIZE;
    public static final int REGION_VOLUME = REGION_SIZE * REGION_SIZE * REGION_SIZE;
    public static final int SEA_LEVEL = 63;
    public static final int DAY_TIME = 24000;
    private final WorldCollider worldCollider = new WorldCollider(this);
    private final RNG rng = new JavaRNG();

    public abstract BlockState get(int x, int y, int z);

    public abstract boolean set(int x, int y, int z, BlockState state, @MagicConstant(flagsFromClass = BlockFlags.class) int flags);

    public abstract Chunk getChunk(int x, int y, int z);

    public abstract Chunk getChunkAt(int x, int y, int z);

    public Chunk getChunk(ChunkVec pos) {
        return getChunk(pos.x, pos.y, pos.z);
    }

    public Chunk getChunkAt(BlockVec pos) {
        return getChunkAt(pos.x, pos.y, pos.z);
    }

    public float getRenderDistanceSquared() {
        return getRenderDistance() * getRenderDistance();
    }

    public abstract float getRenderDistance();

    public WorldCollider getWorldCollider() {
        return worldCollider;
    }

    /**
     * Collision detection against blocks.
     *
     * @param box          The bounding box of an entity.
     * @param collideFluid If true, will check for fluid collision.
     * @return A list of bounding boxes that got collided with.
     */
    public List<BoundingBox> collide(BoundingBox box, boolean collideFluid) {
        List<BoundingBox> boxes = new ArrayList<>();
        int xMin = (int) Math.floor(box.min().x);
        int xMax = (int) Math.floor(box.max().x);
        int yMin = (int) Math.floor(box.min().y);
        int yMax = (int) Math.floor(box.max().y);
        int zMin = (int) Math.floor(box.min().z);
        int zMax = (int) Math.floor(box.max().z);

        for (int x = xMin; x <= xMax; x++) {
            for (int y = yMin; y <= yMax; y++) {
                for (int z = zMin; z <= zMax; z++) {
                    BlockState block = get(x, y, z);
                    if (block == null || (!block.hasCollision() || (collideFluid && !block.isFluid())) && !block.isClimbable())
                        continue;

                    for (BoundingBox local : block.getBoundingBoxes(this, new BlockVec(x, y, z))) {
                        BoundingBox blockBox = local.offset(x, block.hasBottomCollision() ? y - 1 : y, z);
                        if (blockBox.intersects(box)) {
                            blockBox.userData = new BlockCollision(block, new BlockVec(x, y, z), this);
                            boxes.add(blockBox);
                        }
                    }
                }
            }
        }

        return boxes;
    }

    public Difficulty getDifficulty() {
        return null;
    }

    /**
     * Cast a ray and return detailed hit info.
     */
    public HitResult castRay(@Nullable Entity from, Vector3d origin, Vector3d direction, float maxDist) {
        return BoundingBoxRaycaster.rayCast(from, this, origin, direction, maxDist);
    }

    private static double intBound(double s, double ds) {
        if (ds > 0) return (Math.ceil(s) - s) / ds;
        else if (ds < 0) return (s - Math.floor(s)) / -ds;
        else return Double.POSITIVE_INFINITY;
    }

    protected abstract @Nullable Integer readHeight(int x, int z, HeightmapType heightmapType);

    public abstract boolean isClientSide();

    public BlockState get(BlockVec pos) {
        return get(pos.x, pos.y, pos.z);
    }

    public boolean set(BlockVec blockVec, BlockState blockState) {
        return set(blockVec.x, blockVec.y, blockVec.z, blockState, BlockFlags.NOTIFY_CLIENTS | BlockFlags.NEIGHBOR_UPDATE);
    }

    public FluidState getFluidAt(int x, int y, int z) {
        BlockState blockState = get(x, y, z);
        if (blockState == null || !blockState.isFluid())
            return Fluids.EMPTY.getState(0);

        return blockState.getFluid();
    }

    public FluidState getFluidAt(BlockVec blockVec) {
        return getFluidAt(blockVec.x, blockVec.y, blockVec.z);
    }

    public float getSkyLight() {
        return 1f;
    }

    public abstract FeatureSet getFeatures();

    public Iterable<Entity> getEntities() {
        return Collections.emptyList();
    }

    public RNG getRNG() {
        return rng;
    }

    public void destroyBlock(int x, int y, int z) {

    }
}
