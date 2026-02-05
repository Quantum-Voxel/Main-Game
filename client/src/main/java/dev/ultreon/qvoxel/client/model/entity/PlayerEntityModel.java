package dev.ultreon.qvoxel.client.model.entity;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

public class PlayerEntityModel extends ResourceEntityModel<PlayerEntity> {
    private Node head;
    private Node headwear;

    public PlayerEntityModel(Identifier modelId) {
        super(modelId);
    }

    @Override
    public void onLoaded(@NotNull Node root) {
        head = getNode("head");
        headwear = getNode("headwear");
    }

    public Node getHead() {
        return head;
    }

    public Node getHeadwear() {
        return headwear;
    }
}
