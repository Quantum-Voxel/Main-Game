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

public class NoiseUtils {
    private NoiseUtils() {
    }

    public static double remapValue(double value, double initialMin, double initialMax, double outputMin, double outputMax) {
        return outputMin + (value - initialMin) * (outputMax - outputMin) / (initialMax - initialMin);
    }

    public static double remapValue01(double value, double outputMin, double outputMax) {
        return outputMin + (value - 0) * (outputMax - outputMin);
    }

    public static int remapValue01ToInt(double value, double outputMin, double outputMax) {
        return (int) remapValue01(value, outputMin, outputMax);
    }

    public static double redistribution(double noise, NoiseInstance settings) {
        return Math.pow(noise * settings.redistributionModifier(), settings.exponent());
    }

    public static double octavePerlin(double x, double z, NoiseInstance settings) {
        double zoom = settings.noiseZoom();
        x *= zoom;
        z *= zoom;
        x += zoom;
        z += zoom;

        Vector2f offset = settings.offset();

        double total = 0.0F;
        double frequency = 1.0F;
        double amplitude = 1.0F;
        double amplitudeSum = 0.0F;  // Used for normalizing result to 0.0 - 1.0 chance

//        for (int i = 0; i < settings.octaves(); i++) {
        total += settings.eval((offset.x + x) * frequency, (offset.y + z) * frequency) * amplitude;

//            amplitudeSum += amplitude;
//
//            amplitude *= settings.persistence();
//            frequency *= 2;
//        }

        total *= settings.amplitude();
        total += settings.base();

        return total;
    }
}