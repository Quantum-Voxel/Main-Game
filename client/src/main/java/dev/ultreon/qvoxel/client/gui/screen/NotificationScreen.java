package dev.ultreon.qvoxel.client.gui.screen;

import dev.ultreon.qvoxel.client.gui.Screen;
import dev.ultreon.qvoxel.client.gui.widget.TextButtonWidget;
import dev.ultreon.qvoxel.client.render.GuiRenderer;

import java.util.Arrays;
import java.util.stream.Stream;

public class NotificationScreen extends Screen {
    private final String title;
    private final String message;
    private TextButtonWidget okButton;

    public NotificationScreen(String title, String message) {
        super("Notification");
        this.title = title;
        this.message = message;
    }

    public static void showMessage(String translate) {
        new NotificationScreen("Notification", translate).open();
    }

    @Override
    public void init() {
        okButton = addWidget(new TextButtonWidget("OK"));
        okButton.setCallback(client::popGuiLayer);
    }

    @Override
    public void resized() {
        if (okButton != null) {
            okButton.resize(100, 20);
            okButton.setPosition(size.x / 2 - 50, size.y - 30);
        }
    }

    @Override
    public void renderForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
        super.renderForeground(renderer, mouseX, mouseY, partialTicks);

        String message = this.message;

        int lineHeight = client.font.lineHeight;
        int height = lineHeight;
        int width = 0;
        try (Stream<String> lines = Arrays.stream(message.split("(\r\n|\n|\r)"))) {
            for (String line : lines.toList()) {
                height += lineHeight;
                width = Math.max(width, client.font.widthOf(line));
            }
        }

        renderer.drawLabel(size.x / 2 - width / 2 - 10, size.y / 2 - height / 2 - 10, width + 20, height + 20);
        renderer.drawCenteredString(getTitle(), size.x / 2, size.y / 2 - height / 2 + lineHeight / 2, 0xFFFFFFFF);
        renderer.drawCenteredString(message, size.x / 2, size.y / 2 - height / 2 + lineHeight + lineHeight / 2, 0xFF6080FF);
    }

    @Override
    public boolean canCloseWithEscape() {
        return false;
    }
}
