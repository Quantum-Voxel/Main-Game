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

package dev.ultreon.qvoxel.entity;

import dev.ultreon.qvoxel.block.BlockCollision;
import dev.ultreon.qvoxel.block.BoundingBox;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.resource.GameObject;
import dev.ultreon.qvoxel.util.AABBUtils;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.util.UboUtils;
import dev.ultreon.qvoxel.world.Axis;
import dev.ultreon.qvoxel.world.HitResult;
import dev.ultreon.qvoxel.world.World;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.List;
import java.util.UUID;

public abstract class Entity extends GameObject {
    private int id;
    public float yawBody;
    public float yawHead;
    public float pitchHead;
    protected double jumpStrength = 0.42;
    private World world;
    private EntitySize entitySize = new EntitySize(0.7F, 1.8F);
    public float gravity = 0.08F;
    public float drag = 0.98F;
    public float friction = 0.6F;
    protected final Vector3d oldDelta = new Vector3d();
    public final Vector3d velocity = new Vector3d();

    private boolean isCollidingX;
    private boolean isCollidingY;
    private boolean isCollidingZ;
    private boolean isColliding;
    protected boolean onGround;
    private boolean wasOnGround;
    protected float fallDistance;
    private boolean wasInFluid;
    private UUID uuid;
    protected boolean swimUp;
    protected boolean noGravity;
    protected final BlockVec tmpBV = new BlockVec();

    public Entity(World world) {
        this.world = world;
    }

    public void onSpawn() {
        oldPosition.set(position);
        velocity.set(0, 0, 0);
        onTeleport(position.x, position.y, position.z);
    }

    /**
     * Moves the entity upward if affected by fluid.
     */
    protected void swimUp() {
        // If affected by fluid, swim up
        if (isAffectedByFluid()) {
            swimUp = true;
        }

        // Check if the entity was previously in fluid
        if (!wasInFluid && isAffectedByFluid()) {
            wasInFluid = true;
        }
    }

    public void teleport(Vector3d position) {
        oldPosition.set(position);
        this.position.set(position);
        velocity.set(0, 0, 0);
        onTeleport(position.x, position.y, position.z);
    }

    public void teleport(double x, double y, double z) {
        oldPosition.set(x, y, z);
        position.set(x, y, z);
        velocity.set(0, 0, 0);
        onTeleport(x, y, z);
    }

    public EntitySize getEntitySize() {
        return entitySize;
    }

    public void setEntitySize(EntitySize entitySize) {
        this.entitySize = entitySize;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Vector3d getPosition() {
        return position;
    }

    public void setPosition(Vector3d position) {
        this.position.set(position);
    }

    public Vector3d getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector3d velocity) {
        this.velocity.set(velocity);
    }

    public void setVelocity(double x, double y, double z) {
        velocity.set(x, y, z);
    }

    public float getYawBody() {
        return yawBody;
    }

    public void setYawBody(float yawBody) {
        this.yawBody = yawBody;
    }

    public float getPitchHead() {
        return pitchHead;
    }

    public void setPitchHead(float pitchHead) {
        this.pitchHead = pitchHead;
    }

    public float getYawHead() {
        return yawHead;
    }

    public void setYawHead(float yawHead) {
        this.yawHead = yawHead;
    }

    public void tick() {
        oldDelta.set(velocity);

        // Apply gravity if not in the noGravity state
        boolean affectedByGravity = isAffectedByGravity();
        if (affectedByGravity) {
            // If affected by fluid and swimming up, stop swimming up
            boolean affectedByFluid = isAffectedByFluid();
            if (affectedByFluid && swimUp) {
                swimUp = false;
            } else if (affectedByFluid) {
                velocity.y -= gravity / 3;
                velocity.x *= 0.85f;
                velocity.y *= 0.85f;
                velocity.z *= 0.85f;
            }
            fallbackVelocity(true);
        } else fallbackVelocity(false);
    }

    private void fallbackVelocity(boolean affectedByGravity) {
        move(world, velocity.x, velocity.y, velocity.z, true);

        if (onGround) {
            velocity.y = 0; // Cancel falling
        }

        // Update the entity's velocity based on gravity
        if (affectedByGravity) {
            velocity.y = (velocity.y - gravity) * drag;
        } else {
            velocity.y *= friction;
        }

        velocity.x *= friction;
        velocity.z *= friction;
    }

    public BoundingBox getBoundingBox() {
        float w = entitySize.width();
        float h = entitySize.height();
        Vector3d pos = getPosition();
        return new BoundingBox(
                pos.x - w / 2, pos.y, pos.z - w / 2,
                pos.x + w / 2, pos.y + h, pos.z + w / 2
        );
    }

