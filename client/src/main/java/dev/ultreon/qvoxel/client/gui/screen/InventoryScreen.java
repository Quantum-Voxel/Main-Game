package dev.ultreon.qvoxel.client.gui.screen;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.menu.ContainerMenu;

public class InventoryScreen extends MenuScreen {
    public InventoryScreen(ContainerMenu menu, String title) {
        super(menu, title);
    }

    @Override
    public int getGuiWidth() {
        return 180;
    }

    @Override
    public int getGuiHeight() {
        return 108;
    }

    @Override
    public void renderBackground(GuiRenderer renderer) {
        super.renderBackground(renderer);

        renderer.drawTexture(CommonConstants.id("textures/gui/container/inventory.png"), getGuiX(), getGuiY(), 180, 107, 0, 0, 180, 108, 256, 256);
    }
}
