package dev.ultreon.qvoxel.client.gui.widget;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.Clipboard;
import dev.ultreon.qvoxel.client.gui.FontRenderer;
import dev.ultreon.qvoxel.client.gui.callback.SubmitCallback;
import dev.ultreon.qvoxel.client.gui.Widget;
import dev.ultreon.qvoxel.client.render.Color;
import dev.ultreon.qvoxel.client.render.DebugDraw;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.sdl.SDLKeycode;
import org.lwjgl.sdl.SDLMouse;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * TextInputWidget is a GUI element that allows for text input and editing.
 * It extends the functionality of the base Widget class and provides features such as
 * text selection, cursor manipulation, input handling, and rendering.
 */
public class TextInputWidget extends Widget {
    private int scrollX;
    private int cursor;
    private int selectionStart = -1;
    private int selectionEnd = -1;
    private @NotNull String value = "";
    private @NotNull String hint = "";
    private int maxLength = Integer.MAX_VALUE;
    private float tickTime = 0;

    // Callbacks
    private SubmitCallback<TextInputWidget> onSubmit = (_) -> {
    };
    private EditCallback<String> onEdit = (_) -> {
    };
    private boolean shadow = true;
    private final List<Btn> buttons = new ArrayList<>();
    private int width;
    private Color color = null;
    private boolean selecting;
    private int oldMouseX = Integer.MIN_VALUE;
    private int oldMouseY = Integer.MIN_VALUE;
    private int selectingFrom;

    public TextInputWidget() {
        super();
    }

    /**
     * Retrieves the hint text associated with this widget.
     *
     * @return The hint text as a non-null string.
     */
    public @NotNull String getHint() {
        return hint;
    }

    /**
     * Sets the hint text for the text input widget.
     *
     * @param hint The hint text to be displayed. Must be a non-null string.
     */
    public void setHint(@NotNull String hint) {
        this.hint = hint;
    }

    /**
     * Retrieves the color associated with this text input widget.
     *
     * @return The current color of the widget as a {@code Color} object.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the color for the text input widget.
     *
     * @param color The new color to be applied to this widget.
     *              Must be a non-null {@code Color} object.
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Retrieves the current value of the text input widget.
     *
     * @return The current value as a non-null string.
     */
    public @NotNull String getValue() {
        return value;
    }

    /**
     * Sets the current value of the text input widget. If the new value is different
     * from the current value, the cursor is updated to the end of the input, the
     * horizontal scroll position is reset, and any existing text selection is cleared.
     *
     * @param value The new value to set. Must be a non-null string.
     */
    public void setValue(@NotNull String value) {
        if (value.equals(this.value)) return;

        this.value = value;
        onEdit.call(value);
        updateCursor(value.length());
        scrollX = 0;
        deselectAll();
    }

    /**
     * Sets the position of the cursor for the text input widget. This updates the
     * internal cursor value, adjusts the horizontal scroll position to ensure the
     * cursor is visible, and clears any existing text selection.
     *
     * @param cursor The new cursor position, representing the index in the text
     *               where the cursor should be placed.
     */
    public void setCursor(int cursor) {
        this.updateCursor(cursor);
        int i = font.widthOf(value.substring(0, cursor));
        if (i < -scrollX)
            scrollX = i;
        else if (i > -scrollX + width)
            scrollX = i - width;
        deselectAll();
    }

    private boolean posOutOfRange(int i) {
        return i < -scrollX || i > -scrollX + size.x;
    }

