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

package dev.ultreon.qvoxel.client.gui.screen;

import dev.ultreon.qvoxel.client.gui.Screen;
import dev.ultreon.qvoxel.client.gui.widget.InputSlotWidget;
import dev.ultreon.qvoxel.menu.ContainerMenu;
import dev.ultreon.qvoxel.menu.ItemSlot;

public abstract class MenuScreen extends Screen {
    private final ContainerMenu menu;
    private final InputSlotWidget[] slotWidgets;

    protected MenuScreen(ContainerMenu menu, String title) {
        super(title);
        this.menu = menu;
        slotWidgets = new InputSlotWidget[menu.slots.length];
    }

    @Override
    public void init() {
        ItemSlot[] slots = menu.slots;
        for (int i = 0, slotsLength = slots.length; i < slotsLength; i++) {
            ItemSlot slot = slots[i];
            slotWidgets[i] = addWidget(new InputSlotWidget(slot));
        }
    }

    @Override
    public void resized() {
        int guiX = getGuiX();
        int guiY = getGuiY();

        for (InputSlotWidget slotWidget : slotWidgets) {
            ItemSlot slot = slotWidget.getSlot();
            slotWidget.setPosition(guiX + slot.getSlotX(), guiY + slot.getSlotY());
        }
    }

    public int getGuiX() {
        return size.x / 2 - getGuiWidth() / 2;
    }

    public int getGuiY() {
        return size.y / 2 - getGuiHeight() / 2;
    }

    public abstract int getGuiWidth();
    public abstract int getGuiHeight();

    public ContainerMenu getMenu() {
        return menu;
    }

    @Override
    public final boolean isModal() {
        return false;
    }

    @Override
    public boolean doesPauseGame() {
        return false;
    }
}
