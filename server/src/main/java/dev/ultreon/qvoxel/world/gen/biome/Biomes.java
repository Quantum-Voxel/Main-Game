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

package dev.ultreon.qvoxel.world.gen.biome;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.block.Blocks;
import dev.ultreon.qvoxel.registry.RegistryKey;
import dev.ultreon.qvoxel.registry.RegistryKeys;
import dev.ultreon.qvoxel.resource.GameNode;
import dev.ultreon.qvoxel.server.QuantumServer;
import dev.ultreon.qvoxel.server.ServerWorld;
import dev.ultreon.qvoxel.world.gen.feature.*;
import dev.ultreon.qvoxel.world.gen.layer.*;
import dev.ultreon.qvoxel.world.gen.noise.NoiseConfigs;
import dev.ultreon.ubo.types.StringType;
import org.jetbrains.annotations.NotNull;

/**
 * The Biomes class represents a collection of biomes in the game.
 * These biomes include various landscapes and climatic regions such as forests, deserts, and oceans.
 * The class provides methods to initialize and register biomes.
 */
public class Biomes extends GameNode {
    public static final RegistryKey<Biome> VOID = RegistryKey.of(RegistryKeys.BIOME, CommonConstants.id("void"));

    private final QuantumServer server;

    public final Biome void_;
    public final Biome snowyPlains;
    public final Biome deepTaiga;
    public final Biome taiga;
    public final Biome plains;
    public final Biome dryPlains;
    public final Biome rockyPlains;
    public final Biome coldPlains;
    public final Biome frozenPlains;
    public final Biome hills;
    public final Biome jungle;
    public final Biome forest;
    public final Biome desert;
    public final Biome beach;
    public final Biome mountains;
    public final Biome ocean;
    public final Biome lukeWarmOcean;
    public final Biome warmOcean;
    public final Biome coldOcean;
    public final Biome space;

    private RegistryKey<Biome> defaultKey;

