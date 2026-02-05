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

import java.util.Arrays;
import java.util.stream.Stream;

public class DisconnectScreen extends Screen {
    private final String message;
    private final boolean isMemory;
    private TextButtonWidget backButton;

    public DisconnectScreen(String message, boolean isMemory) {
        super(Language.translate(isMemory ? "qvoxel.screen.disconnect.singleplayer" : "qvoxel.screen.disconnect.remote"));
        this.message = message;
        this.isMemory = isMemory;
    }

    @Override
    public void init() {
        backButton = addWidget(new TextButtonWidget("Go Back"));
        backButton.setCallback(client::popGuiLayer);
    }

    @Override
    public void resized() {
        backButton.resize(200, 20);
        backButton.setPosition(size.x / 2 - 100, size.y - 30);
    }

    @Override
    public void renderForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
        super.renderForeground(renderer, mouseX, mouseY, partialTicks);

        String message = this.message;

        int lineHeight = client.font.lineHeight;
        int height = lineHeight;
        int width = 0;
        try (Stream<String> lines = Arrays.stream(message.split("(\r\n|\n|\r)"))) {
            for (String line : lines.toList()) {
                height += lineHeight;
                width = Math.max(width, client.font.widthOf(line));
            }
        }

        renderer.drawLabel(size.x / 2 - width / 2 - 10, size.y / 2 - height / 2 - 10, width + 20, height + 20);
        renderer.drawCenteredString(Language.translate("qvoxel.screen.disconnect.title"), size.x / 2, size.y / 2 - height / 2 + lineHeight / 2, 0xFFFFFFFF);
        renderer.drawCenteredString(message, size.x / 2, size.y / 2 - height / 2 + lineHeight + lineHeight / 2, 0xFFFF6060);
    }
}
