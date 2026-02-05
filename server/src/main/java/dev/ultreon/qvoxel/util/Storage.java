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

import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.ubo.types.DataType;
import dev.ultreon.ubo.types.MapType;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;

public interface Storage<D> extends Cloneable {

    <T extends DataType<?>> MapType save(MapType outputData, Class<T> dataType, Function<D, T> encoder);

    <T extends DataType<?>> void load(MapType inputData, Class<T> dataType, Function<T, D> decoder);

    void write(PacketIO buffer, BiConsumer<PacketIO, D> encoder);

    void read(PacketIO buffer, Function<PacketIO, D> decoder);

    boolean set(int idx, D value);

    D get(int idx);

    <R> Storage<R> map(R defaultValue, IntFunction<R[]> type, Function<D, R> o);

    Storage<D> clone();

    boolean isUniform();

    /*@Nullable D getRandom(RNG rng, AtomicInteger integer, Predicate<D> predicate);*/

    void setUniform(D value);
}
