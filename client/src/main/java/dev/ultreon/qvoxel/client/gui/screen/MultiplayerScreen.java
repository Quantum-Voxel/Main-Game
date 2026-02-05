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

import dev.ultreon.libs.translations.v1.Language;
import dev.ultreon.qvoxel.client.gui.Screen;
import dev.ultreon.qvoxel.client.gui.widget.TextButtonWidget;
import dev.ultreon.qvoxel.client.render.GuiRenderer;

public class MultiplayerScreen extends Screen {
    private TextButtonWidget singleplayerBtn;
    private TextButtonWidget backBtn;

    public MultiplayerScreen() {
        super(Language.translate("qvoxel.screen.multiplayer"));
    }

    @Override
    public void init() {
        singleplayerBtn = addWidget(new TextButtonWidget("Play Singleplayer Instead"));
        backBtn = addWidget(new TextButtonWidget("Back"));
    }

    @Override
    public void resized() {
        singleplayerBtn.setPosition(size.x / 2 - 100, size.y / 2);
        backBtn.setPosition(size.x / 2 - 100, size.y / 2 + 30);

        singleplayerBtn.resize(200, 20);
        backBtn.resize(200, 20);
    }

    @Override
    public void renderForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
        super.renderForeground(renderer, mouseX, mouseY, partialTicks);

        renderer.drawCenteredString(
                Language.translate("qvoxel.screen.multiplayer.workInProgress"),
                size.x / 2,
                size.y / 2 - 30,
                0xFFFFFFFF
        );
    }
}
