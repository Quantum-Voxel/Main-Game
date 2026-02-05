/*
 * Copyright 2025. Quinten 'Qubix' Jungblut
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.ultreon.qvoxel.server;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.Audience;
import dev.ultreon.qvoxel.block.Blocks;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.item.Item;
import dev.ultreon.qvoxel.item.ItemStack;
import dev.ultreon.qvoxel.item.Items;
import dev.ultreon.qvoxel.menu.ItemSlot;
import dev.ultreon.qvoxel.menu.Menu;
import dev.ultreon.qvoxel.network.Packet;
import dev.ultreon.qvoxel.network.handler.ClientPacketHandler;
import dev.ultreon.qvoxel.network.handler.ServerPacketHandler;
import dev.ultreon.qvoxel.network.packets.s2c.*;
import dev.ultreon.qvoxel.network.system.CloseCodes;
import dev.ultreon.qvoxel.network.system.IConnection;
import dev.ultreon.qvoxel.particle.BlockParticleData;
import dev.ultreon.qvoxel.particle.ParticleTypes;
import dev.ultreon.qvoxel.player.PlayerAbilities;
import dev.ultreon.qvoxel.player.PlayerEntity;
import dev.ultreon.qvoxel.registry.Registries;
import dev.ultreon.qvoxel.sound.SoundEvent;
import dev.ultreon.qvoxel.util.*;
import dev.ultreon.qvoxel.world.*;
import dev.ultreon.qvoxel.world.gen.GenerationBarrier;
import dev.ultreon.ubo.types.MapType;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Server-side implementation of {@link PlayerEntity}.
 */
public class ServerPlayerEntity extends PlayerEntity implements Audience {
    public final IConnection<? extends ServerPacketHandler, ClientPacketHandler> connection;
    private final QuantumServer server;
    private final int clientRenderDistance;
    private final List<ChunkVec> toLoadChunks = new ArrayList<>();
    private int initialLoadCount = -1;
    private int loaded = 0;
    private boolean loggedIn;
    private ItemStack cursor;
    private float oldHealth;
    private final Vector3d tmp = new Vector3d();
    private double walkStep;
    final List<ChunkVec> loadedChunks = new CopyOnWriteArrayList<>();
    private boolean dataLoaded;
    private boolean loadedInitChunks;

    public ServerPlayerEntity(IConnection<? extends ServerPacketHandler, ClientPacketHandler> connection, String name, int clientRenderDistance, QuantumServer server) {
        super(null);
        this.clientRenderDistance = clientRenderDistance;
        this.connection = connection;
        this.server = server;

        username = name;
    }

    @Override
    public void tick() {
        pollChunkLoad();

        // FIXME: This stuff is broken asf
//        if (!onGround && !isFlying() && loggedIn) {
//            flyingTicks++;
//            if (flyingTicks > 200) {
//                flyingTicks = 0;
//                connection.disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), "Flying is disabled!");
//                return;
//            }
//        }

        if (oldHealth != getHealth()) {
            oldHealth = getHealth();
            connection.send(new S2CHealthPacket(getHealth()));
        }

        getFoodStatus().tick(this);

        for (int cx = -clientRenderDistance / World.CHUNK_SIZE; cx <= clientRenderDistance / World.CHUNK_SIZE; cx++) {
            for (int cy = -clientRenderDistance / World.CHUNK_SIZE; cy <= clientRenderDistance / World.CHUNK_SIZE; cy++) {
                for (int cz = -clientRenderDistance / World.CHUNK_SIZE; cz <= clientRenderDistance / World.CHUNK_SIZE; cz++) {
                    Chunk chunk = getServerWorld().getChunkOrNull(cx + BlockVec.chunkOf((int) position.x), cy + BlockVec.chunkOf((int) position.y), cz + BlockVec.chunkOf((int) position.z));
                    if (chunk != null) {
                        chunk.consumeTicket(ChunkLoadTicket.PLAYER);
                    }
                }
            }
        }

        oldDelta.set(velocity);

        move(getWorld(), velocity.x, velocity.y, velocity.z, false);

        if (onGround) {
            // Cancel falling
            velocity.y = 0;
        }

        // Update the entity's velocity based on gravity
        velocity.y = (velocity.y - gravity) * drag;

        velocity.x *= friction;
        velocity.z *= friction;
    }

