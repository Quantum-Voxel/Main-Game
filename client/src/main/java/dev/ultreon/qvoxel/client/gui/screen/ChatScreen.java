package dev.ultreon.qvoxel.client.gui.screen;

import dev.ultreon.libs.translations.v1.Language;
import dev.ultreon.qvoxel.client.ClientCommands;
import dev.ultreon.qvoxel.client.CommandExecutor;
import dev.ultreon.qvoxel.client.gui.Overlays;
import dev.ultreon.qvoxel.client.gui.Screen;
import dev.ultreon.qvoxel.client.gui.Widget;
import dev.ultreon.qvoxel.client.gui.widget.TextInputWidget;
import dev.ultreon.qvoxel.client.render.GuiRenderer;

public class ChatScreen extends Screen {
    private TextInputWidget textInput;

    public ChatScreen() {
        super(Language.translate("quantum.screen.chat"));
    }

    @Override
    public void init() {
        textInput = new TextInputWidget();
        addWidget(textInput);

        super.changeFocus(textInput);
    }

    @Override
    public void resized() {
        textInput.resize(size.x - 20, 20);
        textInput.setPosition(10, size.y - 30);

        textInput.setMaxLength(128);
        textInput.setOnSubmit(widget -> {
            String message = widget.getValue();
            widget.setValue("");
            if (message.startsWith("/")) {
                String[] cmdAndArgs = message.substring(1).split(" ", 2);
                String cmd = cmdAndArgs[0];
                String[] args;
                if (cmdAndArgs.length > 1) {
                    args = cmdAndArgs[1].split(" ");
                } else {
                    args = new String[0];
                }

                CommandExecutor command = ClientCommands.get(cmd);
                if (command != null) {
                    command.execute(client.players.getFirst(), args);
                    return;
                }
            }
            client.players.getFirst().sendChatMessage(message);
        });
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public void renderForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
        super.renderForeground(renderer, mouseX, mouseY, partialTicks);

        Overlays.CHAT.renderDirect(renderer, partialTicks, 40);
    }

    @Override
    public void changeFocus(Widget widget) {
        // no-op
    }

    @Override
    public boolean doesPauseGame() {
        return false;
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
