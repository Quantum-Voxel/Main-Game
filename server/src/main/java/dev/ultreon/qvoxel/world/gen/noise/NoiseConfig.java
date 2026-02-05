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

import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex2DVariant;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex3DVariant;
import de.articdive.jnoise.generators.noise_parameters.simplex_variants.Simplex4DVariant;
import de.articdive.jnoise.modules.octavation.fractal_functions.FractalFunction;
import de.articdive.jnoise.pipeline.JNoise;
import org.joml.Vector2f;

import java.util.Objects;

public final class NoiseConfig {
    private final float noiseZoom;
    private final float octaves;
    private final Vector2f offset;
    private long seed;
    private final float persistence;
    private final float redistributionModifier;
    private final float exponent;
    private final float amplitude;
    private final float base;

    /**
     * @param noiseZoom              the zoom of the noise
     * @param octaves                the roughness
     * @param offset
     * @param seed
     * @param persistence
     * @param redistributionModifier
     * @param exponent
     * @param amplitude
     * @param base
     */
    public NoiseConfig(float noiseZoom, float octaves, Vector2f offset, long seed, float persistence,
                       float redistributionModifier, float exponent, float amplitude, float base) {
        this.noiseZoom = noiseZoom;
        this.octaves = octaves;
        this.offset = offset;
        this.seed = seed;
        this.persistence = persistence;
        this.redistributionModifier = redistributionModifier;
        this.exponent = exponent;
        this.amplitude = amplitude;
        this.base = base;
    }

    public NoiseInstance create(long seed) {
        this.seed = seed;

        return new NoiseInstance(new JNoiseType(JNoise.newBuilder()
                .fastSimplex(seed, Simplex2DVariant.CLASSIC, Simplex3DVariant.IMPROVE_XZ, Simplex4DVariant.IMRPOVE_XYZ)
                .scale(noiseZoom)
                .octavate((int) octaves, 1, 1, FractalFunction.FBM, true)
//                .addModifier(v -> Math.pow(v, this.exponent))
                .clamp(0, 1)
                .addModifier(v -> v * amplitude + base)
                .build()), seed);
    }

    public float noiseZoom() {
        return noiseZoom;
    }

    public float octaves() {
        return octaves;
    }

    public Vector2f offset() {
        return offset;
    }

    public long seed() {
        return seed;
    }

    public float persistence() {
        return persistence;
    }

    public float redistributionModifier() {
        return redistributionModifier;
    }

    public float exponent() {
        return exponent;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        NoiseConfig that = (NoiseConfig) obj;
        return Float.floatToIntBits(noiseZoom) == Float.floatToIntBits(that.noiseZoom) &&
                octaves == that.octaves &&
                Objects.equals(offset, that.offset) &&
                Objects.equals(seed, that.seed) &&
                Float.floatToIntBits(persistence) == Float.floatToIntBits(that.persistence) &&
                Float.floatToIntBits(redistributionModifier) == Float.floatToIntBits(that.redistributionModifier) &&
                Float.floatToIntBits(exponent) == Float.floatToIntBits(that.exponent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(noiseZoom, octaves, offset, seed, persistence, redistributionModifier, exponent);
    }

    @Override
    public String toString() {
        return "NoiseConfig[" +
                "noiseZoom=" + noiseZoom + ", " +
                "octaves=" + octaves + ", " +
                "offset=" + offset + ", " +
                "worldOffset=" + seed + ", " +
                "persistence=" + persistence + ", " +
                "redistributionModifier=" + redistributionModifier + ", " +
                "exponent=" + exponent + ']';
    }

}
