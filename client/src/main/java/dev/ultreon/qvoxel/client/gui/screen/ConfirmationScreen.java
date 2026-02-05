package dev.ultreon.qvoxel.client.gui.screen;

import dev.ultreon.libs.translations.v1.Language;
import dev.ultreon.qvoxel.client.gui.Screen;
import dev.ultreon.qvoxel.client.gui.widget.TextButtonWidget;
import dev.ultreon.qvoxel.client.render.GuiRenderer;

public class ConfirmationScreen extends Screen {
    private final String message;
    private final Runnable onAccept;
    private TextButtonWidget yesBtn;
    private TextButtonWidget noBtn;

    public ConfirmationScreen(String message, Runnable onAccept) {
        super("Confirm");
        this.message = message;
        this.onAccept = onAccept;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void init() {
        yesBtn = addWidget(new TextButtonWidget(Language.translate("quantum.screen.confirm.yes")));
        noBtn = addWidget(new TextButtonWidget(Language.translate("quantum.screen.confirm.no")));

        yesBtn.setCallback(this::accept);
        noBtn.setCallback(this::close);
    }

    public void accept() {
        onAccept.run();
        close();
    }

    @Override
    public void resized() {
        int height = font.heightOf(message) + 30;

        yesBtn.resize(98, 20);
        yesBtn.setPosition(size.x / 2 - 100, size.y / 2 - 75 + height);
        noBtn.resize(98, 20);
        noBtn.setPosition(size.x / 2 +  2, size.y / 2 - 75 + height);
    }

    @Override
    public void renderForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
        super.renderForeground(renderer, mouseX, mouseY, partialTicks);

        int width = font.widthOf(message) + 30;
        int height = font.heightOf(message);
        renderer.drawLabel(size.x / 2 - width / 2, size.y / 2 - 75, width, height + 15);
        renderer.drawCenteredString(message, size.x / 2, size.y / 2 - 60, 0xFFFFFFFF);
    }
}
