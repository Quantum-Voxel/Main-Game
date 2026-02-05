package dev.ultreon.qvoxel.client.entity;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.model.EntityModel;
import dev.ultreon.qvoxel.client.world.RenderBufferSource;
import dev.ultreon.qvoxel.client.world.RenderType;
import dev.ultreon.qvoxel.entity.Entity;
import org.joml.Math;
import org.joml.Vector3d;

public abstract class SimpleEntityRenderer<TEntity extends Entity, TModel extends EntityModel<TEntity>> implements EntityRenderer<TEntity, TModel> {
    private final QuantumClient client;

    public SimpleEntityRenderer(QuantumClient client) {
        this.client = client;
    }

    @Override
    public void render(TEntity entity, TModel model, RenderBufferSource source, float partialTicks) {
        Identifier texture = getTexture(entity);

        updateAnim(entity, model, partialTicks);

        client.getTextureManager().getTexture(texture).use();
        source.get(getRenderType()).render((view, transform, origin, buffer, partialTicks1) -> {
            transform.pushMatrix();
            Vector3d position = entity.getPosition(partialTicks1);
            transform.translate((float) (position.x - origin.x), (float) (position.y - origin.y), (float) (position.z - origin.z));
            transform.rotateZYX(0, Math.toRadians(-entity.yawBody), 0);
            model.render(view, transform, origin, buffer, partialTicks1);
            transform.popMatrix();
        });
    }

    protected RenderType getRenderType() {
        return RenderType.ENTITY_CUTOUT;
    }

    protected void updateAnim(TEntity playerEntity, TModel playerEntityModel, float partialTicks) {

    }

    public abstract Identifier getTexture(TEntity entity);
}
