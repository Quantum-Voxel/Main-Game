/*
 * Copyright 2025. Quinten 'Qubix' Jungblut
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.ultreon.qvoxel.client.gui;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.client.world.BlockRenderTypeRegistry;
import dev.ultreon.qvoxel.client.world.ClientPlayerEntity;
import dev.ultreon.qvoxel.client.world.RenderType;

public class BuriedBlockOverlay extends Overlay {

    @Override
    public void render(ClientPlayerEntity player, GuiRenderer renderer, float partialTick) {
        if (player == null) return;
        BlockState state = player.getBuriedBlock();
        if (state == null || state.isAir() || state.isTransparent() && !state.isFluid()) return;

        renderer.setTextureColor(0xFFFFFFFF);
        RenderType renderType = BlockRenderTypeRegistry.getRenderType(state);
        Identifier buriedTexture = client.getBlockModel(state).getBuriedTexture();
        if (buriedTexture == null) return;
        if (renderType == RenderType.CUTOUT) {
            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 4; y++) {
                    renderer.drawTexture(buriedTexture.mapPath(s -> "textures/" + s + ".png"), -client.getScaledWidth() / 4 + x * client.getScaledWidth() / 2, -client.getScaledHeight() / 4 + y * client.getScaledHeight() / 2, client.getScaledWidth() / 2, client.getScaledHeight() / 2);
                }
            }
            renderer.fillRect(0, 0, client.getScaledWidth(), client.getScaledHeight(), 0x60000000);
            renderer.drawTexture(buriedTexture.mapPath(s -> "textures/" + s + ".png"), 0, 0, client.getScaledWidth(), client.getScaledHeight());
            renderer.fillRect(0, 0, client.getScaledWidth(), client.getScaledHeight(), 0x60000000);
            return;
        }
        renderer.drawTexture(buriedTexture.mapPath(s -> "textures/" + s + ".png"), 0, 0, client.getScaledWidth(), client.getScaledHeight());
        renderer.fillRect(0, 0, client.getScaledWidth(), client.getScaledHeight(), 0x60000000);
    }
}
