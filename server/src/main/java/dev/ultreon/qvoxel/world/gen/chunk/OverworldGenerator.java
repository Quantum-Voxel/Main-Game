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

package dev.ultreon.qvoxel.world.gen.chunk;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.network.system.DevFlag;
import dev.ultreon.qvoxel.network.system.DeveloperMode;
import dev.ultreon.qvoxel.registry.Registry;
import dev.ultreon.qvoxel.server.ServerWorld;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.world.BuilderChunk;
import dev.ultreon.qvoxel.world.HeightmapType;
import dev.ultreon.qvoxel.world.World;
import dev.ultreon.qvoxel.world.gen.GenerationBarrier;
import dev.ultreon.qvoxel.world.gen.biome.Biome;
import dev.ultreon.qvoxel.world.gen.biome.BiomeData;
import dev.ultreon.qvoxel.world.gen.biome.BiomeGenerator;
import dev.ultreon.qvoxel.world.gen.carver.Carver;
import dev.ultreon.qvoxel.world.gen.carver.OverworldCarver;
import dev.ultreon.qvoxel.world.gen.noise.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The OverworldGenerator is responsible for generating the terrain of the Overworld.
 * It uses various noise configurations and biome data to create diverse and immersive biomes.
 * It extends the SimpleChunkGenerator, inheriting its basic functionalities and providing further customization.
 */
public class OverworldGenerator extends SimpleChunkGenerator {
    private final List<BiomeData> biomeGenData = new ArrayList<>();

    private NoiseConfig noiseConfig;

    private @UnknownNullability DomainWarping biomeDomain;
    private @UnknownNullability BiomeNoise humidNoise;
    private @UnknownNullability BiomeNoise tempNoise;
    private @UnknownNullability BiomeNoise variationNoise;
    private @UnknownNullability Carver carver;
    private DerivativeTunnelClosingCaveCarver caveCarver = new DerivativeTunnelClosingCaveCarver();

    public OverworldGenerator(Registry<Biome> biomeRegistry) {
        super(biomeRegistry);

        Collection<Biome> overworldBiomes = biomeRegistry.getTag(CommonConstants.id("overworld_biomes")).orElseThrow().getValues();
        for (Biome biome : overworldBiomes) {
            if (biome.doesNotGenerate()) continue;
            addBiome(biomeRegistry.getKey(biome));
        }
    }

    @Override
    public void create(@NotNull ServerWorld world, long seed) {
        NoiseConfigs noiseConfigs = world.getServer().getNoiseConfigs();
        noiseConfig = noiseConfigs.biomeMap;
        biomeDomain = new DomainWarping(seed + 600);

        TerrainNoise noise = new TerrainNoise(world.getSeed());
        humidNoise = new BiomeNoise(world.getSeed() + 400);
        tempNoise = new BiomeNoise(world.getSeed() + 410);
        variationNoise = new BiomeNoise(world.getSeed() + 420);
        carver = new OverworldCarver(noise, world.getSeed() + 500, tempNoise);

        for (Biome biome : biomes) {
            biomeGenData.add(new BiomeData(
                    biome.getTemperatureStart(), biome.getTemperatureEnd(),
                    biome.getHumidityStart(), biome.getHumidityEnd(),
                    biome.getHeightStart(), biome.getHeightEnd(),
                    biome.getHillinessStart(), biome.getHillinessEnd(),
                    biome.isOcean(), biome.create(world)
            ));
        }
    }

    @Override
    protected void generateTerrain(@NotNull BuilderChunk chunk, @NotNull Carver carver, GenerationBarrier barrier) {
        BlockVec offset = chunk.blockStart;
        if (barrier.compareTo(GenerationBarrier.CARVED) < 0) return;
        if (chunk.currentBarrier.compareTo(GenerationBarrier.CARVED) < 0) {
            for (var x = 0; x < World.CHUNK_SIZE; x++) {
                for (var z = 0; z < World.CHUNK_SIZE; z++) {
                    carver.carve(chunk, x, z);
                }
            }

            caveCarver.carve(chunk);
        }

        if (barrier.compareTo(GenerationBarrier.TERRAIN) < 0) return;
        if (chunk.currentBarrier.compareTo(GenerationBarrier.TERRAIN) < 0) {
            for (var x = 0; x < World.CHUNK_SIZE; x++) {
                for (var z = 0; z < World.CHUNK_SIZE; z++) {
                    double groundPos = chunk.getHeight(x, z, HeightmapType.TERRAIN);

                    var index = findGenerator(new Vector3i(offset.x + x, 0, offset.z + z), groundPos);
                    chunk.setBiomeGenerator(x, z, index.biomeGenerator);
                    index.biomeGenerator.processColumn(chunk, x, (int) Math.floor(groundPos), z);
                }
            }
        }
    }

