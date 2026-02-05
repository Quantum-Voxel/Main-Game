package dev.ultreon.qvoxel;

import dev.ultreon.qvoxel.block.Block;
import dev.ultreon.qvoxel.block.BoundingBox;
import dev.ultreon.qvoxel.block.ModelEffect;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.world.World;

import java.util.List;
import java.util.Random;

public class FoliageBlock extends Block {
    public FoliageBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ModelEffect getModelEffect() {
        return ModelEffect.OffsetXZ;
    }

    @Override
    public List<BoundingBox> getBoundingBoxes(BlockState blockState, World world, BlockVec pos) {
        long seed = BlockVec.hash64(pos.x, pos.y, pos.z);
        Random random = new Random(seed);

        float dx = 0;
        float dz = 0;
        dx = random.nextFloat(-0.25f, 0.25f);
        dz = random.nextFloat(-0.25f, 0.25f);

        return List.of(
                new BoundingBox(2 / 16f, 0, 2 / 16f, 14 / 16f, 10 / 16f, 14 / 16f).offset(dx, 0, dz)
        );
    }
}
