package dev.ultreon.qvoxel.block.actor;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.registry.Registries;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.world.World;

@SuppressWarnings("ClassCanBeRecord")
public final class BlockActorFactory<TActor extends BlockActor> {
    private final BlockActorConstructor<TActor> constructor;

    public BlockActorFactory(BlockActorConstructor<TActor> constructor) {
        this.constructor = constructor;
    }

    public TActor create(World world, BlockVec pos) {
        return constructor.create(world, pos);
    }

    public Identifier getId() {
        return Registries.BLOCK_ACTOR.getId(this);
    }

    @FunctionalInterface
    public interface BlockActorConstructor<TActor extends BlockActor> {
        TActor create(World world, BlockVec pos);
    }
}
