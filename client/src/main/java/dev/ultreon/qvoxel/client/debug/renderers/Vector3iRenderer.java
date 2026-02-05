package dev.ultreon.qvoxel.client.debug.renderers;

import dev.ultreon.qvoxel.client.debug.Renderer;
import imgui.ImGui;
import imgui.type.ImInt;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.function.Consumer;

public class Vector3iRenderer implements Renderer<Vector3i> {
    @Override
    public void render(Vector3i object, @Nullable Consumer<Vector3i> setter) {
        boolean readOnly = setter == null;
        ImInt x = new ImInt(object.x);
        ImInt y = new ImInt(object.y);
        ImInt z = new ImInt(object.z);

        ImGui.text("X:");
        ImGui.sameLine();
        if (ImGui.inputInt("##Vector3i[0]", x)) {
            object.x = x.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
        ImGui.sameLine();
        ImGui.text("Y:");
        ImGui.sameLine();
        if (ImGui.inputInt("##Vector3i[1]", y)) {
            object.y = y.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
        ImGui.sameLine();
        ImGui.text("Z:");
        ImGui.sameLine();
        if (ImGui.inputInt("##Vector3i[2]", z)) {
            object.z = z.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
    }
}
