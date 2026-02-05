package dev.ultreon.qvoxel.client.debug.renderers;

import dev.ultreon.qvoxel.client.debug.Renderer;
import imgui.ImGui;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public record ErrorRenderer<T>(Throwable throwable) implements Renderer<T> {
    @Override
    public void render(T object, @Nullable Consumer<T> setter) {
        ImGui.textColored(1, 0, 0, 1, throwable.toString());
    }
}
