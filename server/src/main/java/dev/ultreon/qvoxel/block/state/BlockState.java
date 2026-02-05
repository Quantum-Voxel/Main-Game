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

package dev.ultreon.qvoxel.block.state;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.block.*;
import dev.ultreon.qvoxel.block.state.property.StatePropertyKey;
import dev.ultreon.qvoxel.fluid.FluidState;
import dev.ultreon.qvoxel.item.ToolType;
import dev.ultreon.qvoxel.loot.LootGenerator;
import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.player.PlayerEntity;
import dev.ultreon.qvoxel.registry.Registries;
import dev.ultreon.qvoxel.registry.RegistryKeys;
import dev.ultreon.qvoxel.resource.GameObject;
import dev.ultreon.qvoxel.server.ServerWorld;
import dev.ultreon.qvoxel.sound.SoundEvent;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.world.Axis;
import dev.ultreon.qvoxel.world.Chunk;
import dev.ultreon.qvoxel.world.CollisionType;
import dev.ultreon.qvoxel.world.World;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public final class BlockState implements BlockLike {
    public static final PacketCodec<BlockState> PACKET_CODEC = PacketCodec.packed(
            Registries.BLOCK.packetCodec(), BlockState::getBlock,
            PacketCodec.VAR_INT, BlockState::getIndex,
            BlockState::new
    );
    private final BlockStateDefinition definition;
    private final int value;

    public BlockState(BlockStateDefinition definition, int value) {
        this.definition = definition;
        this.value = value;
    }

    private BlockState(Block block, int value) {
        this.definition = block.getDefinition();
        this.value = value;
    }

    public static BlockState read(PacketIO packetIO) {
        int id = packetIO.readVarInt();
        int index = packetIO.readVarInt();

        return packetIO.get(RegistryKeys.BLOCK).byRawId(id).getDefinition().getStateByIndex(index);
    }

    public static BlockState empty(BlockStateDefinition definition) {
        return definition.getStateByIndex(0);
    }

    public BlockStateDefinition getDefinition() {
        return definition;
    }

    public int getIndex() {
        return value;
    }

    public <T> T get(StatePropertyKey<T> key) {
        int keyIndex = definition.getKeys().indexOf(key);
        int stride = definition.getStride(keyIndex);
        int valueIndex = value / stride % key.getValueCount();
        return key.getValueByIndex(valueIndex);
    }

    public <T> BlockState with(StatePropertyKey<T> key, T newValue) {
        int keyIndex = definition.getKeys().indexOf(key);
        int newValueIndex = key.getValueIndex(newValue);
        int stride = definition.getStride(keyIndex);
        int oldValueIndex = value / stride % key.getValueCount();
        int delta = (newValueIndex - oldValueIndex) * stride;
        return definition.getStateByIndex(value + delta);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getBlock().getId()).append("[");
        List<StatePropertyKey<?>> keys = definition.getKeys();
        for (int i = 0; i < keys.size(); i++) {
            StatePropertyKey<?> key = keys.get(i);
            Object val = get(key);
            sb.append(definition.getKeys().indexOf(key)).append("=").append(val);
            if (i < keys.size() - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    public Block getBlock() {
        return definition.block();
    }

    public void write(PacketIO packetIO) {
        packetIO.writeVarInt(packetIO.get(RegistryKeys.BLOCK).getRawId(getBlock()));
        packetIO.writeVarInt(value);
    }

    /**
     * Called when the block is placed in the world.
     *
     * @param serverWorld the world the block is being placed in
     * @param blockVec    the relativePos of the block
     */
    public void onPlace(ServerWorld serverWorld, BlockVec blockVec) {
        definition.block.onPlace(serverWorld, blockVec, this);
    }

    /**
     * Checks if the block is water.
     *
     * @return true if the block is water, false otherwise
     */
    public boolean isWater() {
        return definition.block == Blocks.WATER;
    }

    /**
     * Checks if the block has a collider.
     *
     * @return true if the block has a collider, false otherwise
     */
    public boolean hasCollision() {
        return definition.block.hasCollision();
    }

    /**
     * Checks if the block is a fluid.
     *
     * @return true if the block is a fluid, false otherwise
     */
    public boolean isFluid() {
        return definition.block.isFluid();
    }

    /**
     * Gets the bounding box of the block at the given relativePos.
     *
     * @return the bounding box of the block
     */
    public @NotNull BoundingBox getBoundingBox() {
        if (definition.block.isAir()) return BoundingBox.EMPTY;
        return definition.block.getBoundingBox(this);
    }

    /**
     * Checks if the block is replaceable.
     *
     * @return true if the block is replaceable, false otherwise
     */
    public boolean isReplaceable() {
        return definition.block.isReplaceable();
    }

    /**
     * Gets the effective tool for the block.
     *
     * @return the effective tool for the block, or null if none
     */
    public @Nullable ToolType getEffectiveTool() {
        return definition.block.getEffectiveTool();
    }

    /**
     * Gets the hardness of the block.
     *
     * @return the hardness of the block
     */
    public float getHardness() {
        return definition.block.getHardness();
    }

    /**
     * Checks if the block requires a tool to be broken.
     *
     * @return true if the block requires a tool to be broken, false otherwise
     */
    public boolean isToolRequired() {
        return definition.block.isToolRequired();
    }

    /**
     * Gets the loot generator for the block.
     *
     * @return the loot generator for the block, or null if none
     */
    public @Nullable LootGenerator getLootGen() {
        return definition.block.getLootGen(this);
    }

    /**
     * Checks if the block is transparent.
     *
     * @return true if the block is transparent, false otherwise
     */
    public boolean isTransparent() {
        return definition.block.isTransparent();
    }

    /**
     * Checks if another object is equal to this block state.
     *
     * @param o the object to check
     * @return true if the object is equal to this block state, false otherwise
     */
    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockState that = (BlockState) o;
        return Objects.equals(definition.block, that.definition.block) && value == that.value;
    }

    /**
     * Returns the hash code value of this {@code BlockProperties} object.
     *
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(definition.block, value);
    }

    /**
     * Updates the block represented by this {@code BlockProperties} in the given {@code World} at the given {@code BlockVec}.
     *
     * @param world  the {@code World} to update the block in
     * @param offset the {@code BlockVec} to update the block at
     */
    public void update(World world, BlockVec offset) {
        definition.block.update(world, offset, this);
    }

    /**
     * Checks if this {@code BlockProperties} can be replaced by the given {@code UseItemContext}.
     *
     * @param context the {@code UseItemContext} to check
     * @return {@code true} if the block can be replaced, {@code false} otherwise
     */
    public boolean canBeReplacedBy(UseItemContext context) {
        return definition.block.canBeReplacedBy(context, this);
    }

    /**
     * Checks if this {@code BlockProperties} represents the given {@code Block}.
     *
     * @param block the {@code Block} to check
     * @return {@code true} if this {@code BlockProperties} represents the given {@code Block}, {@code false} otherwise
     */
    public boolean is(Block block) {
        return definition.block == block;
    }

    /**
     * Called when the block represented by this {@code BlockProperties} is destroyed in the given {@code World} at the given {@code BlockVec}.
     *
     * @param world    the {@code World} where the block is destroyed
     * @param breaking the {@code BlockVec} where the block is destroyed
     * @param breaker  the {@code Player} who destroyed the block
     */
    public void onDestroy(World world, BlockVec breaking, @Nullable PlayerEntity breaker) {
        definition.block.onDestroy(world, breaking, this, breaker);
    }

    /**
     * Returns the an RGB color value representing the light level emitted by the block represented by this {@code BlockProperties}.
     *
     * @return the RGB color value representing the light level emitted by the block
     */
    public int getLightEmission() {
        return definition.block.getLightEmission(this);
    }

    /**
     * Returns the reduction of light level emitted by the block represented by this {@code BlockProperties} when light passes through it.
     *
     * @return the reduction of light level emitted by the block
     */
    public int getLightReduction() {
        return definition.block.getLightReduction(this);
    }

    public void randomTick(Chunk chunk, BlockVec position) {
        definition.block.randomTick(chunk.getWorld(), position, this);
    }

    public boolean doesRandomTick() {
        return definition.block.doesRandomTick();
    }

    public boolean isInvisible() {
        return !definition.block.doesRender();
    }

    public boolean isAir() {
        return definition.block.isAir();
    }

    public MapType save() {
        MapType map = new MapType();
        Identifier id = Registries.BLOCK.getId(definition.block);
        if (id == null)
            throw new IllegalArgumentException("Block " + definition.block + " isn't registered");

        map.putString("block", id.toString());

        MapType entriesData = new MapType();
        definition.block.saveBlockState(entriesData, this);

        map.put("Entries", entriesData);
        return map;
    }

    public static BlockState load(MapType data) {
        Block block = Registries.BLOCK.get(Identifier.parse(data.getString("block")));
        return block.loadBlockState(data);
    }

    public List<BoundingBox> getBoundingBoxes(World world, BlockVec pos) {
        return getBlock().getBoundingBoxes(this, world, pos);
    }

    public boolean onTouch(GameObject entity, BlockCollision collision, BoundingBox box, Axis axis, double pressure) {
        return getBlock().onTouch(entity, collision, box, pressure, axis, this);
    }

    public void onWalkOn(GameObject entity, BlockCollision collision, BoundingBox box, double pressure) {
        getBlock().onWalkOn(entity, collision, box, pressure, this);
    }

    public boolean isSolid() {
        return getBlock().isSolid(this);
    }

    public boolean ambientOcclusion() {
        return getBlock().ambientOcclusion(this);
    }

    public SoundEvent getStepSound() {
        return getBlock().getStepSound(this);
    }

    public FluidState getFluid() {
        return getBlock().getFluid(this);
    }

    public boolean blocksLight() {
        return getLightReduction() > 0;
    }

    public CollisionType getCollision() {
        return getBlock().getCollision(this);
    }

    public boolean hasBottomCollision() {
        return getBlock().hasBottomCollision(this);
    }

    public boolean isClimbable() {
        return getBlock().isClimbable(this);
    }
}
