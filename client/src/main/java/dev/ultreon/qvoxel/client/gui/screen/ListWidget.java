package dev.ultreon.qvoxel.client.gui.screen;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.gui.Widget;
import dev.ultreon.qvoxel.client.render.DebugDraw;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.sound.SoundEvent;
import org.lwjgl.sdl.SDLMouse;

import java.util.ArrayList;
import java.util.List;

public class ListWidget<T extends ListWidget.Entry> extends Widget {
    public static final Identifier BACKGROUND = CommonConstants.id("textures/gui/buttons/dark.png");
    public static final Identifier BACKGROUND_HOVER = CommonConstants.id("textures/gui/buttons/dark_hover.png");
    public static final Identifier BACKGROUND_SELECTED = CommonConstants.id("textures/gui/buttons/dark_pressed.png");
    public static final Identifier BACKGROUND_SELECTED_HOVER = CommonConstants.id("textures/gui/buttons/dark_pressed_hover.png");
    public static final Identifier LIGHT_BACKGROUND = CommonConstants.id("textures/gui/buttons/light.png");
    public static final Identifier LIGHT_BACKGROUND_HOVER = CommonConstants.id("textures/gui/buttons/light_hover.png");
    public static final Identifier LIGHT_BACKGROUND_SELECTED = CommonConstants.id("textures/gui/buttons/light_pressed.png");
    public static final Identifier LIGHT_BACKGROUND_SELECTED_HOVER = CommonConstants.id("textures/gui/buttons/light_pressed_hover.png");

    private final List<T> entries = new ArrayList<>();
    private int selectedIndex = -1;
    private int scrollAmount;
    private int maxScroll;
    private boolean selectable = true;
    private boolean unselectable = false;
    private boolean renderSelection = true;
    private T selected;
    private String noEntriesMessage;
    private SelectCallback<T> selectCallback = _ -> {
    };
    private DeselectCallback deselect = () -> {};

    public List<T> getEntries() {
        return entries;
    }

    public int getEntryCount() {
        return entries.size();
    }

    public void addEntry(T entry) {
        entries.add(entry);
    }

    public void setEntry(int index, T entry) {
        entries.set(index, entry);
    }

    public T getEntry(int index) {
        return entries.get(index);
    }

    public void clearEntries() {
        entries.clear();
        selectedIndex = -1;
        selected = null;
    }

