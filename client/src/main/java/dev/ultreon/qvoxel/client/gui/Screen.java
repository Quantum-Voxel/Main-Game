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
import dev.ultreon.qvoxel.client.gui.screen.TitleScreen;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.client.texture.Texture;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.lwjgl.sdl.SDLKeycode;

public abstract class Screen extends ContainerWidget {
    private static final Identifier BACKGROUND_TEXTURE = CommonConstants.id("textures/gui/title_background.png");
    protected Widget currentFocus = this;
    private String title;
    private final Vector2i fitSize = new Vector2i();
    private boolean opened = false;

    protected Screen(String title) {
        super();
        size.set(client.getScaledWidth(), client.getScaledHeight());
        this.title = title;
    }

    public abstract void init();

    public abstract void resized();

    @Override
    public final void renderWidget(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
        renderBackground(renderer);
        super.renderWidget(renderer, mouseX, mouseY, partialTicks);
        renderForeground(renderer, mouseX, mouseY, partialTicks);
    }

    public void changeFocus(Widget widget) {
        if (currentFocus != widget) {
            currentFocus.onFocusLost();
            currentFocus.focused = false;
            currentFocus = widget;
            widget.onFocusGained();
            widget.focused = true;
        }
    }

    public void renderBackground(GuiRenderer renderer) {
        if (isModal() || client.getWorld() != null && !(this instanceof TitleScreen)) {
            int alpha = (int) (160 / (client.getScreenIndex(this) * 0.8 + 0.2));
            renderer.fillRect(0, 0, size.x, size.y, alpha << 24);
            return;
        }
        fill(size, fitSize, client.getTextureManager().getTexture(BACKGROUND_TEXTURE));
        renderBackgroundImage(renderer);
    }

    private void renderBackgroundImage(GuiRenderer renderer) {
        int x = (size.x - fitSize.x) / 2;
        int y = (size.y - fitSize.y) / 2;
        renderer.drawTexture(BACKGROUND_TEXTURE, x, y, fitSize.x, fitSize.y);
    }

    private void fill(Vector2i size, Vector2i fitSize, Texture texture) {
        float scaleX = (float) size.x / texture.getWidth();
        float scaleY = (float) size.y / texture.getHeight();
        float scale = Math.max(scaleX, scaleY);

        fitSize.x = (int) (texture.getWidth() * scale);
        fitSize.y = (int) (texture.getHeight() * scale);
    }

    public void renderForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {

    }

    public final void init(int width, int height) {
        size.set(width, height);
        init();
        resized();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        resized();
    }

    public final void open() {
        client.showScreen(this);
    }

    protected final void close() {
        if (client.getScreenIndex(this) == 0 && client.getWorld() != null) {
            client.getWindow().captureMouse();
        }
        client.popGuiLayer();
    }

    @Override
    public <T extends Widget> T addWidget(@NotNull T widget) {
        super.addWidget(widget);
        widget.screen = this;
        return widget;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void onOpen() {
        if (opened) return;
        opened = true;

        onFocusGained();
        init(client.getScaledWidth(), client.getScaledHeight());
    }

    public void onClose() {
        if (!opened) return;
        opened = false;
        onFocusLost();
        clearWidgets();
    }

    @Override
    public void onKeyPress(int key, int scancode, int mods) {
        super.onKeyPress(key, scancode, mods);
        if (canCloseWithEscape() && key == SDLKeycode.SDLK_ESCAPE) {
            close();
        }
    }

    public boolean isModal() {
        return true;
    }

    public boolean canCloseWithEscape() {
        return isModal() || client.getWorld() != null;
    }

    public boolean doesPauseGame() {
        return !isModal();
    }
}