    @Override
    protected void onMoved(double deltaX, double deltaY, double deltaZ) {
        super.onMoved(deltaX, deltaY, deltaZ);

        fallDistance += (float) deltaY;
        if (onGround) {
            if (fallDistance > 3)
                onFall(fallDistance - 2);

            fallDistance = 0;
        }
    }

    private BlockState getStepBlock() {
        BlockVec blockVec = BlockVec.of(position);
        blockVec.y--;
        return getServerWorld().get(blockVec);
    }

    private void onFall(float fallDistance) {
        damage(new DamageSource(DamageType.FALL, this, null, position), fallDistance);
    }

    @SuppressWarnings("D")
    public void refreshChunks() {
        loaded = 0;
        int clientRenderDistance1 = clientRenderDistance;
        if (initialLoadCount == -1)
            clientRenderDistance1 = Math.min(clientRenderDistance1, 64);
        List<ChunkVec> newChunks = new CopyOnWriteArrayList<>();
        for (int cx = -clientRenderDistance1 / World.CHUNK_SIZE; cx <= clientRenderDistance1 / World.CHUNK_SIZE; cx++) {
            for (int cy = -clientRenderDistance1 / World.CHUNK_SIZE; cy <= clientRenderDistance1 / World.CHUNK_SIZE; cy++) {
                for (int cz = -clientRenderDistance1 / World.CHUNK_SIZE; cz <= clientRenderDistance1 / World.CHUNK_SIZE; cz++) {
                    Chunk chunkOrNull = getServerWorld().getChunkOrNull(cx + BlockVec.chunkOf((int) position.x), cy + BlockVec.chunkOf((int) position.y), cz + BlockVec.chunkOf((int) position.z));
                    if (chunkOrNull instanceof @Nullable WorldChunk chunk) {
                        chunk.consumeTicket(ChunkLoadTicket.PLAYER);
                        if (!loadedChunks.contains(chunk.vec)) {
                            chunk.sendChunkData(this);
                        }
                        loaded++;
                    } else {
                        newChunks.add(getChunkVec(cx, cy, cz));
                    }
                }
            }
        }
        synchronized (toLoadChunks) {
            toLoadChunks.removeIf(chunkVec -> !newChunks.contains(chunkVec));
            toLoadChunks.addAll(newChunks);
            if (initialLoadCount == -1)
                initialLoadCount = toLoadChunks.size();
            toLoadChunks.sort((o1, o2) -> {
                if (o1 == null || o2 == null) return 0;
                Vector3i v1 = new Vector3i(o1.x, o1.y, o1.z);
                Vector3i v2 = new Vector3i(o2.x, o2.y, o2.z);
                long d1 = v1.distanceSquared(new Vector3i(BlockVec.chunkOf((int) position.x), BlockVec.chunkOf((int) position.y), BlockVec.chunkOf((int) position.z)));
                long d2 = v2.distanceSquared(new Vector3i(BlockVec.chunkOf((int) position.x), BlockVec.chunkOf((int) position.y), BlockVec.chunkOf((int) position.z)));
                return Long.compare(d1, d2);
            });
        }
        server.onChunkLoad(0, initialLoadCount, null);
    }

    private void pollChunkLoad() {
        ChunkVec chunkVec;
        synchronized (toLoadChunks) {
            if (toLoadChunks.isEmpty()) {
                return;
            }
            chunkVec = toLoadChunks.removeFirst();
        }
        Chunk chunkOrNull1 = getServerWorld().getChunk(chunkVec.x, chunkVec.y, chunkVec.z, GenerationBarrier.ALL);
        if (chunkOrNull1 instanceof WorldChunk) {
            CommonConstants.LOGGER.trace("Generating Chunk {}, {}. {}", chunkVec.x, chunkVec.y, chunkVec.z);

            CompletableFuture<? extends ServerChunk> chunkFuture = getServerWorld().loadChunkAsync(chunkVec.x, chunkVec.y, chunkVec.z, ChunkLoadTicket.PLAYER, GenerationBarrier.ALL);
            chunkFuture.exceptionally(throwable -> {
                CommonConstants.LOGGER.error("Failed to load chunk for player", throwable);
                return null;
            }).thenAccept(chunk -> {
                if (chunk == null) {
                    return;
                }
                ((WorldChunk) chunk).sendChunkData(this);
                loaded++;
                if (loaded == initialLoadCount) {
                    initialLoadCount = 0;
                    loaded = 0;
                    toLoadChunks.clear();
                    loadedInitChunks = true;
                }
                server.onChunkLoad(initialLoadCount - toLoadChunks.size(), initialLoadCount, chunk.vec);
            }).exceptionally(throwable -> {
                CommonConstants.LOGGER.error("Failed to send chunk data to player", throwable);
                return null;
            });
        }

        if (toLoadChunks.isEmpty()) {
            loggedIn = true;
        }
    }

