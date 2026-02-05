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

package dev.ultreon.qvoxel.client.world;

import dev.ultreon.qvoxel.block.Blocks;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.client.Keyboard;
import dev.ultreon.qvoxel.client.PlayerViewMode;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.framebuffer.Framebuffer;
import dev.ultreon.qvoxel.client.gui.ChatMessage;
import dev.ultreon.qvoxel.client.gui.Overlays;
import dev.ultreon.qvoxel.client.gui.screen.DeathScreen;
import dev.ultreon.qvoxel.client.gui.screen.InventoryScreen;
import dev.ultreon.qvoxel.client.input.Mouse;
import dev.ultreon.qvoxel.client.network.ClientConnection;
import dev.ultreon.qvoxel.client.texture.TextureFormat;
import dev.ultreon.qvoxel.fluid.Fluids;
import dev.ultreon.qvoxel.item.UseResult;
import dev.ultreon.qvoxel.menu.Inventory;
import dev.ultreon.qvoxel.menu.ItemSlot;
import dev.ultreon.qvoxel.network.packets.c2s.*;
import dev.ultreon.qvoxel.player.PlayerAbilities;
import dev.ultreon.qvoxel.player.PlayerEntity;
import dev.ultreon.qvoxel.registry.RegistryKey;
import dev.ultreon.qvoxel.server.GameMode;
import dev.ultreon.qvoxel.util.BlockFlags;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.world.BlockHitResult;
import dev.ultreon.qvoxel.world.DimensionInfo;
import dev.ultreon.qvoxel.world.HitResult;
import dev.ultreon.qvoxel.world.World;
import org.joml.Math;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector3f;

import static org.joml.Math.cos;
import static org.joml.Math.sin;

public class ClientPlayerEntity extends PlayerEntity {
    public boolean forward, backward, left, right, jumping, down;
    public float runModifier = 1.3f;
    public boolean running = false;
    private final Vector3f tmpV3f = new Vector3f();
    private final Vector3d tmpV3d1 = new Vector3d();
    private final Vector3d tmpV3d2 = new Vector3d();
    public double walkDistance = 0.0;
    public double prevWalkDistance = 0.0;
    private final QuantumClient client = QuantumClient.get();
    public Keyboard keyboard;
    public Mouse mouse;
    public ClientConnection connection;
    private Framebuffer playerView;
    public int playerViewX;
    public int playerViewY;
    private RegistryKey<DimensionInfo> dimension;
    private boolean online = true;
    private double selectedFloat = 0.0f;
    public float horizSpeed;
    private int onGroundTicks;
    private final BlockVec tmpBV = new BlockVec();
    private boolean wasInWater;
    private WorldRenderer worldRenderer;
    private double oldYaw;
    private double oldPitch;
    private PlayerViewMode viewMode = PlayerViewMode.FirstPerson;

    public ClientPlayerEntity(Framebuffer playerView, String username, World world) {
        super(world);
        this.username = username;
        this.playerView = playerView;
    }

    @Override
    public void setHealth(float health) {
        float oldHealth = getHealth();

        super.setHealth(health);

        if (health < oldHealth) {
            regenFlashTimer = 5;
        } else if (health > oldHealth) {
            regenFlashTimer = 10;
        }
    }

    @Override
    public void onDeath() {
        super.onDeath();
        client.showScreen(new DeathScreen(this));
    }

    @Override
    public void sendMessage(String message) {
        Overlays.CHAT.addMessage(ChatMessage.system(message));
    }

    @Override
    public void sendAbilitiesPacket(PlayerAbilities playerAbilities) {
        connection.send(new C2SPlayerAbilitiesPacket(playerAbilities));
    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
    }

    public Vector3d getPosition(Vector3d position, float partialTicks) {
        position.set(oldPosition).lerp(this.position, partialTicks);
        return position;
    }

    public void breakBlock() {
        if (dimension == null) return;
        HitResult hitResult = castRay(6.0F);
        if (!(hitResult instanceof BlockHitResult blockHitResult)) return;
        BlockVec blockVec = blockHitResult.hitBlock();
        getWorld().set(blockVec.x, blockVec.y, blockVec.z, Blocks.AIR.getDefaultState(), BlockFlags.NONE);
        connection.send(new C2SBlockBreakPacket(blockVec.x, blockVec.y, blockVec.z));
    }

    // returns difference in range [-180, 180)
    public static float shortestDelta(float from, float to) {
        float diff = normalize360(to) - normalize360(from);
        if (diff >= 180f) diff -= 360f;
        if (diff < -180f) diff += 360f;
        return diff;
    }

    public static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    // Return true if angle is within [min..max] on a circle, where min/max are normalized 0..360.
// If min <= max it is the simple interval; if min > max it wraps across 0.
    public static boolean angleInWindow(float angle, float min, float max) {
        angle = normalize360(angle);
        min = normalize360(min);
        max = normalize360(max);
        if (min <= max) {
            return angle >= min && angle <= max;
        } else {
            // wrapped: [min..360) U [0..max]
            return angle >= min || angle <= max;
        }
    }

