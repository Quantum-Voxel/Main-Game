package dev.ultreon.qvoxel.client.entity;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.model.entity.PlayerEntityModel;
import dev.ultreon.qvoxel.client.world.RenderBufferSource;
import dev.ultreon.qvoxel.player.PlayerEntity;
import org.joml.Math;

public class PlayerEntityRenderer extends SimpleEntityRenderer<PlayerEntity, PlayerEntityModel> {
    public PlayerEntityRenderer(QuantumClient client) {
        super(client);
    }

    @Override
    public void render(PlayerEntity playerEntity, PlayerEntityModel playerEntityModel, RenderBufferSource source, float partialTicks) {
        super.render(playerEntity, playerEntityModel, source, partialTicks);
    }

    @Override
    protected void updateAnim(PlayerEntity playerEntity, PlayerEntityModel playerEntityModel, float partialTicks) {
        playerEntityModel.getHead().rot.identity().rotateZYX(0, Math.toRadians(playerEntity.yawBody - playerEntity.yawHead), Math.toRadians(-playerEntity.pitchHead));
        playerEntityModel.getHeadwear().rot.identity().rotateZYX(0, Math.toRadians(playerEntity.yawBody - playerEntity.yawHead), Math.toRadians(-playerEntity.pitchHead));
    }

    @Override
    public Identifier getTexture(PlayerEntity entity) {
        return new Identifier("textures/entity/player.png");
    }
}
