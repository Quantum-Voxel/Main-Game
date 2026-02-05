package dev.ultreon.qvoxel.block.actor;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.registry.Registries;

public class BlockActors {
    public static final BlockActorFactory<LaveBlockActor> LAVE = register("lave", new BlockActorFactory<>(LaveBlockActor::new));

    private static <T extends BlockActor> BlockActorFactory<T> register(String name, BlockActorFactory<T> factory) {
        Registries.BLOCK_ACTOR.register(CommonConstants.id(name), factory);
        return factory;
    }

    public static void init() {

    }
}
