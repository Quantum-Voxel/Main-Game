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
import dev.ultreon.qvoxel.block.Block;
import dev.ultreon.qvoxel.block.Blocks;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.registry.RegistryKey;
import dev.ultreon.qvoxel.registry.RegistryKeys;
import dev.ultreon.qvoxel.server.QuantumServer;
import dev.ultreon.qvoxel.server.ServerWorld;
import dev.ultreon.qvoxel.world.gen.TerrainFeature;
import dev.ultreon.qvoxel.world.gen.layer.GroundTerrainLayer;
import dev.ultreon.qvoxel.world.gen.layer.SurfaceTerrainLayer;
import dev.ultreon.qvoxel.world.gen.layer.TerrainLayer;
import dev.ultreon.ubo.types.MapType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * The Biome class represents a specific type of terrain with defined characteristics like temperature,
 * humidity, height, and hilliness.
 * It includes terrain layers and world generation features.
 * Biome instances are built using the nested {@link Biome.Builder} class.
 */
public abstract class Biome {
    public static final RegistryKey<Biome> DEFAULT = RegistryKey.of(RegistryKeys.BIOME, CommonConstants.id("unknown"));
    public static final PacketCodec<Biome> PACKET_CODEC = PacketCodec.registry(RegistryKeys.BIOME);
    protected final List<TerrainLayer> layers = new ArrayList<>();
    protected final List<TerrainFeature> surfaceFeatures = new ArrayList<>();
    protected final List<TerrainFeature> undergroundFeatures = new ArrayList<>();
    private final float temperatureStart;
    private final float temperatureEnd;
    private final boolean isOcean;
    private final boolean doesNotGenerate;
    private final float humidityStart;
    private final float humidityEnd;
    private final float heightStart;
    private final float heightEnd;
    private final float hillinessStart;
    private final float hillinessEnd;

