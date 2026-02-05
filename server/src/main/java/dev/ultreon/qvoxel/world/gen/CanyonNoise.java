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

package dev.ultreon.qvoxel.world.gen;

import de.articdive.jnoise.core.api.pipeline.NoiseSource;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex2DVariant;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex3DVariant;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex4DVariant;
import de.articdive.jnoise.modules.octavation.fractal_functions.FractalFunction;
import de.articdive.jnoise.pipeline.JNoise;

public class CanyonNoise implements NoiseSource {
    private final JNoise noise;
    private final double strength;
    private final NoiseSource temperatureNoise;
    private final NoiseSource heightNoise;

    public CanyonNoise(long seed, double strength, double maxValue, NoiseSource temperatureNoise, NoiseSource heightNoise) {
        this.strength = strength;
        this.temperatureNoise = temperatureNoise;
        this.heightNoise = heightNoise;
        noise = JNoise.newBuilder()
                .fastSimplex(seed, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                .octavate(3, 0.1, 5, FractalFunction.TURBULENCE, true)
                .scale(1 / 32.0)
                .addModifier(value -> value * 2.0)
                .clamp(-2.0, maxValue)
                .build();
    }

    @Override
    public double evaluateNoise(double x) {
        throw new UnsupportedOperationException("Requires 2D noise");
    }

    @Override
    public double evaluateNoise(double x, double y) {
        double temperature = temperatureNoise.evaluateNoise(x, y);
        double height = heightNoise.evaluateNoise(x, y);
        double height00 = heightNoise.evaluateNoise(x - 2, y - 2);
        double height01 = heightNoise.evaluateNoise(x - 2, y + 2);
        double height10 = heightNoise.evaluateNoise(x + 2, y - 2);
        double height11 = heightNoise.evaluateNoise(x + 2, y + 2);

        double flatness = 0.001;
        double height0 = (height00 + height01) / 2.0;
        double height1 = (height10 + height11) / 2.0;
        double heightCenter = (height0 + height1) / 2.0;
        double heightDelta = Math.abs(heightCenter - height);

        double canyonEffect = Math.pow(Math.max(0.0, 1.0 - heightDelta / flatness), strength);

        return temperature * canyonEffect;
    }

    @Override
    public double evaluateNoise(double x, double y, double z) {
        throw new UnsupportedOperationException("Requires 2D noise");
    }

    @Override
    public double evaluateNoise(double x, double y, double z, double w) {
        throw new UnsupportedOperationException("Requires 2D noise");
    }
}