    private ChunkVec getChunkVec(int cx, int cy, int cz) {
        return new ChunkVec(BlockVec.chunkOf((int) position.x) + cx, BlockVec.chunkOf((int) position.y) + cy, BlockVec.chunkOf((int) position.z) + cz);
    }

    @Override
    protected void onUnloadedChunk(ChunkVec chunkVec) {
        super.onUnloadedChunk(chunkVec);
        CommonConstants.LOGGER.info("Player entered unloaded chunk: {}", chunkVec);
    }

    public ServerWorld getServerWorld() {
        return (ServerWorld) getWorld();
    }

    @Override
    public void onTeleport(double x, double y, double z) {
        toLoadChunks.clear();
        loadedChunks.clear();
        super.onTeleport(x, y, z);

        if (loggedIn) {
            refreshChunks();
            connection.send(new S2CTeleportPacket(yawHead, pitchHead, position.x(), position.y(), position.z()));
            CommonConstants.LOGGER.info("Player '{}' moved to {} {} {}", getUsername(), x, y, z);
            if (dataLoaded) {
                dataLoaded = false;
            }
        }
    }

    @Override
    protected void onInvalidMotionBox() {
        if (isFlying()) return;

        super.onInvalidMotionBox();
        CommonConstants.LOGGER.warn("Player '{}' moved wrongly!", getUsername());
        connection.send(new S2CTeleportPacket(yawHead, pitchHead, position.x(), position.y(), position.z()));
        if (server.isDedicated()) {
            connection.disconnect(CloseCodes.PROTOCOL_ERROR.getCode(), "Player moved wrongly!");
        }
    }

    @Override
    public void setWorld(World world) {
        if (getServerWorld() != null) {
            getServerWorld().removePlayer(this);
        }

        super.setWorld(world);

        ServerWorld serverWorld = (ServerWorld) world;
        serverWorld.addPlayer(this);
    }

    public void onDisconnect(String message) {
        CommonConstants.LOGGER.info("Player '{}' disconnected: {}", getUsername(), message);
        server.getPlayerManager().removePlayer(this);
        getServerWorld().removePlayer(this);
    }

    public void setRotation(float yaw, float pitch) {
        this.yawHead = yaw;
        this.pitchHead = pitch;
    }

