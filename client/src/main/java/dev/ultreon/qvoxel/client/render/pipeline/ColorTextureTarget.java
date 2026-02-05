package dev.ultreon.qvoxel.client.render.pipeline;

import dev.ultreon.qvoxel.client.framebuffer.ColorAttachment;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.client.texture.TextureFormat;
import dev.ultreon.qvoxel.client.world.ClientPlayerEntity;

public class ColorTextureTarget extends ColorAttachment implements TextureTarget {
    public ColorTextureTarget(TextureFormat format) {
        super(format);
    }

    @Override
    public void render(ClientPlayerEntity player, GuiRenderer renderer) {
        if (frameBuffer instanceof RenderNode renderNode) {
            renderNode.render(player, renderer);
            return;
        }
        super.render(player, renderer);
    }
}
