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

package dev.ultreon.qvoxel.block;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.block.state.BlockStateDefinition;
import dev.ultreon.qvoxel.fluid.FluidState;
import dev.ultreon.qvoxel.fluid.Fluids;
import dev.ultreon.qvoxel.item.ToolType;
import dev.ultreon.qvoxel.loot.LootGenerator;
import dev.ultreon.qvoxel.player.PlayerEntity;
import dev.ultreon.qvoxel.registry.Registries;
import dev.ultreon.qvoxel.resource.GameObject;
import dev.ultreon.qvoxel.server.ServerWorld;
import dev.ultreon.qvoxel.sound.SoundEvent;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.world.Axis;
import dev.ultreon.qvoxel.world.CollisionType;
import dev.ultreon.qvoxel.world.World;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Block {
    public static final BoundingBox FULL = new BoundingBox(new Vector3d(0, 0, 0), new Vector3d(1, 1, 1));
    private final Settings settings;
    private BlockState defaultState;
    private final BlockStateDefinition definition;
    private final Map<BlockState, VoxelShape> voxelShapes = new HashMap<>();

    public Block(Settings settings) {
        this.settings = settings;

        BlockStateDefinition.Builder def = new BlockStateDefinition.Builder(this);
        defineState(def);
        definition = def.build();
        defaultState = definition.getStateByIndex(0);
    }

    public void setDefaultState(BlockState defaultState) {
        this.defaultState = defaultState;
    }

    public BlockStateDefinition getDefinition() {
        return definition;
    }

    public void setVoxelShape(BlockState state, VoxelShape shape) {
        voxelShapes.put(state, shape);
    }

    protected void defineState(BlockStateDefinition.Builder def) {

    }

    public Settings getSettings() {
        return settings;
    }

    public boolean isTransparent() {
        return settings.transparent;
    }

    public boolean hasCollision() {
        return settings.collision;
    }

    public boolean doesRender() {
        return true;
    }

    public void onPlace(ServerWorld serverWorld, BlockVec blockVec, BlockState blockState) {

    }

    public boolean isFluid() {
        return settings.fluid;
    }

    public boolean isAir() {
        return settings.air;
    }

    public float getHardness() {
        return settings.hardness;
    }

    public float getResistance() {
        return settings.resistance;
    }

    public int getLightLevel() {
        return settings.lightEmission;
    }

    public int getLightReduction() {
        return settings.lightReduction;
    }

    public BlockState getDefaultState() {
        return defaultState;
    }

    public BoundingBox getBoundingBox(BlockState blockState) {
        return FULL;
    }

    public boolean isReplaceable() {
        return settings.replaceable;
    }

    public boolean hasBottomCollision(BlockState blockState) {
        return settings.bottomCollision;
    }

    public boolean isClimbable(BlockState blockState) {
        return settings.climbable;
    }

    public @Nullable ToolType getEffectiveTool() {
        return null;
    }

    public @Nullable LootGenerator getLootGen(BlockState blockState) {
        return null;
    }

    public void update(World world, BlockVec offset, BlockState blockState) {

    }

    public boolean canBeReplacedBy(UseItemContext context, BlockState blockState) {
        return false;
    }

    public void onDestroy(World world, BlockVec breaking, BlockState blockState, @Nullable PlayerEntity breaker) {

    }

    /**
     * Returns the RGB color value representing the light level emitted by the block represented by this {@code BlockProperties}.
     *
     * @param blockState the {@code BlockProperties} to get the light level from
     * @return the RGB color value representing the light level emitted by the block
     */
    public int getLightEmission(BlockState blockState) {
        return settings.lightEmission;
    }

    /**
     * Retrieves the light reduction value of the block when a specified block state is provided.
     *
     * @param blockState the block state for which the light reduction is to be determined
     * @return the light reduction value of the block
     */
    public int getLightReduction(BlockState blockState) {
        return settings.lightReduction;
    }

    public void randomTick(World world, BlockVec position, BlockState blockState) {
        // do nothing by default
    }

    public List<BoundingBox> getBoundingBoxes(BlockState blockState, World world, BlockVec pos) {
        VoxelShape voxelShape = voxelShapes.get(blockState);
        if (voxelShape == null) return List.of(new BoundingBox(0, 0, 0, 1, 1, 1));
        return voxelShape.getBoxes();
    }

    public BlockState loadBlockState(MapType data) {
        return definition.load(data);
    }

    public void saveBlockState(MapType entriesData, BlockState blockState) {
        definition.save(blockState, entriesData);
    }

    public boolean isToolRequired() {
        return getEffectiveTool() != null;
    }

    public boolean doesRandomTick() {
        return false;
    }

    public Identifier getId() {
        return Registries.BLOCK.getId(this);
    }

    public boolean culls() {
        return !isAir() && !settings.transparent;
    }

    /**
     * Handles entities touching this block. Most stuff would only need to affect players, make sure to verify this for
     *  your use case.
     *
     * @param object the entity colliding.
     * @param collision the collision detected.
     * @param box the bounding box collided.
     * @param pressure the pressure of the collision.
     * @param axis the axis of the collision detected.
     * @param state the state of the block touched.
     * @return true to drop through the collision, false to block.
     */
    public boolean onTouch(GameObject object, BlockCollision collision, BoundingBox box, double pressure, Axis axis, BlockState state) {
        if (hasBottomCollision(state) || (isClimbable(state) && !hasCollision())) {
            return (axis != Axis.Y) || object instanceof PlayerEntity player && player.isCrouching();
        }
        return !hasCollision();
    }

    public void onWalkOn(GameObject entity, BlockCollision collision, BoundingBox box, double pressure, BlockState blockState) {

    }

    public boolean canBePlacedAt(World world, BlockVec blockVec, BlockState blockState) {
        return true;
    }

    public boolean isSolid(BlockState blockState) {
        return !isAir() && !settings.transparent;
    }

    public boolean ambientOcclusion(BlockState blockState) {
        return !isFluid() && !isAir() && settings.ambientOcclusion && !settings.transparent;
    }

    public SoundEvent getStepSound(BlockState blockState) {
        return settings.stepSound;
    }

    public FluidState getFluid(BlockState blockState) {
        Function<BlockState, FluidState> fluidFor = settings.fluidFor;
        if (fluidFor == null) {
            return Fluids.EMPTY.getState(0);
        }
        return fluidFor.apply(blockState);
    }

    public CollisionType getCollision(BlockState blockState) {
        if (isFluid()) return CollisionType.LIQUID;
        return CollisionType.SOLID;
    }

    public ModelEffect getModelEffect() {
        return ModelEffect.None;
    }

    public static class Settings {
        public Function<BlockState, FluidState> fluidFor;
        private SoundEvent stepSound = SoundEvent.STEP_STONE;
        private boolean replaceable;
        private boolean bottomCollision;
        private float hardness = 0;
        private float resistance = 0;
        private int lightEmission = 0x000000; // No light
        private int lightReduction = 15;
        private boolean air = false;
        private boolean fluid = false;
        private boolean transparent = false;
        private boolean collision = true;
        private boolean ambientOcclusion = true;
        private boolean climbable = false;

        public Settings() {

        }

        public Settings fluidFor(Function<BlockState, FluidState> fluidFor) {
            this.fluidFor = fluidFor;
            return this;
        }

        public Settings stepSound(SoundEvent stepSound) {
            this.stepSound = stepSound;
            return this;
        }

        public Settings replaceable(boolean replaceable) {
            this.replaceable = replaceable;
            return this;
        }

        public Settings hardness(float hardness) {
            this.hardness = hardness;
            return this;
        }

        public Settings resistance(float resistance) {
            this.resistance = resistance;
            return this;
        }

        public Settings lightEmission(int lightEmission) {
            this.lightEmission = lightEmission;
            return this;
        }

        public Settings lightReduction(int lightReduction) {
            this.lightReduction = lightReduction;
            return this;
        }

        public Settings lightReduction(float lightReduction) {
            this.lightReduction = (int) lightReduction;
            return this;
        }

        public Settings air(boolean air) {
            this.air = air;
            return this;
        }

        public Settings fluid(boolean fluid) {
            this.fluid = fluid;
            return this;
        }

        public Settings transparent(boolean transparent) {
            this.transparent = transparent;
            return this;
        }

        public Settings collision(boolean collision) {
            this.collision = collision;
            return this;
        }

        public Settings strength(float strength) {
            return hardness(strength).resistance(strength);
        }

        public Settings bottomCollision() {
            bottomCollision = true;
            return this;
        }

        public Settings climbable() {
            climbable = true;
            return this;
        }

        public Settings ambientOcclusion(boolean b) {
            ambientOcclusion = b;
            return this;
        }
    }

    @Override
    public String toString() {
        if (getId() == null) return "Block[<unregistered>]";
        return "Block[" +
                "id=" + getId() + ']';
    }
}
