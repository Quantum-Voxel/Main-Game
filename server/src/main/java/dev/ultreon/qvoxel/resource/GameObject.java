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

package dev.ultreon.qvoxel.resource;

import dev.ultreon.qvoxel.block.BoundingBox;
import dev.ultreon.qvoxel.util.ChunkVec;
import dev.ultreon.qvoxel.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

/**
 * Represents an abstract game object within the game world with spatial and
 * transformation properties such as position, rotation, scale, and model matrix.
 * This class extends {@code GameNode} and provides mechanisms for movement,
 * transformation, and collision detection.
 */
public abstract class GameObject extends GameNode {
    public final Vector3f scale = new Vector3f(1);
    public final Quaternionf rotation = new Quaternionf();
    public final Vector3d position = new Vector3d();
    public final Matrix4f modelMatrix = new Matrix4f();
    public final Vector3d oldPosition = new Vector3d();

    protected boolean noClip;

    @Override
    public void update(Vector3d origin) {
        modelMatrix.identity();
        if (parent instanceof GameObject gameObject)
            modelMatrix.mul(gameObject.modelMatrix);

        modelMatrix.translate((float) (position.x - origin.x), (float) (position.y - origin.y), (float) (position.z - origin.z));
        modelMatrix.rotate(rotation);
        modelMatrix.scale(scale);

        super.update(origin);
    }

    public Vector3f getRotation(Vector3f vec) {
        return rotation.getEulerAnglesXYZ(vec);
    }

    public void setRotation(float x, float y, float z) {
        rotation.rotateXYZ(x, y, z);
    }

    public Vector3f getPosition(Vector3f vec) {
        return position.get(vec);
    }

    public void setPosition(double x, double y, double z) {
        position.set(x, y, z);
    }

    public Vector3f getScale(Vector3f vec) {
        return scale.get(vec);
    }

    public void setScale(float x, float y, float z) {
        scale.set(x, y, z);
    }

    public void setScale(float scale) {
        this.scale.set(scale);
    }

    public void setScale(Vector3f scale) {
        this.scale.set(scale);
    }


    /**
     * Moves the entity by the specified deltas.
     *
     * @param deltaX the change in x-coordinate
     * @param deltaY the change in y-coordinate
     * @param deltaZ the change in z-coordinate
     * @return true if the entity is colliding after the move, false otherwise
     */
    public boolean move(double deltaX, double deltaY, double deltaZ) {
        return move(deltaX, deltaY, deltaZ, true);
    }

    /**
     * Moves the entity by the specified deltas.
     *
     * @param deltaX   the change in x-coordinate
     * @param deltaY   the change in y-coordinate
     * @param deltaZ   the change in z-coordinate
     * @param validate
     * @return true if the entity is colliding after the move, false otherwise
     */
    public boolean move(double deltaX, double deltaY, double deltaZ, boolean validate) {
        return move(null, deltaX, deltaY, deltaZ, validate);
    }

    /**
     * Moves the entity by the specified deltas.
     *
     * @param deltaX the change in x-coordinate
     * @param deltaY the change in y-coordinate
     * @param deltaZ the change in z-coordinate
     * @return true if the entity is colliding after the move, false otherwise
     */
    public boolean move(@Nullable World world, double deltaX, double deltaY, double deltaZ, boolean validate) {
        oldPosition.set(position);

        // Move the entity based on the updated bounding box and deltas
        if (noClip || world == null) {
            position.x += deltaX;
            position.y += deltaY;
            position.z += deltaZ;
            onMoved(deltaX, deltaY, deltaZ);
            return false;
        }

        boolean collided = moveWithCollision(world, deltaX, deltaY, deltaZ, validate);
        onMoved(oldPosition.x - position.x, oldPosition.y - position.y, oldPosition.z - position.z);
        return collided;
    }

    public BoundingBox getBoundingBox() {
        return new BoundingBox(new Vector3d(position), new Vector3d(position));
    }

    protected void onUnloadedChunk(ChunkVec chunkVec) {

    }

    protected void onMoved(double deltaX, double deltaY, double deltaZ) {

    }

    /**
     * min.copy(), max.copy());
     * Moves the entity with collision detection and response.
     *
     * @param dx       Change in x-coordinate
     * @param dy       Change in y-coordinate
     * @param dz       Change in z-coordinate
     * @param validate Whether to validate the entity's bounding box after the move
     */
    protected boolean moveWithCollision(@NotNull World world, double dx, double dy, double dz, boolean validate) {
        return false;
    }

    public boolean isNoClip() {
        return noClip;
    }

    public void setNoClip(boolean noClip) {
        this.noClip = noClip;
    }
}
