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

package dev.ultreon.qvoxel.world.gen.carver;

import dev.ultreon.qvoxel.world.BuilderChunk;

/**
 * Carver is an interface used to define the methods necessary for carving terrain in a chunk.
 */
public interface Carver {
    /**
     * Carves the terrain within the given chunk at specified coordinates based on hilliness.
     *
     * @param chunk The chunk in which the terrain carving is to be performed.
     * @param x     The x-coordinate within the chunk where the carving starts.
     * @param z     The z-coordinate within the chunk where the carving starts.
     * @return The height of the terrain at the given coordinates after carving, returns -1 if undetermined.
     */
    double carve(BuilderChunk chunk, int x, int z);

    /**
     * Computes the height of the terrain surface noise at the specified coordinates.
     *
     * @param x The x-coordinate to compute the surface height noise.
     * @param z The z-coordinate to compute the surface height noise.
     * @return The height of the surface noise at the specified coordinates.
     */
    double evaluateNoise(double x, double z);

    /**
     * Determines if the block at the specified coordinates (x, y, z) is air.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param z The z-coordinate.
     * @return true if the block at the given coordinates is air, false otherwise.
     */
    boolean isAir(int x, int y, int z);
}
