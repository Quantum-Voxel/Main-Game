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

import com.google.gson.JsonObject;
import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.ubo.types.BooleanType;
import dev.ultreon.ubo.types.DataType;
import dev.ultreon.ubo.types.IntType;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

public abstract class BlockDataEntry<T> {
    public final T value;

    public BlockDataEntry(T value) {
        this.value = value;
    }

    public static <T extends Enum<T>> BlockDataEntry<T> ofEnum(T value) {
        return new EnumProperty<>(value);
    }

    public static BlockDataEntry<Integer> of(int value, int min, int max) {
        return new IntProperty(value, min, max);
    }

    public static BlockDataEntry<Boolean> of(boolean value) {
        return new BooleanEntry(value);
    }

    public abstract BlockDataEntry<?> read(PacketIO packetBuffer);

    public abstract BlockDataEntry<?> load(DataType<?> type);

    public T getValue() {
        return value;
    }

    @SuppressWarnings("unchecked")
    public <R> BlockDataEntry<R> cast(Class<R> type) {
        if (!type.isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("Cannot cast " + value.getClass() + " to " + type);
        }
        return (BlockDataEntry<R>) this;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public abstract DataType<?> save();

    public abstract void write(PacketIO packetBuffer);

    public abstract BlockDataEntry<?> parse(JsonObject overrideObj);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockDataEntry<?> that = (BlockDataEntry<?>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public abstract BlockDataEntry<T> copy();

    public abstract BlockDataEntry<T> with(T apply);

    public BlockDataEntry<T> map(Function<T, T> o) {
        return with(o.apply(value));
    }

    private static class BooleanEntry extends BlockDataEntry<Boolean> {
        public BooleanEntry(boolean value) {
            super(value);
        }

        public BooleanEntry() {
            super(false);
        }

        @Override
        public BlockDataEntry<?> read(PacketIO packetBuffer) {
            return with(packetBuffer.readBoolean());
        }

        @Override
        public BlockDataEntry<?> load(DataType<?> type) {
            return with(((BooleanType) type).getValue());
        }

        @Override
        public DataType<?> save() {
            return new BooleanType(value);
        }

        @Override
        public void write(PacketIO packetBuffer) {
            packetBuffer.writeBoolean(value);
        }

        @Override
        public BlockDataEntry<?> parse(JsonObject overrideObj) {
            return with(overrideObj.getAsJsonPrimitive("value").getAsBoolean());
        }

        @Override
        public BlockDataEntry<Boolean> copy() {
            return new BooleanEntry(value);
        }

        @Override
        public BlockDataEntry<Boolean> with(Boolean apply) {
            return new BooleanEntry(apply);
        }
    }

    private static class IntProperty extends BlockDataEntry<Integer> {
        private final int min;
        private final int max;

        public IntProperty(int value, int min, int max) {
            super(value);
            this.min = min;
            this.max = max;
        }

        public IntProperty() {
            super(0);

            min = 0;
            max = 0;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }

        @Override
        public BlockDataEntry<?> read(PacketIO packetBuffer) {
            return with(packetBuffer.readInt());
        }

        @Override
        public BlockDataEntry<?> load(DataType<?> type) {
            return with(((IntType) type).getValue());
        }

        @Override
        public DataType<?> save() {
            return new IntType(value);
        }

        @Override
        public void write(PacketIO packetBuffer) {
            packetBuffer.writeInt(value);
        }

        @Override
        public BlockDataEntry<?> parse(JsonObject overrideObj) {
            return with(overrideObj.get("value").getAsInt());
        }

        @Override
        public BlockDataEntry<Integer> copy() {
            return new IntProperty(value, min, max);
        }

        @Override
        public BlockDataEntry<Integer> with(Integer apply) {
            return new IntProperty(apply, min, max);
        }
    }

    private static class EnumProperty<T extends Enum<T>> extends BlockDataEntry<T> {
        public EnumProperty(T value) {
            super(value);
        }

        @SuppressWarnings("unchecked")
        @Override
        public BlockDataEntry<?> read(PacketIO packetBuffer) {
            return with((T) value.getClass().getEnumConstants()[packetBuffer.readInt()]);
        }

        @Override
        @SuppressWarnings("unchecked")
        public BlockDataEntry<?> load(DataType<?> type) {
            return with((T) value.getClass().getEnumConstants()[((IntType) type).getValue()]);
        }

        @Override
        public DataType<?> save() {
            return new IntType(value.ordinal());
        }

        @Override
        public void write(PacketIO packetBuffer) {
            packetBuffer.writeInt(value.ordinal());
        }

        @Override
        @SuppressWarnings("unchecked")
        public BlockDataEntry<?> parse(JsonObject overrideObj) {
            return with((T) Enum.valueOf(value.getClass(), overrideObj.get("value").getAsString().toUpperCase()));
        }

        @Override
        public BlockDataEntry<T> copy() {
            return new EnumProperty<>(value);
        }

        @Override
        public BlockDataEntry<T> with(T apply) {
            return new EnumProperty<>(apply);
        }

        @Override
        public String toString() {
            return '"' + value.name().toLowerCase(Locale.ROOT)
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"") + '"';
        }
    }
}
