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

package dev.ultreon.qvoxel.item;

import dev.ultreon.qvoxel.block.Block;
import dev.ultreon.qvoxel.block.BlockLike;
import dev.ultreon.qvoxel.block.BoundingBox;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.entity.Entity;
import dev.ultreon.qvoxel.menu.ItemSlot;
import dev.ultreon.qvoxel.player.PlayerEntity;
import dev.ultreon.qvoxel.server.GameMode;
import dev.ultreon.qvoxel.server.ServerPlayerEntity;
import dev.ultreon.qvoxel.server.ServerWorld;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.world.BlockHitResult;
import dev.ultreon.qvoxel.world.World;
import dev.ultreon.qvoxel.world.gen.GenerationBarrier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class BlockItem extends Item implements BlockLike {
    private final Supplier<Block> blockProvider;
    private Block block;

    public BlockItem(Settings settings, Supplier<Block> blockProvider) {
        super(settings);
        this.blockProvider = blockProvider;
    }

    @Override
    public Block getBlock() {
        if (block != null) return block;

        block = blockProvider.get();
        return block;
    }

    @Override
    public UseResult use(World world, ItemSlot slot, BlockHitResult hitResult, PlayerEntity player) {
        BlockVec hitVec = hitResult.hitBlock();
        BlockState hitState = world.get(hitVec);
        if (hitState.isReplaceable()) {
            Block targetBlock = getBlock();
            UseResult fail = handleBlockPlacement(world, slot, player, targetBlock, hitVec, hitState);
            if (fail != null) return fail;
            return UseResult.FAIL;
        }


        BlockVec adjacentVec = hitResult.adjacentBlock();
        BlockState adjacentState = world.get(adjacentVec);
        if (adjacentState.isAir() || adjacentState.isReplaceable()) {
            Block targetBlock = getBlock();
            UseResult fail = handleBlockPlacement(world, slot, player, targetBlock, adjacentVec, adjacentState);
            if (fail != null) return fail;
        }
        return UseResult.FAIL;
    }

    private static @Nullable UseResult handleBlockPlacement(World world, ItemSlot slot, PlayerEntity player, Block targetBlock, BlockVec adjacentVec, BlockState adjacentState) {
        if (targetBlock.canBePlacedAt(world, adjacentVec, adjacentState)) {
            BoundingBox boundingBox = targetBlock.getBoundingBox(targetBlock.getDefaultState());
            if (world.isClientSide() && boundingBox != null && boundingBox.intersects(player.getBoundingBox(), adjacentVec.x, adjacentVec.y, adjacentVec.z))
                return UseResult.FAIL;
            for (Entity entity : world.getEntities()) {
                if (entity == player) continue;
                if (boundingBox != null && boundingBox.intersects(entity.getBoundingBox(), adjacentVec.x, adjacentVec.y, adjacentVec.z)) {
                    player.sendMessage("");
                    if (world instanceof ServerWorld serverWorld && player instanceof ServerPlayerEntity serverPlayer)
                        serverWorld.getChunkAt(adjacentVec.x, adjacentVec.y, adjacentVec.z, GenerationBarrier.ALL).sendChunk(serverPlayer);
                    return UseResult.FAIL;
                }
            }
            if (!world.isClientSide()) {
                targetBlock.onPlace((ServerWorld) world, adjacentVec, adjacentState);
            }
            world.set(adjacentVec, targetBlock.getDefaultState());
            if (!world.isClientSide() && player.getGameMode() != GameMode.BUILDING) {
                slot.shrink(1);
            }
            return world.isClientSide() ? UseResult.SEND : UseResult.SUCCESS;
        } else if (world instanceof ServerWorld serverWorld && player instanceof ServerPlayerEntity serverPlayer) {
            player.sendMessage("");
            serverWorld.getChunkAt(adjacentVec.x, adjacentVec.y, adjacentVec.z, GenerationBarrier.ALL).sendChunk(serverPlayer);
        }
        return null;
    }
}
