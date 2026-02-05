package dev.ultreon.qvoxel.client.debug.renderers;

import dev.ultreon.qvoxel.client.debug.Renderer;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class StringRenderer implements Renderer<String> {
    @Override
    public void render(String object, @Nullable Consumer<String> setter) {
        ImString s = new ImString(object, Math.max(65536, object.getBytes(StandardCharsets.UTF_8).length));
        if (ImGui.inputText("##String", s) && setter != null) {
            setter.accept(s.get());
        }
    }
}
