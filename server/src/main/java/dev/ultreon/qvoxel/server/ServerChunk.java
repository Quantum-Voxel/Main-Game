package dev.ultreon.qvoxel.server;

import dev.ultreon.qvoxel.block.actor.BlockActor;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.fluid.FluidState;
import dev.ultreon.qvoxel.registry.RegistryKey;
import dev.ultreon.qvoxel.util.*;
import dev.ultreon.qvoxel.world.BuilderChunk;
import dev.ultreon.qvoxel.world.Chunk;
import dev.ultreon.qvoxel.world.HeightmapType;
import dev.ultreon.qvoxel.world.World;
import dev.ultreon.qvoxel.world.gen.GenerationBarrier;
import dev.ultreon.qvoxel.world.gen.biome.Biome;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class ServerChunk extends Chunk {
    private final @NotNull QuantumServer server;
    private final @NotNull RNG rng;
    public boolean modified = false;
    private Region region;

    public ServerChunk(ServerWorld world, ChunkVec pos, Region region) {
        super(world, pos);
        server = world.getServer();
        rng = new JavaRNG(getWorld().getSeed() + (pos.x ^ (long) pos.z << 4) & 0x3FFFFFFF);
        this.region = region;
    }

    public ServerChunk(ServerWorld world, ChunkVec pos, PaletteStorage<BlockState> blockStorage, PaletteStorage<FluidState> fluidStorage, Map<BlockVec, BlockActor> blockActors, PaletteStorage<RegistryKey<Biome>> biomeStorage, Region region) {
        super(world, pos, blockStorage, fluidStorage, blockActors, biomeStorage);
        server = world.getServer();
        rng = new JavaRNG(getWorld().getSeed() + (pos.x ^ (long) pos.z << 4) & 0x3FFFFFFF);
        this.region = region;
    }

    @Override
    public ServerWorld getWorld() {
        return (ServerWorld) super.getWorld();
    }

    public RNG getRNG() {
        return rng;
    }

    public QuantumServer getServer() {
        return server;
    }

    public void close() {
        region.saveChunk(this);
        region.removeChunk(this);
        region = null;
    }

    public abstract MapType save();

    public static ServerChunk load(ServerWorld world, ChunkVec pos, MapType map, @NotNull Region region) {
        boolean generated = map.getBoolean("generated");
        if (generated) {
            return WorldChunk.load(world, map, pos.x, pos.y, pos.z, region);
        } else {
            return BuilderChunk.load(world, pos, map, region);
        }
    }

    public void consumeTicket(ChunkLoadTicket ticket) {

    }

    public int getHeight(int cx, int cz, HeightmapType type) {
        int wx = blockStart.x + cx;
        int wz = blockStart.z + cz;
        return getWorld().getHeight(wx, wz, type);
    }

    public int getFirstBlockFromBottom() {
        if (blockStorage.isEmpty()) {
            return -1;
        }

        for (int y = 0; y < World.CHUNK_SIZE; y++) {
            for (int x = 0; x < World.CHUNK_SIZE; x++) {
                for (int z = 0; z < World.CHUNK_SIZE; z++) {
                    if (!get(x, y, z).isAir())
                        return y;
                }
            }
        }

        return World.CHUNK_SIZE;
    }

    public int getFirstBlockFromTop() {
        if (blockStorage.isEmpty()) {
            return -1;
        }

        for (int x = 0; x < World.CHUNK_SIZE; x++) {
            for (int z = 0; z < World.CHUNK_SIZE; z++) {
                if (!get(x, World.CHUNK_SIZE - 1, z).isAir())
                    return World.CHUNK_SIZE;
            }
        }

        for (int y = World.CHUNK_SIZE - 2; y >= 0; y--) {
            for (int x = 0; x < World.CHUNK_SIZE; x++) {
                for (int z = 0; z < World.CHUNK_SIZE; z++) {
                    if (!get(x, y, z).isAir())
                        return y;
                }
            }
        }

        return -1;
    }

    public int getLowestBlockInChunk(int x, int z) {
        for (int y = World.CHUNK_SIZE - 1; y >= 0; y--) {
            BlockState blockState = get(x, y, z);
            if (!blockState.isAir())
                return y;
        }

        return -1;
    }

    public int getLowestWorldBlockAt(int x, int z) {
        int cx = vec.x;
        int cy = vec.y;
        int cz = vec.z;

        ServerChunk chunk = getWorld().getChunk(cx, cy, cz, GenerationBarrier.SPAWN);

        int lowestBlock;
        while ((lowestBlock = chunk.getLowestBlockInChunk(x, z)) == -1) {
            cy--;
            int wy = cy * World.CHUNK_SIZE;
            if (wy < -6144)
                return wy;
            chunk = getWorld().getChunk(cx, cy, cz, GenerationBarrier.SPAWN);
        }

        return chunk.blockStart.y + lowestBlock;
    }

    public void discard() {
        modified = false;
        region.removeChunk(this);
        region = null;
    }

    public void sendChunk(ServerPlayerEntity serverPlayer) {

    }
}
