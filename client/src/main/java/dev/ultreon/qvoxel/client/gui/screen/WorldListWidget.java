package dev.ultreon.qvoxel.client.gui.screen;

import dev.ultreon.qvoxel.client.render.Color;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.server.WorldSaveInfo;
import dev.ultreon.qvoxel.server.WorldStorage;

import java.util.List;

public class WorldListWidget extends ListWidget<WorldListWidget.Entry> {
    public static class Entry extends ListWidget.Entry {
        private final String name;
        private final WorldStorage world;
        private Color tmpColor = new Color();
        private WorldSaveInfo worldSaveInfo;

        public Entry(ListWidget<?> listWidget, String name, WorldStorage world) {
            super(listWidget, 20);
            this.name = name;
            this.world = world;
            worldSaveInfo = world.loadInfo();
        }

        public Entry(ListWidget<?> listWidget, WorldStorage world) {
            this(listWidget, world.getName(), world);
        }

        public String getName() {
            return name;
        }

        public WorldStorage getWorld() {
            return world;
        }

        @Override
        public void render(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
            if (worldSaveInfo.isFavorite()) {
                tmpColor.fromARGB(0xFF000000 | worldSaveInfo.getColorArgb());
                tmpColor.a = 1f;
                tmpColor.clampBrightness(0.2f, 0.8f).brighten(1.4f);
                renderer.setTextureColor(tmpColor.toARGB());
                renderLight = true;
            } else {
                renderLight = false;
            }
            super.render(renderer, mouseX, mouseY, partialTicks);
            renderer.setTextureColor(0xFFFFFFFF);

            int lineHeight = client.font.lineHeight;

            try (var lines = name.lines()) {
                int index = 0;
                List<String> list = lines.toList();
                for (String line : list) {
                    renderer.drawCenteredString(line, size.x / 2, ((int) position.y) + size.y / 2 - list.size() + 3 + lineHeight * index, 0xFFFFFFFF);
                    index++;
                }
            }
        }
    }
}
