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

package dev.ultreon.qvoxel.menu;

import dev.ultreon.qvoxel.entity.Entity;
import dev.ultreon.qvoxel.item.ItemStack;
import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.handler.InGameClientPacketHandler;
import dev.ultreon.qvoxel.network.packets.s2c.S2CInventoryContentChangedPacket;
import dev.ultreon.qvoxel.player.PlayerEntity;
import dev.ultreon.qvoxel.server.ServerPlayerEntity;
import dev.ultreon.ubo.types.ListType;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Inventory extends ContainerMenu {
    public static final int MAX_SLOTS = 36;

    public final ItemSlot[] hotbar = new ItemSlot[9];
    public final ItemSlot[][] inv = new ItemSlot[9][3];

    private final PlayerEntity holder;
    private final List<ItemSlot> changed = new ArrayList<>();

    public Inventory(@NotNull MenuType<?> type, @NotNull Entity entity) {
        super(type, entity, MAX_SLOTS, null);

        if (!(entity instanceof PlayerEntity player)) {
            throw new IllegalArgumentException("Entity must be a player!");
        }

        holder = player;
        watching.add(player);
    }

    @Override
    public void build() {
        int idx = 0;
        for (int x = 0; x < 9; x++) {
            hotbar[x] = addSlot(new ItemSlot(idx++, this, new ItemStack(), x * 19 + 4, 81));
        }

        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 3; y++) {
                inv[x][y] = addSlot(new ItemSlot(idx++, this, new ItemStack(), x * 19 + 4, y * 18 + 6));
            }
        }
    }

    @Override
    protected @Nullable Packet<InGameClientPacketHandler> createPacket(ServerPlayerEntity player, ItemSlot... slot) {
        Map<Integer, ItemStack> map = new HashMap<>();
        for (ItemSlot itemSlot : slot) {
            if (map.put(itemSlot.getIndex(), itemSlot.getItem()) != null) {
                throw new IllegalStateException("Duplicate key");
            }
        }
        return new S2CInventoryContentChangedPacket(map);
    }

    public ItemSlot getHotbarSlot(int index) {
        return hotbar[index];
    }

    public List<ItemSlot> getHotbarSlots() {
        return Arrays.asList(hotbar);
    }

    /**
     * Adds a list of item stacks to the inventory.
     *
     * @param stacks the list of item stacks.
     * @return true if all items could fit.
     */
    public boolean addItems(Iterable<ItemStack> stacks) {
        boolean fit = true;
        for (ItemStack stack : stacks) {
            fit &= addItem(stack.copy(), false);
        }
        onChanged(changed);
        changed.clear();
        return fit;
    }

    /**
     * Adds an item stack to the inventory holder.
     *
     * @param stack the item stack to add.
     * @return true if the item stack could fully fit in the inventory.
     */
    public boolean addItem(ItemStack stack) {
        return addItem(stack, true);
    }

    /**
     * Adds an item stack to the inventory holder.
     *
     * @param stack the item stack to add.
     * @return true if the item stack could fully fit in the inventory.
     */
    public boolean addItem(ItemStack stack, boolean emitUpdate) {
        for (ItemSlot slot : slots) {
            ItemStack slotItem = slot.getItem();

            if (slotItem.isEmpty()) {
                int maxStackSize = stack.getItem().getMaxStackSize();
                int transferAmount = Math.min(stack.getCount(), maxStackSize);
                stack.transferTo(slotItem, transferAmount);
                if (emitUpdate) onChanged(slot);
                else changed.add(slot);
            } else if (slotItem.sameItemSameData(stack)) {
                stack.transferTo(slotItem, stack.getCount());
                if (emitUpdate) onChanged(slot);
                else changed.add(slot);
            }

            // If the stack is fully distributed, exit the loop
            if (stack.isEmpty()) {
                return true;
            }
        }

        // If the loop completes and there's still some stack remaining, it means it couldn't be fully added to slots.
        return stack.isEmpty();
    }

    public PlayerEntity getHolder() {
        return holder;
    }

    @Override
    public List<ItemSlot> getInputs() {
        return Arrays.asList(slots);
    }

    @Override
    public List<ItemSlot> getOutputs() {
        return Arrays.asList();
    }

    @Override
    public @NotNull Iterator<ItemStack> iterator() {
        List<ItemStack> list = new ArrayList<>();
        for (ItemSlot slot : slots) {
            ItemStack item = slot.getItem();
            list.add(item);
        }
        return list.iterator();
    }


    public ListType<MapType> save() {
        ListType<MapType> listData = new ListType<>();
        for (ItemSlot slot : slots) {
            listData.add(slot.getItem().save());
        }
        return listData;
    }

    public void load(ListType<MapType> listData) {
        if (listData.size() != slots.length) {
            clear();
            return;
        }
        for (int i = 0; i < slots.length; i++) {
            ItemStack load = ItemStack.load(listData.get(i));
            slots[i].setItem(load, false);
        }

        onAllChanged();
    }

    public void clear() {
        for (ItemSlot slot : slots) {
            slot.setItem(ItemStack.empty(), true);
        }

        onAllChanged();
    }
}
