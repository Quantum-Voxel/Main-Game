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

package dev.ultreon.qvoxel.world;

import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.util.Direction;
import org.joml.Vector3d;

/**
 * @param hitBlock      The block that was hit
 * @param adjacentBlock The block next to the hit face
 * @param hitPos        Exact hit relativePos
 * @param face          Face that was hit
 * @param distance      Distance along ray
 * @param state         BlockState of hit block
 */
public record BlockHitResult(
        BlockVec hitBlock,
        BlockVec adjacentBlock,
        Vector3d hitPos,
        World world,
        Direction face,
        double distance,
        BlockState state
) implements HitResult {
}