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

import dev.ultreon.libs.commons.v0.util.EnumUtils;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.ubo.types.DataType;
import dev.ultreon.ubo.types.StringType;

public class EnumPropertyKey<T extends Enum<T> & StringSerializable> extends StatePropertyKey<T> {
    public EnumPropertyKey(String name, Class<T> enumClass) {
        super(name, createValues(enumClass), enumClass);
    }

    @SafeVarargs
    public EnumPropertyKey(String name, Class<T> enumClass, T... values) {
        super(name, values, enumClass);
    }

    private static <T extends Enum<T> & StringSerializable> T[] createValues(Class<T> enumClass) {
        return enumClass.getEnumConstants();
    }

    @Override
    public int indexOf(T value) {
        return value.ordinal();
    }

    @Override
    public void load(BlockState blockState, DataType<?> value) {
        blockState.with(this, EnumUtils.byName((String) value.getValue(), getValues().get(0)));
    }

    @Override
    public DataType<?> save(BlockState blockState) {
        return new StringType(blockState.get(this).name());
    }
}
