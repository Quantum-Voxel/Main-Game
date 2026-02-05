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

import dev.ultreon.qvoxel.client.render.GuiRenderer;

import java.util.Arrays;
import java.util.stream.Stream;

public class LoadingScreen extends Screen {
    private String message;

    public LoadingScreen(String title, String message) {
        super(title);
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public void init() {
        // Nothing to do here
    }

    @Override
    public void resized() {
        // Nothing to do here
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
        renderer.drawCenteredString(getTitle(), size.x / 2, size.y / 2 - height / 2 + lineHeight / 2, 0xFFFFFFFF);
        renderer.drawCenteredString(message, size.x / 2, size.y / 2 - height / 2 + lineHeight + lineHeight / 2, 0xFF6080FF);
    }

    @Override
    public boolean canCloseWithEscape() {
        return false;
    }
}
