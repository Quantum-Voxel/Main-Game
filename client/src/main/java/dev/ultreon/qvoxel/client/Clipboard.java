package dev.ultreon.qvoxel.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.sdl.SDLClipboard;

import java.nio.file.Path;

public class Clipboard {
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    public static final Path[] EMPTY_PATH_ARRAY = new Path[0];

    public @Nullable String getText() {
        return SDLClipboard.SDL_GetClipboardText();
    }

    public void setText(@NotNull String value) {
        SDLClipboard.SDL_SetClipboardText(value);
    }

    public String[] getMimeTypes() {
        PointerBuffer pointerBuffer = SDLClipboard.SDL_GetClipboardMimeTypes();
        if (pointerBuffer == null) return EMPTY_STRING_ARRAY;

        int capacity = pointerBuffer.capacity();
        String[] mimeTypes = new String[capacity];
        for (int i = 0; i < capacity; i++) {
            String mimeType = pointerBuffer.getStringUTF8(i);
            mimeTypes[i] = mimeType;
        }

        return mimeTypes;
    }
}
