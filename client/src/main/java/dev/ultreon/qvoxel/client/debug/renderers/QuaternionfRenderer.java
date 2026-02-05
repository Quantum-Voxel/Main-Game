package dev.ultreon.qvoxel.client.debug.renderers;

import dev.ultreon.qvoxel.client.debug.Renderer;
import imgui.ImGui;
import imgui.type.ImFloat;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.function.Consumer;

public class QuaternionfRenderer implements Renderer<Quaternionf> {
    private boolean eulerMode = true;

    @Override
    public void render(Quaternionf object, @Nullable Consumer<Quaternionf> setter) {
        if (ImGui.checkbox("##Euler Mode", eulerMode)) {
            eulerMode = !eulerMode;
        }

        if (eulerMode) {
            Vector3f euler = new Vector3f();
            object.getEulerAnglesXYZ(euler);

            ImFloat x = new ImFloat(euler.x);
            ImFloat y = new ImFloat(euler.y);
            ImFloat z = new ImFloat(euler.z);

            boolean edited = false;

            ImGui.text("X:");
            ImGui.sameLine();
            if (ImGui.inputFloat("##Vector3f[0]", x)) {
                euler.x = x.get();
                edited = true;
            }
            ImGui.sameLine();
            ImGui.text("Y:");
            ImGui.sameLine();
            if (ImGui.inputFloat("##Vector3f[1]", y)) {
                euler.y = y.get();
                edited = true;
            }
            ImGui.sameLine();
            ImGui.text("Z:");
            ImGui.sameLine();
            if (ImGui.inputFloat("##Vector3f[2]", z)) {
                euler.z = z.get();
                edited = true;
            }

            if (edited) {
                if (setter == null) {
                    setter = quaternionf -> {
                        quaternionf.identity();
                        quaternionf.rotateXYZ(euler.x, euler.y, euler.z);
                    };
                }
                if (!setter.equals(object)) {
                    setter.accept(object);
                }
            }
        } else {
            Quaternionf quat = new Quaternionf(object);
            boolean readOnly = setter == null;
            ImFloat x = new ImFloat(quat.x);
            ImFloat y = new ImFloat(quat.y);
            ImFloat z = new ImFloat(quat.z);
            ImFloat w = new ImFloat(quat.w);

            ImGui.text("X:");
            ImGui.sameLine();
            if (ImGui.inputFloat("##Vector4f[0]", x)) {
                quat.x = x.get();
                if (!readOnly) {
                    setter.accept(quat);
                }
            }
            ImGui.sameLine();
            ImGui.text("Y:");
            ImGui.sameLine();
            if (ImGui.inputFloat("##Vector4f[1]", y)) {
                quat.y = y.get();
                if (!readOnly) {
                    setter.accept(quat);
                }
            }
            ImGui.sameLine();
            ImGui.text("Z:");
            ImGui.sameLine();
            if (ImGui.inputFloat("##Vector4f[2]", z)) {
                quat.z = z.get();
                if (!readOnly) {
                    setter.accept(quat);
                }
            }
            ImGui.sameLine();
            ImGui.text("W:");
            ImGui.sameLine();
            if (ImGui.inputFloat("##Vector4f[3]", w)) {
                quat.w = w.get();
                if (!readOnly) {
                    setter.accept(quat);
                }
            }
        }
    }
}
