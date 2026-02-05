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

import dev.ultreon.qvoxel.item.Item;
import dev.ultreon.qvoxel.item.ItemStack;
import dev.ultreon.qvoxel.player.PlayerEntity;

/**
 * Item slot for {@link ContainerMenu}.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @see ItemStack
 */
public class ItemSlot {
    private final ContainerMenu container;
    int index;
    private ItemStack item;
    private final int slotX;
    private final int slotY;

    public ItemSlot(int index, ContainerMenu container, ItemStack item, int slotX, int slotY) {
        this.index = index;
        this.container = container;
        this.item = item;
        this.slotX = slotX;
        this.slotY = slotY;
    }

    /**
     * @param item the item to put in the slot.
     */
    public void setItem(ItemStack item) {
        setItem(item, true);
    }

    /**
     * @param item the item to put in the slot.
     * @return the previous item in the slot.
     */
    public ItemStack setItem(ItemStack item, boolean emitEvent) {
        ItemStack old = this.item;
        this.item = item;

        if (emitEvent) update();
        return old;
    }

    public boolean isWithinBounds(int x, int y) {
        return x >= getSlotX() && y >= getSlotY() && x <= getSlotX() + 16 && y <= getSlotY() + 16;
    }

    /**
     * Takes an item from the slot. This will set the current item to empty and return the original item.
     *
     * @return the item in the slot.
     */
    public ItemStack takeItem() {
        ItemStack copy = getItem();
        setItem(new ItemStack());
        return copy;
    }

    public void update() {
        container.onChanged(this);
    }

    @Override
    public String toString() {
        return "ItemSlot(" + index + ')';
    }

    public ItemStack split() {
        ItemStack remainder = getItem().split();
        update();
        return remainder;
    }

    public ItemStack split(int amount) {
        ItemStack remainder = getItem().split(amount);
        update();
        return remainder;
    }

    public boolean isEmpty() {
        return getItem().isEmpty();
    }

    public void shrink(int amount) {
        getItem().shrink(amount);
        update();
    }

    public void grow(int amount) {
        getItem().grow(amount);
        update();
    }

    public boolean mayPickup(PlayerEntity player) {
        return true;
    }

    public boolean mayPlace(Item carried) {
        return true;
    }

    public ContainerMenu getContainer() {
        return container;
    }

    public int getIndex() {
        return index;
    }

    public ItemStack getItem() {
        return item;
    }

    public int getSlotX() {
        return slotX;
    }

    public int getSlotY() {
        return slotY;
    }
}