    @Override
    protected boolean moveWithCollision(@NotNull World world, double dx, double dy, double dz, boolean validate) {
        double oldDx = dx;
        double oldDy = dy;
        double oldDz = dz;

        // Update the bounding box based on the modified deltas
        BoundingBox ext = getBoundingBox().updateByDelta(dx, dy, dz);

        // Get a list of bounding boxes the entity collides with
        List<BoundingBox> boxes = world.collide(ext, false);

        BoundingBox motionBox = getBoundingBox(); // Get the entity's bounding box

        isColliding = false;

        isCollidingX = false;

        double heightToStep = 0;

        // Check collision and update x-coordinate
        for (BoundingBox box : boxes) {
            double dx2 = AABBUtils.clipXCollide(box, motionBox, dx);
            boolean colliding = dx != dx2;
            Object userData = box.userData;
            if (colliding && userData instanceof BlockCollision collision) {
                if (collision.block().onTouch(this, collision, box, Axis.X, Math.abs(dx))) {
                    continue;
                }
                if (collision.block().isClimbable()) dy += getClimbSpeed();
            }
            heightToStep = Math.max(heightToStep, box.max().y - motionBox.min().y);
            isColliding |= colliding;
            isCollidingX |= colliding;
            dx = dx2;
        }

        // Update the x-coordinate of the bounding box
        motionBox.min().add(dx, 0.0f, 0.0f);
        motionBox.max().add(dx, 0.0f, 0.0f);
        motionBox.update();

        isCollidingZ = false;

        // Check collision and update z-coordinate
        for (BoundingBox box : boxes) {
            double dz2 = AABBUtils.clipZCollide(box, motionBox, dz);
            boolean colliding = dz != dz2;
            Object userData = box.userData;
            if (colliding && userData instanceof BlockCollision collision) {
                if (collision.block().onTouch(this, collision, box, Axis.Z, Math.abs(dz))) {
                    continue;
                }
                if (collision.block().isClimbable()) dy += getClimbSpeed();
            }
            heightToStep = Math.max(heightToStep, box.max().y - motionBox.min().y);
            isColliding |= colliding;
            isCollidingZ |= colliding;
            dz = dz2;
        }

        // Update the x-coordinate of the bounding box
        motionBox.min().add(0.0f, 0.0f, dz);
        motionBox.max().add(0.0f, 0.0f, dz);
        motionBox.update();

        isCollidingY = false;

        dy = collideY(dy, boxes, motionBox);

        postCollide(dx, dy, dz, oldDx, oldDy, oldDz, motionBox, heightToStep, validate);
        return isColliding;
    }

    private double getClimbSpeed() {
        return 0.1f;
    }

    private void postCollide(double dx, double dy, double dz, double oldDx, double oldDy, double oldDz, BoundingBox motionBox, double heightToStep, boolean validate) {
        // Check if the entity is on the ground
        wasOnGround = onGround;
        onGround = oldDy != dy && oldDy < 0.0f;

        if (heightToStep > 0 && heightToStep < 0.6 && onGround) {
            step(heightToStep, motionBox);
            return;
        }

        // Reset velocity if there was a collision in x-coordinate
        if (oldDx != dx) {
            velocity.x = 0.0f;
        }

        // Reset fall distance if the entity is moving upwards
        if (dy >= 0) {
            fallDistance = 0.0F;
        }

        dy = falling(dy);

        // Reset velocity if there was a collision in z-coordinate
        if (oldDz != dz) {
            velocity.z = 0.0f;
        }

        boolean invalidMotionBox = accuracy(position.x + dx, (motionBox.min().x + motionBox.max().x) / 2.0f, 1f)
                || accuracy(position.z + dz, (motionBox.min().z + motionBox.max().z) / 2.0f, 1.5f)
                || accuracy(position.y + dy, motionBox.min().y, 1f);


        if (validate) {
            position.x = (motionBox.min().x + motionBox.max().x) / 2.0f;
            position.y = motionBox.min().y;
            position.z = (motionBox.min().z + motionBox.max().z) / 2.0f;
        } else {
            // Update entity's relativePos
            position.x += dx;
            position.y += dy;
            position.z += dz;
        }
        if (invalidMotionBox && !noClip)
            onInvalidMotionBox();
    }

    private boolean accuracy(double a, double b, float accuracy) {
        return a < b ? a - accuracy >= b && a + accuracy <= b : b - accuracy >= a && b + accuracy <= a;
    }

    protected void onInvalidMotionBox() {

    }

    private double collideY(double dy, List<BoundingBox> boxes, BoundingBox motionBox) {
        // Check collision and update y-coordinate
        for (BoundingBox box : boxes) {
            double dy2 = AABBUtils.clipYCollide(box, motionBox, dy);
            boolean colliding = dy != dy2;
            Object userData = box.userData;
            if (colliding && userData instanceof BlockCollision collision) {
                BlockState block = collision.block();
                if (dy < 0) {
                    block.onWalkOn(this, collision, box, -dy);
                } else if (block.hasBottomCollision() || (block.isClimbable() && !block.hasCollision())) {
                    continue;
                }
                if (block.onTouch(this, collision, box, Axis.Y, Math.abs(dy)))
                    continue;
            }
            isColliding |= colliding;
            isCollidingY |= colliding;
            dy = dy2;
        }

        // Update the y-coordinate of the bounding box
        motionBox.min().add(0.0f, dy, 0.0f);
        motionBox.max().add(0.0f, dy, 0.0f);
        motionBox.update();

        return dy;
    }