    protected Biome(float temperatureStart,
                    float temperatureEnd,
                    boolean isOcean,
                    boolean doesNotGenerate,
                    float humidityStart,
                    float humidityEnd,
                    float heightStart,
                    float heightEnd,
                    float hillinessStart,
                    float hillinessEnd) {
        this.temperatureStart = temperatureStart;
        this.temperatureEnd = temperatureEnd;
        this.isOcean = isOcean;
        this.doesNotGenerate = doesNotGenerate;
        this.humidityStart = humidityStart;
        this.humidityEnd = humidityEnd;
        this.heightStart = heightStart;
        this.heightEnd = heightEnd;
        this.hillinessStart = hillinessStart;
        this.hillinessEnd = hillinessEnd;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final void buildLayers() {
        onBuildLayers();
    }

    protected abstract void onBuildLayers();

    public boolean doesNotGenerate() {
        return doesNotGenerate;
    }

    public boolean isValidForHeight(float height) {
        return height >= heightStart && height < heightEnd;
    }

    public BiomeGenerator create(ServerWorld world) {
        Random rng = new Random();
        layers.forEach(layer -> layer.create(world));
        rng.setSeed(world.getSeed() * 375432987432L);
        surfaceFeatures.forEach(feature -> feature.create(world, rng.nextLong()));
        rng.setSeed(world.getSeed() * 837489327838L);
        undergroundFeatures.forEach(feature -> feature.create(world, rng.nextLong()));

        return new BiomeGenerator(world, this, layers, surfaceFeatures, undergroundFeatures);
    }

    public float getTemperatureStart() {
        return temperatureStart;
    }

    public float getTemperatureEnd() {
        return temperatureEnd;
    }

    public MapType save(QuantumServer server) {
        MapType mapType = new MapType();
        mapType.putString("id", String.valueOf(server.getRegistries().get(RegistryKeys.BIOME).getKey(this).id()));
        return mapType;
    }

    public static Biome load(QuantumServer server, MapType mapType) {
        Biome biome = QuantumServer.get().getRegistries().get(RegistryKeys.BIOME).get(Identifier.tryParse(mapType.getString("id", "plains")));
        return biome != null ? biome : server.getBiomes().plains;
    }

    public boolean isOcean() {
        return isOcean;
    }

    public boolean isTopBlock(BlockState currentBlock) {
        if (currentBlock.getBlock() == Blocks.AIR) return true;
        return layers.stream().anyMatch(terrainLayer -> terrainLayer instanceof SurfaceTerrainLayer && ((SurfaceTerrainLayer) terrainLayer).surfaceBlock == currentBlock.getBlock());
    }

    public BlockState getTopMaterial() {
        return layers.stream().map(terrainLayer -> terrainLayer instanceof SurfaceTerrainLayer ? ((SurfaceTerrainLayer) terrainLayer).surfaceBlock : null).filter(Objects::nonNull).findFirst().map(Block::getDefaultState).orElse(null);
    }

    public BlockState getFillerMaterial() {
        return layers.stream().map(terrainLayer -> terrainLayer instanceof GroundTerrainLayer ? ((GroundTerrainLayer) terrainLayer).block : null).filter(Objects::nonNull).findFirst().map(Block::getDefaultState).orElse(null);
    }

    public float getHumidityStart() {
        return humidityStart;
    }

    public float getHumidityEnd() {
        return humidityEnd;
    }

    public float getHeightStart() {
        return heightStart;
    }

    public float getHeightEnd() {
        return heightEnd;
    }

    public float getHillinessStart() {
        return hillinessStart;
    }

    public float getHillinessEnd() {
        return hillinessEnd;
    }

    /**
     * Builder class for constructing {@link Biome} instances with various configurations.
     * This builder allows setting noise configurations, terrain layers, world generation features,
     * temperature, humidity, height, and hilliness ranges, as well as marking the biome as an ocean
     * or a non-generating biome.
     */
    public static class Builder {
        private final List<TerrainLayer> layers = new ArrayList<>();
        private final List<TerrainFeature> surfaceFeatures = new ArrayList<>();
        private final List<TerrainFeature> undergroundFeatures = new ArrayList<>();
        private float temperatureStart = -2.0f;
        private float temperatureEnd = 2.0f;
        private float humidityStart = -2.0f;
        private float humidityEnd = 2.0f;
        private float heightStart = -64f;
        private float heightEnd = 320f;
        private float hillinessStart = -2.0f;
        private float hillinessEnd = 2.0f;
        private boolean isOcean;
        private boolean doesNotGenerate;

        private Builder() {

        }

        public Builder layer(TerrainLayer layer) {
            layers.add(layer);
            return this;
        }

        public Builder surfaceFeature(TerrainFeature feature) {
            surfaceFeatures.add(feature);
            return this;
        }

        public Builder undergroundFeature(TerrainFeature feature) {
            undergroundFeatures.add(feature);
            return this;
        }

        @Deprecated
        public Builder temperatureStart(float temperatureStart) {
            this.temperatureStart = temperatureStart;
            return this;
        }

        @Deprecated
        public Builder temperatureEnd(float temperatureEnd) {
            this.temperatureEnd = temperatureEnd;
            return this;
        }

        @Deprecated
        public Builder humidityStart(float humidityStart) {
            this.humidityStart = humidityStart;
            return this;
        }

        @Deprecated
        public Builder humidityEnd(float humidityEnd) {
            this.humidityEnd = humidityEnd;
            return this;
        }

        public Builder temperatureRange(float temperatureStart, float temperatureEnd) {
            this.temperatureStart = temperatureStart;
            this.temperatureEnd = temperatureEnd;
            return this;
        }

        public Builder humidityRange(float humidityStart, float humidityEnd) {
            this.humidityStart = humidityStart;
            this.humidityEnd = humidityEnd;
            return this;
        }

        public Builder heightRange(float heightStart, float heightEnd) {
            this.heightStart = heightStart;
            this.heightEnd = heightEnd;
            return this;
        }

        public Builder hillinessRange(float hillinessStart, float hillinessEnd) {
            this.hillinessStart = hillinessStart;
            this.hillinessEnd = hillinessEnd;
            return this;
        }

        public Builder ocean() {
            isOcean = true;
            return this;
        }

        public Biome build() {
            if (Float.isNaN(temperatureStart)) throw new IllegalArgumentException("Temperature start not set.");
            if (Float.isNaN(temperatureEnd)) throw new IllegalArgumentException("Temperature end not set.");

            return new Biome(temperatureStart, temperatureEnd, isOcean, doesNotGenerate, humidityStart, humidityEnd, heightStart, heightEnd, hillinessStart, hillinessEnd) {
                @Override
                protected void onBuildLayers() {
                    layers.addAll(Builder.this.layers);
                    surfaceFeatures.addAll(Builder.this.surfaceFeatures);
                    undergroundFeatures.addAll(Builder.this.undergroundFeatures);
                }
            };
        }

        public Builder doesNotGenerate() {
            doesNotGenerate = true;
            return this;
        }
    }
}
