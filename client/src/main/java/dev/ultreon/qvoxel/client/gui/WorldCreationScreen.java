package dev.ultreon.qvoxel.client.gui;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.libs.translations.v1.Language;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.gui.widget.TextButtonWidget;
import dev.ultreon.qvoxel.client.gui.widget.TextInputWidget;
import dev.ultreon.qvoxel.client.render.Color;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.featureflags.FeatureSet;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.regex.Pattern;

public class WorldCreationScreen extends Screen {
    // Characters illegal on Windows/macOS/Linux
    private static final Pattern ILLEGAL_CHARS = Pattern.compile("[<>:\"/\\\\|?*\\x00]");

    // Windows reserved device names
    private static final Pattern RESERVED_NAMES = Pattern.compile(
            "(?i)^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])$"
    );

    public static final Identifier BACKGROUND = CommonConstants.id("textures/gui/buttons/dark.png");
    private TextInputWidget nameInput;
    private TextInputWidget seedInput;
    private TextButtonWidget backButton;
    private TextButtonWidget createButton;
    private long seed;


    public WorldCreationScreen() {
        super(Language.translate("quantum.screen.world_creation"));
    }

    @Override
    public void init() {
        nameInput = addWidget(new TextInputWidget());
        nameInput.setHint(Language.translate("quantum.screen.world_creation.name"));
        nameInput.setMaxLength(64);
        nameInput.setShadow(false);

        seedInput = addWidget(new TextInputWidget());
        seedInput.setHint(Language.translate("quantum.screen.world_creation.seed"));
        seedInput.setMaxLength(64);
        seedInput.setShadow(false);
        seedInput.setOnEdit(this::editSeed);
        seedInput.setOnSubmit(this::submitSeed);

        backButton = addWidget(new TextButtonWidget(Language.translate("quantum.ui.back")));
        backButton.setCallback(client::popGuiLayer);

        createButton = addWidget(new TextButtonWidget(Language.translate("quantum.ui.create")));
        createButton.setCallback(() -> client.openWorld(client.createNewWorld(nameInput.getValue(), findAvailableName(Path.of("worlds", sanitize(nameInput.getValue())))), FeatureSet.NONE));

        changeFocus(nameInput);

        seedInput.setValue(String.valueOf(CommonConstants.RANDOM.nextLong()));
    }

    private boolean validate(@NotNull String value) {
        try {
            seed = Long.parseLong(value);
            createButton.isEnabled = true;
            return true;
        } catch (NumberFormatException e) {
            createButton.isEnabled = false;
            return false;
        }
    }

    private Path findAvailableName(Path worlds) {
        int i = 0;
        while (Files.exists(worlds)) {
            worlds = worlds.resolveSibling(sanitize(worlds.getFileName().toString()) + " (" + ++i + ")");
        }
        return worlds;
    }


    public static String sanitize(String name) {
        if (name == null || name.isEmpty()) return "_";

        String sanitized = ILLEGAL_CHARS.matcher(name).replaceAll("_");

        sanitized = sanitized.replaceAll("[ .]+$", "");

        if (RESERVED_NAMES.matcher(sanitized).matches()) {
            sanitized = "_" + sanitized;
        }

        if (sanitized.isEmpty()) {
            sanitized = "_";
        }

        if (sanitized.length() > 255) {
            sanitized = sanitized.substring(0, 255);
        }

        return sanitized;
    }

    @Override
    public void resized() {
        nameInput.setPosition(size.x / 2 - 100, size.y / 2 - 50);
        nameInput.resize(200, 20);

        seedInput.setPosition(size.x / 2 - 100, size.y / 2 - 15);
        seedInput.resize(200, 20);

        backButton.setPosition(size.x / 2 - 100, size.y / 2 + 30);
        backButton.resize(95, 20);

        createButton.setPosition(size.x / 2 + 5, size.y / 2 + 30);
        createButton.resize(95, 20);
    }

    @Override
    public void renderBackground(GuiRenderer renderer) {
        super.renderBackground(renderer);

        renderer.drawNinePatch(BACKGROUND, size.x / 2 - 110, size.y / 2 - 60, 220, 124, 0, 0, 21, 21, 21, 21, 7);
    }

    @Override
    public void renderForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
        super.renderForeground(renderer, mouseX, mouseY, partialTicks);

        renderer.drawCenteredString(
                getTitle(),
                size.x / 2,
                size.y / 2 - 100 - 20,
                0xFFFFFFFF
        );
    }

    public TextInputWidget getNameInput() {
        return nameInput;
    }

    public TextButtonWidget getBackButton() {
        return backButton;
    }

    public TextButtonWidget getCreateButton() {
        return createButton;
    }

    private void submitSeed(TextInputWidget widget) {
        if (!validate(widget.getValue())) {
            widget.setValue(String.valueOf(seed));
        }
    }

    private void editSeed(String value) {
        seedInput.setColor(validate(value) ? null : Color.RED);
    }
}