    /**
     * Initializes biome configurations for a QuantumServer instance.
     *
     * @param server the QuantumServer instance to which these biomes belong
     */
    public Biomes(QuantumServer server) {
        this.server = server;

        NoiseConfigs noiseConfigs = server.getNoiseConfigs();

        void_ = register("void", Biome.builder()
                .temperatureRange(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY)
                .doesNotGenerate()
                .build());
        snowyPlains = register("snowy_plains", Biome.builder()
                .temperatureRange(-1.0f, 0.0f)
                .humidityRange(-1.0f, 0.0f)
                .heightRange(65.6f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.SNOWY_GRASS_BLOCK, 0))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.patch, Blocks.DIRT, 0.5f, 4))
                .surfaceFeature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.0005f))
                .surfaceFeature(new FoliageFeature(Blocks.SNOWY_SHORT_GRASS.getDefaultState(), 0.425f, Blocks.SNOWY_GRASS_BLOCK))
                .surfaceFeature(new MapleTreeFeature(Blocks.MAPLE_LOG.getDefaultState(), Blocks.MAPLE_LEAVES.getDefaultState(), 0.007f))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE.getDefaultState(), 20, new IntRange(4, 6), new IntRange(24, 72)))
                .build());
        deepTaiga = register("deep_taiga", Biome.builder()
                .temperatureRange(-2.0f, -1.0f)
                .humidityRange(0.0f, 1.0f)
                .heightRange(65.6f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.SNOWY_GRASS_BLOCK, 0))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.patch, Blocks.DIRT, 0.5f, 4))
                .surfaceFeature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.0005f))
                .surfaceFeature(new FoliageFeature(Blocks.SNOWY_SHORT_GRASS.getDefaultState(), 0.425f, Blocks.SNOWY_GRASS_BLOCK))
                .surfaceFeature(new PineTreeFeature(Blocks.PINE_LOG.getDefaultState(), Blocks.PINE_LEAVES.getDefaultState(), 0.01f))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE.getDefaultState(), 20, new IntRange(4, 6), new IntRange(24, 72)))
                .build());
        taiga = register("taiga", Biome.builder()
                .temperatureRange(-1.0f, 0.0f)
                .humidityRange(0.0f, 1.0f)
                .heightRange(65.6f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.SNOWY_GRASS_BLOCK, 0))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.patch, Blocks.DIRT, 0.5f, 4))
                .surfaceFeature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.0005f))
                .surfaceFeature(new FoliageFeature(Blocks.SNOWY_SHORT_GRASS.getDefaultState(), 0.425f, Blocks.SNOWY_GRASS_BLOCK))
                .surfaceFeature(new PineTreeFeature(Blocks.PINE_LOG.getDefaultState(), Blocks.PINE_LEAVES.getDefaultState(), 0.007f))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE.getDefaultState(), 20, new IntRange(4, 6), new IntRange(24, 72)))
                .build());
        plains = register("plains", Biome.builder()
                .temperatureRange(0.0f, 1.0f)
                .humidityRange(0.0f, 1.0f)
                .heightRange(65.6f, 70.5f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.patch, Blocks.DIRT, 0.5f, 4))
                .surfaceFeature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.0005f))
                .surfaceFeature(new FoliageFeature(Blocks.SHORT_GRASS.getDefaultState(), 0.425f, Blocks.GRASS_BLOCK))
                .surfaceFeature(new OakTreeFeature(Blocks.OAK_LOG.getDefaultState(), Blocks.OAK_LEAVES.getDefaultState(), 0.002f))
                .surfaceFeature(new AspenTreeFeature(Blocks.ASPEN_LOG.getDefaultState(), Blocks.ASPEN_LEAVES.getDefaultState(), 0.002f))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE.getDefaultState(), 20, new IntRange(4, 6), new IntRange(24, 72)))
                .build());
        dryPlains = register("dry_plains", Biome.builder()
                .temperatureRange(0.0f, 1.0f)
                .humidityRange(-2.0f, -1.0f)
                .heightRange(65.6f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(RandomBlocksLayer.surface(1, 64, 108, Blocks.SAND, Blocks.GRASS_BLOCK))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.patch, Blocks.GRASS_BLOCK, 0.2f, 1))
                .surfaceFeature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.0005f))
                .surfaceFeature(new FoliageFeature(Blocks.SHORT_GRASS.getDefaultState(), 0.425f, Blocks.GRASS_BLOCK))
                .surfaceFeature(new TreeFeature(Blocks.MESQUITE_LOG, Blocks.MESQUITE_LEAVES, 0.007f, 3, 5))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE.getDefaultState(), 20, new IntRange(4, 6), new IntRange(24, 72)))
                .build());
        rockyPlains = register("rocky_plains", Biome.builder()
                .temperatureRange(0.0f, 1.0f)
                .humidityRange(-1.0f, 0.0f)
                .heightRange(65.6f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.patch, Blocks.GRASS_BLOCK, 0.2f, 1))
                .surfaceFeature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.005f))
                .surfaceFeature(new FoliageFeature(Blocks.SHORT_GRASS.getDefaultState(), 0.425f, Blocks.GRASS_BLOCK))
                .surfaceFeature(new MapleTreeFeature(Blocks.MAPLE_LOG.getDefaultState(), Blocks.MAPLE_LEAVES.getDefaultState(), 0.007f))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE.getDefaultState(), 20, new IntRange(4, 6), new IntRange(24, 72)))
                .build());
        coldPlains = register("cold_plains", Biome.builder()
                .temperatureRange(0.0f, 1.0f)
                .humidityRange(-1.0f, 0.0f)
                .heightRange(65.6f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.patch, Blocks.GRASS_BLOCK, 0.2f, 1))
                .surfaceFeature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.005f))
                .surfaceFeature(new FoliageFeature(Blocks.SHORT_GRASS.getDefaultState(), 0.425f, Blocks.GRASS_BLOCK))
                .surfaceFeature(new MapleTreeFeature(Blocks.MAPLE_LOG.getDefaultState(), Blocks.MAPLE_LEAVES.getDefaultState(), 0.007f))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE.getDefaultState(), 20, new IntRange(4, 6), new IntRange(24, 72)))
                .build());
        frozenPlains = register("frozen_plains", Biome.builder()
                .temperatureRange(-2.0f, -1.0f)
                .humidityRange(0.0f, 1.0f)
                .heightRange(65.6f, 108.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 3, 1))
                .layer(RandomBlocksLayer.surface(3, 64, 108, Blocks.ICE, Blocks.SNOW_BLOCK))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.patch, Blocks.GRASS_BLOCK, 0.2f, 1))
                .surfaceFeature(new FoliageFeature(Blocks.SNOWY_SHORT_GRASS.getDefaultState(), 0.425f, Blocks.SNOW_BLOCK))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE.getDefaultState(), 20, new IntRange(4, 6), new IntRange(24, 72)))
                .build());
        hills = register("hills", Biome.builder()
                .temperatureRange(0.0f, 1.0f)
                .humidityRange(0.0f, 1.0f)
                .heightRange(70.5f, 108.0f)
                .hillinessRange(1.0f, 3.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.patch, Blocks.DIRT, 0.5f, 4))
                .surfaceFeature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.0005f))
                .surfaceFeature(new FoliageFeature(Blocks.SHORT_GRASS.getDefaultState(), 0.425f, Blocks.GRASS_BLOCK))
                .surfaceFeature(new TreeFeature(Blocks.CEDAR_LOG, Blocks.CEDAR_LEAVES, 0.007f, 3, 5))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE.getDefaultState(), 20, new IntRange(4, 6), new IntRange(24, 72)))
                .build());
        jungle = register("jungle", Biome.builder()
                .temperatureRange(0.0f, 2.0f)
                .humidityRange(1.0f, 2.0f)
                .heightRange(65.6f, 108.0f)
                .hillinessRange(0.0f, 1.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.patch, Blocks.DIRT, 0.5f, 4))
                .surfaceFeature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.0005f))
                .surfaceFeature(new FoliageFeature(Blocks.SHORT_GRASS.getDefaultState(), 0.425f, Blocks.GRASS_BLOCK))
                .surfaceFeature(new TreeFeature(Blocks.MAHOGANY_LOG, Blocks.MAHOGANY_LEAVES, 0.007f, 4, 8))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE.getDefaultState(), 20, new IntRange(4, 6), new IntRange(24, 72)))
                .build());
        forest = register("forest", Biome.builder()
                .temperatureRange(0.0f, 1.0f)
                .humidityRange(1.0f, 2.0f)
                .heightRange(65.6f, 108.0f)
                .hillinessRange(0.0f, 1.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 4))
                .layer(new GroundTerrainLayer(Blocks.DIRT, 1, 3))
                .layer(new SurfaceTerrainLayer(Blocks.GRASS_BLOCK, 0))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.patch, Blocks.DIRT, 0.5f, 4))
                .surfaceFeature(new FoliageFeature(Blocks.SHORT_GRASS.getDefaultState(), 0.425f, Blocks.GRASS_BLOCK))
                .surfaceFeature(new RockFeature(noiseConfigs.rock, Blocks.STONE, 0.0005f))
                .surfaceFeature(new OakTreeFeature(Blocks.OAK_LOG.getDefaultState(), Blocks.OAK_LEAVES.getDefaultState(), 0.025f))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE.getDefaultState(), 20, new IntRange(4, 6), new IntRange(24, 72)))
                .build());
        desert = register("desert", Biome.builder()
                .temperatureRange(1.0f, 2.0f)
                .humidityRange(-2.0f, 0.0f)
                .heightRange(64.0f, 320.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 7))
                .layer(new GroundTerrainLayer(Blocks.SANDSTONE, 3, 4))
                .layer(new SurfaceTerrainLayer(Blocks.SAND, 3))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new CactusFeature(Blocks.CACTUS.getDefaultState(), 0.01f))
                .surfaceFeature(new PatchFeature(noiseConfigs.patch, Blocks.SANDSTONE, 0.1f, 4))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE.getDefaultState(), 20, new IntRange(4, 6), new IntRange(24, 72)))
                .build());
        beach = register("beach", Biome.builder()
                .temperatureRange(-2.0f, 2.0f)
                .humidityRange(-2.0f, 2.0f)
                .heightRange(59.0f, 65.6f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 3))
                .layer(new SurfaceTerrainLayer(Blocks.SAND, 3))
                .layer(new WaterTerrainLayer(64))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE.getDefaultState(), 20, new IntRange(4, 6), new IntRange(24, 72)))
                .build());
        mountains = register("mountains", Biome.builder()
                .temperatureRange(-2.0f, 2.0f)
                .humidityRange(-2.0f, 2.0f)
                .heightRange(108.0f, 320.0f)
                .layer(new AirTerrainLayer())
                .layer(new UndergroundTerrainLayer(Blocks.STONE, 3))
                .layer(RandomBlocksLayer.surface(3, 108, 128, Blocks.COBBLESTONE, Blocks.STONE, Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.GRAVEL))
                .layer(RandomBlocksLayer.surface(3, 128, 160, Blocks.SNOWY_GRASS_BLOCK, Blocks.SNOW_BLOCK))
                .layer(RandomBlocksLayer.surface(4, 160, 256, Blocks.SNOW_BLOCK, Blocks.SNOWY_GRASS_BLOCK, Blocks.ICE))
                .layer(RandomBlocksLayer.surface(4, 256, 384, Blocks.SNOW_BLOCK, Blocks.ICE))
                .layer(RandomBlocksLayer.surface(4, 384, 2147483647, Blocks.ICE))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new FoliageFeature(Blocks.SHORT_GRASS.getDefaultState(), 0.425f, Blocks.GRASS_BLOCK))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE.getDefaultState(), 20, new IntRange(4, 6), new IntRange(24, 72)))
                .build());
        ocean = register("ocean", Biome.builder()
                .temperatureRange(0f, 0.5f)
                .heightRange(Float.NEGATIVE_INFINITY, 64.0f)
                .layer(new SurfaceTerrainLayer(Blocks.DIRT, 4))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.waterPatch1, Blocks.SAND, 0.3f, 4))
                .surfaceFeature(new PatchFeature(noiseConfigs.waterPatch2, Blocks.GRAVEL, 0.3f, 4))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE.getDefaultState(), 20, new IntRange(4, 6), new IntRange(24, 72)))
                .ocean()
                .build());
        lukeWarmOcean = register("luke_warm_ocean", Biome.builder()
                .temperatureRange(0.5f, 1.0f)
                .heightRange(Float.NEGATIVE_INFINITY, 64.0f)
                .layer(new SurfaceTerrainLayer(Blocks.SAND, 4))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.waterPatch1, Blocks.SAND, 0.3f, 4))
                .surfaceFeature(new PatchFeature(noiseConfigs.waterPatch2, Blocks.DIRT, 0.3f, 4))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE.getDefaultState(), 20, new IntRange(4, 6), new IntRange(24, 72)))
                .ocean()
                .build());
        warmOcean = register("warm_ocean", Biome.builder()
                .temperatureRange(1.0f, 2.0f)
                .heightRange(Float.NEGATIVE_INFINITY, 64.0f)
                .layer(new SurfaceTerrainLayer(Blocks.SANDSTONE, 4))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.waterPatch1, Blocks.SAND, 0.3f, 4))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE.getDefaultState(), 20, new IntRange(4, 6), new IntRange(24, 72)))
                .ocean()
                .build());
        coldOcean = register("cold_ocean", Biome.builder()
                .temperatureRange(-2f, 0.0f)
                .heightRange(Float.NEGATIVE_INFINITY, 64.0f)
                .layer(new SurfaceTerrainLayer(Blocks.DIRT, 4))
                .layer(new WaterTerrainLayer(64))
                .surfaceFeature(new PatchFeature(noiseConfigs.waterPatch2, Blocks.GRAVEL, 0.3f, 4))
                .undergroundFeature(new OreFeature(Blocks.IRON_ORE.getDefaultState(), 20, new IntRange(4, 6), new IntRange(24, 72)))
                .ocean()
                .build());

        space = register("space", Biome.builder()
                .temperatureRange(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY)
                .heightRange(Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY)
                .build());

        for (Biome biome : server.getRegistries().get(RegistryKeys.BIOME).values()) {
            biome.buildLayers();
        }
    }

    /**
     * Registers a biome with the specified name in the server's biome registry.
     *
     * @param name  the name of the biome to register
     * @param biome the biome instance to register
     * @return the registered biome instance
     */
    public Biome register(String name, Biome biome) {
        server.getRegistries().get(RegistryKeys.BIOME).register(new Identifier(name), biome);
        return biome;
    }

    public void init() {
        // NOOP
    }

    public RegistryKey<Biome> getDefaultKey() {
        if (defaultKey == null) {
            defaultKey = server.getRegistries().get(RegistryKeys.BIOME).getKey(plains);
        }
        return defaultKey;
    }

    public @NotNull BiomeGenerator load(ServerWorld world, StringType stringType) {
        String biomeName = stringType.getValue();
        Identifier biomeId = Identifier.tryParse(biomeName);
        if (biomeId == null) {
            return world.getBiomeGen(server.getRegistries().get(RegistryKeys.BIOME).get(getDefaultKey()));
        } else {
            return world.getBiomeGen(server.getRegistries().get(RegistryKeys.BIOME).get(biomeId));
        }
    }
}
