package dev.ultreon.qvoxel.client.debug.renderers;

import dev.ultreon.qvoxel.client.debug.Renderer;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImFloat;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.function.Consumer;

public class Matrix4fRenderer implements Renderer<Matrix4f> {

    @Override
    public void render(Matrix4f object, @Nullable Consumer<Matrix4f> setter) {
        boolean readOnly = setter == null;
        float[] matrix = new float[16];
        object.get(matrix);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                ImGui.text(String.format("m%d%d:", i, j));
                ImGui.sameLine();
                ImFloat matrixElement = new ImFloat(matrix[i * 4 + j]);
                ImGui.setNextItemWidth(100f);
                if (ImGui.inputFloat("##Matrix4f[" + i + "][" + j + "]", matrixElement, 0.01f, readOnly ? ImGuiInputTextFlags.ReadOnly : 0)) {
                    matrix[i * 4 + j] = matrixElement.get();
                    object.set(matrix);
                }
                if (j < 3) ImGui.sameLine();
            }
        }
    }
}