    private void step(double heightToStep, BoundingBox motionBox) {
        velocity.y = 0;
        isCollidingY = true;
        isColliding = true;
        isCollidingX = false;
        isCollidingZ = false;
        fallDistance = 0.0F;
        wasOnGround = onGround;
        onGround = true;

        falling(0);

        // Update entity's relativePos
        position.x = (motionBox.min().x + motionBox.max().x) / 2.0f;
        position.y += heightToStep;
        position.z = (motionBox.min().z + motionBox.max().z) / 2.0f;
    }

    private double falling(double dy) {
        // Handle collision responses and update fall distance
        if (isAffectedByFluid()) {
            wasInFluid = true;
            fallDistance = 0;
            dy = 0.0f;
        } else if (wasInFluid && !isAffectedByFluid()) {
            wasInFluid = false;
            fallDistance = 0;
            dy = 0.0f;
        } else if (onGround && !wasOnGround) {
            hitGround(fallDistance);
            fallDistance = 0.0F;
            velocity.y = 0.0f;
            dy = 0.0f;
        } else if (dy < 0) {
            fallDistance += org.joml.Math.abs((float) dy);
        }
        return dy;
    }

    protected void hitGround(float fallDistance) {

    }

    protected boolean isAffectedByFluid() {
        return !world.getFluidAt((int) Math.floor(position.x), (int) Math.floor(position.y), (int) Math.floor(position.z)).isEmpty();
    }

    public boolean isColliding() {
        return isColliding;
    }

    public boolean isCollidingX() {
        return isCollidingX;
    }

    public boolean isCollidingY() {
        return isCollidingY;
    }

    public boolean isCollidingZ() {
        return isCollidingZ;
    }

    public float getFallDistance() {
        return fallDistance;
    }

    public float getEyeHeight() {
        return entitySize.height() * 0.85F;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public @Nullable World getWorld() {
        return world;
    }

    public Vector3d getForward() {
        Vector3d forward = new Vector3d(0, 0, 1);
        forward.rotateY(Math.toRadians(yawHead));
        return forward;
    }

    public void setWorld(World world) {
        if (world == null) throw new IllegalArgumentException("world cannot be null");
        this.world = world;
    }

    public MapType save() {
        MapType map = new MapType();
        UboUtils.putVector3d(map, "relativePos", position);
        UboUtils.putVector3d(map, "velocity", velocity);
        map.putFloat("yawBody", yawBody);
        map.putFloat("yaw", yawHead);
        map.putFloat("pitch", pitchHead);
        map.putFloat("fallDistance", fallDistance);
        map.putBoolean("noClip", isNoClip());
        map.putFloat("gravity", gravity);
        map.putFloat("drag", drag);
        map.putFloat("friction", friction);
        return map;
    }

    public void load(MapType map) {
        UboUtils.getVector3d(map, "relativePos", position);
        UboUtils.getVector3d(map, "velocity", velocity);
        yawBody = map.getFloat("yawBody");
        yawHead = map.getFloat("yaw");
        pitchHead = map.getFloat("pitch");
        fallDistance = map.getFloat("fallDistance");
        setNoClip(map.getBoolean("noClip"));
        gravity = map.getFloat("gravity");
        drag = map.getFloat("drag");
        friction = map.getFloat("friction");
    }

    public void onTeleport(double x, double y, double z) {

    }

    public HitResult castRay(float distance) {
        Vector3d direction = new Vector3d(0, 0, -1)
                .rotateX(Math.toRadians(-pitchHead))
                .rotateY(Math.toRadians(-yawHead))
                .normalize();

        return world.castRay(this, new Vector3d(position.x, position.y + getEyeHeight(), position.z), direction, distance);
    }

    public BlockState getBuriedBlock() {
        BlockVec blockVec = tmpBV.set((int) org.joml.Math.floor(position.x), (int) org.joml.Math.floor(position.y + getEyeHeight()), (int) org.joml.Math.floor(position.z));;
        if (world == null)
            return null;
        return getWorld().get(blockVec.x, blockVec.y, blockVec.z);
    }

    public BlockVec getBlockVec(BlockVec tmpBV) {
        return tmpBV.set((int) org.joml.Math.floor(position.x), (int) org.joml.Math.floor(position.y), (int) org.joml.Math.floor(position.z));
    }

    public boolean isAffectedByGravity() {
        BlockState blockState = getWorld().get(getBlockVec(tmpBV));
        return !noGravity && !blockState.isClimbable();
    }

    public Vector3d getPosition(float partialTicks) {
        return new Vector3d(oldPosition).lerp(position, partialTicks);
    }
}
