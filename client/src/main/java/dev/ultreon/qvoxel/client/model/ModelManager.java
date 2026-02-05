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

package dev.ultreon.qvoxel.client.model;

import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.shader.Reloadable;
import dev.ultreon.qvoxel.item.Item;
import dev.ultreon.qvoxel.resource.ReloadContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ModelManager implements Reloadable {
    private final Map<BlockState, BlockModel> blockModels = new HashMap<>();
    private final Map<Item, ItemModel> itemModels = new HashMap<>();
    private final QuantumClient client;

    public ModelManager(QuantumClient client) {
        this.client = client;
    }

    public void registerBlockModel(BlockState blockState, BlockModel model) {
        blockModels.put(blockState, model);
    }

    public void registerItemModel(Item item, ItemModel model) {
        itemModels.put(item, model);
    }

    public BlockModel getBlockModel(BlockState blockState) {
        return blockModels.getOrDefault(blockState, BlockModel.DEFAULT);
    }

    public ItemModel getItemModel(Item item) {
        return itemModels.get(item);
    }

    public Collection<BlockModel> getBlockModels() {
        return blockModels.values();
    }

    public Collection<ItemModel> getItemModels() {
        return itemModels.values();
    }

    public CompletableFuture<?> reload(ReloadContext context) {
        return context.submitSafe(() -> client.resetAtlas(context))
                .thenRunAsync(() -> client.discoverModels(context))
                .thenRunAsync(() -> client.loadModels(context));
    }
}
