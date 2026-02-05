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

import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.ubo.types.BooleanType;
import dev.ultreon.ubo.types.DataType;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class BoolPropertyKey extends StatePropertyKey<Boolean> {
    public BoolPropertyKey(String name) {
        super(name, new Boolean[]{FALSE, TRUE}, Boolean.class);
    }

    @Override
    public int indexOf(Boolean value) {
        return value ? 1 : 0;
    }

    @Override
    public void load(BlockState blockState, DataType<?> value) {
        blockState.with(this, (Boolean) value.getValue());
    }

    @Override
    public DataType<?> save(BlockState blockState) {
        return new BooleanType(blockState.get(this));
    }
}
