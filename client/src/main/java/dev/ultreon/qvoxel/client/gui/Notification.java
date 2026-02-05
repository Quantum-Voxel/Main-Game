package dev.ultreon.qvoxel.client.gui;

import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.render.GuiRenderer;

public record Notification(
        String title,
        String message,
        long created
) {
    public Notification(String message, String title) {
        this(title, message, System.currentTimeMillis());
    }

    public int render(GuiRenderer renderer, int y) {
        FontRenderer fontRenderer = QuantumClient.get().font;
        int width = fontRenderer.widthOf(title);
        int width1 = fontRenderer.widthOf(message);
        renderer.drawDisplay(5, 5 + y, Math.max(width1, width), 22);
        renderer.drawString(title, 5, 5 + y);
        renderer.drawString(message, 5, 5 + y + 11);
        return y + 22;
    }
}
