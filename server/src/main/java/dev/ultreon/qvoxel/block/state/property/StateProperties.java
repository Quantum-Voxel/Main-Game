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

package dev.ultreon.qvoxel.block.state.property;

import dev.ultreon.qvoxel.block.SlabBlock;
import dev.ultreon.qvoxel.util.Direction;

public class StateProperties {
    public static final StatePropertyKey<Boolean> ABSORB_LIGHT = new BoolPropertyKey("absorb_light");
    public static final StatePropertyKey<SlabBlock.Type> SLAB_TYPE = new EnumPropertyKey<>("type", SlabBlock.Type.class);
    public static final StatePropertyKey<Boolean> LIT = new BoolPropertyKey("lit");
    public static final StatePropertyKey<Direction> FACING = new EnumPropertyKey<>("facing", Direction.class, Direction.HORIZONTAL);
}
