package dev.ultreon.qvoxel.server;

import dev.ultreon.qvoxel.world.World;

public class ChunkColumn {
    private final ServerChunk[] chunks = new ServerChunk[World.CHUNK_SIZE];
    private final QuantumServer server;
    private final ServerWorld world;
    private final Region region;
    private final int x, z;

    public ChunkColumn(QuantumServer server, ServerWorld world, Region region, int x, int z) {
        this.server = server;
        this.world = world;
        this.region = region;
        this.x = x;
        this.z = z;
    }

    public QuantumServer getServer() {
        return server;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public ServerChunk getChunk(int index) {
        return chunks[index];
    }

    public void setChunk(int index, ServerChunk chunk) {
        chunks[index] = chunk;
    }

    public void tick() {
        for (ServerChunk chunk : chunks) {
            if (chunk != null) {
                chunk.tick();
            }
        }
    }

    public void close() {
        for (ServerChunk chunk : chunks) {
            if (chunk != null) {
                chunk.close();
            }
        }
    }

    public void save() {
        for (ServerChunk chunk : chunks) {
            if (chunk != null) {
                region.saveChunk(chunk);
            }
        }
    }

    public void removeChunk(int y) {
        if (y < 0 || y >= World.REGION_SIZE) throw new IndexOutOfBoundsException("Chunk Y-coordinate out of bounds: " + y + " (expected 0.." + (World.REGION_SIZE - 1) + ")");

        chunks[y] = null;
        for (int i = y + 1; i < World.CHUNK_SIZE; i++)
            if (chunks[i] != null) return;

        region.removeColumn(x, z);
    }
}
