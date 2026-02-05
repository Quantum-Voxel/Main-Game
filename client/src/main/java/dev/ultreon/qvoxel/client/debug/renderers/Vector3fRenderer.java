package dev.ultreon.qvoxel.client.debug.renderers;

import dev.ultreon.qvoxel.client.debug.Renderer;
import imgui.ImGui;
import imgui.type.ImFloat;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class Vector3fRenderer implements Renderer<Vector3f> {
    @Override
    public void render(Vector3f object, @Nullable Consumer<Vector3f> setter) {
        boolean readOnly = setter == null;
        ImFloat x = new ImFloat(object.x);
        ImFloat y = new ImFloat(object.y);
        ImFloat z = new ImFloat(object.z);

        ImGui.text("X:");
        ImGui.sameLine();
        if (ImGui.inputFloat("##Vector3f[0]", x)) {
            object.x = x.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
        ImGui.sameLine();
        ImGui.text("Y:");
        ImGui.sameLine();
        if (ImGui.inputFloat("##Vector3f[1]", y)) {
            object.y = y.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
        ImGui.sameLine();
        ImGui.text("Z:");
        ImGui.sameLine();
        if (ImGui.inputFloat("##Vector3f[2]", z)) {
            object.z = z.get();
            if (!readOnly) {
                setter.accept(object);
            }
        }
    }
}
