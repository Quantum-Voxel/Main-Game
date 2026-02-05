package dev.ultreon.qvoxel.client.debug.renderers;

import dev.ultreon.qvoxel.client.debug.Renderer;
import imgui.ImGui;
import imgui.type.ImDouble;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

import java.util.function.Consumer;

public class Vector2dRenderer implements Renderer<Vector2d> {
    @Override
    public void render(Vector2d object, @Nullable Consumer<Vector2d> setter) {
        boolean readOnly = setter == null;
        ImDouble x = new ImDouble(object.x);
        ImDouble y = new ImDouble(object.y);

        ImGui.text("X:");
        ImGui.sameLine();
        if (ImGui.inputDouble("##Vector2d[0]", x)) {
            object.x = x.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
        ImGui.sameLine();
        ImGui.text("Y:");
        ImGui.sameLine();
        if (ImGui.inputDouble("##Vector2d[1]", y)) {
            object.y = y.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
    }
}
