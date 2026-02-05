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

import org.joml.Vector2f;

import java.io.Closeable;

public class NoiseInstance implements Closeable {
    private final NoiseType noise;
    private final long seed;
    private final double noiseZoom;
    private final double octaves;
    private final Vector2f offset;
    private final double redistributionModifier;
    private final double exponent;
    private final double persistence;
    private final double amplitude;
    private final double base;

    public NoiseInstance(NoiseType noise, long seed) {
        this(noise, seed, 1, 0, new Vector2f(), 1, 1, 0, 1, 0);
    }

    public NoiseInstance(NoiseType noise, long seed, double noiseZoom, double octaves, Vector2f offset, double redistributionModifier, double exponent, double persistence, double amplitude, double base) {
        this.noise = noise;
        this.seed = seed;
        this.noiseZoom = noiseZoom;
        this.octaves = octaves;
        this.offset = offset;
        this.redistributionModifier = redistributionModifier;
        this.exponent = exponent;
        this.persistence = persistence;
        this.amplitude = amplitude;
        this.base = base;
    }

    public double eval(double x, double y) {
        return noise.eval(x, y);
    }

    public double eval(double x, double y, double z) {
        return noise.eval(x, y, z);
    }

    public long seed() {
        return seed;
    }

    public double noiseZoom() {
        return noiseZoom;
    }

    public double octaves() {
        return octaves;
    }

    public Vector2f offset() {
        return offset;
    }

    public double redistributionModifier() {
        return redistributionModifier;
    }

    public double exponent() {
        return exponent;
    }

    public double persistence() {
        return persistence;
    }

    @Override
    public void close() {
        noise.close();
    }

    public double amplitude() {
        return amplitude;
    }

    public double base() {
        return base;
    }
}
