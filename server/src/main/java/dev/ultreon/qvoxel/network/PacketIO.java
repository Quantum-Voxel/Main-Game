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

package dev.ultreon.qvoxel.network;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.libs.commons.v0.tuple.Pair;
import dev.ultreon.libs.commons.v0.util.EnumUtils;
import dev.ultreon.qvoxel.block.state.BlockState;
import dev.ultreon.qvoxel.featureflags.FeatureSet;
import dev.ultreon.qvoxel.item.ItemStack;
import dev.ultreon.qvoxel.registry.*;
import dev.ultreon.qvoxel.sound.SoundEvent;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.util.ChunkVec;
import dev.ultreon.qvoxel.util.PaletteStorage;
import dev.ultreon.ubo.DataTypeRegistry;
import dev.ultreon.ubo.types.DataType;
import io.netty.buffer.ByteBuf;
import org.joml.*;
import org.joml.Math;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * The PacketIO class provides methods for reading and writing various data types to and from input and output streams.
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public final class PacketIO implements RegistryHandle {
    private static final int MAX_UBO_SIZE = 1024 * 1024 * 2;
    private final RegistryHandle handle;
    private final ByteBuf buf;

    /**
     * Initializes a new PacketIO instance using the specified socket.
     * The socket's input and output streams are used for reading and writing packets.
     *
     * @param buf the socket to be used for input and output streams.
     * @throws IOException if an I/O error occurs when creating the input or output streams.
     */
    public PacketIO(ByteBuf buf, RegistryHandle handle) throws IOException {
        this.buf = buf;
        this.handle = handle;
    }

    public String readString(int max) {
        if (max < 0)
            throw new IllegalArgumentException("Invalid max length: " + max + ". Must be greater than or equal to 0.");
        byte[] bytes = readByteArray(max);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public String readString() {
        int len = readShort();
        byte[] bytes = readByteArray();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public PacketIO writeString(String string, int max) {
        if (max < 0)
            throw new IllegalArgumentException("Invalid max length: " + max + ". Must be greater than or equal to 0.");
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > max) throw new PacketOverflowException("string", bytes.length, max);
        writeByteArray(string.getBytes(StandardCharsets.UTF_8));
        return this;
    }

    public void writeString(String string) {
        writeByteArray(string.getBytes(StandardCharsets.UTF_8));
    }

    public byte[] readByteArray(int max) {
        if (max < 0)
            throw new IllegalArgumentException("Invalid max length: " + max + ". Must be greater than or equal to 0.");
        int len;
        len = buf.readInt();
        if (len > max) throw new PacketOverflowException("byte array", len, max);
        byte[] bytes = new byte[len];
        buf.readBytes(bytes);
        return bytes;
    }

    public byte[] readByteArray() {
        int len;
        len = buf.readInt();
        byte[] bytes = new byte[len];
        buf.readBytes(bytes);
        return bytes;
    }

    public void writeByteArray(byte[] array, int max) {
        if (array.length > max) throw new PacketOverflowException("byte array", array.length, max);
        buf.writeInt(array.length);
        buf.writeBytes(array);
    }

    public void writeByteArray(byte[] array) {
        buf.writeInt(array.length);
        buf.writeBytes(array);
    }

    public Identifier readId() {
        var location = readString(100);
        var path = readString(200);
        return new Identifier(location, path);
    }

    public PacketIO writeId(Identifier id) {
        writeString(id.location(), 100);
        writeString(id.path(), 200);
        return this;
    }

    public byte readByte() {
        return buf.readByte();
    }

    public short readUnsignedByte() {
        return buf.readUnsignedByte();
    }

    public PacketIO writeByte(int value) {
        buf.writeByte(value);
        return this;
    }

    public PacketIO writeByte(byte value) {
        buf.writeByte(value);
        return this;
    }

    public short readShort() {
        return buf.readShort();
    }

    public int readUnsignedShort() {
        return buf.readUnsignedShort();
    }

    public PacketIO writeShort(int value) {
        buf.writeShort(value);
        return this;
    }

    public PacketIO writeShort(short value) {
        buf.writeShort(value);
        return this;
    }

    public int readInt() {
        return buf.readInt();
    }

    public PacketIO writeInt(int value) {
        buf.writeInt(value);
        return this;
    }

    public long readLong() {
        return buf.readLong();
    }

    public PacketIO writeLong(long value) {
        buf.writeLong(value);
        return this;
    }

    public float readFloat() {
        return buf.readFloat();
    }

    public PacketIO writeFloat(float value) {
        buf.writeFloat(value);
        return this;
    }

    public double readDouble() {
        return buf.readDouble();
    }

    public PacketIO writeDouble(double value) {
        buf.writeDouble(value);
        return this;
    }

    public char readChar() {
        return buf.readChar();
    }

    public PacketIO writeChar(char value) {
        buf.writeChar(value);
        return this;
    }

    public boolean readBoolean() {
        return buf.readBoolean();
    }

    public PacketIO writeBoolean(boolean value) {
        buf.writeBoolean(value);
        return this;
    }

    public UUID readUuid() {
        long mostSigBits = readLong();
        long leastSigBits = readLong();

        return new UUID(mostSigBits, leastSigBits);
    }

    public PacketIO writeUuid(UUID value) {
        return writeLong(value.getMostSignificantBits())
                .writeLong(value.getLeastSignificantBits());
    }

    public BitSet readBitSet() {
        return BitSet.valueOf(readLongArray());
    }

    public BitSet readBitSet(int maxBytes) {
        return BitSet.valueOf(readLongArray(maxBytes / 8));
    }

    public PacketIO writeBitSet(BitSet value) {
        return writeLongArray(value.toLongArray());
    }

    public Vector2f readVector2f(Vector2f vector) {
        return vector.set(readFloat(), readFloat());
    }

    public PacketIO writeVector2f(Vector2f vec) {
        return writeFloat(vec.x).writeFloat(vec.y);
    }

    public Vector3f readVector3f(Vector3f vector) {
        return vector.set(readFloat(), readFloat(), readFloat());
    }

    public PacketIO writeVector3f(Vector3f vec) {
        return writeFloat(vec.x).writeFloat(vec.y).writeFloat(vec.z);
    }

    public Vector4f readVector4f(Vector4f vector) {
        return vector.set(readFloat(), readFloat(), readFloat(), readFloat());
    }

    public PacketIO writeVector4f(Vector4f vec) {
        return writeFloat(vec.x).writeFloat(vec.y).writeFloat(vec.z).writeFloat(vec.w);
    }

    public PacketIO writeVector2d(Vector2d vec) {
        return writeDouble(vec.x).writeDouble(vec.y);
    }

    public Vector2d readVector2d(Vector2d vector) {
        return vector.set(readDouble(), readDouble());
    }

    public PacketIO writeVector2f(Vector2d vec) {
        return writeDouble(vec.x).writeDouble(vec.y);
    }

    public Vector3d readVector3d(Vector3d vector) {
        return vector.set(readDouble(), readDouble(), readDouble());
    }

    public PacketIO writeVector3d(Vector3d vec) {
        return writeDouble(vec.x).writeDouble(vec.y).writeDouble(vec.z);
    }

    public Vector4d readVector4d(Vector4d vector) {
        return vector.set(readDouble(), readDouble(), readDouble(), readDouble());
    }

    public PacketIO writeVector4d(Vector4d vec) {
        return writeDouble(vec.x).writeDouble(vec.y).writeDouble(vec.z).writeDouble(vec.w);
    }

    public Vector2i readVector2fi(Vector2i vector) {
        return vector.set(readInt(), readInt());
    }

    public PacketIO writeVector2fi(Vector2i vec) {
        return writeInt(vec.x).writeInt(vec.y);
    }

    public Vector3i readVector3fi(Vector3i vector) {
        return vector.set(readInt(), readInt(), readInt());
    }

    public PacketIO writeVector3fi(Vector3i vec) {
        return writeInt(vec.x).writeInt(vec.y).writeInt(vec.z);
    }

    public Vector4i readVector4fi() {
        int x = readInt();
        int y = readInt();
        int z = readInt();
        int w = readInt();

        return new Vector4i(x, y, z, w);
    }

    public PacketIO writeVector4fi(Vector4i vec) {
        return writeInt(vec.x).writeInt(vec.y).writeInt(vec.z).writeInt(vec.w);
    }

    public BlockVec readBlockVec() {
        int x = readInt();
        int y = readInt();
        int z = readInt();

        return new BlockVec(x, y, z);
    }

    public PacketIO writeBlockVec(BlockVec pos) {
        return writeInt(pos.x).writeInt(pos.y).writeInt(pos.z);
    }

    public ChunkVec readChunkVec() {
        int x = readInt();
        int y = readInt();
        int z = readInt();

        return new ChunkVec(x, y, z);
    }

    public PacketIO writeChunkVec(ChunkVec pos) {
        return writeInt(pos.x).writeInt(pos.y).writeInt(pos.z);
    }

    public int readVarInt() {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = readByte();
            int value = read & 0x7F;
            result |= value << 7 * numRead;

            numRead++;
            if (numRead > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while ((read & 0x80) != 0);

        return result;
    }

    public PacketIO writeVarInt(int value) {
        while ((value & 0xFFFFFF80) != 0L) {
            writeByte(value & 0x7F | 0x80);
            value >>>= 7;
        }
        return writeByte(value & 0x7F);
    }

    public void writeUbo(DataType<?> ubo) {
        writeByte(ubo.id());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (var output = new DataOutputStream(bos)) {
            ubo.write(output);
            bos.flush();
        } catch (IOException ignored) {
            try {
                bos.close();
            } catch (IOException e) {
                throw new PacketException(e);
            }
        }

        writeByteArray(bos.toByteArray(), PacketIO.MAX_UBO_SIZE);
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public final <T extends DataType<?>> T readUbo(T... typeGetter) {
        T data;
        int id = readUnsignedByte();
        byte[] bytes = readByteArray(PacketIO.MAX_UBO_SIZE);

        try (DataInputStream stream = new DataInputStream(new ByteArrayInputStream(bytes))) {
            Class<? extends DataType<?>> componentType = (Class<? extends DataType<?>>) typeGetter.getClass().getComponentType();
            if (id != DataTypeRegistry.getId(componentType))
                throw new PacketException("Id doesn't match requested type.");
            data = (T) DataTypeRegistry.read(DataTypeRegistry.getId(componentType), stream);
        } catch (IOException e) {
            throw new PacketException(e);
        }
        return data;
    }

    public ByteOrder order() {
        return ByteOrder.nativeOrder();
    }

    @Deprecated
    public PacketIO order(ByteOrder order) {
        return this;
    }

    public PacketIO unwrap() {
        return this;
    }

    public boolean isDirect() {
        return true;
    }

    public boolean isReadOnly() {
        return buf.isReadOnly();
    }

    public boolean isReadable() {
        return buf.isReadable();
    }

    public boolean isReadable(int size) {
        return buf.isReadable();
    }

    public boolean isWritable() {
        return buf.isWritable();
    }

    public boolean isWritable(int size) {
        return buf.isWritable(size);
    }

    public PacketIO clear() {
        throw new UnsupportedOperationException();
    }

    public int readMedium() {
        return buf.readMedium();
    }

    public ByteBuf readBytes(int length) {
        return buf.readBytes(length);
    }

    public PacketIO readBytes(byte[] dst) {
        buf.readBytes(dst);
        return this;
    }

    public PacketIO readBytes(byte[] dst, int dstIndex, int length) {
        buf.readBytes(dst, dstIndex, length);
        return this;
    }

    public PacketIO readBytes(OutputStream out, int length) throws IOException {
        buf.readBytes(out, length);
        return this;
    }

    public CharSequence readCharSequence(int length, Charset charset) {
        byte[] bytes = readByteArray(length * (int) Math.ceil(charset.newEncoder().maxBytesPerChar()));

        return new String(bytes, charset);
    }

    public int readBytes(FileChannel out, long position, int length) throws IOException {
        return buf.readBytes(out, position, length);
    }

    public PacketIO writeMedium(int value) {
        byte bits16 = (byte) (value >> 16 & 0xFF);
        byte bits8 = (byte) (value >> 8 & 0xFF);
        byte bits = (byte) (value & 0xFF);

        buf.writeByte(bits16);
        buf.writeByte(bits8);
        buf.writeByte(bits);

        return this;
    }

    public PacketIO writeChar(int value) {
        buf.writeChar(value);
        return this;
    }

    public PacketIO writeBytes(byte[] src) {
        buf.writeBytes(src);
        return this;
    }

    public PacketIO writeBytes(byte[] src, int srcIndex, int length) {
        buf.writeBytes(src, srcIndex, length);
        return this;
    }

    public void writeBytes(InputStream in, int length) {
        try {
            buf.writeBytes(in, length);
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public void writeBytes(ScatteringByteChannel in, int length) {
        try {
            buf.writeBytes(in, length);
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    public int writeBytes(FileChannel in, long position, int length) {
        try {
            return buf.writeBytes(in, position, length);
        } catch (IOException e) {
            throw new PacketException(e);
        }
    }

    @Deprecated
    public boolean hasArray() {
        return buf.hasArray();
    }

    @Deprecated
    public byte[] array() {
        throw new UnsupportedOperationException();
    }

    public int arrayOffset() {
        return buf.arrayOffset();
    }

    public boolean hasMemoryAddress() {
        return false;
    }

    public long memoryAddress() {
        return 0L;
    }

    public String toString(Charset charset) {
        return "PacketIO";
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        return equals(obj);
    }

    public String toString() {
        return toString(StandardCharsets.UTF_8);
    }

    public PacketIO asPacketBuffer() {
        return this;
    }

    public short[] readShortArray() {
        int len = readVarInt();
        short[] array = new short[len];

        for (int i = 0; i < len; i++) {
            array[i] = readShort();
        }

        return array;
    }

    public short[] readShortArray(int max) {
        int len = readVarInt();
        if (len > max) {
            throw new PacketOverflowException("Array too large", max, len);
        }

        short[] array = new short[len];

        for (int i = 0; i < len; i++) {
            array[i] = readShort();
        }

        return array;
    }

    public PacketIO writeShortArray(short[] array) {
        writeVarInt(array.length);
        for (short s : array) {
            writeShort(s);
        }

        return this;
    }

    public int[] readMediumArray() {
        int len = readVarInt();
        int[] array = new int[len];

        for (int i = 0; i < len; i++) {
            array[i] = readMedium();
        }

        return array;
    }

    public int[] readMediumArray(int max) {
        int len = readVarInt();
        if (len > max) {
            throw new PacketOverflowException("Array too large", max, len);
        }

        int[] array = new int[len];

        for (int i = 0; i < len; i++) {
            array[i] = readMedium();
        }

        return array;
    }

    public PacketIO writeMediumArray(int[] array) {
        writeVarInt(array.length);
        for (int i : array) {
            writeMedium(i);
        }

        return this;
    }

    public int[] readIntArray() {
        int len = readVarInt();
        int[] array = new int[len];

        for (int i = 0; i < len; i++) {
            array[i] = readInt();
        }

        return array;
    }

    public int[] readIntArray(int max) {
        int len = readVarInt();
        if (len > max) {
            throw new PacketOverflowException("Array too large", max, len);
        }

        int[] array = new int[len];

        for (int i = 0; i < len; i++) {
            array[i] = readInt();
        }

        return array;
    }

    public PacketIO writeIntArray(int[] array) {
        writeVarInt(array.length);
        for (int i : array) {
            writeInt(i);
        }

        return this;
    }

    public long[] readLongArray() {
        int len = readVarInt();
        long[] array = new long[len];

        for (int i = 0; i < len; i++) {
            array[i] = readLong();
        }

        return array;
    }

    public long[] readLongArray(int max) {
        int len = readVarInt();
        if (len > max) {
            throw new PacketOverflowException("Array too large", max, len);
        }

        long[] array = new long[len];

        for (int i = 0; i < len; i++) {
            array[i] = readLong();
        }

        return array;
    }

    public PacketIO writeLongArray(long[] array) {
        writeVarInt(array.length);
        for (long l : array) {
            writeLong(l);
        }

        return this;
    }

    public float[] readFloatArray() {
        int len = readVarInt();
        float[] array = new float[len];

        for (int i = 0; i < len; i++) {
            array[i] = readFloat();
        }

        return array;
    }

    public float[] readFloatArray(int max) {
        int len = readVarInt();
        if (len > max) {
            throw new PacketOverflowException("Array too large", max, len);
        }

        float[] array = new float[len];

        for (int i = 0; i < len; i++) {
            array[i] = readFloat();
        }

        return array;
    }

    public PacketIO writeFloatArray(float[] array) {
        writeVarInt(array.length);
        for (float f : array) {
            writeFloat(f);
        }

        return this;
    }

    public double[] readDoubleArray() {
        int len = readVarInt();
        double[] array = new double[len];

        for (int i = 0; i < len; i++) {
            array[i] = readDouble();
        }

        return array;
    }

    public double[] readDoubleArray(int max) {
        int len = readVarInt();
        if (len > max) {
            throw new PacketOverflowException("Array too large", max, len);
        }

        double[] array = new double[len];

        for (int i = 0; i < len; i++) {
            array[i] = readDouble();
        }

        return array;
    }

    public PacketIO writeDoubleArray(double[] array) {
        writeVarInt(array.length);
        for (double d : array) {
            writeDouble(d);
        }

        return this;
    }

    public <T> List<T> readList(Function<PacketIO, T> decoder) {
        int size = readInt();
        var list = new ArrayList<T>();

        for (int i = 0; i < size; i++) {
            list.add(decoder.apply(this));
        }

        return list;
    }

    public <T> List<T> readList(Function<PacketIO, T> decoder, int max) {
        int size = readInt();
        if (size > max) {
            throw new PacketException(String.format("List too large, max = %d, actual = %d", max, size));
        }

        var list = new ArrayList<T>();

        for (int i = 0; i < size; i++) {
            list.add(decoder.apply(this));
        }

        return list;
    }

    public <T> PacketIO writeList(List<T> list, BiConsumer<PacketIO, T> encoder) {
        writeInt(list.size());
        for (T item : list) {
            encoder.accept(this, item);
        }

        return this;
    }

    public <K, V> Map<K, V> readMap(Function<PacketIO, K> keyDecoder, Function<PacketIO, V> valueDecoder) {
        int size = readMedium();
        var map = new HashMap<K, V>();

        for (int i = 0; i < size; i++) {
            map.put(keyDecoder.apply(this), valueDecoder.apply(this));
        }

        return map;
    }

    public <K, V> Map<K, V> readMap(Function<PacketIO, K> keyDecoder, Function<PacketIO, V> valueDecoder, int max) {
        int size = readMedium();
        if (size > max) {
            throw new PacketException(String.format("Map too large, max = %d, actual = %d", max, size));
        }

        var map = new HashMap<K, V>();

        for (int i = 0; i < size; i++) {
            map.put(keyDecoder.apply(this), valueDecoder.apply(this));
        }

        return map;
    }

    public <K, V> PacketIO writeMap(Map<K, V> map, BiConsumer<PacketIO, K> keyEncoder, BiConsumer<PacketIO, V> valueEncoder) {
        writeMedium(map.size());
        for (Map.Entry<K, V> entry : map.entrySet()) {
            keyEncoder.accept(this, entry.getKey());
            valueEncoder.accept(this, entry.getValue());
        }

        return this;
    }

    public <F, S> Pair<F, S> readPair(Function<PacketIO, F> firstDecoder, Function<PacketIO, S> secondDecoder) {
        return new Pair<>(firstDecoder.apply(this), secondDecoder.apply(this));
    }

    public <F, S> PacketIO writePair(Pair<F, S> pair, BiConsumer<PacketIO, F> firstEncoder, BiConsumer<PacketIO, S> secondEncoder) {
        firstEncoder.accept(this, pair.getFirst());
        secondEncoder.accept(this, pair.getSecond());
        return this;
    }

    public <T extends Enum<T>> T readEnum(T fallback) {
        return EnumUtils.byOrdinal(readByte(), fallback);
    }

    public <T extends Enum<T>> T readEnum(Class<T> type) {
        T[] enumConstants = type.getEnumConstants();
        byte b = readByte();
        if (b >= enumConstants.length) {
            throw new PacketException("Invalid enum ordinal: " + b + " for " + type);
        }
        return enumConstants[b];
    }

    public PacketIO writeEnum(Enum<?> value) {
        return writeByte(value.ordinal());
    }

    public BlockState readBlockState() {
        return BlockState.read(this);
    }

    public void writeBlockState(BlockState blockMeta) {
        blockMeta.write(this);
    }

    public int readableBytes() {
        return buf.readableBytes();
    }

    public <K, V, T extends Map<K, V>> T readObjectMap(IntFunction<T> factory, Function<PacketIO, K> keyDecoder, Function<PacketIO, V> valueDecoder) {
        int size = readMedium();
        var map = factory.apply(size);

        for (int i = 0; i < size; i++) {
            map.put(keyDecoder.apply(this), valueDecoder.apply(this));
        }

        return map;
    }

    public <K, V> void writeObjectMap(Map<K, V> map, BiConsumer<PacketIO, K> keyEncoder, BiConsumer<PacketIO, V> valueEncoder) {
        writeMedium(map.size());
        for (Map.Entry<K, V> entry : map.entrySet()) {
            keyEncoder.accept(this, entry.getKey());
            valueEncoder.accept(this, entry.getValue());
        }
    }

    @Override
    public <T> IdRegistry<T> get(RegistryKey<? extends Registry<T>> key) {
        return handle.get(key);
    }

    public PacketIO write(NetworkSerializable serializable) {
        serializable.write(this);
        return this;
    }

    public <T> PacketIO writePaletteStorage(PaletteStorage<T> states, BiConsumer<PacketIO, T> statesEncoder) {
        states.write(this, statesEncoder);
        return this;
    }

    public short[] readShorts(int staticSize) {
        short[] shorts = new short[staticSize];
        for (int i = 0; i < shorts.length; i++) {
            shorts[i] = readShort();
        }
        return shorts;
    }

    @SuppressWarnings("unchecked")
    public <T> T[] readArray(Class<T> cls, Function<PacketIO, T> decoder) {
        int size = readVarInt();
        T[] array = (T[]) Array.newInstance(cls, size);
        for (int i = 0; i < size; i++) {
            array[i] = decoder.apply(this);
        }
        return array;
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public final <T> T[] readArray(Function<PacketIO, T> decoder, T... typeGetter) {
        int size = readVarInt();
        Class<?> cls = typeGetter.getClass().getComponentType();
        T[] array = (T[]) Array.newInstance(cls, size);
        for (int i = 0; i < size; i++) {
            array[i] = decoder.apply(this);
        }
        return array;
    }

    public PacketIO writeShorts(short[] statePalette) {
        for (short s : statePalette) {
            writeShort(s);
        }
        return this;
    }

    public <T> PacketIO writeArray(T[] states, BiConsumer<T, PacketIO> statesEncoder) {
        writeVarInt(states.length);
        for (T state : states) {
            statesEncoder.accept(state, this);
        }
        return this;
    }

    public void writeItemStack(ItemStack itemStack) {
        itemStack.write(this);
    }

    public ItemStack readItemStack() {
        return ItemStack.read(this);
    }

    public PacketIO writeSoundEvent(SoundEvent soundEvent) {
        writeInt(get(RegistryKeys.SOUND_EVENT).getRawId(soundEvent));
        return this;
    }

    public SoundEvent readSoundEvent() {
        return get(RegistryKeys.SOUND_EVENT).byRawId(readInt());
    }

    public <T> PacketIO write(T particleEventType, RegistryKey<Registry<T>> particleType) {
        writeVarInt(get(particleType).getRawId(particleEventType));
        return this;
    }

    public <T> T read(RegistryKey<Registry<T>> particleType) {
        return get(particleType).byRawId(readVarInt());
    }

    public FeatureSet getFeatures() {
        return handle.getFeatures();
    }

    private static class NullInputStream extends InputStream {

        @Override
        public int read() {
            return 0;
        }
    }

    private static class NullOutputStream extends OutputStream {
        @Override
        public void write(int b) {

        }
    }
}
