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

import java.util.Objects;

public final class JNoiseType implements NoiseType {
    private final NoiseSource source;

    public JNoiseType(NoiseSource source) {
        this.source = source;
    }

    @Override
    public void close() {

    }

    @Override
    public double eval(double x, double y) {
        return source.evaluateNoise(x, y) + 1 / 2.0;
    }

    @Override
    public double eval(double x, double y, double z) {
        return source.evaluateNoise(x, y, z) + 1 / 2.0;
    }

    public NoiseSource source() {
        return source;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        var that = (JNoiseType) obj;
        return Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source);
    }

    @Override
    public String toString() {
        return "JNoiseType[" +
                "source=" + source + ']';
    }

}
