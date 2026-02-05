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

import dev.ultreon.qvoxel.block.Block;
import dev.ultreon.qvoxel.block.Blocks;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.resource.ReloadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BlockRenderTypeRegistry {
    private static final Map<Block, RenderType> RENDER_TYPES = new HashMap<>();

    public static RenderType getRenderType(Block block) {
        return RENDER_TYPES.getOrDefault(block, RenderType.SOLID);
    }

    public static void register(Block block, RenderType renderType) {
        if (RENDER_TYPES.containsKey(block))
            throw new IllegalArgumentException("Render type already registered for block " + block.getId());
        RENDER_TYPES.put(block, renderType);
    }

    public static RenderType getRenderType(BlockState block) {
        return getRenderType(block.getBlock());
    }

    public static CompletableFuture<?> reload(ReloadContext context) {
        return context.submitSafe(RENDER_TYPES::clear).thenRunAsync(BlockRenderTypeRegistry::doRegister, context);
    }

    private static void doRegister() {
        BlockRenderTypeRegistry.register(Blocks.WATER, RenderType.WATER);
        BlockRenderTypeRegistry.register(Blocks.SHORT_GRASS, RenderType.CUTOUT);
        BlockRenderTypeRegistry.register(Blocks.CACTUS, RenderType.CUTOUT);
        BlockRenderTypeRegistry.register(Blocks.SNOWY_SHORT_GRASS, RenderType.LEAVES);
        BlockRenderTypeRegistry.register(Blocks.ASPEN_LEAVES, RenderType.LEAVES);
        BlockRenderTypeRegistry.register(Blocks.CEDAR_LEAVES, RenderType.LEAVES);
        BlockRenderTypeRegistry.register(Blocks.CYPRESS_LEAVES, RenderType.LEAVES);
        BlockRenderTypeRegistry.register(Blocks.DOGWOOD_LEAVES, RenderType.LEAVES);
        BlockRenderTypeRegistry.register(Blocks.MAPLE_LEAVES, RenderType.LEAVES);
        BlockRenderTypeRegistry.register(Blocks.EUCALYPTUS_LEAVES, RenderType.LEAVES);
        BlockRenderTypeRegistry.register(Blocks.MAHOGANY_LEAVES, RenderType.LEAVES);
        BlockRenderTypeRegistry.register(Blocks.MESQUITE_LEAVES, RenderType.LEAVES);
        BlockRenderTypeRegistry.register(Blocks.PINE_LEAVES, RenderType.LEAVES);
        BlockRenderTypeRegistry.register(Blocks.WILLOW_LEAVES, RenderType.LEAVES);
        BlockRenderTypeRegistry.register(Blocks.OAK_LEAVES, RenderType.LEAVES);
    }
}