    public void removeEntry(T entry) {
        entries.remove(entry);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    public boolean isRenderSelection() {
        return renderSelection;
    }

    public void setRenderSelection(boolean renderSelection) {
        this.renderSelection = renderSelection;
    }

    public T getSelected() {
        return selected;
    }

    public boolean isUnselectable() {
        return unselectable;
    }

    public void setUnselectable(boolean unselectable) {
        this.unselectable = unselectable;
    }

    @Override
    public void render(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
        int x = (int) position.x;
        int y = (int) position.y;
        int width = size.x;
        int height = size.y;

        if (entries.isEmpty()) {
            if (noEntriesMessage != null)
                renderer.drawString(noEntriesMessage, x + (width - renderer.getStringWidth(noEntriesMessage)) / 2, y + (height - renderer.getFontHeight()) / 2, 0xFFFFFFFF);
            return;
        }

        int entryY = scrollAmount;

        if (renderer.pushScissors(0, 0, width, height)) {
            for (int i = 0; i < entries.size(); i++) {
                T entry = entries.get(i);

                boolean isHovered = isHovered(mouseX, mouseY, width, entryY, entry);
                boolean currentSelected = selectedIndex == i;
                int finalY = entryY + (currentSelected ? 2 : 0);
                entry.isHovered = isHovered;
                entry.isSelected = currentSelected;
                entry.index = i;
                entry.setPosition(x, entryY);
                entry.resize(width, entry.getHeight());
                entry.render(i, renderer, mouseX, mouseY, partialTicks);
                renderer.debugDraw(new DebugDraw(0, finalY, width, entry.getHeight(), 0x800080FF));
                entryY += entry.getHeight();
            }
            renderer.popScissors();
        }
    }

    private static <T extends Entry> boolean isHovered(int mouseX, int mouseY, int width, int entryY, T entry) {
        return mouseX >= 0 && mouseX < 0 + width && mouseY >= entryY && mouseY < entryY + entry.getHeight();
    }

    @Override
    public boolean onMouseButtonPress(int mouseX, int mouseY, int button, int mods) {
        if (button == SDLMouse.SDL_BUTTON_LEFT) {
            int x = 0;
            int width = size.x;

            if (entries.isEmpty()) {
                return renderSelection;
            }

            int entryY = scrollAmount;

            boolean clickedAny = false;
            for (int i = 0; i < entries.size(); i++) {
                T entry = entries.get(i);

                boolean isHovered = isHovered(mouseX, mouseY, width, entryY, entry);
                if (isHovered) {
                    clickedAny = true;
                    if (selectable) {
                        if (selected != entry) {
                            client.playSound(SoundEvent.UI_MENU_TICK, 1.0f);
                            selectedIndex = i;
                            selected = entry;
                            selectCallback.onSelect(entry);
                        } else {
                            entry.submit();
                        }
                    }
                }
                entryY += entry.getHeight();
            }

            if (clickedAny) {
                return true;
            } else if (unselectable) {
                selectedIndex = -1;
                selected = null;
                deselect.onDeselect();
                return true;
            }
        }

        if (renderSelection) {
            super.onMouseButtonPress(mouseX, mouseY, button, mods);
            return true;
        }

        return super.onMouseButtonPress(mouseX, mouseY, button, mods);
    }

    @Override
    protected void onFocusLost() {
        super.onFocusLost();

        Entry entry = selectedIndex >= 0 && selectedIndex < entries.size() ? entries.get(selectedIndex) : null;
        if (entry != null) {
            entry.onFocusLost();
        }
    }

    @Override
    protected void onFocusGained() {
        super.onFocusGained();

        Entry entry = selectedIndex >= 0 && selectedIndex < entries.size() ? entries.get(selectedIndex) : null;
        if (entry != null) {
            entry.onFocusGained();
        }
    }

    public String getNoEntriesMessage() {
        return noEntriesMessage;
    }

    public void setNoEntriesMessage(String noEntriesMessage) {
        this.noEntriesMessage = noEntriesMessage;
    }

    public void onSelect(SelectCallback<T> callback) {
        selectable = true;
        selected = null;
        selectedIndex = -1;
        selectCallback = callback;
    }

    public void onDeselect(DeselectCallback callback) {
        deselect = callback;
    }

    public static class Entry extends Widget {
        protected final QuantumClient client = QuantumClient.get();
        protected boolean isSelected = false;
        protected int index;
        private final ListWidget<?> list;
        protected boolean renderLight;

        public Entry(ListWidget<?> list) {
            this(list, 20);
        }

        public Entry(ListWidget<?> list, int height) {
            this.list = list;
            size.y = height;
        }

        @Override
        public void render(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
            // Do nothing

            if (list.renderSelection) {
                Identifier background = renderLight ? LIGHT_BACKGROUND : BACKGROUND;
                if (isHovered) {
                    background = renderLight ? LIGHT_BACKGROUND_HOVER : BACKGROUND_HOVER;
                    if (isSelected) {
                        background = renderLight ? LIGHT_BACKGROUND_SELECTED_HOVER : BACKGROUND_SELECTED_HOVER;
                    }
                } else if (isSelected) {
                    background = renderLight ? LIGHT_BACKGROUND_SELECTED : BACKGROUND_SELECTED;
                }
                renderer.drawNinePatch(background, 0, (int) position.y, getWidth(), getHeight() + 2, 0, 0, 21, 21, 21, 21, 7);
            }
        }

        public void render(int index, GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
            this.index = index;
            render(renderer, mouseX, mouseY, partialTicks);
        }

        public int getHeight() {
            return size.y;
        }

        public void setHeight(int height) {
            size.y = height;
        }

        public void submit() {
            // Do nothing
        }

        public void onFocusLost() {

        }

        public void onFocusGained() {

        }
    }

    @FunctionalInterface
    public interface SelectCallback<T extends ListWidget.Entry> {
        void onSelect(T entry);
    }

    @FunctionalInterface
    public interface DeselectCallback {
        void onDeselect();
    }
}
