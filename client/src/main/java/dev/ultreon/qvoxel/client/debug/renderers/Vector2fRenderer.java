package dev.ultreon.qvoxel.client.debug.renderers;

import dev.ultreon.qvoxel.client.debug.Renderer;
import imgui.ImGui;
import imgui.type.ImDouble;
import imgui.type.ImFloat;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2f;

import java.util.function.Consumer;

public class Vector2fRenderer implements Renderer<Vector2f> {
    @Override
    public void render(Vector2f object, @Nullable Consumer<Vector2f> setter) {
        boolean readOnly = setter == null;
        ImFloat x = new ImFloat(object.x);
        ImFloat y = new ImFloat(object.y);

        ImGui.text("X:");
        ImGui.sameLine();
        if (ImGui.inputFloat("##Vector2f[0]", x)) {
            object.x = x.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
        ImGui.sameLine();
        ImGui.text("Y:");
        ImGui.sameLine();
        if (ImGui.inputFloat("##Vector2f[1]", y)) {
            object.y = y.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
    }
}
