package dev.ultreon.qvoxel.client.render;

import org.joml.Vector3f;

/**
 * Represents a graphical debug draw operation in a 2D space, defined by position, dimensions,
 * color, and whether it is filled or not.
 *
 * @param x         The x-coordinate of the top-left corner of the debug draw area.
 * @param y         The y-coordinate of the top-left corner of the debug draw area.
 * @param width     The width of the debug draw area.
 * @param height    The height of the debug draw area.
 * @param colorArgb The color of the debug draw in ARGB format.
 * @param filled    Whether the debug area should be filled or not
 * @see GuiRenderer#debugDraw(DebugDraw)
 */
public record DebugDraw(int x, int y, int width, int height, int colorArgb, boolean filled) {
    /**
     * Constructs a DebugDraw object with the specified position, dimensions, and color.
     * The created object represents an unfilled graphical debug draw operation by default.
     *
     * @param x         The x-coordinate of the top-left corner of the debug draw area.
     * @param y         The y-coordinate of the top-left corner of the debug draw area.
     * @param width     The width of the debug draw area.
     * @param height    The height of the debug draw area.
     * @param colorArgb The color of the debug draw in ARGB format.
     */
    public DebugDraw(int x, int y, int width, int height, int colorArgb) {
        this(x, y, width, height, colorArgb, false);
    }

    /**
     * Translates the debug draw's position by applying the given translation vector
     * scaled by the specified scale vector. The resulting position is adjusted
     * based on the scaled translation values.
     *
     * @param translation A {@code Vector3f} representing the translation values to be applied
     *                    to the current position.
     * @param scale       A {@code Vector3f} representing the scaling factors applied to the translation
     *                    values for each axis.
     * @return A new {@code DebugDraw} object with the translated position, maintaining all
     * other properties of the original object.
     */
    public DebugDraw translate(Vector3f translation, Vector3f scale) {
        return new DebugDraw(x + (int) (translation.x * scale.x), y + (int) (translation.y * scale.y), width, height, colorArgb, filled);
    }
}
