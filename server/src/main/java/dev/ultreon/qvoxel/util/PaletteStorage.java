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

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.resource.GameNode;
import dev.ultreon.ubo.types.DataType;
import dev.ultreon.ubo.types.ListType;
import dev.ultreon.ubo.types.MapType;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * <p>Palette storage is used for storing data in palettes.
 * It's used for optimizing memory and storage usage.
 * Generally used for advanced voxel games.</p>
 *
 * <p>It makes use of short arrays to store {@link #getPalette() index pointers} to the {@linkplain #getData() data}.
 * While the data itself is stored without any duplicates.</p>
 *
 * @param <D> the data type.
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 */
@SuppressWarnings("unchecked")
@ApiStatus.Experimental
public class PaletteStorage<D> extends GameNode implements Storage<D> {
    private final D defaultValue;
    private short[] palette;
    private List<D> data;

    @Deprecated
    public PaletteStorage(D defaultValue, int size) {
        this(size, defaultValue);
    }

    public PaletteStorage(D defaultValue, short[] palette, D[] data) {
        this.defaultValue = defaultValue;
        this.palette = palette;
        this.data = new ArrayList<>(data.length);
        for (D d : data) {
            if (d == null) {
                this.data.add(this.defaultValue);
                continue;
            }
            this.data.add(d);
        }
    }

    public PaletteStorage(int size, D defaultValue) {
        this.defaultValue = defaultValue;

        palette = new short[size];
        data = new ArrayList<>();
        Arrays.fill(palette, (short) -1);
    }

    @Override
    public <T extends DataType<?>> MapType save(MapType outputData, Class<T> dataType, Function<D, T> encoder) {
        synchronized (this) {
            ListType<T> data = new ListType<>(dataType);
            for (@NotNull D entry : this.data) {
                data.add(encoder.apply(entry));
            }
            outputData.put("Data", data);

            outputData.putShortArray("Palette", palette.clone());

            return outputData;
        }
    }

    @Override
    public <T extends DataType<?>> void load(MapType inputData, Class<T> dataType, Function<T, D> decoder) {
        synchronized (this) {
            data.clear();
            ListType<T> data = inputData.getList("Data", new ListType<>(dataType));
            for (T entryData : data.getValue()) {
                D entry = decoder.apply(entryData);
                if (entry == null) {
                    this.data.add(defaultValue);
                    continue;
                }
                this.data.add(entry);
            }

            palette = inputData.getShortArray("Palette", new short[palette.length]);
        }
    }

    @Override
    public void write(PacketIO buffer, BiConsumer<PacketIO, D> encoder) {
        synchronized (this) {
            buffer.writeVarInt(data.size());
            for (D entry : data) if (entry != null) encoder.accept(buffer, entry);
            buffer.writeVarInt(palette.length);
            for (short v : palette) {
                if (v != -1 && v >= data.size()) throw new IllegalArgumentException("Invalid palette index " + v);
                buffer.writeShort(v);
            }
        }
    }

    @Override
    public void read(PacketIO buffer, Function<PacketIO, D> decoder) {
        synchronized (this) {
            var data = new ArrayList<D>();
            var dataSize = buffer.readVarInt();
            for (int i = 0; i < dataSize; i++)
                data.add(decoder.apply(buffer));
            this.data = data;

            short[] palette = new short[buffer.readVarInt()];
            for (int i = 0; i < palette.length; i++) {
                palette[i] = buffer.readShort();
                if (palette[i] != -1 && palette[i] >= this.data.size())
                    throw new IllegalArgumentException("Invalid palette index " + palette[i]);
            }

            this.palette = palette;
        }
    }

    @Override
    public boolean set(int idx, D value) {
        synchronized (this) {
            if (idx < 0 || idx >= palette.length) throw new IndexOutOfBoundsException();

            if (value == null) {
                remove(idx);
                return false;
            }

            short old = palette[idx];

            short setIdx = (short) data.indexOf(value);
            if (setIdx == -1) {
                setIdx = add(idx, value);
            }
            palette[idx] = setIdx;

            if (old < 0 || ArrayUtils.contains(palette, old))
                return false;

            int i1 = data.indexOf(value);
            if (i1 >= 0) {
                data.set(old, value);
                return false;
            }

            data.remove(old);

            // Update paletteMap entries for indices after the removed one
            for (int i = 0; i < palette.length; i++) {
                int oldValue = palette[i];
                palette[i] = (short) (oldValue - 1);
            }
            return false;
        }
    }

    public short toDataIdx(int idx) {
        return idx >= 0 && idx < palette.length ? palette[idx] : -1;
    }

    public D direct(int dataIdx) {
        synchronized (this) {
            if (dataIdx >= 0 && dataIdx < data.size()) {
                D d = data.get(dataIdx);
                return d != null ? d : defaultValue;
            }
        }

        return defaultValue;
    }

    public short add(int idx, D value) {
        short dataIdx = (short) data.size();
        data.add(value);
        palette[idx] = dataIdx;
        return dataIdx;
    }

    public void remove(int idx) {
        if (idx >= 0 && idx < data.size()) {
            int dataIdx = toDataIdx(idx);
            if (dataIdx < 0) return;
            data.remove(dataIdx);
            palette[idx] = -1;

            // Update paletteMap entries for indices after the removed one
            for (int i = idx; i < palette.length; i++) {
                int oldValue = palette[i];
                palette[i] = (short) (oldValue - 1);
            }
        }
    }

    @NotNull
    @Override
    public D get(int idx) {
        synchronized (this) {
            short paletteIdx = toDataIdx(idx);
            return paletteIdx < 0 ? defaultValue : direct(paletteIdx);
        }
    }

    @Override
    public <R> PaletteStorage<R> map(@NotNull R defaultValue, IntFunction<R[]> generator, @NotNull Function<@NotNull D, @Nullable R> mapper) {
        var ref = new Object() {
            final transient Function<D, R> mapperRef = mapper;
        };

        List<R> data = new ArrayList<>(this.data.size());
        for (D d : this.data) {
            if (ref.mapperRef == null) {
                CommonConstants.LOGGER.warn("Mapper in PaletteStorage.mapper(...) just nullified out of thin air! What the f*** is going on?");
                data.add(defaultValue);
                continue;
            }

            R applied;
            try {
                applied = ref.mapperRef.apply(d);
            } catch (NullPointerException e) {
                CommonConstants.LOGGER.warn("Something sus going on, why is there a nullptr? Double check passed, third check failed :huh:", e);
                data.add(defaultValue);
                continue;
            }
            data.add(applied == null ? defaultValue : applied);
        }
        return new PaletteStorage<>(defaultValue, palette, data.toArray(generator));
    }

    public short[] getPalette() {
        synchronized (this) {
            return palette.clone();
        }
    }

    public List<D> getData() {
        synchronized (this) {
            return List.copyOf(data);
        }
    }

    public void set(short[] palette, D[] data) {
        if (this.palette.length != palette.length)
            throw new IllegalArgumentException("Palette length must be equal.");

        if (ArrayUtils.contains(data, null))
            throw new IllegalArgumentException("Data cannot contain null values.");

        this.palette = palette;
        this.data = new ArrayList<>(data.length);
        for (D d : data) {
            if (d == null) {
                this.data.add(defaultValue);
                continue;
            }
            this.data.add(d);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaletteStorage<?> that = (PaletteStorage<?>) o;
        return Arrays.equals(palette, that.palette) && data.equals(that.data);
    }

    @Override
    public int hashCode() {
        int result = data.hashCode();
        result = 31 * result + Arrays.hashCode(palette);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PaletteStorage<D> clone() {
        try {
            synchronized (this) {
                PaletteStorage<D> clone = (PaletteStorage<D>) super.clone();
                clone.data = List.copyOf(data);
                clone.palette = palette.clone();
                return clone;
            }
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isUniform() {
        return data.size() <= 1;
    }

    @Override
    public void setUniform(D value) {
        data.clear();
        data.add(value);
        Arrays.fill(palette, (short) 0);
    }

    public boolean isEmpty() {
        return data.isEmpty() || data.getFirst().equals(defaultValue);
    }
}
