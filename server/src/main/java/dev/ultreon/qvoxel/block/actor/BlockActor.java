package dev.ultreon.qvoxel.block.actor;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.registry.Registries;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.world.World;
import dev.ultreon.ubo.types.MapType;

public abstract class BlockActor {
    private final World world;
    private final BlockVec pos;
    private final BlockActorFactory<?> factory;

    protected BlockActor(World world, BlockVec pos, BlockActorFactory<?> factory) {
        this.world = world;
        this.pos = pos;
        this.factory = factory;
    }

    public static BlockActor loadFully(MapType data, World world) {
        String id = data.getString("id");
        Identifier parse = Identifier.parse(id);

        BlockActorFactory<?> factory = Registries.BLOCK_ACTOR.get(parse);
        if (factory == null) {
            return null;
        }

        return factory.create(world, BlockVec.load(data.getMap("Position")));
    }

    public void tick() {

    }

    public MapType save(MapType data) {
        data.put("Position", pos.save());
        data.putString("id", factory.getId().toString());
        return data;
    }

    public void load(MapType data) {

    }

    public World getWorld() {
        return world;
    }

    public BlockVec getPos() {
        return pos;
    }

    public BlockActorFactory<?> getFactory() {
        return factory;
    }
}
