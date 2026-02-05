/*
 * Copyright 2025. Quinten 'Qubix' Jungblut
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.ultreon.qvoxel.client;

public enum Interpolation {
    LINEAR {
        @Override
        public double interpolate(double a, double b, double t) {
            return a + (b - a) * t;
        }
    },
    EASE_IN {
        @Override
        public double interpolate(double a, double b, double t) {
            // Quadratic ease-in
            return a + (b - a) * (t * t);
        }
    },
    EASE_OUT {
        @Override
        public double interpolate(double a, double b, double t) {
            // Quadratic ease-out
            return a + (b - a) * (1 - (1 - t) * (1 - t));
        }
    },
    EASE_IN_OUT {
        @Override
        public double interpolate(double a, double b, double t) {
            // Smoothstep
            double tt = t * t * (3 - 2 * t);
            return a + (b - a) * tt;
        }
    },
    CUBIC {
        @Override
        public double interpolate(double a, double b, double t) {
            // Cubic interpolation (ease-in-out curve)
            return a + (b - a) * (t * t * (3 - 2 * t));
        }
    },
    EXPONENTIAL {
        @Override
        public double interpolate(double a, double b, double t) {
            // Exponential (ease-in)
            return a + (b - a) * Math.pow(2, 10 * (t - 1));
        }
    },
    REVERSE_LINEAR {
        @Override
        public double interpolate(double a, double b, double t) {
            return b + (a - b) * t;
        }
    },
    STEP {
        @Override
        public double interpolate(double a, double b, double t) {
            return t < 0.5 ? a : b;
        }
    },
    SINE {
        @Override
        public double interpolate(double a, double b, double t) {
            // Sine interpolation (ease-in-out)
            double st = -(Math.cos(Math.PI * t) - 1) / 2;
            return a + (b - a) * st;
        }
    },
    ;

    public abstract double interpolate(double a, double b, double t);
}
