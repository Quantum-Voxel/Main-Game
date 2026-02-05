package dev.ultreon.qvoxel.client;

import java.util.Objects;

public final class Marker {
    private final int x;
    private final int y;
    private final int color;
    private int ticks;

    public Marker(int x, int y, int ticks, int color) {
        this.x = x;
        this.y = y;
        this.ticks = ticks;
        this.color = color;
    }

    public boolean tick() {
        return --ticks <= 0;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int ticks() {
        return ticks;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Marker) obj;
        return this.x == that.x &&
                this.y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Marker[" +
                "x=" + x + ", " +
                "y=" + y + ", " +
                "ticks=" + ticks + ']';
    }

    public int color() {
        return color;
    }
}

