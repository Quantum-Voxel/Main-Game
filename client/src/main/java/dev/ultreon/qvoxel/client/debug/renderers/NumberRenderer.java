package dev.ultreon.qvoxel.client.debug.renderers;

import dev.ultreon.qvoxel.client.debug.Renderer;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImDouble;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public class NumberRenderer implements Renderer<Number> {

    @Override
    public void render(Number object, @Nullable Consumer<Number> setter) {
        boolean readOnly = setter == null;
        if (Objects.requireNonNull(object) instanceof Integer) {
            ImInt i = new ImInt((int) object);
            if (!readOnly) {
                if (ImGui.inputInt("## Value", i, 1, 5000, readOnly ? ImGuiInputTextFlags.ReadOnly : 0)) {
                    setter.accept(i.get());
                }
            } else {
                ImGui.text(String.valueOf(object));
            }
        } else if (object instanceof Float) {
            ImFloat f = new ImFloat((float) object);
            if (!readOnly) {
                if (ImGui.inputFloat("## Value", f, 0.001f, 1, "%.3f", readOnly ? ImGuiInputTextFlags.ReadOnly : 0)) {
                    setter.accept(f.get());
                }
            } else {
                ImGui.text(String.valueOf(object));
            }
        } else if (object instanceof Double) {
            ImDouble d = new ImDouble((double) object);
            if (!readOnly) {
                if (ImGui.inputDouble("## Value", d, 0.001, 1, "%.3f", readOnly ? ImGuiInputTextFlags.ReadOnly : 0)) {
                    setter.accept(d.get());
                }
            } else {
                ImGui.text(String.valueOf(object));
            }
        } else if (object instanceof Long) {
            ImString l = new ImString(String.valueOf(object));
            if (!readOnly) {
                if (ImGui.inputText("## Value", l, readOnly ? ImGuiInputTextFlags.ReadOnly : 0)) {
                    try {
                        setter.accept(Long.parseLong(l.get()));
                    } catch (NumberFormatException ignored) {

                    }
                }
            } else {
                ImGui.text(String.valueOf(object));
            }
        } else if (object instanceof Short) {
            ImInt s = new ImInt((int) object);
            if (!readOnly) {
                if (ImGui.inputInt("## Value", s, 1, 5000, readOnly ? ImGuiInputTextFlags.ReadOnly : 0)) {
                    setter.accept((short) s.get());
                }
            } else {
                ImGui.text(String.valueOf(object));
            }
        } else if (object instanceof Byte) {
            ImInt b = new ImInt((int) object);
            if (!readOnly) {
                if (ImGui.inputInt("## Value", b, 1, 20, readOnly ? ImGuiInputTextFlags.ReadOnly : 0)) {
                    setter.accept((byte) b.get());
                }
            } else {
                ImGui.text(String.valueOf(object));
            }
        } else {
            ImGui.text(String.valueOf(object));
        }
    }
}
