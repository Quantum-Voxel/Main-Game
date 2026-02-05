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

public interface RNG {
    int randint(int min, int max);

    boolean chance(int max);

    boolean chance(float chance);

    float randrange(float min, float max);

    double randrange(double min, double max);

    void setSeed(long seed);

    long nextLong();

    int nextInt(int bound);

    float nextFloat();

    int nextInt(int min, int max);
}
