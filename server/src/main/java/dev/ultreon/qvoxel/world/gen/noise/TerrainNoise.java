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

package dev.ultreon.qvoxel.world.gen.noise;

import de.articdive.jnoise.core.api.functions.Combiner;
import de.articdive.jnoise.core.api.pipeline.NoiseSource;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex2DVariant;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex3DVariant;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex4DVariant;
import de.articdive.jnoise.modules.octavation.fractal_functions.FractalFunction;
import de.articdive.jnoise.pipeline.JNoise;

/**
 * The TerrainNoise class generates terrain noise using a combination of various simplex noise sources.
 * It supports noise generation in one, two, three, and four dimensions.
 */
public class TerrainNoise implements NoiseSource {
    public static final int OCEAN_ELEVATION = 72;
    public static final int OCEAN_ELEVATION_HEIGHT = 16;
    private final NoiseSource noise;

    /**
     * Creates a new terrain noise generator with the given seed.
     *
     * @param seed the seed for the generator
     */
    public TerrainNoise(long seed) {
        noise = JNoise.newBuilder()
                .fastSimplex(seed, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                .combine(
                        // Smaller island generation
                        JNoise.newBuilder().fastSimplex(seed++, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                                .scale(1 / 2048f)
                                .build(),
                        Combiner.ADD
                ).combine(
                        JNoise.newBuilder().fastSimplex(seed++, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                                .scale(1 / 512f)
                                .addModifier(result -> result * 8)
                                .build(),
                        Combiner.ADD
                ).combine(
                        JNoise.newBuilder().fastSimplex(seed++, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                                .scale(1 / 256f)
                                .addModifier(result -> result * 64 + 64)
                                .build(),
                        Combiner.ADD
                ).combine(
                        // Smaller island generation
                        JNoise.newBuilder().fastSimplex(seed++, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                                .scale(1 / 1024f)
                                .addModifier(result -> result * 10)
                                .build(),
                        Combiner.ADD
                ).combine(
                        JNoise.newBuilder().fastSimplex(seed++, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                                .scale(1 / 128f)
                                .addModifier(result -> result * 8)
                                .build(),
                        Combiner.ADD
                ).combine(
                        JNoise.newBuilder().fastSimplex(seed++, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                                .scale(1 / 64f)
                                .addModifier(result -> result * 12)
                                .build(),
                        Combiner.ADD
                ).combine(
                        JNoise.newBuilder().fastSimplex(seed++, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                                .scale(1 / 16f)
                                .addModifier(result -> -result * 14)
                                .build(),
                        Combiner.ADD
                ).combine(
                        JNoise.newBuilder().fastSimplex(seed++, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                                .scale(1 / 8f)
                                .addModifier(result -> -result * OCEAN_ELEVATION_HEIGHT)
                                .build(),
                        Combiner.ADD
                ).combine(
                        JNoise.newBuilder().fastSimplex(seed++, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                                .scale(1 / 8f)
                                .addModifier(result -> result * OCEAN_ELEVATION_HEIGHT)
                                .build(),
                        Combiner.ADD
                ).combine(
                        JNoise.newBuilder().fastSimplex(seed++, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                                .scale(1 / 4f)
                                .addModifier(result -> result * 8)
                                .build(),
                        Combiner.ADD
                )
                .addModifier(result -> result + OCEAN_ELEVATION_HEIGHT < OCEAN_ELEVATION ? (result + OCEAN_ELEVATION_HEIGHT - OCEAN_ELEVATION) / 1.5 + OCEAN_ELEVATION : result + OCEAN_ELEVATION_HEIGHT)
                .addModifier(result -> result < 32 ? (result - 32) / 8 + 32 : result)
                .addModifier(result -> result + 64)
                .octavate(2, 0.1, 3, FractalFunction.TURBULENCE, false)
                .scale(1 / 16f).build();
    }

    /**
     * Evaluates the noise at the given position.
     *
     * @param x the x-coordinate of the position
     * @return the noise value at the given position
     */
    @Override
    public double evaluateNoise(double x) {
        return noise.evaluateNoise(x);
    }

    /**
     * Evaluates the noise at the given 2D position.
     *
     * @param x the x-coordinate of the position
     * @param y the y-coordinate of the position
     * @return the noise value at the given position
     */
    @Override
    public double evaluateNoise(double x, double y) {
        return noise.evaluateNoise(x, y);
    }

    /**
     * Evaluates the noise at the given 3D position.
     *
     * @param x the x-coordinate of the position
     * @param y the y-coordinate of the position
     * @param z the z-coordinate of the position
     * @return the noise value at the given position
     */
    @Override
    public double evaluateNoise(double x, double y, double z) {
        return noise.evaluateNoise(x, y, z);
    }

    /**
     * Evaluates the noise at the given 4D position.
     *
     * @param x the x-coordinate of the position
     * @param y the y-coordinate of the position
     * @param z the z-coordinate of the position
     * @param w the w-coordinate of the position
     * @return the noise value at the given position
     */
    @Override
    public double evaluateNoise(double x, double y, double z, double w) {
        return noise.evaluateNoise(x, y, z, w);
    }
}
