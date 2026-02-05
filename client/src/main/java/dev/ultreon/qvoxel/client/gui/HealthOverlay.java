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

public class HealthOverlay extends Overlay {

    public static final Identifier ICONS_TEX = CommonConstants.id("textures/gui/icons.png");

    @Override
    public void render(ClientPlayerEntity player, GuiRenderer renderer, float partialTick) {
        if (player == null) return;
        if (player.isInvincible()) return;

        int x = (int) ((float) client.getScaledWidth() / 2) - 88;

        int delU = 2;
        if (player.regenFlashTimer % 2 == 0) delU = 29;

        renderer.setTextureColor(0xFFFFFFFF);

        for (int emptyHeartX = 0; emptyHeartX < 10; emptyHeartX++)
            renderer.drawTexture(ICONS_TEX, x + emptyHeartX * 8, leftY - 9, 9, 9, 34 + delU, 0, 9, 9, 256, 256);

        int heartX;
        for (heartX = 0; heartX < Math.floor(player.getHealth() / 2); heartX++)
            renderer.drawTexture(ICONS_TEX, x + heartX * 8, leftY - 9, 9, 9, 16 + delU, 0, 9, 9, 256, 256);

        if ((int) player.getHealth() % 2 == 1)
            renderer.drawTexture(ICONS_TEX, x + heartX * 8, leftY - 9, 9, 9, 25 + delU, 0, 9, 9, 256, 256);

        leftY -= 13;
    }
}