    @Override
    public void render(GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
        tickTime += partialTicks;

        if (selecting && (oldMouseX != mouseX || oldMouseY != mouseY)) {
            oldMouseX = mouseX;
            oldMouseY = mouseY;

            mouseSelect(mouseX);
        }

        if (color != null)
            renderer.drawColorDisplay(-4, -4, size.x + 8, size.y + 8, color.toARGB());
        else
            renderer.drawLabel(-4, -4, size.x + 8, size.y + 8, shadow);

        int buttonWidth = calculateButtonWidth();
        width = size.x - buttonWidth - 2;

        int x = size.x - buttonWidth;
        for (Btn btn : buttons) {
            btn.render(this, x, renderer, mouseX, mouseY, partialTicks);
            x += btn.width(font);
        }

        if (renderer.pushScissors(0, 0, width, size.y)) {
            int y = size.y / 2 + 2;
            if (value.isEmpty()) {
                renderer.drawString(hint, -scrollX, y, 0xFF808080);
            } else {
                if (selectionStart != -1 && selectionEnd != -1) {
                    String selStart = value.substring(0, selectionStart);
                    String sel = value.substring(selectionStart, selectionEnd);
                    String selEnd = value.substring(selectionEnd);
                    int selStartLen = font.widthOf(selStart);
                    int selLen = font.widthOf(sel);
                    int selEndLen = selStartLen + selLen;
                    renderer.drawString(selStart, -scrollX, y, 0xFFFFFFFF);
                    renderer.drawString(sel, -scrollX + selStartLen, y, 0xFFFFFFFF);
                    renderer.drawString(selEnd, -scrollX + selEndLen, y, 0xFFFFFFFF);
                    renderer.fillRect(-scrollX + selStartLen, y - font.lineHeight / 2 - 2, selLen, font.lineHeight, 0x804080FF);
                } else {
                    renderer.drawString(value, -scrollX, y, 0xFFFFFFFF);
                }
            }
            if ((int) (client.getTotalTimeSeconds() * 2) % 2 == 0 && focused) {
                renderer.drawString("_", -scrollX + font.widthOf(value.substring(0, cursor)), 2 + y, 0xFFFFFFFF);
            }
            renderer.popScissors();
        }
    }

    private void mouseSelect(int mouseX) {
        for (int i = 0; i < value.length(); i++) {
            int x = scrollX + font.widthOf(value.substring(0, i));

            if (x >= mouseX) {
                updateCursor(i);
                select(selectingFrom, i);
                return;
            }
        }

        int i = value.length();
        updateCursor(i);
        select(selectingFrom, i);
    }

    private int updateCursor(int cursor) {
        this.cursor = cursor;
        int screenX = getScreenX();
        int screenY = getScreenY();
        this.client.setInputRect(screenX, screenY, screenX + size.x, screenY + size.y, font.widthOf(value.substring(0, cursor)));
        return this.cursor;
    }

    private int calculateButtonWidth() {
        int buttonWidth = 0;
        for (Btn btn : buttons) {
            buttonWidth += btn.width(font);
        }
        return buttonWidth;
    }

    public void selectAll() {
        selectionStart = 0;
        selectionEnd = value.length();
    }

    public void deselectAll() {
        selectionStart = -1;
        selectionEnd = -1;
    }

    public boolean isSelected() {
        return selectionStart != -1 && selectionEnd != -1;
    }

    public void select(int a, int b) {
        if (a == b) {
            selectionStart = -1;
            selectionEnd = -1;
            updateCursor(a);
            return;
        }

        selectionStart = Math.min(a, b);
        selectionEnd = Math.max(a, b);
    }

    public int getSelectionStart() {
        return selectionStart;
    }

    public int getSelectionEnd() {
        return selectionEnd;
    }

    public void setMaxLength(int length) {
        maxLength = length;
    }

    public int getMaxLength() {
        return maxLength;
    }