    // Clamp angle to the circular window [min..max] (min/max normalized 0..360).
// If angle already inside window, returns it unchanged. Otherwise returns the nearer boundary.
    public static float clampAngleToWindow(float angle, float min, float max) {
        angle = normalize360(angle);
        min = normalize360(min);
        max = normalize360(max);

        if (angleInWindow(angle, min, max)) return angle;

        // compute circular distances to both boundaries and pick nearest
        float distToMin = Math.abs(shortestDelta(angle, min));
        float distToMax = Math.abs(shortestDelta(angle, max));
        return (distToMin <= distToMax) ? min : max;
    }

    /**
     * Step yawBody toward yawHead by at most maxStep degrees, but keep it within yawHead ± maxOffset.
     *
     * @param yawBody current body yaw (any float)
     * @param yawHead target head yaw (any float)
     * @param maxStep maximum step per tick (e.g. 1f)
     * @param maxOffset allowed offset from head (e.g. 40f)
     * @return new yawBody
     */
    public static float stepBodyTowardsHead(float yawBody, float yawHead, float maxStep, float maxOffset) {
        // 1) step toward head by up to maxStep (shortest direction)
        float delta = shortestDelta(yawBody, yawHead);
        float step = clamp(delta, -maxStep, maxStep);
        float candidate = normalize360(yawBody + step);

        // 2) compute allowed window around head
        float min = normalize360(yawHead - maxOffset);
        float max = normalize360(yawHead + maxOffset);

        // 3) clamp candidate to window if needed
        candidate = clampAngleToWindow(candidate, min, max);

        return candidate;
    }

    public void rotate(float yaw, float pitch) {
        this.yawHead += yaw;
        this.yawHead %= 360;
        this.yawBody = clampAngleToWindow(yawBody, yawHead - 40, yawHead + 40); // returns [-180..180]
        this.pitchHead = java.lang.Math.clamp(this.pitchHead + pitch, -90, 90);
    }
    /** Normalize to [0, 360) */
    public static float normalize360(float a) {
        a = a % 360f;
        if (a < 0) a += 360f;
        return a;
    }

    /** Step `current` toward `target` by up to `step` degrees (shortest path). */
    public static float stepAngle(float current, float target, float step) {
        current = normalize360(current);
        target  = normalize360(target);

        // signed shortest delta in range (-180, 180]
        float delta = normalize360(target - current);
        if (delta > 180f) delta -= 360f;

        // if within step, snap to target
        if (Math.abs(delta) <= step) return target;

        // move by step in direction of delta
        return normalize360(current + Math.signum(delta) * step);
    }

    public void tick() {
        super.tick();

        tmpV3f.set(0.0f, 0.0f, 0.0f);
        if (forward) {
            tmpV3f.add(0.0f, 0.0f, 1.0f);
            yawBody = stepBodyTowardsHead(yawBody, yawHead, 2, 40f);
        }
        if (backward) {
            tmpV3f.add(0.0f, 0.0f, -1.0f);
        }
        if (left) {
            tmpV3f.add(1.0f, 0.0f, 0.0f);
            yawBody = stepBodyTowardsHead(yawBody, yawHead - 40, 2, 80f);
        }
        if (right) {
            tmpV3f.add(-1.0f, 0.0f, 0.0f);
            yawBody = stepBodyTowardsHead(yawBody, yawHead + 40, 2, 80f);
        }

        float speed = getSpeed();
        boolean inWater = isInWater();
        if (!inWater && !onGround) speed *= 0.65f;
        boolean run = running && !inWater && online;
        float curSpeed = speed * (run ? runModifier : 1.0f);
        if (tmpV3f.lengthSquared() != 0.0f) {
            tmpV3f.normalize(curSpeed);
            tmpV3f.rotateY(Math.toRadians(-yawHead + 180));
        }

        velocity.add(tmpV3f);

        BlockVec feetBlockPos = getBlockVec(tmpBV);
        BlockState feetBlock = getWorld().get(feetBlockPos);
        if (isFlying()) {
            if (jumping) velocity.y = curSpeed * 1.5;
            if (down) velocity.y = -curSpeed * 1.5;
        } else if (inWater) {
            if (jumping) velocity.y += speed * 1.5;
            if (down) velocity.y += -speed * 1.5;
        } else if (feetBlock.isClimbable()) {
            float absX = tmpV3f.x;
            float absZ = tmpV3f.z;
            if (jumping || absX != 0.0f && absZ != 0.0f) velocity.y = curSpeed;
            else velocity.y = -curSpeed;
        } else if (jumping && onGround && onGroundTicks > 0 && !wasInWater) {
            onGroundTicks = 0;
            jump();
        }
        if (wasInWater && !inWater)
            velocity.y += 0.2;
        this.wasInWater = inWater;
        if (onGround) {
            onGroundTicks++;
        } else {
            onGroundTicks = 0;
        }

        prevWalkDistance = walkDistance;

        var horizSpeed = oldPosition.distance(position);
        walkDistance += horizSpeed * 4.0;
        this.horizSpeed = (float) horizSpeed;

        if (Vector2d.distance(yawHead, pitchHead, oldYaw, oldPitch) > 1) {
            oldYaw = yawHead;
            oldPitch = pitchHead;

            connection.send(new C2SRotatePacket(yawHead, pitchHead));
        }
    }

