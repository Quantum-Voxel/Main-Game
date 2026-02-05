package dev.ultreon.qvoxel.client;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.Objects;

public final class Keyboard {
    private final int keyboardID;
    private final String name;
    private final IntSet down0 = new IntArraySet();
    private final IntSet down = new IntArraySet();
    private final IntSet wasDown = new IntArraySet();

    public Keyboard(int keyboardID, String name) {
        this.keyboardID = keyboardID;
        this.name = name;
    }

    public void onPress(int keycode) {
        down0.add(keycode);
    }

    public void onRelease(int keycode) {
        down0.remove(keycode);
    }

    public void update() {
        wasDown.clear();
        wasDown.addAll(down);
        down.clear();
        down.addAll(down0);
    }

    public boolean isKeyPressed(int keycode) {
        return down.contains(keycode);
    }

    public boolean isKeyJustPressed(int keycode) {
        return down.contains(keycode) && !wasDown.contains(keycode);
    }

    public boolean isKeyJustReleased(int keycode) {
        return !down.contains(keycode) && wasDown.contains(keycode);
    }

    public int keyboardID() {
        return keyboardID;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Keyboard) obj;
        return this.keyboardID == that.keyboardID &&
                Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyboardID, name);
    }

    @Override
    public String toString() {
        return "Keyboard[" +
                "keyboardID=" + keyboardID + ", " +
                "name=" + name + ']';
    }

}
