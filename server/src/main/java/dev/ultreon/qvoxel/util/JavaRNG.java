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

package dev.ultreon.qvoxel.util;

import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class JavaRNG implements RNG {
    public static final @Nullable RNG GLOBAL = new JavaRNG();
    private final Random random;

    public JavaRNG() {
        this(new Random());
    }

    public JavaRNG(long seed) {
        this(new Random(seed));
    }

    public JavaRNG(Random random) {
        this.random = random;
    }

    @Override
    public int randint(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    @Override
    public boolean chance(int max) {
        return random.nextInt(max + 1) == 0;
    }

    @Override
    public boolean chance(float chance) {
        return random.nextFloat() <= chance;
    }

    @Override
    public float randrange(float min, float max) {
        return random.nextFloat() * (max - min) + min;
    }

    @Override
    public double randrange(double min, double max) {
        return random.nextDouble() * (max - min) + min;
    }

    @Override
    public void setSeed(long seed) {
        random.setSeed(seed);
    }

    @Override
    public long nextLong() {
        return random.nextLong();
    }

    @Override
    public int nextInt(int bound) {
        return random.nextInt(bound);
    }

    @Override
    public float nextFloat() {
        return random.nextFloat();
    }

    @Override
    public int nextInt(int min, int max) {
        return random.nextInt(max - min) + min;
    }
}
