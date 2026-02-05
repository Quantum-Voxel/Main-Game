package dev.ultreon.qvoxel.client.debug.renderers;

import dev.ultreon.qvoxel.client.debug.Renderer;
import imgui.ImGui;
import imgui.type.ImBoolean;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class BooleanRenderer implements Renderer<Boolean> {
    @Override
    public void render(Boolean object, @Nullable Consumer<Boolean> setter) {
        ImBoolean b = new ImBoolean(object);
        if (ImGui.checkbox("##Boolean", b) && setter != null) {
            setter.accept(b.get());
        }
    }
}
