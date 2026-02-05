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

package dev.ultreon.qvoxel.client.gui.widget;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.gui.Widget;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.sound.SoundEvent;
import org.lwjgl.sdl.SDLMouse;

public class TextButtonWidget extends Widget {
    private static final Identifier BUTTON_TEXTURE = CommonConstants.id("textures/gui/buttons/dark.png");
    private static final Identifier BUTTON_HOVER_TEXTURE = CommonConstants.id("textures/gui/buttons/dark_hover.png");
    private static final Identifier BUTTON_PRESSED_TEXTURE = CommonConstants.id("textures/gui/buttons/dark_pressed.png");
    private static final Identifier BUTTON_DISABLED_TEXTURE = CommonConstants.id("textures/gui/buttons/dark_disabled.png");
    private static final Identifier BUTTON_PRESSED_HOVER_TEXTURE = CommonConstants.id("textures/gui/buttons/dark_pressed_hover.png");
    private static final Identifier BUTTON_PRESSED_DISABLED_TEXTURE = CommonConstants.id("textures/gui/buttons/dark_pressed_disabled.png");
    private final String message;
    private Callback callback;
    private boolean isPressed;

    public TextButtonWidget(String message) {
        this.message = message;
        size.set(100, 20);
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void render(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
        Identifier texture = BUTTON_TEXTURE;
        if (isHovered) {
            texture = BUTTON_HOVER_TEXTURE;
            if (isPressed) {
                texture = BUTTON_PRESSED_HOVER_TEXTURE;
            } else if (!isEnabled) {
                texture = BUTTON_PRESSED_DISABLED_TEXTURE;
            }
        } else if (isPressed) {
            texture = BUTTON_PRESSED_TEXTURE;
        } else if (!isEnabled) {
            texture = BUTTON_DISABLED_TEXTURE;
        }
        renderer.drawNinePatch(texture, 0, 0, size.x, size.y, 0, 0, 21, 21, 21, 21, 7);
        int offset = isPressed && isEnabled ? 2 : 0;
        renderer.drawCenteredString(message, size.x / 2, size.y / 2 + offset, 0xFFFFFFFF);
    }

    @Override
    public boolean onMouseButtonPress(int mouseX, int mouseY, int button, int mods) {
        super.onMouseButtonPress(mouseX, mouseY, button, mods);
        if (!isEnabled || !isHovered || button != SDLMouse.SDL_BUTTON_LEFT) {
            return false;
        }
        isPressed = true;
        client.playSound(SoundEvent.UI_BUTTON_PRESS, 1.0f);
        return true;
    }

    @Override
    public void onMouseButtonRelease(int mouseX, int mouseY, int button, int mods) {
        super.onMouseButtonRelease(mouseX, mouseY, button, mods);
        if (button != SDLMouse.SDL_BUTTON_LEFT) {
            return;
        }
        if (isPressed && isEnabled && isHovered && callback != null) {
            callback.onClick();
        }
        isPressed = false;
        client.playSound(SoundEvent.UI_BUTTON_RELEASE, 1.0f);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public Callback getCallback() {
        return callback;
    }

    public interface Callback {
        void onClick();
    }
}
