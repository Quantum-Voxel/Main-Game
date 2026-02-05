package dev.ultreon.qvoxel.client.gui;

import org.joml.Vector2f;

public class Resizer {
    private final float ratio;
    private final float relativeRatio;
    private final Orientation orientation;
    private float sourceWidth;
    private float sourceHeight;

    public Resizer(float srcWidth, float srcHeight) {
        ratio = srcWidth / srcHeight;

        if (srcWidth > srcHeight) {
            relativeRatio = srcWidth / srcHeight;
            orientation = Orientation.LANDSCAPE;
        } else if (srcWidth < srcHeight) {
            relativeRatio = srcHeight / srcWidth;
            orientation = Orientation.PORTRAIT;
        } else {
            relativeRatio = 1;
            orientation = Orientation.SQUARE;
        }

        sourceWidth = srcWidth;
        sourceHeight = srcHeight;
    }

    public Resizer() {
        this(1, 1);
    }

    public Vector2f fill(float maxWidth, float maxHeight) {
        float aspectRatio;
        float width;
        float height;

        if (sourceWidth < sourceHeight) {
            aspectRatio = (float) (sourceWidth / (double) sourceHeight);

            width = maxWidth;
            height = (int) (width / aspectRatio);

            if (height < maxHeight) {
                aspectRatio = (float) (sourceHeight / (double) sourceWidth);

                height = maxHeight;
                width = (int) (height / aspectRatio);
            }
        } else {
            aspectRatio = (float) (sourceHeight / (double) sourceWidth);

            height = maxHeight;
            width = (int) (height / aspectRatio);
            if (width < maxWidth) {
                aspectRatio = (float) (sourceWidth / (double) sourceHeight);

                width = maxWidth;
                height = (int) (width / aspectRatio);
            }
        }

        return new Vector2f(width, height);
    }

    public void set(int width, int height) {
        sourceWidth = width;
        sourceHeight = height;
    }

    public Vector2f fit(float maxWidth, float maxHeight) {
        float aspectRatio = sourceWidth / sourceHeight;
        float width;
        float height;

        if (maxWidth / maxHeight > aspectRatio) {
            height = maxHeight;
            width = height * aspectRatio;
        } else {
            width = maxWidth;
            height = width / aspectRatio;
        }

        return new Vector2f(width, height);
    }

    public Vector2f center(float maxWidth, float maxHeight) {
        float width = sourceWidth;
        float height = sourceHeight;

        return width < maxWidth && height < maxHeight
                ? new Vector2f(width, height)
                : fit(maxWidth, maxHeight);
    }


    /**
     * Aspect ratio orientation.
     */
    public enum Orientation {
        LANDSCAPE,
        SQUARE,
        PORTRAIT
    }

    public float getRatio() {
        return ratio;
    }

    public float getRelativeRatio() {
        return relativeRatio;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public float getSourceWidth() {
        return sourceWidth;
    }

    public float getSourceHeight() {
        return sourceHeight;
    }
}
