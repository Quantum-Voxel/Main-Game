package dev.ultreon.qvoxel.client.debug.renderers;

import dev.ultreon.qvoxel.client.debug.Renderer;
import imgui.ImGui;
import imgui.type.ImFloat;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

import java.util.function.Consumer;

public class Vector4fRenderer implements Renderer<Vector4f> {
    @Override
    public void render(Vector4f object, @Nullable Consumer<Vector4f> setter) {
        boolean readOnly = setter == null;
        ImFloat x = new ImFloat(object.x);
        ImFloat y = new ImFloat(object.y);
        ImFloat z = new ImFloat(object.z);
        ImFloat w = new ImFloat(object.w);

        ImGui.text("X:");
        ImGui.sameLine();
        if (ImGui.inputFloat("##Vector4f[0]", x)) {
            object.x = x.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
        ImGui.sameLine();
        ImGui.text("Y:");
        ImGui.sameLine();
        if (ImGui.inputFloat("##Vector4f[1]", y)) {
            object.y = y.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
        ImGui.sameLine();
        ImGui.text("Z:");
        ImGui.sameLine();
        if (ImGui.inputFloat("##Vector4f[2]", z)) {
            object.z = z.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
        ImGui.sameLine();
        ImGui.text("W:");
        ImGui.sameLine();
        if (ImGui.inputFloat("##Vector4f[3]", w)) {
            object.w = w.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
    }
}
