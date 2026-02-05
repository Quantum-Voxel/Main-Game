package dev.ultreon.qvoxel.client.input;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.client.render.Window;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.Objects;

public final class Mouse {
    private final int mouseID;
    private final @Nullable String s;
    private float x = 0;
    private float y = 0;
    private float deltaX = 0;
    private float deltaY = 0;
    private boolean main;
    private final BitSet buttons = new BitSet(5);

    public Mouse(int mouseID, @Nullable String s) {
        this.mouseID = mouseID;
        this.s = s;
    }

    public void render(GuiRenderer renderer) {
        if (QuantumClient.get().mouseEjected && main) return;

        if (main) {
            renderer.drawTexture(CommonConstants.id("textures/cursors/normal.png"), (int) x, (int) y, 32, 32, 0, 0, 32, 32, 32, 32);
        } else {
            renderer.setTextureColor(0x80ffffff);
            renderer.drawTexture(CommonConstants.id("textures/cursors/normal.png"), (int) x, (int) y, 32, 32, 0, 0, 32, 32, 32, 32);
            renderer.setTextureColor(0xffffffff);
        }
    }

    public void onMove(float x, float y) {
        QuantumClient client = QuantumClient.get();
        if (client.mouseEjected) return;

        Window window = client.getWindow();
        if (!window.isMouseCaptured()) {
            float newX = this.x + x;
            float newY = this.y + y;
            int width = window.getWidth();
            int height = window.getHeight();
            if (main && (newX < 0 || newY < 0 || newX > width || newY > height) && window.ejectMouse(newX, newY)) {
                return;
            }

            this.x = Math.clamp(newX, 0, width);
            this.y = Math.clamp(newY, 0, height);
        }
        deltaX = x;
        deltaY = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getDeltaX() {
        return deltaX;
    }

    public float getDeltaY() {
        return deltaY;
    }

    public int mouseID() {
        return mouseID;
    }

    public @Nullable String name() {
        return s;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Mouse) obj;
        return this.mouseID == that.mouseID &&
                Objects.equals(this.s, that.s);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mouseID, s);
    }

    @Override
    public String toString() {
        return "Mouse[" +
                "mouseID=" + mouseID + ", " +
                "s=" + s + ']';
    }

    public void makeMain() {
        main = true;
    }

    public void removeMain() {
        main = false;
    }

    public void postUpdate() {
        deltaX = 0;
        deltaY = 0;
    }

    public boolean isEjected() {
        return QuantumClient.get().mouseEjected && main;
    }

    public boolean isPressed(int button) {
        return buttons.get(button);
    }

    public void onPress(int button) {
        buttons.set(button);
    }

    public void onRelease(int button) {
        buttons.clear(button);
    }

    public void onEnter(float x, float y) {
        int width = QuantumClient.get().getWindow().getWidth();
        int height = QuantumClient.get().getWindow().getHeight();
        this.x = Math.clamp(x, 0, width);
        this.y = Math.clamp(y, 0, height);
        this.deltaX = 0;
        this.deltaY = 0;
    }
}
