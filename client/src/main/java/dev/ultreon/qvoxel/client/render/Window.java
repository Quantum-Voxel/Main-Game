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

package dev.ultreon.qvoxel.client.render;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.Keyboard;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.debug.ImGuiOverlay;
import dev.ultreon.qvoxel.client.input.Mouse;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.sdl.*;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

public class Window implements GLFWObject {
    private final GLCapabilities capabilities;
    private final QuantumClient client;
    private final long window;
    private final long context;
    public float xScale;
    public float yScale;
    private float mouseX;
    private float mouseY;
    private boolean vsync;
    private boolean mouseCaptured;
    private int x;
    private int y;
    private int width;
    private int height;

    /**
     * Initializes a window using the GLFW library and OpenGL context.
     *
     * @throws SDLInitException if GLFW initialization fails or if the window cannot be created.
     * @throws GLInitException  if OpenGL version 4.1 or higher is not supported.
     */
    public Window(QuantumClient client, int width, int height, String title) {
        this.client = client;
        this.width = width;
        this.height = height;
        vsync = true;

        window = SDLVideo.SDL_CreateWindow(title, width, height, SDLVideo.SDL_WINDOW_RESIZABLE | SDLVideo.SDL_WINDOW_OPENGL | SDLVideo.SDL_WINDOW_HIDDEN | SDLVideo.SDL_WINDOW_FULLSCREEN);
        if (window == 0) {
            throw new IllegalStateException("Unable to create window");
        }
        SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_MAJOR_VERSION, 4);
        SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_MINOR_VERSION, 6);
        SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_FLAGS, SDLVideo.SDL_GL_CONTEXT_FORWARD_COMPATIBLE_FLAG);
        context = SDLVideo.SDL_GL_CreateContext(window);
        if (!SDLVideo.SDL_GL_MakeCurrent(window, context)) {
            throw new GLException("Failed to make context current");
        }

        SDLVideo.SDL_GL_SetSwapInterval(1);

        capabilities = GL.createCapabilities();
        if (!capabilities.OpenGL41) {
            throw new GLInitException("Unable to initialize OpenGL: OpenGL 4.1 not supported");
        }

        xScale = yScale = SDLVideo.SDL_GetWindowDisplayScale(window);

        SDLEvents.SDL_SetEventEnabled(SDLEvents.SDL_EVENT_WINDOW_CLOSE_REQUESTED, true);
        SDLEvents.SDL_SetEventEnabled(SDLEvents.SDL_EVENT_WINDOW_FOCUS_LOST, true);
        SDLEvents.SDL_SetEventEnabled(SDLEvents.SDL_EVENT_WINDOW_FOCUS_GAINED, true);
        SDLEvents.SDL_SetEventEnabled(SDLEvents.SDL_EVENT_WINDOW_DISPLAY_SCALE_CHANGED, true);
        SDLEvents.SDL_SetEventEnabled(SDLEvents.SDL_EVENT_WINDOW_RESIZED, true);
        SDLEvents.SDL_SetEventEnabled(SDLEvents.SDL_EVENT_KEY_DOWN, true);
        SDLEvents.SDL_SetEventEnabled(SDLEvents.SDL_EVENT_KEY_UP, true);
        SDLEvents.SDL_SetEventEnabled(SDLEvents.SDL_EVENT_TEXT_INPUT, true);
        SDLEvents.SDL_SetEventEnabled(SDLEvents.SDL_EVENT_KEYBOARD_ADDED, true);
        SDLEvents.SDL_SetEventEnabled(SDLEvents.SDL_EVENT_KEYBOARD_REMOVED, true);
        SDLEvents.SDL_SetEventEnabled(SDLEvents.SDL_EVENT_MOUSE_ADDED, true);
        SDLEvents.SDL_SetEventEnabled(SDLEvents.SDL_EVENT_MOUSE_REMOVED, true);
        SDLEvents.SDL_SetEventEnabled(SDLEvents.SDL_EVENT_MOUSE_MOTION, true);
        SDLEvents.SDL_SetEventEnabled(SDLEvents.SDL_EVENT_MOUSE_BUTTON_DOWN, true);
        SDLEvents.SDL_SetEventEnabled(SDLEvents.SDL_EVENT_MOUSE_BUTTON_UP, true);
        SDLEvents.SDL_SetEventEnabled(SDLEvents.SDL_EVENT_MOUSE_WHEEL, true);
        SDLEvents.SDL_AddEventWatch((_, event) -> {
            SDL_Event sdlEvent = SDL_Event.createSafe(event);
            if (sdlEvent == null) return false;
            switch (sdlEvent.type()) {
                case SDLEvents.SDL_EVENT_WINDOW_CLOSE_REQUESTED -> client.onWindowClose();
                case SDLEvents.SDL_EVENT_WINDOW_FOCUS_LOST -> client.onWindowUnfocus();
                case SDLEvents.SDL_EVENT_WINDOW_FOCUS_GAINED -> client.onWindowFocus();
                case SDLEvents.SDL_EVENT_WINDOW_DISPLAY_SCALE_CHANGED -> QuantumClient.invoke(() -> {
                    xScale = yScale = SDLVideo.SDL_GetWindowDisplayScale(window);
                });
                case SDLEvents.SDL_EVENT_KEY_UP -> {
                    SDL_KeyboardEvent key = sdlEvent.key();
                    int which = key.which();
                    int key1 = key.key();
                    int scancode = key.scancode();
                    short mod = key.mod();
                    QuantumClient.invoke(() -> client.onKeyRelease(which, key1, scancode, mod));
                }
                case SDLEvents.SDL_EVENT_TEXT_INPUT -> {
                    SDL_TextInputEvent key = sdlEvent.text();
                    String text = key.textString();
                    System.out.println("text = " + text);
                    if (text == null) return true;
                    QuantumClient.invoke(() -> client.onCharTyped(text.charAt(0)));
                }
                case SDLEvents.SDL_EVENT_KEY_DOWN -> {
                    SDL_KeyboardEvent key = sdlEvent.key();
                    int which = key.which();
                    int key1 = key.key();
                    int scancode = key.scancode();
                    short mod = key.mod();
                    QuantumClient.invoke(() -> client.onKeyPress(which, key1, scancode, mod));
                }
                case SDLEvents.SDL_EVENT_MOUSE_MOTION -> {
                    SDL_MouseMotionEvent motion = sdlEvent.motion();
                    int which = motion.which();
                    if (which == 0) {
                        float x = motion.x();
                        float y = motion.y();
                        QuantumClient.invoke(() -> client.onMouseEnter(x, y));
                    } else {
                        float xrel = motion.xrel();
                        float yrel = motion.yrel();
                        QuantumClient.invoke(() -> client.onMouseMove(which, xrel, yrel));
                    }
                }
                case SDLEvents.SDL_EVENT_MOUSE_WHEEL -> {
                    SDL_MouseWheelEvent wheel = sdlEvent.wheel();
                    int which = wheel.which();
                    float x1 = wheel.x();
                    float y1 = wheel.y();
                    QuantumClient.invoke(() -> client.onMouseScroll(which, x1, y1));
                }
                case SDLEvents.SDL_EVENT_MOUSE_BUTTON_DOWN -> {
                    SDL_MouseButtonEvent button = sdlEvent.button();
                    int which = button.which();
                    float x1 = button.x();
                    float y1 = button.y();
                    byte button1 = button.button();
                    QuantumClient.invoke(() -> client.onMouseButtonPress(which, x1, y1, button1, 0));
                }
                case SDLEvents.SDL_EVENT_MOUSE_BUTTON_UP -> {
                    SDL_MouseButtonEvent button = sdlEvent.button();
                    int which = button.which();
                    byte button1 = button.button();
                    QuantumClient.invoke(() -> client.onMouseButtonRelease(which, button1, 0));
                }
                case SDLEvents.SDL_EVENT_MOUSE_ADDED -> {
                    SDL_MouseDeviceEvent mouseDevice = sdlEvent.mdevice();
                    int which = mouseDevice.which();
                    QuantumClient.invoke(() -> client.addMouse(createMouse(which)));
                }
                case SDLEvents.SDL_EVENT_MOUSE_REMOVED -> {
                    SDL_MouseDeviceEvent mouseDevice = sdlEvent.mdevice();
                    int which = mouseDevice.which();
                    QuantumClient.invoke(() -> client.removeMouse(which));
                }
                case SDLEvents.SDL_EVENT_KEYBOARD_ADDED -> {
                    SDL_KeyboardDeviceEvent keyboard = sdlEvent.kdevice();
                    int which = keyboard.which();
                    QuantumClient.invoke(() -> client.addKeyboard(createKeyboard(which)));
                }
                case SDLEvents.SDL_EVENT_KEYBOARD_REMOVED -> {
                    SDL_KeyboardDeviceEvent keyboard = sdlEvent.kdevice();
                    int which = keyboard.which();
                    QuantumClient.invoke(() -> client.removeKeyboard(which));
                }
                case SDLEvents.SDL_EVENT_WINDOW_RESIZED -> {
                    try (MemoryStack stack = MemoryStack.stackPush()) {
                        IntBuffer newWidth = stack.callocInt(1);
                        IntBuffer newHeight = stack.callocInt(1);
                        SDLVideo.SDL_GetWindowSize(this.window, newWidth, newHeight);
                        onResize(newWidth.get(0), newHeight.get(0));
                        client.render();
                        SDLVideo.SDL_GL_SwapWindow(this.window);
                    }
                }
                case SDLEvents.SDL_EVENT_WINDOW_MOVED -> {
                    try (MemoryStack stack = MemoryStack.stackPush()) {
                        IntBuffer newX = stack.callocInt(1);
                        IntBuffer newY = stack.callocInt(1);
                        SDLVideo.SDL_GetWindowPosition(this.window, newX, newY);
                        onMove(window, newX.get(0), newY.get(0));
                    }
                }
            }

            return true;
        }, 0);
    }

    public Mouse createMouse(int mouseID) {
        return new Mouse(mouseID, SDLMouse.SDL_GetMouseNameForID(mouseID));
    }

    public Keyboard createKeyboard(int keyboardID) {
        return new Keyboard(keyboardID, SDLKeyboard.SDL_GetKeyboardNameForID(keyboardID));
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        if (ImGuiOverlay.isShown())
            return ImGuiOverlay.getGameWidth();
        return width;
    }

    public int getHeight() {
        if (ImGuiOverlay.isShown())
            return ImGuiOverlay.getGameHeight();
        return height;
    }

    public double getMouseX() {
        Mouse mainMouse = client.getMainMouse();
        if (mainMouse == null) return Double.NaN;
        return mainMouse.getX();
    }

    public double getMouseY() {
        Mouse mainMouse = client.getMainMouse();
        if (mainMouse == null) return Double.NaN;
        return mainMouse.getY();
    }

    public void show() {
        SDLVideo.SDL_ShowWindow(window);
        SDLMouse.SDL_CaptureMouse(false);
        SDLMouse.SDL_ShowCursor();

        SDLMouse.SDL_SetWindowRelativeMouseMode(window, true);
    }

    public void hide() {
        SDLVideo.SDL_HideWindow(window);
    }

    public void maximize() {
        SDLVideo.SDL_MaximizeWindow(window);
    }

    public void minimize() {
        SDLVideo.SDL_MinimizeWindow(window);
    }

    public void restore() {
        SDLVideo.SDL_RestoreWindow(window);
    }

    public void focus() {
        SDLVideo.SDL_RaiseWindow(window);
    }

    public void requestAttention() {
        SDLVideo.SDL_FlashWindow(window, SDLVideo.SDL_FLASH_UNTIL_FOCUSED);
    }

    public void setTitle(String title) {
        SDLVideo.SDL_SetWindowTitle(window, title);
    }

    public String getTitle() {
        return SDLVideo.SDL_GetWindowTitle(window);
    }

    public void setResizable(boolean resizable) {
        SDLVideo.SDL_SetWindowResizable(window, resizable);
    }

    public boolean isResizable() {
        long i = SDLVideo.SDL_GetWindowFlags(window);
        return (i & SDLVideo.SDL_WINDOW_RESIZABLE) == SDLVideo.SDL_WINDOW_RESIZABLE;
    }

    public boolean isFocused() {
        long i = SDLVideo.SDL_GetWindowFlags(window);
        return (i & SDLVideo.SDL_WINDOW_INPUT_FOCUS) == SDLVideo.SDL_WINDOW_INPUT_FOCUS;
    }

    public boolean isHovered() {
        return true;
    }

    public boolean isMinimized() {
        long i = SDLVideo.SDL_GetWindowFlags(window);
        return (i & SDLVideo.SDL_WINDOW_MINIMIZED) == SDLVideo.SDL_WINDOW_MINIMIZED;
    }

    public boolean isMaximized() {
        long i = SDLVideo.SDL_GetWindowFlags(window);
        return (i & SDLVideo.SDL_WINDOW_MAXIMIZED) == SDLVideo.SDL_WINDOW_MAXIMIZED;
    }

    public boolean isVisible() {
        long i = SDLVideo.SDL_GetWindowFlags(window);
        return (i & SDLVideo.SDL_WINDOW_HIDDEN) != SDLVideo.SDL_WINDOW_HIDDEN;
    }

    public boolean isBordered() {
        long i = SDLVideo.SDL_GetWindowFlags(window);
        return (i & SDLVideo.SDL_WINDOW_BORDERLESS) != SDLVideo.SDL_WINDOW_BORDERLESS;
    }

    public boolean isAutoIconify() {
        return false;
    }

    public boolean isModal() {
        long i = SDLVideo.SDL_GetWindowFlags(window);
        return (i & SDLVideo.SDL_WINDOW_MODAL) == SDLVideo.SDL_WINDOW_MODAL;
    }

    public void setAutoIconify(boolean autoIconify) {

    }

    public void setDecorated(boolean decorated) {
        SDLVideo.SDL_SetWindowBordered(window, decorated);
    }

    public void setModal(boolean floating) {
        SDLVideo.SDL_SetWindowModal(window, floating);
    }

    public void setFocusable(boolean focusable) {
        SDLVideo.SDL_SetWindowFocusable(window, focusable);
    }

    public void update() {
        SDLEvents.SDL_PumpEvents();
    }

    public void flip() {
        SDLVideo.SDL_GL_SwapWindow(window);
    }

    public GLCapabilities getCapabilities() {
        return capabilities;
    }

    public long getObjectId() {
        return window;
    }

    @Override
    public void delete() {
        SDLVideo.SDL_DestroyWindow(window);
    }

    public void setSize(int width, int height) {
        SDLVideo.SDL_SetWindowSize(window, width, height);
    }

    public void setPosition(int x, int y) {
        SDLVideo.SDL_SetWindowPosition(window, x, y);
    }

    public void setVSync(boolean vsync) {
        SDLVideo.SDL_GL_SetSwapInterval(vsync ? 1 : 0);
    }

    public boolean isVSync() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer interval = stack.callocInt(1);
            SDLVideo.SDL_GL_GetSwapInterval(interval);
            return interval.get(0) > 0;
        }
    }

    public boolean shouldClose() {
        return false;
    }

    public void captureMouse() {
        mouseCaptured = true;
    }

    public void releaseMouse() {
        mouseCaptured = false;
    }

    public boolean isMouseCaptured() {
        return mouseCaptured;
    }

    public void setMouseCaptured(boolean mouseCaptured) {
        if (this.mouseCaptured == mouseCaptured) return;
        this.mouseCaptured = mouseCaptured;
    }

    private void onResize(int width, int height) {
        this.width = width;
        this.height = height;

        if (ImGuiOverlay.isShown())
            client.onResize(ImGuiOverlay.getGameWidth(), ImGuiOverlay.getGameHeight());
        else client.onResize((int) (width / xScale), (int) (height / yScale));
    }

    private void onMove(long window, int x, int y) {
        this.x = x;
        this.y = y;
    }

    public synchronized void setFullscreen(boolean fullscreen) {
        SDLVideo.SDL_SetWindowFullscreen(window, fullscreen);
    }

    public boolean isFullscreen() {
        long flags = SDLVideo.SDL_GetWindowFlags(window);
        return (flags & SDLVideo.SDL_WINDOW_FULLSCREEN) == SDLVideo.SDL_WINDOW_FULLSCREEN;
    }

    public void refresh() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.callocInt(1);
            IntBuffer height = stack.callocInt(1);
            SDLVideo.SDL_GetWindowSizeInPixels(window, width, height);
            onResize(width.get(0), height.get(0));
        }
    }

    public void startTextInput() {
        SDLKeyboard.SDL_StartTextInput(window);
    }

    public void stopTextInput() {
        SDLKeyboard.SDL_StopTextInput(window);
    }

    public void setTextInputRect(int x, int y, int width, int height, int cursorX) {
        try (SDL_Rect.Buffer rect = SDL_Rect.calloc(1).x(x).y(y).w(width).h(height)) {
            SDLKeyboard.SDL_SetTextInputArea(window, rect, cursorX);
        }
    }

    public boolean ejectMouse(float atX, float atY) {
        if (QuantumClient.get().mouseEjected) {
            return false;
        }

        IntBuffer winX = BufferUtils.createIntBuffer(1);
        IntBuffer winY = BufferUtils.createIntBuffer(1);
        SDLVideo.SDL_GetWindowPosition(window, winX, winY);
        int scrX = (int) atX + winX.get(0);
        int scrY = (int) atY + winY.get(0);
        boolean match = isWithinAnyDisplay(scrX, scrY);
        if (!match)
            return false;

        QuantumClient.get().mouseEjected = true;
        SDLMouse.SDL_SetWindowRelativeMouseMode(window, false);
        SDLMouse.SDL_WarpMouseInWindow(window, atX, atY);
        return true;
    }

    private static boolean isWithinAnyDisplay(int scrX, int scrY) {
        IntBuffer display = SDLVideo.SDL_GetDisplays();
        boolean match = false;
        if (display == null) return false;

        try (SDL_Rect bounds = SDL_Rect.malloc();
             SDL_Point pos = SDL_Point.malloc().set(scrX, scrY)) {
            int limit = display.limit();
            for (int i = 0; i < limit; i++) {
                SDLVideo.SDL_GetDisplayUsableBounds(display.get(i), bounds);
                if (SDLRect.SDL_PointInRect(pos, bounds)) {
                    match = true;
                    break;
                }
            }
        }
        return match;
    }

    public boolean confineCursor() {
        if (!isFocused()) return false;
        SDLMouse.SDL_SetWindowRelativeMouseMode(window, true);
        return true;
    }
}
