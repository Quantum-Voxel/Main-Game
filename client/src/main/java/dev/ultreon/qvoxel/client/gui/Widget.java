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

import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.resource.GameObject;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector2i;

/**
 * A widget is a GUI element that can be rendered and interacted with.
 * It can be a container for other widgets (see {@link ContainerWidget}).
 * <p>
 * If your widget is only for rendering and should pass mouse events to other widgets,
 *   you should override {@link #onMouseButtonPress(int, int, int, int)} to return false.
 *
 * @see Screen#addWidget(Widget)
 */
public abstract class Widget extends GameObject {
    protected final QuantumClient client = QuantumClient.get();
    public final FontRenderer font = client.font;
    protected Screen screen;
    public final Vector2i size = new Vector2i();
    public boolean isHovered = false;
    public boolean isEnabled = true;
    public boolean isVisible = true;
    private ContainerWidget container;
    protected boolean focused = false;

    protected Widget() {
        screen = null;
    }

    @ApiStatus.Internal
    public void attach(ContainerWidget container) {
        this.container = container;
        screen = container.screen;
    }

    @ApiStatus.Internal
    public void detach() {
        container = null;
        screen = null;
    }

    /**
     * Retrieves the container widget associated with this widget.
     *
     * @return The container widget associated with this widget, or {@code null} if no container is set.
     */
    public ContainerWidget getContainer() {
        return container;
    }

    /**
     * Returns the {@code Screen} associated with this widget. If the widget is an instance
     * of {@code Screen}, this will return itself. Otherwise, it will return the {@code Screen}
     * object associated with the widget.
     *
     * @return The {@code Screen} associated with this widget, or {@code null} if no screen is set.
     */
    public Screen getScreen() {
        if (this instanceof Screen) return (Screen) this;
        return screen;
    }

    /**
     * Renders the widget.
     * This method may not be called each frame, e.g., when the widget is not visible.
     * But it doesn't necessarily mean it's invisible.
     * Can be called for caching the image in a framebuffer, for example.
     *
     * @param renderer     The renderer to use for rendering.
     * @param mouseX       The X position of the mouse.
     * @param mouseY       The Y position of the mouse.
     * @param partialTicks The partial ticks.
     */
    public abstract void render(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks);

    /**
     * Called when a key is pressed.
     *
     * @param key      The key that was pressed.
     * @param scancode The scancode of the key that was pressed.
     * @param mods     Keyboard modifiers; is zero when no modifier keys were held down.
     */
    public void onKeyPress(int key, int scancode, int mods) {

    }

    /**
     * Called when a key is held down.
     * This may be called with a delay after {@link #onKeyPress(int, int, int)} is called.
     *
     * @param key      The key that was held down.
     * @param scancode The scancode of the key that was held down.
     * @param mods     Keyboard modifiers; is zero when no modifier keys were held down.
     */
    public void onKeyRepeat(int key, int scancode, int mods) {

    }

    /**
     * Called when a key is released.
     *
     * @param key      The key that was released.
     * @param scancode The scancode of the key that was released.
     * @param mods     Keyboard modifiers; is zero when no modifier keys were held down.
     */
    public void onKeyRelease(int key, int scancode, int mods) {

    }

    /**
     * Called when a character is typed.
     *
     * @param character The character that was typed.
     */
    public void onCharTyped(char character) {

    }

    /**
     * Called when a mouse button is pressed.
     *
     * @param mouseX The X position of the mouse when the button was pressed.
     * @param mouseY The Y position of the mouse when the button was pressed.
     * @param button The button that was pressed
     * @param mods   Keyboard modifiers; is zero when no modifier keys were held down.
     * @return True if the mouse button event should be consumed. False to pass the event to other widgets.
     */
    public boolean onMouseButtonPress(int mouseX, int mouseY, int button, int mods) {
        return true;
    }

    /**
     * Called when a mouse button is released.
     *
     * @param mouseX The X position of the mouse when the button was released.
     * @param mouseY The Y position of the mouse when the button was released.
     * @param button The button that was released
     * @param mods   Keyboard modifiers; is zero when no modifier keys were held down.
     */
    public void onMouseButtonRelease(int mouseX, int mouseY, int button, int mods) {

    }

    /**
     * Called when the mouse is moved.
     *
     * @param mouseX The new mouse X position.
     * @param mouseY The new mouse Y position.
     */
    public void onMouseMove(int mouseX, int mouseY) {

    }

    /**
     * Called when the mouse scrolls.
     *
     * @param scrollX The amount the mouse was scrolled horizontally.
     * @param scrollY The amount the mouse was scrolled vertically.
     */
    public void onMouseScroll(double scrollX, double scrollY) {

    }

    /**
     * Resizes the widget.
     * This method may be called each frame instead of each resize.
     * Make sure to cache the width and height to prevent unnecessary calculations (e.g., framebuffer rebuilding)
     *
     * @param width  The new width of the widget.
     * @param height The new height of the widget.
     */
    public void resize(int width, int height) {
        size.x = width;
        size.y = height;
    }

    /**
     * Called when the widget gains focus.
     */
    protected void onFocusGained() {
        focused = true;
    }

    /**
     * Called when the widget loses focus.
     */
    protected void onFocusLost() {
        focused = false;
    }

    public boolean isFocused() {
        return focused && container.isFocused();
    }

    public void setPosition(int x, int y) {
        position.set(x, y, position.z);
    }

    /**
     * Sets the Z-index of this widget. The Z-index determines the drawing order
     * of the widget relative to other widgets. A higher Z-index means the widget
     * will be rendered on top of widgets with a lower Z-index.
     *
     * @param zIndex The Z-index to set for this widget.
     */
    public void setZIndex(int zIndex) {
        position.z = zIndex;
    }

    /**
     * Handles client-side ticking.
     * This is called approximately 20 times a second.
     */
    public void tick() {

    }

    public int getWidth() {
        return size.x;
    }

    public int getHeight() {
        return size.y;
    }

    public void setWidth(int width) {
        size.x = width;
    }

    public void setHeight(int height) {
        size.y = height;
    }

    public int getScreenX() {
        ContainerWidget parent = this.getContainer();
        int x = (int) position.x;
        while (!(parent instanceof Screen)) {
            x += (int) parent.position.x;
            parent = parent.getContainer();
        }

        return x;
    }

    public int getScreenY() {
        ContainerWidget parent = this.getContainer();
        int y = (int) position.y;
        while (!(parent instanceof Screen)) {
            y += (int) parent.position.y;
            parent = parent.getContainer();
        }

        return y;
    }
}