    public QuantumServer getServer() {
        return server;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void breakBlock(int x, int y, int z) {
        HitResult hitResult = castRay(6.0F);
        World world = getWorld();
        if (!(hitResult instanceof BlockHitResult(BlockVec block, _, _, _, _, _, BlockState state))) {
            CommonConstants.LOGGER.warn("Player '{}' tried to break block at {} {} {}, but no block was hit!", getUsername(), x, y, z);
            if (world != null) {
                connection.send(new S2CBlockSetPacket(x, y, z, world.get(x, y, z)));
            }
            return;
        }

        BlockVec blockVec = new BlockVec(x, y, z);
        if (blockVec.distance(block) < Math.sqrt(2.0D)) {
            world.set(x, y, z, Blocks.AIR.getDefaultState(), BlockFlags.NOTIFY_CLIENTS | BlockFlags.NEIGHBOR_UPDATE);
            getServerWorld().spawnParticles(ParticleTypes.BLOCK, 32, Util.make(new BlockParticleData(state), data -> {
                data.position.x = x + 0.5F;
                data.position.y = y + 0.5F;
                data.position.z = z + 0.5F;
                data.delta.set(0.4F);
                data.minSize = 0.1f;
                data.maxSize = 0.1f;
                data.minSpeed = 0.01f;
                data.maxSpeed = 0.02f;
            }));
            BlockState blockState = world.get(x, y, z);
            if (!blockState.equals(Blocks.AIR.getDefaultState()))
                CommonConstants.LOGGER.warn("Block {} {} {} was not set to air: {}", x, y, z, blockState);
        } else {
            CommonConstants.LOGGER.warn("Player '{}' tried to break block at {} {} {}, but the block they hit was not the same! (Expected: {})", getUsername(), x, y, z, block);
            connection.send(new S2CBlockSetPacket(x, y, z, world.get(x, y, z)));
        }
    }

    public void placeBlock(int x, int y, int z, BlockState state) {
        getWorld().set(x, y, z, state, BlockFlags.NOTIFY_CLIENTS | BlockFlags.NEIGHBOR_UPDATE);
        BlockState blockState = getWorld().get(x, y, z);
        if (!blockState.equals(state))
            CommonConstants.LOGGER.warn("Block {} {} {} was not set to {}: {}", x, y, z, state, blockState);
    }

    public void onMove(double x, double y, double z) {
        double ox = position.x;
        double oy = position.y;
        double oz = position.z;
        move(getWorld(), x - ox, y - oy, z - oz, true);
        if (ox != x && oz != z && !abilities.flying) {
            walkStep += Math.sqrt((x - ox) * (x - ox) + (z - oz) * (z - oz));
            BlockState stepBlock = getStepBlock();
            if (walkStep >= 2.0 && onGround && stepBlock != null && stepBlock.isSolid()) {
                walkStep = 0;
                playSound(stepBlock.getStepSound(), 1, ((float) Math.random() - 0.5F) * 0.2F + 1.0F, position, new Vector3d());
            }
        }
    }

    @Override
    public void sendPacket(Packet<? extends ClientPacketHandler> packet) {
        connection.send(packet);
    }

    @Override
    public void heal(float v) {
        super.heal(v);

        connection.send(new S2CHealthPacket(health));
    }

    @Override
    public void openMenu(Menu menu) {
        super.openMenu(menu);
        connection.send(new S2COpenMenuPacket(menu.getType().getRawId()));
    }

    @Override
    public void closeMenu() {
        super.closeMenu();
        connection.send(new S2COpenMenuPacket(-1));
    }

    @Override
    public void sendMessage(String message) {
        super.sendMessage(message);
        connection.send(new S2CChatMessagePacket(message));
    }

    @Override
    public void sendAbilitiesPacket(PlayerAbilities abilities) {
        connection.send(new S2CAbilitiesPacket(abilities));
    }

    public ItemStack getCursor() {
        return cursor;
    }

    public void setCursor(ItemStack cursor) {
        this.cursor = cursor;
    }

    public void init() {
        inventory.addItem(new ItemStack(Items.STONE, 64));
        inventory.addItem(new ItemStack(Items.LAVA, 64));
        inventory.addItem(new ItemStack(Items.LAVE, 64));
        inventory.addItem(new ItemStack(Items.GRASS_BLOCK, 64));
        inventory.addItem(new ItemStack(Items.DIRT, 64));
        inventory.addItem(new ItemStack(Items.SAND, 64));
        inventory.addItem(new ItemStack(Items.SHORT_GRASS, 64));
        inventory.addItem(new ItemStack(Items.MAPLE_LOG, 67));
        inventory.addItem(new ItemStack(Items.MAPLE_LEAVES, 64));
        inventory.addItem(new ItemStack(Items.MAPLE_PLANKS, 64));
        inventory.onAllChanged();
    }


    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
        gameMode.apply(abilities);
        sendPacket(new S2CGameModePacket(gameMode));
        sendAbilitiesPacket(abilities);
    }

    public void useItem() {
        int selected1 = selected;
        ItemSlot slot = inventory.get(selected1);
        if (slot == null) return;
        if (slot.isEmpty()) return;
        HitResult hitResult = castRay(6.0F);
        if (!(hitResult instanceof BlockHitResult blockHitResult)) return;
        slot.getItem().getItem().use(getWorld(), slot, blockHitResult, this);
    }

    public void onSelectItem(int selected) {
        if (selected < 0 || selected >= 9) return;
        this.selected = selected;
    }

    public void playSound(SoundEvent soundEvent, float volume, float pitch, Vector3d position, Vector3d velocity) {
        tmp.set(this.position).sub(position);
        sendPacket(new S2CSoundEventPacket(soundEvent, volume, pitch, new Vector3f(tmp), new Vector3f(velocity)));
    }

    public boolean isDataLoaded() {
        return dataLoaded;
    }

