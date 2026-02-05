package dev.ultreon.qvoxel.client.gui.widget;

import dev.ultreon.qvoxel.client.gui.Widget;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.sound.SoundEvent;

public class TextSpoilerWidget extends Widget {
    private String text;
    private boolean shown;
    private String showMessage = "Show Spoiler";
    private int height;
    private int width;

    public TextSpoilerWidget(String text) {
        this.text = text;
        shown = false;
    }

    public TextSpoilerWidget() {
        this("");
    }

    public void setText(String text) {
        this.text = text;
        width = font.widthOf(text);
        height = font.heightOf(text);
        shown = false;
    }

    public String getText() {
        return text;
    }

    public void setShown(boolean shown) {
        this.shown = shown;
    }

    public boolean isShown() {
        return shown;
    }

    public void setShowMessage(String showMessage) {
        this.showMessage = showMessage;
    }

    public String getShowMessage() {
        return showMessage;
    }

    @Override
    public void render(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
        int textWidth = width;
        int textHeight = height;
        int y = size.y / 2 - font.lineHeight / 2;
        if (!shown) {
            boolean isSpoilerHovered = isPosWithin(mouseX, mouseY, size.x / 2 - textWidth / 2 - 5, y, textWidth + 10, font.lineHeight + 2);
            int color = isSpoilerHovered ? 0xFFa0a0a0 : 0xFF808080;
            renderer.fillRect(size.x / 2 - textWidth / 2 - 5, y, textWidth + 10, textHeight + 2, color);
            renderer.drawCenteredString(showMessage, size.x / 2, y + 8, 0xFFFFFFFF);
        } else {
            renderer.fillRect(size.x / 2 - textWidth / 2 - 5, y, textWidth + 10, textHeight + 2, 0x20ffffff);
            renderer.drawCenteredString(text, size.x / 2, y + 8, 0xFF808080);
        }
    }

    private boolean isPosWithin(int posX, int posY, int minX, int minY, int width, int height) {
        return posX >= minX && posX < minX + width && posY >= minY && posY < minY + height;
    }

    @Override
    public boolean onMouseButtonPress(int mouseX, int mouseY, int button, int mods) {
        int i = font.widthOf(text);
        if (isPosWithin(mouseX, mouseY, size.x / 2 - i / 2 - 5, size.y / 2 - 10, i + 10, font.lineHeight + 2)) {
            client.playSound(SoundEvent.UI_MENU_TICK, 1.0f);
            shown = !shown;
        }

        return super.onMouseButtonPress(mouseX, mouseY, button, mods);
    }
}
