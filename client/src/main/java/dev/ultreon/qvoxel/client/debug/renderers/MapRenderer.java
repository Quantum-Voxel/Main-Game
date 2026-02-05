package dev.ultreon.qvoxel.client.debug.renderers;

import dev.ultreon.qvoxel.client.debug.ImGuiOverlay;
import dev.ultreon.qvoxel.client.debug.Renderer;
import imgui.ImGui;
import imgui.flag.ImGuiTableFlags;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;

public class MapRenderer implements Renderer<Map<?, ?>> {
    @Override
    public void render(Map<?, ?> object, @Nullable Consumer<Map<?, ?>> setter) {
        if (ImGui.beginTable("##Object", 2, ImGuiTableFlags.Borders)) {
            ImGui.pushID("##Object");
            ImGui.tableHeadersRow();
            ImGui.tableSetColumnIndex(0);
            ImGui.text("Key");
            ImGui.tableSetColumnIndex(1);
            ImGui.text("Value");
            for (Object e : object.entrySet()) {
                if (!(e instanceof Map.Entry<?, ?> entry))
                    continue;

                ImGui.tableNextRow();
                ImGui.tableSetColumnIndex(0);
                ImGui.text(entry.getKey().toString());
                ImGui.tableSetColumnIndex(1);
                ImGuiOverlay.renderComponent(entry.getValue());
            }
            ImGui.popID();
            ImGui.endTable();
        }
    }
}
