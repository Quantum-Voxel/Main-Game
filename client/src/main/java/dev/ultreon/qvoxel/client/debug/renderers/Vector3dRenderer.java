package dev.ultreon.qvoxel.client.debug.renderers;

import dev.ultreon.qvoxel.client.debug.Renderer;
import imgui.ImGui;
import imgui.type.ImDouble;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.function.Consumer;

public class Vector3dRenderer implements Renderer<Vector3d> {
    @Override
    public void render(Vector3d object, @Nullable Consumer<Vector3d> setter) {
        boolean readOnly = setter == null;
        ImDouble x = new ImDouble(object.x);
        ImDouble y = new ImDouble(object.y);
        ImDouble z = new ImDouble(object.z);

        ImGui.text("X:");
        ImGui.sameLine();
        if (ImGui.inputDouble("##Vector3d[0]", x)) {
            object.x = x.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
        ImGui.sameLine();
        ImGui.text("Y:");
        ImGui.sameLine();
        if (ImGui.inputDouble("##Vector3d[1]", y)) {
            object.y = y.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
        ImGui.sameLine();
        ImGui.text("Z:");
        ImGui.sameLine();
        if (ImGui.inputDouble("##Vector3d[2]", z)) {
            object.z = z.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
    }
}
