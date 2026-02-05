package dev.ultreon.qvoxel.client.debug;

import dev.ultreon.qvoxel.client.render.Window;
import org.lwjgl.sdl.SDLKeyboard;

import java.nio.ByteBuffer;
import java.util.BitSet;

public class InputSystem {
    private static Window window;
    private static final BitSet pressed = new BitSet();
    private static final BitSet wasPressed = new BitSet();
    private static final BitSet mousePressed = new BitSet();
    private static final BitSet mouseWasPressed = new BitSet();
    
    public static void init(Window window) {
        if (InputSystem.window != null) {
            throw new IllegalStateException("InputSystem already initialized");
        }
        
        InputSystem.window = window;
    }
    
    public static void update() {
        wasPressed.clear();
        wasPressed.or(pressed);
        pressed.clear();
        
        long objectId = window.getObjectId();
        ByteBuffer byteBuffer = SDLKeyboard.SDL_GetKeyboardState();
//        for (int i = 0; i < byteBuffer; i++) {
//            if (GLFW.glfwGetKey(objectId, i) == GLFW.GLFW_PRESS) {
//                pressed.set(i);
//            }
//        }
        
        mouseWasPressed.clear();
        mouseWasPressed.or(mousePressed);
        mousePressed.clear();
        
        for (int i = 0; i < 6; i++) {
//            if (SDLMouse.SDL_glfwGetMouseButton(objectId, i) == GLFW.GLFW_PRESS) {
//                mousePressed.set(i);
//            }
        }
    }
    
    public static boolean isKeyJustPressed(int glfwKey) {
        return pressed.get(glfwKey) && !wasPressed.get(glfwKey);
    }
    
    public static boolean isKeyDown(int glfwKey) {
        return pressed.get(glfwKey);
    }
    
    public static boolean isKeyJustReleased(int glfwKey) {
        return !pressed.get(glfwKey) && wasPressed.get(glfwKey);
    }
    
    public static boolean isButtonJustPressed(int glfwButton) {
        return mousePressed.get(glfwButton) && !mouseWasPressed.get(glfwButton);
    }
    
    public static boolean isButtonDown(int glfwButton) {
        return mousePressed.get(glfwButton);
    }
    
    public static boolean isButtonJustReleased(int glfwButton) {
        return !mousePressed.get(glfwButton) && mouseWasPressed.get(glfwButton);
    }
}
