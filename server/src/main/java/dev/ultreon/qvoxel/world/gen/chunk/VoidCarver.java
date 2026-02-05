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

package dev.ultreon.qvoxel.world.gen.chunk;

import dev.ultreon.qvoxel.world.BuilderChunk;
import dev.ultreon.qvoxel.world.gen.carver.Carver;

public class VoidCarver implements Carver {
    @Override
    public double carve(BuilderChunk chunk, int x, int z) {
        return 0;
    }

    @Override
    public double evaluateNoise(double x, double z) {
        return 0;
    }

    @Override
    public boolean isAir(int x, int y, int z) {
        return y > 0;
    }
}