    @Override
    public void onKeyPress(int key, int scancode, int mods) {
        if (key == SDLKeycode.SDLK_BACKSPACE) {
            if (selectionStart != -1 && selectionEnd != -1) {
                value = value.substring(0, selectionStart) + value.substring(selectionEnd);
                onEdit.call(value);
                updateCursor(selectionStart);
                scroll();
                deselectAll();
            } else if (cursor > 0) {
                int orig = cursor;
                value = value.substring(0, left(mods, false)) + value.substring(orig);
                onEdit.call(value);
                scroll();
            }
        } else if (key == SDLKeycode.SDLK_DELETE) {
            if (selectionStart != -1 && selectionEnd != -1) {
                value = value.substring(0, selectionStart) + value.substring(selectionEnd);
                onEdit.call(value);
                updateCursor(selectionStart);
                scroll();
                deselectAll();
            } else if (cursor < value.length()) {
                int orig = cursor;
                value = value.substring(0, orig) + value.substring(right(mods, false));
                onEdit.call(value);
                updateCursor(orig);
                scroll();
            }
        } else if (key == SDLKeycode.SDLK_RETURN) {
            deselectAll();
            submit();
        } else if (key == SDLKeycode.SDLK_LEFT) {
            if (cursor == 0) {
                if ((mods & SDLKeycode.SDL_KMOD_SHIFT) == 0)
                    deselectAll();
                return;
            }
            if ((mods & SDLKeycode.SDL_KMOD_SHIFT) != 0) {
                if (selectionStart == -1 || selectionEnd == -1) {
                    int first = cursor;
                    select(first, left(mods, false));
                } else if (selectionStart == cursor) {
                    select(left(mods, false), selectionEnd);
                } else {
                    select(selectionStart, left(mods, true));
                }
                scroll();
            } else {
                left(mods, false);
                scroll();
                deselectAll();
            }
        } else if (key == SDLKeycode.SDLK_RIGHT) {
            if (cursor == value.length()) {
                if ((mods & SDLKeycode.SDL_KMOD_SHIFT) == 0)
                    deselectAll();
                return;
            }
            if ((mods & SDLKeycode.SDL_KMOD_SHIFT) != 0) {
                if (selectionStart == -1 || selectionEnd == -1) {
                    int first = cursor;
                    select(right(mods, false), first);
                } else if (selectionStart == cursor) {
                    select(right(mods, true), selectionEnd);
                } else {
                    select(selectionStart, right(mods, false));
                }
                scroll();
            } else {
                right(mods, false);
                scroll();
                deselectAll();
            }
        } else if (key == SDLKeycode.SDLK_HOME) {
            if (mods == SDLKeycode.SDL_KMOD_SHIFT) {
                select(0, selectionEnd);
            }
            updateCursor(0);
            scrollX = 0;
        } else if (key == SDLKeycode.SDLK_END) {
            if (mods == SDLKeycode.SDL_KMOD_SHIFT) {
                select(selectionStart, value.length());
            }

            updateCursor(value.length());
            scroll();
        } else if (key == SDLKeycode.SDLK_A && (mods & SDLKeycode.SDL_KMOD_CTRL) != 0) {
            selectAll();
        } else if (key == SDLKeycode.SDLK_C && (mods & SDLKeycode.SDL_KMOD_CTRL) != 0) {
            if (!isSelected()) selectAll();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(value.substring(selectionStart, selectionEnd)), null);
        } else if (key == SDLKeycode.SDLK_X && (mods & SDLKeycode.SDL_KMOD_CTRL) != 0 && isSelected()) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(value.substring(selectionStart, selectionEnd)), null);
            value = value.substring(0, selectionStart) + value.substring(selectionEnd);
            onEdit.call(value);
            updateCursor(selectionStart);
            scroll();
            deselectAll();
        } else if (key == SDLKeycode.SDLK_V && (mods & SDLKeycode.SDL_KMOD_CTRL) != 0) {
            Clipboard clipboard = client.getClipboard();
            String[] mimeTypes = clipboard.getMimeTypes();
            System.out.println("mimeTypes = " + Arrays.toString(mimeTypes));

            String text = clipboard.getText();
            if (text != null) {
                if (isSelected()) {
                    value = value.substring(0, selectionStart) + text + value.substring(selectionEnd);
                    updateCursor(selectionStart + text.length());
                    scroll();
                    deselectAll();
                    onEdit.call(value);
                }
                value = value.substring(0, cursor) + text + value.substring(cursor);
                cursor = cursor + text.length();
                scroll();
                onEdit.call(value);
            }
        }
    }

    private int left(int mods, boolean reverseWhitespace) {
        if ((mods & SDLKeycode.SDL_KMOD_CTRL) != 0) {
            // Jump one word left
            if (cursor == 0) return 0;

            int i = cursor - 1;
            // Skip whitespace first
            while (i > 0 && Character.isWhitespace(value.charAt(i - 1))) {
                i--;
            }
            // Skip word characters
            while (i > 0 && Character.isLetterOrDigit(value.charAt(i - 1))) {
                i--;
            }
            if (reverseWhitespace)
                while (i > 0 && !Character.isLetterOrDigit(value.charAt(i - 1))) {
                    i--;
                }

            return updateCursor(i);
        }

        // Normal move left
        return cursor > 0 ? --cursor : 0;
    }

    private int right(int mods, boolean reverseWhitespace) {
        if ((mods & SDLKeycode.SDL_KMOD_CTRL) != 0) {
            // Jump one word right
            if (cursor >= value.length()) return value.length();

            int i = cursor;
            // Skip whitespace first
            while (i < value.length() && Character.isWhitespace(value.charAt(i))) {
                i++;
            }
            // Skip current word characters
            while (i < value.length() && Character.isLetterOrDigit(value.charAt(i))) {
                i++;
            }
            if (reverseWhitespace)
                while (i < value.length() && !Character.isLetterOrDigit(value.charAt(i))) {
                    i++;
                }

            return updateCursor(i);
        }

        // Normal move right
        return cursor < value.length() ? ++cursor : value.length();
    }

    @Override
    public void onKeyRepeat(int key, int scancode, int mods) {
        super.onKeyRepeat(key, scancode, mods);
        onKeyPress(key, scancode, mods);
    }

    @Override
    public void onCharTyped(char character) {
        super.onCharTyped(character);

        if (value.length() < maxLength) {
            value = value.substring(0, cursor) + character + value.substring(cursor);
            onEdit.call(value);
            cursor++;
            scroll();
        }
    }

    private void scroll() {
        int cursorX = font.widthOf(value.substring(0, cursor));
        int viewWidth = width;
        int cursorWidth = font.widthOf("_");
        int margin = 8;

        if (cursorX < scrollX + margin) {
            // Cursor went past left edge → scroll left just enough
            scrollX = cursorX - margin;
        }
        // Ensure scrollX never pushes content left of the viewport
        if (cursorX - scrollX + cursorWidth + margin > viewWidth) {
            // Cursor went past right edge → scroll right just enough
            scrollX = cursorX - (viewWidth - cursorWidth - margin);
        }

        // Clamp scrollX to prevent negative or over-scroll
        int textWidth = font.widthOf(value);
        int maxScroll = Math.max(0, textWidth - viewWidth + cursorWidth);
        if (scrollX > maxScroll) scrollX = maxScroll;
        if (scrollX < 0) scrollX = 0;
    }

    public void submit() {
        onSubmit.call(this);
    }

    public void setShadow(boolean enabled) {
        shadow = enabled;
    }

    public boolean isShadow() {
        return shadow;
    }

    public void setOnSubmit(SubmitCallback<TextInputWidget> callback) {
        onSubmit = callback;
    }

    public void addToggleButton(Identifier icon, Supplier<Boolean> state) {
        buttons.add(new ToggleBtn(icon, state));
        int buttonWidth = calculateButtonWidth();
        width = size.x - buttonWidth - 2;
    }

    public void addSimpleButton(Identifier icon, Runnable callback) {
        buttons.add(new SimpleBtn(icon, callback));
        int buttonWidth = calculateButtonWidth();
        width = size.x - buttonWidth - 2;
    }

    public void setOnEdit(EditCallback<String> callback) {
        this.onEdit = callback;
    }

    @FunctionalInterface
    public interface EditCallback<T> {
        void call(T value);
    }

    @Override
    public boolean onMouseButtonPress(int mouseX, int mouseY, int button, int mods) {
        if (button == SDLMouse.SDL_BUTTON_LEFT) {
            int x = width + 2;
            for (Btn btn : buttons) {
                if (mouseX >= x && mouseX <= x + btn.width(font)) {
                    btn.onClick();
                    return true;
                }
                x += btn.width(font);
            }
        }

        int screenX = getScreenX();
        int screenY = getScreenY();
        if (button == SDLMouse.SDL_BUTTON_LEFT && !posOutOfRange(mouseX)) {
            client.addMarker(mouseX + screenX, mouseY + screenY, 100, 0xff8040);
            for (int i = 0; i < value.length(); i++) {
                int x = scrollX + font.widthOf(value.substring(0, i));

                if (x >= mouseX) {
                    client.addMarker(screenX + x, screenY + mouseY, 100, 0xffffff);
                    updateCursor(i);
                    selecting = true;
                    selectingFrom = i;
                    return true;
                }
            }

            client.addMarker(screenX + scrollX + font.widthOf(value), screenY + mouseY, 100, 0xffffff);
            updateCursor(value.length());
            selecting = true;
            selectingFrom = value.length();
            return true;
        } else {
            client.addMarker(mouseX + screenX, mouseY + screenY, 100, 0xff4040);
        }

        return super.onMouseButtonPress(mouseX, mouseY, button, mods);
    }

    @Override
    public void onMouseButtonRelease(int mouseX, int mouseY, int button, int mods) {
        super.onMouseButtonRelease(mouseX, mouseY, button, mods);

        selecting = false;
    }

    @Override
    protected void onFocusGained() {
        super.onFocusGained();

        client.startTextInput();
    }

    @Override
    protected void onFocusLost() {
        super.onFocusLost();

        client.stopTextInput();
    }

    private interface Btn {
        Identifier icon();

        void onClick();

        boolean isHighlighted(boolean hovered);

        int width(FontRenderer font);

        default void render(Widget widget, int x, GuiRenderer renderer, int mouseX, int mouseY, float partialTicks) {
            int y = (widget.size.y - widget.font.lineHeight) / 2;
            if (isHighlighted(mouseX >= x && mouseY >= y && mouseX < x + width(widget.font) && mouseY < y + 12)) {
                renderer.setTextureColor(0xFFFFFFFF);
                renderer.drawTexture(icon(), x + 2, y + 1, 12, 12);
                renderer.debugDraw(new DebugDraw(x, y, width(widget.font), 12, 0x80FFFF00, true));
            } else {
                renderer.setTextureColor(0xFFA0A0A0);
                renderer.drawTexture(icon(), x + 2, y + 1, 12, 12);
                renderer.setTextureColor(0xFFFFFFFF);
                renderer.debugDraw(new DebugDraw(x, y, width(widget.font), 12, 0x80FF8000, true));
            }
        }
    }

    private static final class ToggleBtn implements Btn {
        private final Identifier icon;
        private final Supplier<Boolean> state;
        private boolean currentState;

        private ToggleBtn(Identifier icon, Supplier<Boolean> state) {
            this.icon = icon;
            this.state = state;
        }

        @Override
        public Identifier icon() {
            return icon;
        }

        @Override
        public void onClick() {
            currentState = state.get();
        }

        @Override
        public boolean isHighlighted(boolean hovered) {
            return hovered || currentState;
        }

        @Override
        public int width(FontRenderer font) {
            return 16;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != getClass()) return false;
            var that = (ToggleBtn) obj;
            return Objects.equals(icon, that.icon) &&
                    Objects.equals(state, that.state);
        }

        @Override
        public int hashCode() {
            return Objects.hash(icon, state);
        }

        @Override
        public String toString() {
            return "ToggleBtn[" +
                    "icon=" + icon + ", " +
                    "state=" + state + ']';
        }
    }

    private record SimpleBtn(Identifier icon, Runnable callback) implements Btn {
        @Override
        public void onClick() {
            callback.run();
        }

        @Override
        public boolean isHighlighted(boolean hovered) {
            return hovered;
        }

        @Override
        public int width(FontRenderer font) {
            return 16;
        }
    }
}
