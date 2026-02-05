package dev.ultreon.qvoxel.client.gui.screen;

import dev.ultreon.libs.translations.v1.Language;
import dev.ultreon.qvoxel.client.gui.Screen;
import dev.ultreon.qvoxel.client.gui.widget.ColorChooserWidget;
import dev.ultreon.qvoxel.client.gui.widget.TextButtonWidget;
import dev.ultreon.qvoxel.client.gui.widget.TextInputWidget;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.server.WorldSaveInfo;
import dev.ultreon.qvoxel.server.WorldStorage;

import java.io.IOException;

import static dev.ultreon.qvoxel.client.gui.screen.ListWidget.BACKGROUND;

public class WorldEditScreen extends Screen {
    private final WorldStorage storage;
    private final WorldSaveCallback callback;
    private TextInputWidget nameInput;
    private TextButtonWidget backButton;
    private TextButtonWidget saveButton;
    private WorldSaveInfo info;
    private ColorChooserWidget colorChooser;

    public WorldEditScreen(WorldStorage storage, WorldSaveCallback callback) {
        super(Language.translate("quantum.screen.world_edit"));
        this.storage = storage;
        this.callback = callback;
    }

    @Override
    public void init() {
        info = storage.loadInfo();

        String name = info.getName();

        nameInput = addWidget(new TextInputWidget());
        nameInput.setHint(Language.translate("quantum.screen.world_creation.name"));
        nameInput.setMaxLength(64);
        nameInput.setShadow(false);
        nameInput.setValue(name);

        colorChooser = addWidget(new ColorChooserWidget());
        colorChooser.setColor(info.getColorArgb());

        backButton = addWidget(new TextButtonWidget(Language.translate("quantum.ui.back")));
        backButton.setCallback(client::popGuiLayer);

        saveButton = addWidget(new TextButtonWidget(Language.translate("quantum.ui.save")));
        saveButton.setCallback(this::saveWorld);
    }

    private void saveWorld() {
        close();
        info.setName(nameInput.getValue());
        info.setColorArgb(colorChooser.getColor().toARGB());
        try {
            storage.saveInfo(info);
        } catch (IOException e) {
            client.showScreen(new NotificationScreen(Language.translate("quantum.screen.world.failed_to_save"), e.getMessage()));
            return;
        }

        callback.onSave(storage, info);
    }

    @Override
    public void resized() {
        nameInput.setPosition(size.x / 2 - 100, size.y / 2 - 50);
        nameInput.resize(200, 20);

        colorChooser.setPosition(size.x / 2 - 100, size.y / 2);
        colorChooser.resize(200, 70);

        backButton.setPosition(size.x / 2 - 100, size.y / 2 + 80);
        backButton.resize(95, 20);

        saveButton.setPosition(size.x / 2 + 5, size.y / 2 + 80);
        saveButton.resize(95, 20);
    }

    @Override
    public void renderForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
        super.renderForeground(renderer, mouseX, mouseY, partialTicks);

        renderer.drawNinePatch(BACKGROUND, size.x / 2 - 110, size.y / 2 - 60, 220, 174, 0, 0, 21, 21, 21, 21, 7);
    }

    @FunctionalInterface
    public interface WorldSaveCallback {
        void onSave(WorldStorage storage, WorldSaveInfo info);
    }
}
