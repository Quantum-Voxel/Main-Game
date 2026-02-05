package dev.ultreon.qvoxel.client.render.pipeline;

import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.client.world.ClientPlayerEntity;

public interface TextureSource {
    void use();
    void use(int unit);

    void render(ClientPlayerEntity player, GuiRenderer renderer);

    int getWidth();

    int getHeight();
}
