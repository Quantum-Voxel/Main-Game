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

import de.articdive.jnoise.core.api.pipeline.NoiseSource;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex2DVariant;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex3DVariant;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex4DVariant;
import de.articdive.jnoise.modules.octavation.fractal_functions.FractalFunction;
import de.articdive.jnoise.pipeline.JNoise;
import org.joml.Vector2d;
import org.joml.Vector2i;

public record DomainWarping(NoiseSource domainX, NoiseSource domainY, double amplitudeX,
                            double amplitudeY) {
    public DomainWarping(NoiseSource domainX, NoiseSource domainY) {
        this(domainX, domainY, 20, 20);
    }

    public DomainWarping(long seed) {
        this(seed, 20, 20);
    }

    public DomainWarping(long seed, double amplitudeX, double amplitudeY) {
        this((NoiseSource) JNoise.newBuilder()
                        .fastSimplex(seed++, Simplex2DVariant.CLASSIC, Simplex3DVariant.CLASSIC, Simplex4DVariant.CLASSIC)
                        .octavate(3, 0.1, 2.2, FractalFunction.FBM, true)
                        .scale(1 / 16.0)
                        .build(),
                JNoise.newBuilder()
                        .fastSimplex(seed, Simplex2DVariant.CLASSIC, Simplex3DVariant.CLASSIC, Simplex4DVariant.CLASSIC)
                        .octavate(3, 0.1, 2.2, FractalFunction.FBM, true)
                        .scale(1 / 16.0)
                        .build(), amplitudeX, amplitudeY);
    }

    public Vector2d generateDomainOffset(double x, double z) {
        double noiseX = domainX.evaluateNoise(x, 0, z) * amplitudeX;
        double noiseY = domainY.evaluateNoise(x, 0, z) * amplitudeY;
        return new Vector2d(noiseX, noiseY);
    }

    public Vector2i generateDomainOffsetInt(int x, int z) {
        Vector2d round = generateDomainOffset(x, z).round();
        return new Vector2i((int) round.x, (int) round.y);
    }
}
