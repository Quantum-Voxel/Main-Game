package dev.ultreon.qvoxel.client.debug;

import java.util.Objects;

public class ScreenCoordinates {
    public int mouseX, mouseY, width, height;

    public ScreenCoordinates(int mouseX, int mouseY, int width, int height) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.width = width;
        this.height = height;
    }

    public ScreenCoordinates(int horizontal, int vertical) {
        this(horizontal, vertical, horizontal, vertical);
    }

    public ScreenCoordinates(int all) {
        this(all, all, all, all);
    }

    public ScreenCoordinates() {
        this(0, 0, 0, 0);
    }

    public ScreenCoordinates set(int left, int top, int right, int bottom) {
        mouseX = left;
        mouseY = top;
        width = right;
        height = bottom;

        return this;
    }

    public ScreenCoordinates set(int horizontal, int vertical) {
        return set(horizontal, vertical, horizontal, vertical);
    }

    public ScreenCoordinates set(int all) {
        return set(all, all, all, all);
    }

    public ScreenCoordinates idt() {
        return set(0);
    }

    public ScreenCoordinates set(ScreenCoordinates insets) {
        return set(insets.mouseX, insets.mouseY, insets.width, insets.height);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScreenCoordinates that = (ScreenCoordinates) o;
        return mouseX == that.mouseX && mouseY == that.mouseY && width == that.width && height == that.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mouseX, mouseY, width, height);
    }
}
