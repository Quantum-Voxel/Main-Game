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

package dev.ultreon.qvoxel.client.gui;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.client.world.ClientPlayerEntity;
import dev.ultreon.qvoxel.item.ItemStack;
import dev.ultreon.qvoxel.menu.ItemSlot;
import dev.ultreon.qvoxel.registry.Registries;
import dev.ultreon.qvoxel.text.Text;

import java.util.List;

public class HotbarOverlay extends Overlay {

    public static final Identifier WIDGETS_TEX = CommonConstants.id("textures/gui/widgets.png");

    @Override
    public void render(ClientPlayerEntity player, GuiRenderer renderer, float partialTick) {
        if (player == null) return;
        if (player.isSpectator()) return;

        int x = player.selected * 19;
        ItemStack selectedItem = player.getSelectedItem();
        Identifier key = Registries.ITEM.getId(selectedItem.getItem());

        renderer.pushMatrix();
        renderer.translate(0, 0, -1000);
        renderer.drawTexture(WIDGETS_TEX, (int) ((float) client.getScaledWidth() / 2) - 87, leftY - 48, 180, 32, 0, 60, 180, 32, 256, 256);
        renderer.drawTexture(WIDGETS_TEX, (int) ((float) client.getScaledWidth() / 2) - 87 + x + 5, leftY - 42, 18, 18, 0, 42, 18, 18, 256, 256);

        List<ItemSlot> allowed = player.inventory.getHotbarSlots();
        renderer.setFont(client.smallFont);
        for (int index = 0, allowedLength = allowed.size(); index < allowedLength; index++) {
            drawHotbarSlot(renderer, allowed, index, index == player.selected);
        }
        renderer.setFont(client.font);

        if (key != null && !selectedItem.isEmpty() && renderer.pushScissors((int) ((float) client.getScaledWidth() / 2) - 83, leftY - 41, 166, 17)) {
            Text name = selectedItem.getItem().getName();
            int tWidth = client.font.widthOf(name);

            renderer.drawTexture(WIDGETS_TEX, (int) ((float) client.getScaledWidth() / 2) - tWidth / 2 - 4, leftY - 40, 4, 14, 79, 42, 4, 17, 256, 256);
            renderer.drawTexture(WIDGETS_TEX, (int) ((float) client.getScaledWidth() / 2) - tWidth / 2 - 1, leftY - 40, tWidth + 2, 14, 83, 42, 14, 17, 256, 256);
            renderer.drawTexture(WIDGETS_TEX, (int) ((float) client.getScaledWidth() / 2) - tWidth / 2 + tWidth, leftY - 40, 4, 14, 97, 42, 4, 17, 256, 256);

            renderer.drawCenteredString(name, (int) (float) client.getScaledWidth() / 2, leftY - 39, 0xFFFFFFFF);
            renderer.popScissors();
        }
        renderer.popMatrix();
        renderer.drawTexture(WIDGETS_TEX, (int) ((float) client.getScaledWidth() / 2) - 87, leftY - 24, 180, 9, 0, 84, 180, 8, 256, 256);

        leftY -= 63;
        rightY -= 63;
    }

    private void drawHotbarSlot(GuiRenderer renderer, List<ItemSlot> allowed, int index, boolean selected) {
        ItemStack item = allowed.get(index).getItem();
        int ix = (int) ((float) client.getScaledWidth() / 2) - 90 + index * 19 + 2;
        int scaledHeight = client.getScaledHeight();
        client.itemRenderer.render(item.getItem(), renderer, ix, scaledHeight - 24);
        int count = item.getCount();
        if (!item.isEmpty() && count > 1) {
            String text = Integer.toString(count);
            int width = client.smallFont.widthOf(text);
            renderer.drawTexture(WIDGETS_TEX, (int) (ix + 24f - width - 2), scaledHeight - 31, width + 2, 9, 19, 51, width + 2, 9, 256, 256);
            if (selected) {
                renderer.drawTexture(WIDGETS_TEX, (int) (ix + 24f - width - 3), scaledHeight - 32, width + 3, 10, 38, 50, width + 3, 10, 256, 256);
            }
            renderer.drawString(text, (int) (ix + 26f - width - 1), scaledHeight - 24, 0xFFFFFFFF, false);
        }
    }
}
