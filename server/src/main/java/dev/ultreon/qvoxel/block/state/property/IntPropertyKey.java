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
import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.ubo.types.DataType;
import dev.ultreon.ubo.types.IntType;
import org.jetbrains.annotations.NotNull;

public class IntPropertyKey extends StatePropertyKey<Integer> {
    private final int minValue;
    private final int maxValue;

    public IntPropertyKey(String name, int minValue, int maxValue) {
        super(name, createValues(minValue, maxValue), Integer.class);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    private static Integer[] createValues(int minValue, int maxValue) {
        Integer[] values = new Integer[maxValue - minValue + 1];
        for (int i = minValue; i <= maxValue; i++) {
            values[i - minValue] = i;
        }
        return values;
    }

    @Override
    public Integer read(@NotNull PacketIO packetBuffer) {
        return packetBuffer.readInt();
    }

    @Override
    public void write(@NotNull PacketIO packetBuffer, Object value) {
        if (value instanceof Integer) {
            int integer = (Integer) value;
            if (integer < minValue || integer > maxValue)
                throw new IllegalArgumentException("Value " + integer + " is out of range for property " + getName());
            packetBuffer.writeInt(integer);
        }
    }

    @Override
    public int indexOf(Integer value) {
        if (value < minValue || value > maxValue)
            throw new IllegalArgumentException("Value " + value + " is out of range for property " + getName());
        return value - minValue;
    }

    @Override
    public void load(BlockState blockState, DataType<?> value) {
        blockState.with(this, (Integer) value.getValue());
    }

    @Override
    public DataType<?> save(BlockState blockState) {
        return new IntType(blockState.get(this));
    }
}
