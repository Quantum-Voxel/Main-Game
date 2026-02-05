package dev.ultreon.qvoxel.client.debug.renderers;

import dev.ultreon.qvoxel.client.debug.Renderer;
import imgui.ImGui;
import imgui.type.ImDouble;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4d;

import java.util.function.Consumer;

public class Vector4dRenderer implements Renderer<Vector4d> {
    @Override
    public void render(Vector4d object, @Nullable Consumer<Vector4d> setter) {
        boolean readOnly = setter == null;
        ImDouble x = new ImDouble(object.x);
        ImDouble y = new ImDouble(object.y);
        ImDouble z = new ImDouble(object.z);
        ImDouble w = new ImDouble(object.w);

        ImGui.text("X:");
        ImGui.sameLine();
        if (ImGui.inputDouble("##Vector4d[0]", x)) {
            object.x = x.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
        ImGui.sameLine();
        ImGui.text("Y:");
        ImGui.sameLine();
        if (ImGui.inputDouble("##Vector4d[1]", y)) {
            object.y = y.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
        ImGui.sameLine();
        ImGui.text("Z:");
        ImGui.sameLine();
        if (ImGui.inputDouble("##Vector4d[2]", z)) {
            object.z = z.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
        ImGui.sameLine();
        ImGui.text("W:");
        ImGui.sameLine();
        if (ImGui.inputDouble("##Vector4d[3]", w)) {
            object.w = w.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
    }
}
