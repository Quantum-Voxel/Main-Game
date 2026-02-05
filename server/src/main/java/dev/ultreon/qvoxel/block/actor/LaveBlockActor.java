package dev.ultreon.qvoxel.block.actor;

import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.world.World;

public class LaveBlockActor extends BlockActor {
    public LaveBlockActor(World world, BlockVec vec) {
        super(world, vec, BlockActors.LAVE);
    }

    @Override
    public void tick() {
        super.tick();

        World world = getWorld();
        BlockVec pos = getPos();

        for (int x = pos.x - 1; x <= pos.x + 1; x++) {
            for (int y = pos.y - 1; y <= pos.y + 1; y++) {
                for (int z = pos.z - 1; z <= pos.z + 1; z++) {
                    if (pos.equals(x, y, z)) continue;

                    world.destroyBlock(x, y, z);
                }
            }
        }
    }
}
