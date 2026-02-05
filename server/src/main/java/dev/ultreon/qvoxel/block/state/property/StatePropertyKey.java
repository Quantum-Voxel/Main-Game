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

import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.ubo.types.DataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class StatePropertyKey<T> {
    private final List<T> values;
    private String name;
    private Class<T> type;

    public StatePropertyKey(String name, T[] values, Class<T> type) {
        this.values = List.of(values);
        this.name = name;
    }

    public List<T> getValues() {
        return values;
    }

    public int getValueIndex(T value) {
        int idx = values.indexOf(value);
        if (idx < 0) throw new IllegalArgumentException("Invalid value: " + value);
        return idx;
    }

    public T getValueByIndex(int index) {
        return values.get(index);
    }

    public int getValueCount() {
        return values.size();
    }

    public T read(@NotNull PacketIO packetBuffer) {
        int index = packetBuffer.readInt();
        if (index < 0 || index >= values.size()) {
            throw new IllegalArgumentException("Invalid index for property " + name + ": " + index);
        }

        return type.cast(values.get(index));
    }

    public void write(@NotNull PacketIO packetBuffer, Object value) {
        int index = values.indexOf(value);
        if (index == -1) {
            throw new IllegalArgumentException("Invalid value for property " + name + ": " + value);
        }

        packetBuffer.writeInt(index);
    }

    public abstract int indexOf(T value);

    public abstract void load(BlockState blockState, DataType<?> value);

    public abstract DataType<?> save(BlockState blockState);

    public String getName() {
        return name;
    }
}