    @Override
    public void load(MapType map) {
        super.load(map);

        dataLoaded = true;
    }

    public void processMessage(String message) {
        if (message.startsWith("/")) {
            String[] args = message.substring(1).split(" ");
            String command = args[0];
            executeCommand(command, args);
        } else {
            for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager()) {
                serverPlayerEntity.sendMessage(getUsername() + ": " + message);
            }
        }
    }

    public void executeCommand(String command, String... args) {
        switch (command) {
            case "help" -> {
                sendMessage("Available commands:");
                sendMessage("/help - Shows this list");
                sendMessage("/give <item> <amount> - Gives the player an item");
                sendMessage("/tp <x> <y> <z> - Teleports the player to the given coordinates");
            }
            case "give" -> {
                if (args.length < 3) {
                    sendMessage("Usage: /give <item> <amount>");
                    return;
                }
                String itemName = args[1];
                int amount = Integer.parseInt(args[2]);
                Identifier key = Identifier.tryParse(itemName);
                if (key == null) {
                    sendMessage("Unknown item: " + itemName);
                    return;
                }
                Item item = Registries.ITEM.get(key);
                if (item == null) {
                    sendMessage("Unknown item: " + itemName);
                    return;
                }
                ItemStack stack = new ItemStack(item, amount);
                if (!inventory.addItem(stack)) {
                    sendMessage("Inventory is full!");
                    return;
                }

                inventory.onAllChanged();
                sendMessage("Gave " + amount + "x " + itemName + " to you!");
            }
            case "tp" -> {
                if (args.length < 4) {
                    sendMessage("Usage: /tp <x> <y> <z>");
                }

                double x;
                double y;
                double z;
                try {
                    x = Double.parseDouble(args[1]);
                } catch (NumberFormatException _) {
                    sendMessage("Invalid x coordinate!");
                    return;
                }
                try {
                    y = Double.parseDouble(args[2]);
                } catch (NumberFormatException _) {
                    sendMessage("Invalid y coordinate!");
                    return;
                }
                try {
                    z = Double.parseDouble(args[3]);
                } catch (NumberFormatException _) {
                    sendMessage("Invalid z coordinate!");
                    return;
                }
                teleport(x, y, z);
            }
            case "gm" -> {
                if (args.length != 2) {
                    sendMessage("Usage: /gm " + Arrays.stream(GameMode.values()).map(gameMode -> gameMode.name().toLowerCase(Locale.ROOT)).collect(Collectors.joining("|")));
                    return;
                }

                String arg = args[1];
                GameMode gameMode = GameMode.fromName(arg);

                if (gameMode == null) {
                    sendMessage("Invalid game mode: " + arg);
                    sendMessage("Usage: /gm " + Arrays.stream(GameMode.values()).map(gm -> gm.name().toLowerCase(Locale.ROOT)).collect(Collectors.joining("|")));
                    return;
                }

                setGameMode(gameMode);
                sendMessage("Game mode changed to " + gameMode);
            }
            case "spark" -> {
                if (args.length <= 1) {
                    sendMessage("Usage: /spark <args...>");
                    return;
                }

                server.getSparkPlugin().executeCommand(ArrayUtils.remove(args, 0));
            }
            default -> sendMessage("Unknown command: " + command);
        }
    }

    @Override
    public void onDeath() {
        super.onDeath();

        connection.send(new S2CDeathPacket());
    }

    public void openInventory() {
        super.openMenu(inventory);
    }

    public void respawn() {
        int spawnX = getServer().getSpawnX();
        int spawnZ = getServer().getSpawnZ();

        ServerWorld serverWorld = getServerWorld();
        serverWorld.loadChunkAsync(BlockVec.chunkOf(spawnX), 0, BlockVec.chunkOf(spawnZ), ChunkLoadTicket.PLAYER, GenerationBarrier.ALL).thenAccept(chunk -> {
            int height = serverWorld.getHeight(spawnX, spawnZ, HeightmapType.MOTION_BLOCKING);
            serverWorld.loadChunkAsync(BlockVec.chunkOf(spawnX), height, BlockVec.chunkOf(spawnZ), ChunkLoadTicket.PLAYER, GenerationBarrier.ALL).thenAccept(chunk1 -> {
                health = 20;
                setDead(false);
                teleport(spawnX, height, spawnZ);
            });
        });
    }
}
