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

import dev.ultreon.qvoxel.item.ItemStack;

public class RedirectItemSlot extends ItemSlot {
    private final ItemSlot target;

    public RedirectItemSlot(int index, ItemSlot target, int slotX, int slotY) {
        super(index, target.getContainer(), target.getItem(), slotX, slotY);

        this.target = target;
    }

    @Override
    public ItemStack getItem() {
        return target.getItem();
    }

    @Override
    public void setItem(ItemStack item) {
        target.setItem(item);
    }

    @Override
    public ItemStack setItem(ItemStack item, boolean emitEvent) {
        return target.setItem(item, emitEvent);
    }

    @Override
    public ContainerMenu getContainer() {
        return target.getContainer();
    }

    @Override
    public ItemStack takeItem() {
        return target.takeItem();
    }

    @Override
    public String toString() {
        return target.toString();
    }

    @Override
    public ItemStack split() {
        return target.split();
    }

    @Override
    public ItemStack split(int amount) {
        return target.split(amount);
    }

    @Override
    public void update() {
        target.update();
    }

    @Override
    public boolean isEmpty() {
        return target.isEmpty();
    }

    @Override
    public void shrink(int amount) {
        target.shrink(amount);
    }

    @Override
    public void grow(int amount) {
        target.grow(amount);
    }
}
