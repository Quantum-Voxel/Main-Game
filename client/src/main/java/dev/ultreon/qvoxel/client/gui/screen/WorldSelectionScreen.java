package dev.ultreon.qvoxel.client.gui.screen;

import dev.ultreon.libs.translations.v1.Language;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.gui.Screen;
import dev.ultreon.qvoxel.client.gui.Widget;
import dev.ultreon.qvoxel.client.gui.WorldCreationScreen;
import dev.ultreon.qvoxel.client.gui.widget.TextButtonWidget;
import dev.ultreon.qvoxel.client.gui.widget.TextInputWidget;
import dev.ultreon.qvoxel.client.gui.widget.TextSpoilerWidget;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.server.WorldSaveInfo;
import dev.ultreon.qvoxel.server.WorldStorage;
import dev.ultreon.qvoxel.sound.SoundEvent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Objects;

import static dev.ultreon.qvoxel.client.gui.screen.ListWidget.BACKGROUND;

public class WorldSelectionScreen extends Screen {
    private int displayColor = 0xFFFF4040;
    private TextButtonWidget newBtn;
    private TextButtonWidget editBtn;
    private TextButtonWidget loadBtn;
    private WorldListWidget worldList;
    private TextInputWidget searchInput;
    private WorldStorage selected;
    private TextSpoilerWidget spoiler;
    private WorldSaveInfo selectedInfo;
    private String lastPlayedFormatted;
    private boolean favoritesEnabled;
    private boolean showDeletedItems;

    public WorldSelectionScreen() {
        super(Language.translate("quantum.screen.world_selection"));
    }

    @Override
    public void init() {
        worldList = addWidget(new WorldListWidget());
        searchInput = addWidget(new TextInputWidget());
        searchInput.setHint(Language.translate("quantum.screen.world_selection.search"));
        searchInput.setMaxLength(64);
        searchInput.addToggleButton(CommonConstants.id("textures/gui/icons/favorite.png"), () -> {
            boolean next = favoritesEnabled = !favoritesEnabled;
            client.playSound(SoundEvent.UI_MENU_TICK, 1.0f);
            performSearch();
            deselectWorld();
            return next;
        });
        searchInput.addToggleButton(CommonConstants.id("textures/gui/icons/delete.png"), () -> {
            boolean next = showDeletedItems = !showDeletedItems;
            client.playSound(SoundEvent.UI_MENU_TICK, 1.0f);
            performSearch();
            deselectWorld();
            return next;
        });
        searchInput.addSimpleButton(CommonConstants.id("textures/gui/icons/clear.png"), () -> {
            client.playSound(SoundEvent.UI_MENU_TICK, 1.0f);
            searchInput.setValue("");
            performSearch();
        });
        searchInput.addSimpleButton(CommonConstants.id("textures/gui/icons/search.png"), () -> {
            client.playSound(SoundEvent.UI_MENU_TICK, 1.0f);
            performSearch();
        });
        performSearch();

        worldList.onSelect(this::selectWorld);

        searchInput.setOnSubmit(_ -> performSearch());

        newBtn = addWidget(new TextButtonWidget(Language.translate("quantum.screen.world_selection.new")));
        editBtn = addWidget(new TextButtonWidget(Language.translate("quantum.screen.world_selection.edit")));
        loadBtn = addWidget(new TextButtonWidget(Language.translate("quantum.screen.world_selection.load")));

        newBtn.setCallback(this::createWorld);
        editBtn.setCallback(this::editWorld);
        loadBtn.setCallback(this::playWorld);

        spoiler = addWidget(new TextSpoilerWidget());
        spoiler.isVisible = false;
        spoiler.setShowMessage(Language.translate("quantum.screen.world_selection.path_spoiler"));

        changeFocus(searchInput);
    }

    private void deselectWorld() {
        selected = null;
        spoiler.isVisible = false;
    }

    private void performSearch() {
        worldList.clearEntries();
        List<WorldStorage> search = client.getWorldManager().search(searchInput.getValue(), favoritesEnabled, showDeletedItems);
        search.sort(WorldStorage::compareTo);
        for (WorldStorage world : search) {
            worldList.addEntry(new WorldListWidget.Entry(worldList, world));
        }
    }

    @Override
    public void resized() {
        worldList.setPosition(size.x / 2 - 300, size.y / 2 - 100);
        newBtn.setPosition(size.x / 2 - 300, size.y / 2 + 80);
        loadBtn.setPosition(size.x / 2 - 148, size.y / 2 + 80);
        editBtn.setPosition(size.x / 2 + 20, size.y / 2 + 69);
        searchInput.setPosition(size.x / 2 - 300, size.y / 2 - 130);

        worldList.resize(300, 175);
        newBtn.resize(148, 20);
        loadBtn.resize(148, 20);
        editBtn.resize(148, 20);
        searchInput.resize(300, 20);
    }