    public boolean isInWater() {
        return getWorld().getFluidAt((int) java.lang.Math.floor(position.x), (int) java.lang.Math.floor(position.y), (int) java.lang.Math.floor(position.z)).getFluid() == Fluids.WATER;
    }

    @Override
    protected void onMoved(double deltaX, double deltaY, double deltaZ) {
        super.onMoved(deltaX, deltaY, deltaZ);

        if (deltaX != 0.0 || deltaY != 0.0 || deltaZ != 0.0) {
            if (left && !right) {
                yawBody = stepBodyTowardsHead(yawBody, yawHead - 40, 2, 80f);
            } else if (right && !left) {
                yawBody = stepBodyTowardsHead(yawBody, yawHead + 40, 2, 80f);
            } else if (forward && !backward) {
                yawBody = stepBodyTowardsHead(yawBody, yawHead, 2, 40f);
            } else if (backward && !forward) {
                if (yawHead < yawBody)
                    yawBody = stepBodyTowardsHead(yawBody, yawHead + 40f, 2, 40f);
                else
                    yawBody = stepBodyTowardsHead(yawBody, yawHead - 40f, 2, 40f);
            }
            connection.send(new C2SMovePacket(yawHead, pitchHead, position.x, position.y, position.z));
        }
    }

    private void jump() {
        velocity.y = jumpStrength;
        velocity.x *= -sin(yawHead) * 0.3;
        velocity.z *= cos(yawHead) * 0.3;
    }

    public void setRotation(float yaw, float pitch) {
        this.yawHead = yaw;
        this.yawBody = yaw;
        this.pitchHead = pitch;
    }

    public double getJumpStrength() {
        return jumpStrength;
    }

    public void sendChatMessage(String message) {
        connection.send(new C2SChatMessagePacket(message));
    }

    public void setJumpStrength(double jumpStrength) {
        this.jumpStrength = jumpStrength;
    }

    public void setDimension(RegistryKey<DimensionInfo> dimension) {
        this.dimension = dimension;
    }

    public RegistryKey<DimensionInfo> getDimension() {
        return dimension;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void useItem() {
        int selected1 = selected;
        ItemSlot slot = inventory.get(selected1);
        if (slot == null) return;
        if (slot.isEmpty()) return;
        HitResult hitResult = castRay(6.0F);
        if (!(hitResult instanceof BlockHitResult blockHitResult)) return;
        UseResult use = slot.getItem().getItem().use(getWorld(), slot, blockHitResult, this);
        if (use == UseResult.SEND) {
            connection.send(new C2SUseItemPacket());
        }
    }

    public void scrollItem(double amount) {
        selectedFloat = ((selectedFloat + amount) % 9 + 9) % 9;
        int oldSelected = selected;
        selected = (int) selectedFloat;
        if (oldSelected != selected) {
            connection.send(new C2SItemSelectPacket(selected));
        }
    }

    public void selectItem(int index) {
        if (index < 0 || index >= 9) return;

        selectedFloat = index;
        int oldSelected = selected;
        selected = (int) selectedFloat;
        if (oldSelected != selected) {
            connection.send(new C2SItemSelectPacket(selected));
        }
    }

    public void setCrouching(boolean crouching) {
        if (this.crouching == crouching) return;

        this.crouching = crouching;
        connection.send(new C2SPlayerCrouchingPacket(crouching));
    }

    public void onGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public void resizePlayerView(int width, int height) {
        this.playerView.delete();
        this.playerView = new Framebuffer(width, height, TextureFormat.RGB8, true);
        worldRenderer.resize(width, height);
    }

    public Framebuffer getPlayerView() {
        return playerView;
    }

    public void setPlayerView(Framebuffer playerView) {
        this.playerView = playerView;
    }

    public WorldRenderer getWorldRenderer() {
        return worldRenderer;
    }

    public void setWorldRenderer(WorldRenderer worldRenderer) {
        this.worldRenderer = worldRenderer;
    }

    public void close() {
        worldRenderer.close();
        worldRenderer = null;
    }

    public boolean isInThirdPerson() {
        return viewMode != PlayerViewMode.FirstPerson;
    }

    public PlayerViewMode getViewMode() {
        return viewMode;
    }

    public void setViewMode(PlayerViewMode viewMode) {
        this.viewMode = viewMode;
    }

    public void openInventory() {
        connection.send(new C2SOpenInventoryPacket());
        super.openMenu(inventory);

        client.showScreen(new InventoryScreen(inventory, "Inventory"));
    }
}
