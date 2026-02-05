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

public class OceanicNoise implements NoiseSource {
    private final JNoise noise;
    private final double strength;

    public OceanicNoise(long seed, double strength, double maxValue) {
        this.strength = strength;
        noise = JNoise.newBuilder()
                .fastSimplex(seed, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                .octavate(3, 0.1, 5, FractalFunction.TURBULENCE, true)
                .scale(1 / 128.0)
                .addModifier(value -> Math.pow(value + 1.0, strength) + maxValue)
                .build();
    }

    @Override
    public double evaluateNoise(double x) {
        return noise.evaluateNoise(x / 32);
    }

    @Override
    public double evaluateNoise(double x, double y) {
        return noise.evaluateNoise(x / 32, y / 32);
    }

    @Override
    public double evaluateNoise(double x, double y, double z) {
        return noise.evaluateNoise(x, y, z);
    }

    @Override
    public double evaluateNoise(double x, double y, double z, double w) {
        return noise.evaluateNoise(x, y, z, w);
    }
}
