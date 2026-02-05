package dev.ultreon.qvoxel.client.texture;

import dev.ultreon.qvoxel.resource.GameComponent;

import java.util.ArrayList;
import java.util.List;

public class AtlasContainer implements GameComponent {
    public List<TextureAtlas> atlases = new ArrayList<>();

    public void addAtlas(TextureAtlas atlas) {
        atlases.add(atlas);
    }

    public void removeAtlas(TextureAtlas atlas) {
        atlases.remove(atlas);
    }
}
