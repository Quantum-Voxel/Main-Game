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

import dev.ultreon.qvoxel.client.gui.screen.ChatScreen;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.client.world.ClientPlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class ChatOverlay extends Overlay {
    private final List<ChatMessage> messages = new ArrayList<>();

    @Override
    public void render(ClientPlayerEntity player, GuiRenderer renderer, float partialTick) {
        if (client.getScreen() instanceof ChatScreen) return;

        renderDirect(renderer, partialTick, 20);
    }

    public void renderDirect(GuiRenderer renderer, float partialTick, int yOff) {
        int index = 0;
        int size = messages.size();
        int lineHeight = client.font.lineHeight;
        for (int i = size - 1; i >= 0; i--) {
            ChatMessage message = messages.get(i);
            if (message.isExpired()) {
                continue;
            }
            message.render(renderer, partialTick);
            if (index >= 10) {
                break;
            }
            renderer.drawString(message.text(), 20, height - size * lineHeight - yOff - index * lineHeight, 0xffffffff);
            index++;
        }
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
    }

    public void clear() {
        messages.clear();
    }
}
