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
import dev.ultreon.qvoxel.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

public record ChatMessage(
        long timestamp,
        String text,
        @Nullable PlayerEntity sender
) {
    public static ChatMessage system(String message) {
        return new ChatMessage(System.currentTimeMillis(), message, null);
    }

    public boolean isSystemMessage() {
        return sender == null;
    }

    public void render(GuiRenderer renderer, float partialTick) {

    }

    public boolean isExpired() {
        return System.currentTimeMillis() - timestamp > 5000;
    }
}
