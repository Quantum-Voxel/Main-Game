package dev.ultreon.qvoxel.client.debug.renderers;

import dev.ultreon.qvoxel.client.debug.Renderer;
import imgui.ImGui;
import imgui.type.ImInt;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4i;

import java.util.function.Consumer;

public class Vector4iRenderer implements Renderer<Vector4i> {
    @Override
    public void render(Vector4i object, @Nullable Consumer<Vector4i> setter) {
        boolean readOnly = setter == null;
        ImInt x = new ImInt(object.x);
        ImInt y = new ImInt(object.y);
        ImInt z = new ImInt(object.z);
        ImInt w = new ImInt(object.w);

        ImGui.text("X:");
        ImGui.sameLine();
        if (ImGui.inputInt("##Vector4i[0]", x)) {
            object.x = x.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
        ImGui.sameLine();
        ImGui.text("Y:");
        ImGui.sameLine();
        if (ImGui.inputInt("##Vector4i[1]", y)) {
            object.y = y.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
        ImGui.sameLine();
        ImGui.text("Z:");
        ImGui.sameLine();
        if (ImGui.inputInt("##Vector4i[2]", z)) {
            object.z = z.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
        ImGui.sameLine();
        ImGui.text("W:");
        ImGui.sameLine();
        if (ImGui.inputInt("##Vector4i[3]", w)) {
            object.w = w.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
    }
}
