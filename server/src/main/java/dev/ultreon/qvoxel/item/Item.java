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

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.menu.ItemSlot;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.player.PlayerEntity;
import dev.ultreon.qvoxel.registry.Registries;
import dev.ultreon.qvoxel.registry.RegistryHandle;
import dev.ultreon.qvoxel.registry.RegistryKeys;
import dev.ultreon.qvoxel.text.Text;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.world.BlockHitResult;
import dev.ultreon.qvoxel.world.World;

public class Item {
    public static final PacketCodec<Item> PACKET_CODEC = Registries.ITEM.packetCodec();
    private final Settings settings;

    public Item(Settings settings) {
        this.settings = settings;
    }

    public Settings getSettings() {
        return settings;
    }

    public UseResult useOnBlock(World world, BlockState state, BlockVec pos, PlayerEntity player) {
        return UseResult.PASS;
    }

    public UseResult use(World world, ItemSlot slot, BlockHitResult hitResult, PlayerEntity player) {
        return UseResult.PASS;
    }

    public int getMaxStackSize() {
        return settings.maxStackSize;
    }

    public Identifier getId() {
        return Registries.ITEM.getId(this);
    }

    public int getRawId(RegistryHandle handle) {
        return handle.get(RegistryKeys.ITEM).getRawId(this);
    }

    public Text getName() {
        return Text.translatable(getId().location() + ".item." + getId().path().replace("/", "."));
    }

    public static class Settings {
        private int maxStackSize = 64;

        public Settings maxStackSize(int size) {
            maxStackSize = size;
            return this;
        }
    }

    @Override
    public String toString() {
        return "<item " + getId() + ">";
    }
}