    /**
     * Finds the appropriate biome generator index based on the provided offset and height.
     *
     * @param offset the vector offset to locate the generator
     * @param height the height to determine which generator to use
     * @return the biome generator index for the specified offset and height
     */
    public BiomeGenerator.Index findGenerator(Vector3i offset, double height) {
        return findGenerator(offset, height, DeveloperMode.isDevFlagEnabled(DevFlag.DomainWarping));
    }

    /**
     * Finds the appropriate biome generator index based on the provided offset, height, and domain warping preference.
     *
     * @param offset           the vector offset to locate the generator
     * @param height           the height to determine which generator to use
     * @param useDomainWarping flag indicating whether to apply domain warping to the offset
     * @return the biome generator index for the specified offset and height
     */
    public BiomeGenerator.Index findGenerator(Vector3i offset, double height, boolean useDomainWarping) {
        if (useDomainWarping) {
            Vector2d vector2d = biomeDomain.generateDomainOffset(offset.x, offset.z);
            Vector2i domainOffset = new Vector2i((int) Math.round(vector2d.x), (int) Math.round(vector2d.y));
            offset.add(domainOffset.x, 0, domainOffset.y);
        }

        if (humidNoise == null || tempNoise == null || variationNoise == null)
            throw new IllegalStateException("Biome generator noise has not been initialized yet!");

        var humid = humidNoise.evaluateNoise(offset.x * noiseConfig.noiseZoom(), offset.z * noiseConfig.noiseZoom()) * 2.0f;
        var temp = tempNoise.evaluateNoise(offset.x * noiseConfig.noiseZoom(), offset.z * noiseConfig.noiseZoom()) * 2.0f;
        var variation = variationNoise.evaluateNoise(offset.x * noiseConfig.noiseZoom(), offset.z * noiseConfig.noiseZoom()) * 2.0f;
        BiomeGenerator biomeGen = selectGenerator(height, humid, temp, variation);

        return new BiomeGenerator.Index(biomeGen);
    }

    /**
     * Selects an appropriate biome generator based on provided environmental parameters such as height, humidity, temperature, and variation.
     *
     * @param height    the vertical relativePos, used to determine the terrain's elevation
     * @param humid     the humidity level, which influences biome moisture characteristics
     * @param temp      the temperature level, affecting biome heat properties
     * @param variation the variation index, used to account for terrain irregularities
     * @return the selected {@link BiomeGenerator} that matches the given parameters; if no suitable generator is found, a default generator is returned
     */
    @SuppressWarnings("D")
    public BiomeGenerator selectGenerator(double height, double humid, double temp, double variation) {
        BiomeGenerator biomeGen = null;

        if (variation < -2.0 || variation > 2.0) {
            CommonConstants.LOGGER.warn("Invalid variation: {}", variation);
            return biomeGenData.getFirst().biomeGen();
        }

        if (temp < -2.0 || temp > 2.0) {
            CommonConstants.LOGGER.warn("Invalid temperature: {}", temp);
            return biomeGenData.getFirst().biomeGen();
        }

        if (humid < -2.0 || humid > 2.0) {
            CommonConstants.LOGGER.warn("Invalid humidity: {}", humid);
            return biomeGenData.getFirst().biomeGen();
        }

        if (height < -65536.0 || height > 65536.0) {
            CommonConstants.LOGGER.warn("Invalid height: {}", height);
            return biomeGenData.getFirst().biomeGen();
        }

        for (var data : biomeGenData) {
            var currentlyOcean = height < World.SEA_LEVEL - 4;

            boolean validHeight = height >= data.heightStartThreshold() && height < data.heightEndThreshold();
            boolean validHumid = humid >= data.humidityStartThreshold() && humid < data.humidityEndThreshold();
            boolean validTemp = temp >= data.temperatureStartThreshold() && temp < data.temperatureEndThreshold();
            boolean validVar = variation >= data.variationStartThreshold() && variation < data.variationEndThreshold();
            if (validTemp && data.isOcean() == currentlyOcean && validHumid && validHeight && validVar)
                biomeGen = data.biomeGen();
        }

        if (biomeGen == null) {
//            CommonConstants.LOGGER.trace("No biome generator found for height: {}, humid: {}, temp: {}, variation: {}", height, humid, temp, variation);
            return biomeGenData.getFirst().biomeGen();
        }

        return biomeGen;
    }

    @Override
    @NotNull
    public Carver getCarver() {
        if (carver == null) throw new IllegalStateException("Carver not initialized yet!");
        return carver;
    }

    @Override
    public double getTemperature(int x, int z) {
        return tempNoise.evaluateNoise(x * noiseConfig.noiseZoom(), z * noiseConfig.noiseZoom()) * 2.0f;
    }

    @Override
    public double evaluateNoise(int x, int z) {
        return carver.evaluateNoise(x, z);
    }

    @Override
    public boolean is2DNoise() {
        return true;
    }
}