    private void selectWorld(WorldListWidget.Entry entry) {
        if (entry.getWorld() == null) {
            selected = null;
            return;
        }

        if (Objects.equals(selected, entry.getWorld()))
            return;
        selected = entry.getWorld();
        String string = selected.getDirectory().toAbsolutePath().toString();
        if (!spoiler.getText().equals(string))
            spoiler.setText(string);
        spoiler.isVisible = true;
        selectedInfo = selected.loadInfo();

        displayColor = selectedInfo.getColorArgb();
        lastPlayedFormatted = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withZone(ZoneId.systemDefault())
                .format(selectedInfo.getLastSave());
    }

    @Override
    public void renderBackground(GuiRenderer renderer) {
        super.renderBackground(renderer);

        renderer.drawNinePatch(BACKGROUND, size.x / 2 - 302, size.y / 2 - 120, 304, 200, 0, 0, 21, 21, 21, 21, 7);
    }

    @Override
    protected void renderChildren(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
        spoiler.resize(294, 15);
        spoiler.setPosition(size.x / 2 + 8, size.y / 2 - 10);

        super.renderChildren(renderer, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderChild(GuiRenderer renderer, Widget child, int mouseX, int mouseY, float partialTicks) {
        if (child == spoiler) {
            if (renderer.pushScissors(size.x / 2 + 8, size.y / 2 - 100, 294, 200)) {
                try {
                    super.renderChild(renderer, child, mouseX, mouseY, partialTicks);
                } finally {
                    renderer.popScissors();
                }
            }
            return;
        }

        super.renderChild(renderer, child, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
        super.renderForeground(renderer, mouseX, mouseY, partialTicks);

        renderer.drawLabel(size.x / 2 + 5, size.y / 2 - 100, 300, 200);

        if (selected != null) {
            int appliedColor = renderer.drawColorDisplay(size.x / 2 + 20, size.y / 2 - 80, 270, (font.lineHeight + 5) * 2, displayColor);

            renderer.pushScissors(size.x / 2 + 23, size.y / 2 - 80, 264, (font.lineHeight + 5) * 2);
            try {
                int color = contrastedColor(displayColor);
                int nameWidth = font.widthOf(selected.getName());
                if (nameWidth * 2 > 248) {
                    if (nameWidth > 248)
                        renderer.drawCenteredString(selected.getName(), size.x / 2 + 166, size.y / 2 - 62, color);
                    else
                        renderer.drawCenteredString(selected.getName(), size.x / 2 + 160, size.y / 2 - 62, color);
                } else {
                    renderer.pushMatrix();
                    try {
                        renderer.scale(2, 2, 1);
                        renderer.drawCenteredString(selected.getName(), size.x / (2 * 2) + 80, size.y / (2 * 2) - 30, color);
                    } finally {
                        renderer.popMatrix();
                    }
                }

                if (nameWidth > 248) {
                    renderer.fillRect(size.x / 2 + 23, size.y / 2 - 77, 21, (font.lineHeight + 5) * 2 - 6, appliedColor);
                    renderer.fillRect(size.x / 2 + 42, size.y / 2 - 72, 1, 16, 0x80FFFFFF);
                }
                renderer.setTextureColor(color & 0x00FFFFFF | (isPosWithinFavoriteButton(mouseX, mouseY) || selected.loadInfo().isFavorite() ? 0xFF000000 : 0x80000000));
                renderer.drawTexture(CommonConstants.id("textures/gui/icons/favorite.png"), Math.max(size.x / 2 + 149 - nameWidth - 4, size.x / 2 + 25), size.y / 2 - 70, 12, 12, 0, 0, 12, 12, 12, 12);
                renderer.setTextureColor(0xFFFFFFFF);
            } finally {
                renderer.popScissors();
            }
            WorldSaveInfo worldSaveInfo = selected.loadInfo();
            renderer.pushScissors(size.x / 2 + 8, size.y / 2 - 30, 294, 164);
            try {
                renderer.drawCenteredString(lastPlayedFormatted, size.x / 2 + 155, size.y / 2 - 20, 0xFFFFFFFF);
                renderer.drawCenteredString(Language.translate("quantum.screen.world_selection.last_played_in", worldSaveInfo.getLastPlayedGamemode()), size.x / 2 + 155, size.y / 2 + 20, 0xFF808080);
                renderer.drawCenteredString(Language.translate("quantum.screen.world_selection.generator_version", worldSaveInfo.getGeneratorVersion()), size.x / 2 + 155, size.y / 2 + 31, 0xFF808080);

                renderer.setTextureColor(isPosWithinDeleteButton(mouseX, mouseY) ? 0xFFFF4040 : 0x80FFFFFF);
                int delX = size.x / 2 + 5 + (292 - 12 - 5);
                int delY = size.y / 2 - 100 + (190 - 12 - 5);
                renderer.drawTexture(CommonConstants.id("textures/gui/icons/delete.png"), delX, delY, 12, 12, 0, 0, 12, 12, 12, 12);
                renderer.setTextureColor(0xFFFFFFFF);

                if (showDeletedItems) {
                    String restore = Language.translate("quantum.screen.world_selection.restore");
                    int widthOfRestore = font.widthOf(restore);
                    boolean restoreHobered = isPosWithin(mouseX, mouseY, delX - 12 - 5 - widthOfRestore, delY, widthOfRestore, 12);
                    renderer.drawString(restore, delX - 12 - 5 - widthOfRestore, delY + font.lineHeight - 2, restoreHobered ? 0xFF40a0FF : 0xFFFFFFFF);
                }
            } finally {
                renderer.popScissors();
            }

        }
    }

    private boolean isPosWithinFavoriteButton(int mouseX, int mouseY) {
        return isPosWithin(mouseX, mouseY, size.x / 2 + 20, size.y / 2 - 80, 270, (font.lineHeight + 5) * 2);
    }

    private boolean isPosWithinDeleteButton(int mouseX, int mouseY) {
        int delX = size.x / 2 + 5 + (292 - 12 - 5);
        int delY = size.y / 2 - 100 + (190 - 12 - 5);
        return isPosWithin(mouseX, mouseY, delX, delY, 12, 12);
    }

    private boolean isPosWithin(int pointX, int pointY, int rectX, int rectY, int rectW, int rectH) {
        return pointX >= rectX && pointX <= rectX + rectW && pointY >= rectY && pointY <= rectY + rectH;
    }

    private int contrastedColor(int argb) {
        // Extract RGB components
        int r = argb >> 16 & 0xFF;
        int g = argb >> 8 & 0xFF;
        int b = argb & 0xFF;

        // Compute perceived brightness using the luminance formula
        double brightness = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;

        // Return white for dark colors, dark gray for light colors
        return brightness < 0.5f ? 0xFFFFFFFF : 0xFF404040;
    }

    public WorldSaveInfo getSelectedInfo() {
        return selectedInfo;
    }

    @Override
    public boolean onMouseButtonPress(int mouseX, int mouseY, int button, int mods) {
        if (isPosWithinFavoriteButton(mouseX, mouseY)) {
            client.playSound(SoundEvent.UI_MENU_TICK, 1.0f);
            WorldSaveInfo info = selected.loadInfo();
            info.setFavorite(!info.isFavorite());
            try {
                selected.saveInfo(info);
            } catch (IOException e) {
                CommonConstants.LOGGER.error("Failed to save world info", e);
            }
            selected.loadInfo();
            return true;
        }

        int widthOfRestore = font.widthOf(Language.translate("quantum.screen.world_selection.restore"));
        int delX = size.x / 2 + 5 + (292 - 12 - 5);
        int delY = size.y / 2 - 100 + (190 - 12 - 5);
        if (isPosWithin(mouseX, mouseY, delX - 12 - 5 - widthOfRestore, delY, widthOfRestore, 12)) {
            selected.restore();
            performSearch();
            return true;
        }

        if (isPosWithinDeleteButton(mouseX, mouseY)) {
            if (showDeletedItems) {
                client.playSound(SoundEvent.UI_MENU_TICK, 1.0f);
                client.showScreen(new ConfirmationScreen(Language.translate("quantum.screen.world_selection.delete_confirmation", selected.getName()), () -> {
                    boolean delete = false;
                    try {
                        delete = selected.deletePermanently();
                    } catch (IOException _) {
                        NotificationScreen.showMessage(Language.translate("quantum.screen.world_selection.delete_failed", selected.getName()));
                        return;
                    }
                    if (!delete) {
                        NotificationScreen.showMessage(Language.translate("quantum.screen.world_selection.delete_failed", selected.getName()));
                        return;
                    }
                    performSearch();
                }));
            } else {
                client.playSound(SoundEvent.UI_MENU_TICK, 1.0f);
                client.showScreen(new ConfirmationScreen(Language.translate("quantum.screen.world_selection.trash_confirmation", selected.getName(), LocalDateTime.now().plusDays(30).format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))), () -> {
                    boolean delete = selected.delete();
                    if (!delete) {
                        NotificationScreen.showMessage(Language.translate("quantum.screen.world_selection.trash_failed", selected.getName()));
                        return;
                    }
                    performSearch();
                }));
            }

            return true;
        }

        return super.onMouseButtonPress(mouseX, mouseY, button, mods);
    }

    private void playWorld() {
        if (selected == null)
            return;
        client.openWorld(selected);
    }

    private void editWorld() {
        if (selected == null)
            return;

        client.showScreen(new WorldEditScreen(selected, (storage, info) -> {
            selected = storage;
            selectedInfo = info;
            displayColor = info.getColorArgb();
            performSearch();
        }));
    }

    private void createWorld() {
        client.showScreen(new WorldCreationScreen());
    }
}
