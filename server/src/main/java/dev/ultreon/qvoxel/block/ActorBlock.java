package dev.ultreon.qvoxel.block;

import dev.ultreon.qvoxel.block.actor.BlockActor;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.world.World;

public interface ActorBlock {
    BlockActor newBlockActor(World world, BlockVec vec);
}
