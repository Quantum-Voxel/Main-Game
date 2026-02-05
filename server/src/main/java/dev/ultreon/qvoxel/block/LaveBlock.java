package dev.ultreon.qvoxel.block;

import dev.ultreon.qvoxel.block.actor.BlockActor;
import dev.ultreon.qvoxel.block.actor.LaveBlockActor;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.world.World;

public class LaveBlock extends Block implements ActorBlock {
    public LaveBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockActor newBlockActor(World world, BlockVec vec) {
        return new LaveBlockActor(world, vec);
    }
}
