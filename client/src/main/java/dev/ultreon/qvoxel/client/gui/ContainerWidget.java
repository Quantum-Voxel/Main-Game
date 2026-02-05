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
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class ContainerWidget extends Widget {
    protected final List<Widget> widgets = new CopyOnWriteArrayList<>();
    private boolean alreadyHovered;

    protected ContainerWidget() {
        super();
    }

    protected <T extends Widget> T addWidget(@NotNull T widget) {
        widget.attach(this);
        widgets.add(widget);
        return widget;
    }

    protected void removeWidget(Widget widget) {
        if (widgets.remove(widget)) {
            widget.detach();
        }
    }

    protected void clearWidgets() {
        for (Widget widget : widgets) {
            widget.detach();
        }
        widgets.clear();
    }

    @Override
    public final void render(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
        renderWidget(renderer, mouseX, mouseY, partialTicks);
        renderChildren(renderer, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onKeyPress(int key, int scancode, int mods) {
        super.onKeyPress(key, scancode, mods);

        Widget currentFocus = getScreen().currentFocus;
        if (currentFocus != this) {
            currentFocus.onKeyPress(key, scancode, mods);
        }
    }

    @Override
    public void onKeyRepeat(int key, int scancode, int mods) {
        super.onKeyRepeat(key, scancode, mods);

        Widget currentFocus = getScreen().currentFocus;
        if (currentFocus != this) {
            currentFocus.onKeyRepeat(key, scancode, mods);
        }
    }

    @Override
    public void onKeyRelease(int key, int scancode, int mods) {
        super.onKeyRelease(key, scancode, mods);

        Widget currentFocus = getScreen().currentFocus;
        if (currentFocus != this) {
            currentFocus.onKeyRelease(key, scancode, mods);
        }
    }

    @Override
    public void onCharTyped(char character) {
        super.onCharTyped(character);

        Widget currentFocus = getScreen().currentFocus;
        if (currentFocus != this) {
            currentFocus.onCharTyped(character);
        }
    }

    protected void renderChildren(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
        alreadyHovered = false;
        for (Widget child : widgets) {
            renderChild(renderer, child, mouseX, mouseY, partialTicks);
        }
    }

    public void renderChild(GuiRenderer renderer, Widget child, int mouseX, int mouseY, float partialTicks) {
        renderer.debugDraw(child);
        renderer.pushMatrix();
        renderer.translate((float) child.position.x, (float) child.position.y, (float) child.position.z);
        if (!alreadyHovered) {
            child.isHovered = mouseX >= child.position.x && mouseY >= child.position.y && mouseX < child.position.x + child.size.x && mouseY < child.position.y + child.size.y;
            alreadyHovered = child.isHovered;
        } else child.isHovered = false;
        if (child.isVisible)
            child.render(renderer, (int) (mouseX - child.position.x), (int) (mouseY - child.position.y), partialTicks);
        renderer.popMatrix();
    }

    public void renderWidget(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    public boolean onMouseButtonPress(int mouseX, int mouseY, int button, int mods) {
        super.onMouseButtonPress(mouseX, mouseY, button, mods);

        client.addMarker(mouseX, mouseY, 50, 0xffffff);

        for (Widget widget : widgets) {
            if (widget.isEnabled && widget.isVisible && widget.isHovered && widget.onMouseButtonPress((int) (mouseX - widget.position.x), (int) (mouseY - widget.position.y), button, mods)) {
                getScreen().changeFocus(widget);
                return true;
            }
        }
        getScreen().changeFocus(this);
        return false;
    }

    @Override
    public void onMouseButtonRelease(int mouseX, int mouseY, int button, int mods) {
        super.onMouseButtonRelease(mouseX, mouseY, button, mods);

        for (Widget widget : widgets) {
            widget.onMouseButtonRelease((int) (mouseX - widget.position.x), (int) (mouseY - widget.position.y), button, mods);
        }
    }

    public List<Widget> getWidgets() {
        return widgets;
    }

    @Override
    protected void onFocusLost() {
        super.onFocusLost();

        for (Widget widget : widgets) {
            if (widget.focused) {
                widget.onFocusLost();
            }
        }
    }

    @Override
    protected void onFocusGained() {
        super.onFocusGained();
    }
}
