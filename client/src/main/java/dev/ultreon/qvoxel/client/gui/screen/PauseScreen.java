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
import dev.ultreon.qvoxel.client.IntegratedServer;
import dev.ultreon.qvoxel.client.gui.widget.TextButtonWidget;
import dev.ultreon.qvoxel.client.gui.Screen;
import dev.ultreon.qvoxel.client.render.GuiRenderer;

import java.util.concurrent.Future;

import static dev.ultreon.qvoxel.client.gui.screen.TitleScreen.TITLE_TEXTURE;

public class PauseScreen extends Screen {
    private TextButtonWidget resumeBtn;
    private TextButtonWidget exitBtn;
    private Future<?> saved;

    public PauseScreen() {
        super("Pause Screen");
    }

    @Override
    public void init() {
        IntegratedServer integratedServer = client.getIntegratedServer();
        saved = integratedServer.saveAsync();

        resumeBtn = addWidget(new TextButtonWidget("Resume"));
        exitBtn = addWidget(new TextButtonWidget("Exit World"));
        resumeBtn.setCallback(client::resumeGame);
        exitBtn.setCallback(client::quitGame);
    }

    @Override
    public void resized() {
        resumeBtn.resize(100, 40);
        resumeBtn.setPosition(size.x / 2 - 50, size.y / 2 - 20);
        exitBtn.resize(100, 20);
        exitBtn.setPosition(size.x / 2 - 50, size.y / 2 + 18);
    }

    @Override
    public void renderBackground(GuiRenderer renderer) {
        renderer.fillRect(0, 0, size.x, size.y, 0x80000000);
    }

    @Override
    public void renderForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
        super.renderForeground(renderer, mouseX, mouseY, partialTicks);

        renderer.drawTexture(TITLE_TEXTURE, size.x / 2 - 109, 30, 219, 75);
        renderer.drawCenteredString("Paused", size.x / 2, 30 + 75 + 10, 0xFFFFFFFF);

        if (saved != null && !saved.isDone()) {
            String message = Language.translate("qvoxel.screen.pause.saving");
            renderer.drawString(message, size.x - renderer.getStringWidth(message) - 10, size.y - 10, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean isModal() {
        return false;
    }

    @Override
    public boolean canCloseWithEscape() {
        return true;
    }
}
