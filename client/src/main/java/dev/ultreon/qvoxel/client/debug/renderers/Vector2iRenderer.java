package dev.ultreon.qvoxel.client.debug.renderers;

import dev.ultreon.qvoxel.client.debug.Renderer;
import imgui.ImGui;
import imgui.type.ImInt;
import imgui.type.ImInt;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.function.Consumer;

public class Vector2iRenderer implements Renderer<Vector2i> {
    @Override
    public void render(Vector2i object, @Nullable Consumer<Vector2i> setter) {
        boolean readOnly = setter == null;
        ImInt x = new ImInt(object.x);
        ImInt y = new ImInt(object.y);

        ImGui.text("X:");
        ImGui.sameLine();
        if (ImGui.inputInt("##Vector2i[0]", x)) {
            object.x = x.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
        ImGui.sameLine();
        ImGui.text("Y:");
        ImGui.sameLine();
        if (ImGui.inputInt("##Vector2i[1]", y)) {
            object.y = y.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
    }
}
