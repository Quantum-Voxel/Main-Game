package dev.ultreon.qvoxel.client.entity;

import dev.ultreon.qvoxel.client.model.EntityModel;
import dev.ultreon.qvoxel.client.world.RenderBufferSource;
import dev.ultreon.qvoxel.entity.Entity;

public interface EntityRenderer<TEntity extends Entity, TModel extends EntityModel<TEntity>> {
    void render(TEntity entity, TModel model, RenderBufferSource source, float partialTicks);
}
