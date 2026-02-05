package dev.ultreon.qvoxel.devutils;

import dev.ultreon.qvoxel.client.texture.Texture;
import dev.ultreon.qvoxel.client.texture.TextureRegion;
import org.joml.Vector4f;

public class SimpleTextureRegion implements TextureRegion {
    private final Texture texture;
    private Vector4f uvTransform = new Vector4f(0, 0, 1, 1);

    public SimpleTextureRegion(Texture texture) {
        this.texture = texture;
    }

    @Override
    public float x() {
        return 0;
    }

    @Override
    public float y() {
        return 0;
    }

    @Override
    public float width() {
        return texture.getWidth();
    }

    @Override
    public float height() {
        return texture.getHeight();
    }

    @Override
    public Texture texture() {
        return texture;
    }

    @Override
    public float getU() {
        return uvTransform.x;
    }

    @Override
    public float getV() {
        return uvTransform.y;
    }

    @Override
    public float getU2() {
        return uvTransform.w;
    }

    @Override
    public float getV2() {
        return uvTransform.z;
    }

    @Override
    public Vector4f getUvTransform() {
        return uvTransform;
    }
}
