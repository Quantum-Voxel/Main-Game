package dev.ultreon.qvoxel.client.gui.widget;

import dev.ultreon.qvoxel.client.gui.Widget;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.item.ItemStack;
import dev.ultreon.qvoxel.menu.ItemSlot;

import static dev.ultreon.qvoxel.client.gui.HotbarOverlay.WIDGETS_TEX;

public class InputSlotWidget extends Widget {
    private final ItemSlot slot;

    public InputSlotWidget(ItemSlot slot) {
        this.slot = slot;
        size.set(18, 18);
    }

    @Override
    public void render(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
        ItemStack item = slot.getItem();
        renderer.setFont(client.smallFont);
        renderer.drawTexture(WIDGETS_TEX, 0, 0, 18, 18, 0, 109, 18, 18, 256, 256);
        if (isHovered)
            renderer.drawTexture(WIDGETS_TEX, 0, 0, 18, 18, 0, 42, 18, 18, 256, 256);
        client.itemRenderer.render(item.getItem(), renderer, -5, 17);
        int count = item.getCount();
        if (!item.isEmpty() && count > 1) {
            String text = Integer.toString(count);
            int width = client.smallFont.widthOf(text);
            renderer.drawTexture(WIDGETS_TEX, (int) (18f - width - 2), 9, width + 2, 9, 19, 51, width + 2, 9, 256, 256);
            if (isHovered)
                renderer.drawTexture(WIDGETS_TEX, (int) (18f - width - 3), 8, width + 3, 10, 38, 50, width + 3, 10, 256, 256);
            renderer.drawString(text, 20 - width - 1, 17, 0xFFFFFFFF, false);
        }
        renderer.setFont(client.font);
    }

    public ItemSlot getSlot() {
        return slot;
    }
}
