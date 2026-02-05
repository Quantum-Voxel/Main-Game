package dev.ultreon.qvoxel.network.packets;

import dev.ultreon.libs.collections.v0.tables.Table;
import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.network.PacketException;
import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.network.handler.PacketHandler;
import dev.ultreon.qvoxel.registry.Registry;
import dev.ultreon.qvoxel.registry.RegistryKey;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.util.ChunkVec;
import dev.ultreon.ubo.types.MapType;
import kotlin.jvm.functions.*;
import org.jetbrains.annotations.ApiStatus;
import org.joml.*;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface PacketCodec<T> extends PacketWriter<T>, PacketReader<T> {
    @ApiStatus.Obsolete
    PacketCodec<Void> VOID = of((_, _) -> null, (_, _, _) -> {
    });

    PacketCodec<Byte> BYTE = of(PacketIO::readByte, PacketIO::writeByte);
    PacketCodec<Boolean> BOOLEAN = of(PacketIO::readBoolean, PacketIO::writeBoolean);
    PacketCodec<Short> SHORT = of(PacketIO::readShort, PacketIO::writeShort);
    PacketCodec<short[]> SHORTS = of(PacketIO::readShortArray, PacketIO::writeShortArray);
    PacketCodec<Integer> INT = of(PacketIO::readInt, PacketIO::writeInt);
    PacketCodec<int[]> INTS = of(PacketIO::readIntArray, PacketIO::writeIntArray);
    PacketCodec<Integer> VAR_INT = of(PacketIO::readVarInt, PacketIO::writeVarInt);
    PacketCodec<Long> LONG = of(PacketIO::readLong, PacketIO::writeLong);
    PacketCodec<long[]> LONGS = of(PacketIO::readLongArray, PacketIO::writeLongArray);
    PacketCodec<Float> FLOAT = of(PacketIO::readFloat, PacketIO::writeFloat);
    PacketCodec<float[]> FLOATS = of(PacketIO::readFloatArray, PacketIO::writeFloatArray);
    PacketCodec<Double> DOUBLE = of(PacketIO::readDouble, PacketIO::writeDouble);
    PacketCodec<double[]> DOUBLES = of(PacketIO::readDoubleArray, PacketIO::writeDoubleArray);
    PacketCodec<Vector2f> VECTOR2F = of(io -> io.readVector2f(new Vector2f()), PacketIO::writeVector2f);
    PacketCodec<Vector3f> VECTOR3F = of(io -> io.readVector3f(new Vector3f()), PacketIO::writeVector3f);
    PacketCodec<Vector4f> VECTOR4F = of(io -> io.readVector4f(new Vector4f()), PacketIO::writeVector4f);
    PacketCodec<Vector2d> VECTOR2D = of(io -> io.readVector2d(new Vector2d()), PacketIO::writeVector2d);
    PacketCodec<Vector3d> VECTOR3D = of(io -> io.readVector3d(new Vector3d()), PacketIO::writeVector3d);
    PacketCodec<Vector4d> VECTOR4D = of(io -> io.readVector4d(new Vector4d()), PacketIO::writeVector4d);
    PacketCodec<BlockVec> BLOCK_VEC = of(PacketIO::readBlockVec, PacketIO::writeBlockVec);
    PacketCodec<ChunkVec> CHUNK_VEC = of(PacketIO::readChunkVec, PacketIO::writeChunkVec);
    PacketCodec<UUID> UUID = of(PacketIO::readUuid, PacketIO::writeUuid);
    PacketCodec<Identifier> ID = of(PacketIO::readId, PacketIO::writeId);

    static PacketCodec<String> string(int max) {
        return of((io) -> io.readString(max), (io, value) -> io.writeString(value, max));
    }

    static PacketCodec<char[]> chars(int max) {
        return of((io) -> io.readString(max).toCharArray(), (io, value) -> io.writeString(new String(value), max));
    }

    static PacketCodec<byte[]> bytes(int max) {
        return of((io) -> io.readByteArray(max), (io, value) -> io.writeByteArray(value, max));
    }

    static PacketCodec<short[]> shorts(int max) {
        return of((io) -> io.readShortArray(max), PacketIO::writeShortArray);
    }

    static PacketCodec<int[]> ints(int max) {
        return of((io) -> io.readIntArray(max), PacketIO::writeIntArray);
    }

    static PacketCodec<long[]> longs(int max) {
        return of((io) -> io.readLongArray(max), PacketIO::writeLongArray);
    }

    static <T> PacketCodec<T[]> objects(int size, PacketCodec<T> codec, Class<T> type) {
        return of((handler, io) -> {
            @SuppressWarnings("unchecked")
            T[] o = (T[]) Array.newInstance(type, size);
            for (int i = 0; i < size; i++) {
                o[i] = codec.fromBytes(handler, io);
            }
            return o;
        }, (array, handler, io) -> {
            if (array.length < size)
                throw new PacketException("Array size too small to send: " + array.length + "(min: " + size + ")");

            for (T item : array) {
                codec.toBytes(item, handler, io);
            }
        });
    }

    static <T> PacketCodec<T[]> objects(PacketCodec<T> codec, Class<T> type) {
        return of((handler, io) -> {
            int size = io.readVarInt();

            @SuppressWarnings("unchecked")
            T[] o = (T[]) Array.newInstance(type, size);
            for (int i = 0; i < size; i++) {
                o[i] = codec.fromBytes(handler, io);
            }
            return o;
        }, (array, handler, io) -> {
            io.writeVarInt(array.length);
            for (T item : array) {
                codec.toBytes(item, handler, io);
            }
        });
    }

    static <T, C extends Collection<T>> PacketCodec<C> collection(int max, PacketCodec<T> codec, Supplier<C> factory) {
        return of((handler, io) -> {
            int size = io.readVarInt();
            if (size > max) throw new PacketException("List too large: " + size + " (max: " + max + ")");
            C list = factory.get();
            for (int i = 0; i < size; i++) {
                list.add(codec.fromBytes(handler, io));
            }
            return list;
        }, (list, handler, io) -> {
            io.writeVarInt(list.size());
            for (T item : list) {
                codec.toBytes(item, handler, io);
            }
        });
    }

    static <K, V, M extends Map<K, V>> PacketCodec<M> map(int max, PacketCodec<K> keyCodec, PacketCodec<V> valueCode, Supplier<M> factory) {
        return of((handler, io) -> {
            int size = io.readVarInt();
            if (size > max) throw new PacketException("List too large: " + size + " (max: " + max + ")");
            M list = factory.get();
            for (int i = 0; i < size; i++) {
                list.put(keyCodec.fromBytes(handler, io), valueCode.fromBytes(handler, io));
            }
            return list;
        }, (set, handler, io) -> {
            io.writeVarInt(set.size());
            for (Map.Entry<K, V> item : set.entrySet()) {
                V value = item.getValue();
                if (value == null) continue;
                keyCodec.toBytes(item.getKey(), handler, io);
                valueCode.toBytes(value, handler, io);
            }
        });
    }

    static <R, C, V, T extends Table<R, C, V>> PacketCodec<T> table(int rowMax, int columnMax, PacketCodec<R> rowCodec, PacketCodec<C> columnCodec, PacketCodec<V> valueCode, Supplier<T> factory) {
        return of((handler, io) -> {
            int rowSize = io.readVarInt();
            int columnSize = io.readVarInt();
            if (rowSize > rowMax)
                throw new PacketException("Table row too large: " + rowSize + " (max: " + rowMax + ")");
            if (columnSize > columnMax)
                throw new PacketException("Table column too large: " + rowSize + " (max: " + columnSize + ")");
            T table = factory.get();
            for (int x = 0; x < rowSize; x++) {
                for (int y = 0; y < columnSize; y++) {
                    table.put(rowCodec.fromBytes(handler, io), columnCodec.fromBytes(handler, io), valueCode.fromBytes(handler, io));
                }
            }
            return table;
        }, (set, handler, io) -> {
            io.writeVarInt(set.rowSize());
            io.writeVarInt(set.columnSize());
            for (Table.Cell<R, C, V> item : set.cellSet()) {
                rowCodec.toBytes(item.getRow(), handler, io);
                columnCodec.toBytes(item.getColumn(), handler, io);
                valueCode.toBytes(item.getValue(), handler, io);
            }
        });
    }

    static <A, T> PacketCodec<T> packed(PacketCodec<A> codec1,
                                        Function<T, A> extract1,
                                        Function<A, T> pack
    ) {
        return of(
                (handler, io) -> pack.apply(codec1.fromBytes(handler, io)),
                (packet, handler, io) -> codec1.toBytes(extract1.apply(packet), handler, io)
        );
    }

    static <A, B, T> PacketCodec<T> packed(PacketCodec<A> codec1,
                                           Function<T, A> extract1,
                                           PacketCodec<B> codec2,
                                           Function<T, B> extract2,
                                           Function2<A, B, T> pack
    ) {
        return of((handler, io) -> pack.invoke(codec1.fromBytes(handler, io), codec2.fromBytes(handler, io)), (packet, handler, io) -> {
            codec1.toBytes(extract1.apply(packet), handler, io);
            codec2.toBytes(extract2.apply(packet), handler, io);
        });
    }

    static <A, B, C, T> PacketCodec<T> packed(PacketCodec<A> codec1,
                                              Function<T, A> extractA,
                                              PacketCodec<B> codec2,
                                              Function<T, B> extractB,
                                              PacketCodec<C> codec3,
                                              Function<T, C> extractC,
                                              Function3<A, B, C, T> pack
    ) {
        return of((handler, io) -> pack.invoke(codec1.fromBytes(handler, io), codec2.fromBytes(handler, io), codec3.fromBytes(handler, io)), (packet, handler, io) -> {
            codec1.toBytes(extractA.apply(packet), handler, io);
            codec2.toBytes(extractB.apply(packet), handler, io);
            codec3.toBytes(extractC.apply(packet), handler, io);
        });
    }

    static <A, B, C, D, T> PacketCodec<T> packed(PacketCodec<A> codec1,
                                                 Function<T, A> extract1,
                                                 PacketCodec<B> codec2,
                                                 Function<T, B> extract2,
                                                 PacketCodec<C> codec3,
                                                 Function<T, C> extract3,
                                                 PacketCodec<D> codec4,
                                                 Function<T, D> extract4,
                                                 Function4<A, B, C, D, T> pack
    ) {
        return of((handler, io) -> pack.invoke(codec1.fromBytes(handler, io), codec2.fromBytes(handler, io), codec3.fromBytes(handler, io), codec4.fromBytes(handler, io)), (packet, handler, io) -> {
            codec1.toBytes(extract1.apply(packet), handler, io);
            codec2.toBytes(extract2.apply(packet), handler, io);
            codec3.toBytes(extract3.apply(packet), handler, io);
            codec4.toBytes(extract4.apply(packet), handler, io);
        });
    }

    static <A, B, C, D, E, T> PacketCodec<T> packed(PacketCodec<A> codec1,
                                                    Function<T, A> extract1,
                                                    PacketCodec<B> codec2,
                                                    Function<T, B> extract2,
                                                    PacketCodec<C> codec3,
                                                    Function<T, C> extract3,
                                                    PacketCodec<D> codec4,
                                                    Function<T, D> extract4,
                                                    PacketCodec<E> codec5,
                                                    Function<T, E> extract5,
                                                    Function5<A, B, C, D, E, T> pack
    ) {
        return of((handler, io) -> pack.invoke(codec1.fromBytes(handler, io), codec2.fromBytes(handler, io), codec3.fromBytes(handler, io), codec4.fromBytes(handler, io), codec5.fromBytes(handler, io)), (packet, handler, io) -> {
            codec1.toBytes(extract1.apply(packet), handler, io);
            codec2.toBytes(extract2.apply(packet), handler, io);
            codec3.toBytes(extract3.apply(packet), handler, io);
            codec4.toBytes(extract4.apply(packet), handler, io);
            codec5.toBytes(extract5.apply(packet), handler, io);
        });
    }

    static <A, B, C, D, E, F, T> PacketCodec<T> packed(PacketCodec<A> codec1,
                                                       Function<T, A> extract1,
                                                       PacketCodec<B> codec2,
                                                       Function<T, B> extract2,
                                                       PacketCodec<C> codec3,
                                                       Function<T, C> extract3,
                                                       PacketCodec<D> codec4,
                                                       Function<T, D> extract4,
                                                       PacketCodec<E> codec5,
                                                       Function<T, E> extract5,
                                                       PacketCodec<F> codec6,
                                                       Function<T, F> extract6,
                                                       Function6<A, B, C, D, E, F, T> pack) {
        return of((handler, io) -> pack.invoke(codec1.fromBytes(handler, io), codec2.fromBytes(handler, io), codec3.fromBytes(handler, io), codec4.fromBytes(handler, io), codec5.fromBytes(handler, io), codec6.fromBytes(handler, io)), (packet, handler, io) -> {
            codec1.toBytes(extract1.apply(packet), handler, io);
            codec2.toBytes(extract2.apply(packet), handler, io);
            codec3.toBytes(extract3.apply(packet), handler, io);
            codec4.toBytes(extract4.apply(packet), handler, io);
            codec5.toBytes(extract5.apply(packet), handler, io);
            codec6.toBytes(extract6.apply(packet), handler, io);
        });
    }

    static <A, B, C, D, E, F, G, T> PacketCodec<T> packed(PacketCodec<A> codec1,
                                                          Function<T, A> extract1,
                                                          PacketCodec<B> codec2,
                                                          Function<T, B> extract2,
                                                          PacketCodec<C> codec3,
                                                          Function<T, C> extract3,
                                                          PacketCodec<D> codec4,
                                                          Function<T, D> extract4,
                                                          PacketCodec<E> codec5,
                                                          Function<T, E> extract5,
                                                          PacketCodec<F> codec6,
                                                          Function<T, F> extract6,
                                                          PacketCodec<G> codec7,
                                                          Function<T, G> extract7,
                                                          Function7<A, B, C, D, E, F, G, T> pack) {
        return of((handler, io) -> pack.invoke(codec1.fromBytes(handler, io), codec2.fromBytes(handler, io), codec3.fromBytes(handler, io), codec4.fromBytes(handler, io), codec5.fromBytes(handler, io), codec6.fromBytes(handler, io), codec7.fromBytes(handler, io)), (packet, handler, io) -> {
            codec1.toBytes(extract1.apply(packet), handler, io);
            codec2.toBytes(extract2.apply(packet), handler, io);
            codec3.toBytes(extract3.apply(packet), handler, io);
            codec4.toBytes(extract4.apply(packet), handler, io);
            codec5.toBytes(extract5.apply(packet), handler, io);
            codec6.toBytes(extract6.apply(packet), handler, io);
            codec7.toBytes(extract7.apply(packet), handler, io);
        });
    }

    static <A, B, C, D, E, F, G, H, T> PacketCodec<T> packed(PacketCodec<A> codec1,
                                                             Function<T, A> extract1,
                                                             PacketCodec<B> codec2,
                                                             Function<T, B> extract2,
                                                             PacketCodec<C> codec3,
                                                             Function<T, C> extract3,
                                                             PacketCodec<D> codec4,
                                                             Function<T, D> extract4,
                                                             PacketCodec<E> codec5,
                                                             Function<T, E> extract5,
                                                             PacketCodec<F> codec6,
                                                             Function<T, F> extract6,
                                                             PacketCodec<G> codec7,
                                                             Function<T, G> extract7,
                                                             PacketCodec<H> codec8,
                                                             Function<T, H> extract8,
                                                             Function8<A, B, C, D, E, F, G, H, T> pack) {
        return of((handler, io) -> pack.invoke(codec1.fromBytes(handler, io), codec2.fromBytes(handler, io), codec3.fromBytes(handler, io), codec4.fromBytes(handler, io), codec5.fromBytes(handler, io), codec6.fromBytes(handler, io), codec7.fromBytes(handler, io), codec8.fromBytes(handler, io)), (packet, handler, io) -> {
            codec1.toBytes(extract1.apply(packet), handler, io);
            codec2.toBytes(extract2.apply(packet), handler, io);
            codec3.toBytes(extract3.apply(packet), handler, io);
            codec4.toBytes(extract4.apply(packet), handler, io);
            codec5.toBytes(extract5.apply(packet), handler, io);
            codec6.toBytes(extract6.apply(packet), handler, io);
            codec7.toBytes(extract7.apply(packet), handler, io);
            codec8.toBytes(extract8.apply(packet), handler, io);
        });
    }

    static <A, B, C, D, E, F, G, H, I, T> PacketCodec<T> packed(PacketCodec<A> codec1,
                                                                Function<T, A> extract1,
                                                                PacketCodec<B> codec2,
                                                                Function<T, B> extract2,
                                                                PacketCodec<C> codec3,
                                                                Function<T, C> extract3,
                                                                PacketCodec<D> codec4,
                                                                Function<T, D> extract4,
                                                                PacketCodec<E> codec5,
                                                                Function<T, E> extract5,
                                                                PacketCodec<F> codec6,
                                                                Function<T, F> extract6,
                                                                PacketCodec<G> codec7,
                                                                Function<T, G> extract7,
                                                                PacketCodec<H> codec8,
                                                                Function<T, H> extract8,
                                                                PacketCodec<I> codec9,
                                                                Function<T, I> extract9,
                                                                Function9<A, B, C, D, E, F, G, H, I, T> pack) {
        return of((handler, io) -> pack.invoke(codec1.fromBytes(handler, io), codec2.fromBytes(handler, io), codec3.fromBytes(handler, io), codec4.fromBytes(handler, io), codec5.fromBytes(handler, io), codec6.fromBytes(handler, io), codec7.fromBytes(handler, io), codec8.fromBytes(handler, io), codec9.fromBytes(handler, io)), (packet, handler, io) -> {
            codec1.toBytes(extract1.apply(packet), handler, io);
            codec2.toBytes(extract2.apply(packet), handler, io);
            codec3.toBytes(extract3.apply(packet), handler, io);
            codec4.toBytes(extract4.apply(packet), handler, io);
            codec5.toBytes(extract5.apply(packet), handler, io);
            codec6.toBytes(extract6.apply(packet), handler, io);
            codec7.toBytes(extract7.apply(packet), handler, io);
            codec8.toBytes(extract8.apply(packet), handler, io);
            codec9.toBytes(extract9.apply(packet), handler, io);
        });
    }

    static <A, B, C, D, E, F, G, H, I, J, T> PacketCodec<T> packed(PacketCodec<A> codec1,
                                                                   Function<T, A> extract1,
                                                                   PacketCodec<B> codec2,
                                                                   Function<T, B> extract2,
                                                                   PacketCodec<C> codec3,
                                                                   Function<T, C> extract3,
                                                                   PacketCodec<D> codec4,
                                                                   Function<T, D> extract4,
                                                                   PacketCodec<E> codec5,
                                                                   Function<T, E> extract5,
                                                                   PacketCodec<F> codec6,
                                                                   Function<T, F> extract6,
                                                                   PacketCodec<G> codec7,
                                                                   Function<T, G> extract7,
                                                                   PacketCodec<H> codec8,
                                                                   Function<T, H> extract8,
                                                                   PacketCodec<I> codec9,
                                                                   Function<T, I> extract9,
                                                                   PacketCodec<J> codec10,
                                                                   Function<T, J> extract10,
                                                                   Function10<A, B, C, D, E, F, G, H, I, J, T> pack) {
        return of((handler, io) -> pack.invoke(codec1.fromBytes(handler, io), codec2.fromBytes(handler, io), codec3.fromBytes(handler, io), codec4.fromBytes(handler, io), codec5.fromBytes(handler, io), codec6.fromBytes(handler, io), codec7.fromBytes(handler, io), codec8.fromBytes(handler, io), codec9.fromBytes(handler, io), codec10.fromBytes(handler, io)), (packet, handler, io) -> {
            codec1.toBytes(extract1.apply(packet), handler, io);
            codec2.toBytes(extract2.apply(packet), handler, io);
            codec3.toBytes(extract3.apply(packet), handler, io);
            codec4.toBytes(extract4.apply(packet), handler, io);
            codec5.toBytes(extract5.apply(packet), handler, io);
            codec6.toBytes(extract6.apply(packet), handler, io);
            codec7.toBytes(extract7.apply(packet), handler, io);
            codec8.toBytes(extract8.apply(packet), handler, io);
            codec9.toBytes(extract9.apply(packet), handler, io);
            codec10.toBytes(extract10.apply(packet), handler, io);
        });
    }

    static <T> PacketCodec<T> of(PacketReader<T> reader, PacketWriter<T> writer) {
        return new PacketCodec<>() {
            @Override
            public T fromBytes(PacketHandler handler, PacketIO io) {
                return reader.fromBytes(handler, io);
            }

            @Override
            public void toBytes(T packet, PacketHandler handler, PacketIO io) {
                writer.toBytes(packet, handler, io);
            }
        };
    }

    static <T> PacketCodec<T> of(Function<PacketIO, T> reader, BiConsumer<PacketIO, T> writer) {
        return new PacketCodec<>() {
            @Override
            public T fromBytes(PacketHandler handler, PacketIO io) {
                return reader.apply(io);
            }

            @Override
            public void toBytes(T packet, PacketHandler handler, PacketIO io) {
                writer.accept(io, packet);
            }
        };
    }

    static <T> PacketCodec<T> unit(Supplier<T> supplier) {
        return new PacketCodec<>() {
            @Override
            public T fromBytes(PacketHandler handler, PacketIO io) {
                return supplier.get();
            }

            @Override
            public void toBytes(T packet, PacketHandler handler, PacketIO io) {

            }
        };
    }

    @SafeVarargs
    static <T extends MapType> PacketCodec<T> ubo(T... typeGetter) {
        return of(io -> io.readUbo(typeGetter), PacketIO::writeUbo);
    }

    static <T> PacketCodec<T> registry(RegistryKey<Registry<T>> key) {
        return of(io -> io.read(key), (io, value) -> io.write(value, key));
    }

    static <T> PacketCodec<RegistryKey<T>> key(RegistryKey<Registry<T>> registryKey) {
        return of((_, io) -> new RegistryKey<>(registryKey, io.readId()), (packet, _, io) -> io.writeId(packet.id()));
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    static <T> PacketCodec<T[]> objects(PacketCodec<T> codec, T... typeGetter) {
        return objects(codec, (Class<T>) typeGetter.getClass().getComponentType());
    }

    static <T extends Enum<T>> PacketCodec<T> enumClass(Class<T> enumClass) {
        return PacketCodec.of(io -> io.readEnum(enumClass), PacketIO::writeEnum);
    }

    default <R> PacketCodec<R> map(Function<T, R> to, Function<R, T> from) {
        return of((handler, io) -> to.apply(fromBytes(handler, io)), (value, handler, io) -> toBytes(from.apply(value), handler, io));
    }

    default <R> PacketCodec<R> map(BiFunction<PacketHandler, T, R> to, BiFunction<R, PacketHandler, T> from) {
        return of((handler, io) -> to.apply(handler, fromBytes(handler, io)), (value, handler, io) -> toBytes(from.apply(value, handler), handler, io));
    }

    default <R> PacketCodec<R> map(Function<T, R> to, BiFunction<R, PacketHandler, T> from) {
        return of((handler, io) -> to.apply(fromBytes(handler, io)), (value, handler, io) -> toBytes(from.apply(value, handler), handler, io));
    }

    default <R> PacketCodec<R> map(BiFunction<PacketHandler, T, R> to, Function<R, T> from) {
        return of((handler, io) -> to.apply(handler, fromBytes(handler, io)), (value, handler, io) -> toBytes(from.apply(value), handler, io));
    }

    default PacketCodec<T[]> array(Class<T> type) {
        return objects(this, type);
    }

    default PacketCodec<T> nullable() {
        return of((handler, io) -> {
            if (io.readBoolean()) {
                return fromBytes(handler, io);
            }
            return null;
        }, (value, handler, io) -> {
            if (value == null) {
                io.writeBoolean(false);
                return;
            }
            io.writeBoolean(true);
            toBytes(value, handler, io);
        });
    }
}
