/*
 * Copyright 2025. Quinten 'Qubix' Jungblut
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.ultreon.qvoxel.world.light;

import dev.ultreon.qvoxel.featureflags.FeatureSet;
import dev.ultreon.qvoxel.featureflags.Features;
import dev.ultreon.qvoxel.network.NetworkSerializable;
import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.registry.RegistryHandle;
import dev.ultreon.qvoxel.resource.GameComponent;
import dev.ultreon.qvoxel.util.BlockVec;
import dev.ultreon.qvoxel.world.Chunk;

import java.util.Arrays;

/**
 * The LightMap class represents a data structure for storing and manipulating
 * light information in a 3D or chunk-based space. Each element in the internal
 * data array encodes color and skylight levels in 32-bit integers, where each
 * color channel (red, green, blue) and the skylight value are represented by 8 bits.
 * <p>
 * This class provides various methods for extracting and modifying the light
 * components at specific 3D coordinates, or by direct index into the internal
 * data array. Additionally, it supports serialization and deserialization to
 * and from a network-compatible PacketIO buffer.
 * <p>
 * The LightMap class implements Cloneable to allow creation of deep copies and
 * NetworkSerializable to support network-based communication of its data.
 */
public class LightMap implements Cloneable, NetworkSerializable, GameComponent {
    public static final PacketCodec<LightMap> PACKET_CODEC = PacketCodec.INTS.map(LightMap::new, LightMap::getData);

    private final FeatureSet features;

    private int[] data;
    public LightMap(FeatureSet features, int size) {
        this.features = features;
        data = new int[size];
        if (!features.isFeatureEnabled(Features.IMPROVED_LIGHTING_SYSTEM))
            Arrays.fill(data, 15);
    }

    public LightMap(FeatureSet features, int[] data) {
        this.features = features;
        this.data = data;
        if (!features.isFeatureEnabled(Features.IMPROVED_LIGHTING_SYSTEM))
            Arrays.fill(data, 15);
    }

    /**
     * Constructs a LightMap object by initializing its internal data array
     * with the specified length and populating it with values read from the
     * provided PacketIO buffer.
     *
     * @param length the length of the internal data array
     * @param buffer the PacketIO instance used to read the integer data for the array
     */
    public LightMap(FeatureSet features, int length, PacketIO buffer) {
        this.features = features;
        data = new int[length];
        for (int i = 0; i < length; i++) {
            if (!features.isFeatureEnabled(Features.IMPROVED_LIGHTING_SYSTEM))
                data[i] = 15;
            else
                data[i] = buffer.readInt();
        }
    }

    public LightMap(RegistryHandle handle, int[] readData) {
        features = handle.getFeatures();
        data = readData;
    }

    /**
     * Reads a LightMap object from the provided PacketIO buffer.
     * The method first reads the length of the data array as a variable-length integer
     * and then reads individual integer values to populate the LightMap's data array.
     *
     * @param buffer the PacketIO instance from which the LightMap data is read
     * @return a new LightMap object created from the data read from the buffer
     */
    public static LightMap read(PacketIO buffer) {
        int length = buffer.readVarInt();
        int[] data = new int[length];
        for (int i = 0; i < length; i++) {
            data[i] = buffer.readInt();
        }
        return new LightMap(buffer.getFeatures(), data);
    }

    public int[] getData() {
        return data;
    }

    // --- Light extraction ---
    public int getRed(int x, int y, int z) {
        return data[Chunk.getIndex(x, y, z)] >> 24 & 0xFF;
    }

    public int getGreen(int x, int y, int z) {
        return data[Chunk.getIndex(x, y, z)] >> 16 & 0xFF;
    }

    public int getBlue(int x, int y, int z) {
        return data[Chunk.getIndex(x, y, z)] >> 8 & 0xFF;
    }

    public int getSky(int x, int y, int z) {
        if (features.isFeatureEnabled(Features.IMPROVED_LIGHTING_SYSTEM))
            return data[Chunk.getIndex(x, y, z)] & 0xFF;
        else
            return 15;
    }

    // --- Setters ---
    public void setRed(int x, int y, int z, int value) {
        int idx = Chunk.getIndex(x, y, z);
        data[idx] = data[idx] & 0x00FFFFFF | (value & 0xFF) << 24;
    }

    public void setGreen(int x, int y, int z, int value) {
        int idx = Chunk.getIndex(x, y, z);
        data[idx] = data[idx] & 0xFF00FFFF | (value & 0xFF) << 16;
    }

    public void setBlue(int x, int y, int z, int value) {
        int idx = Chunk.getIndex(x, y, z);
        data[idx] = data[idx] & 0xFFFF00FF | (value & 0xFF) << 8;
    }

    public void setSky(int x, int y, int z, int value) {
        int idx = Chunk.getIndex(x, y, z);
        if (features.isFeatureEnabled(Features.IMPROVED_LIGHTING_SYSTEM))
            data[idx] = data[idx] & 0xFFFFFF00 | value & 0xFF;
        else
            data[idx] = data[idx] & 0xFFFFFF00 | 15 & 0xFF;
    }

    // --- Direct index versions ---
    public int getRed(int idx) {
        return data[idx] >> 24 & 0xFF;
    }

    public int getGreen(int idx) {
        return data[idx] >> 16 & 0xFF;
    }

    public int getBlue(int idx) {
        return data[idx] >> 8 & 0xFF;
    }

    public int getSky(int idx) {
        if (features.isFeatureEnabled(Features.IMPROVED_LIGHTING_SYSTEM))
            return data[idx] & 0xFF;
        else
            return 15;
    }

    public void setRed(int idx, int value) {
        data[idx] = data[idx] & 0x00FFFFFF | (value & 0xFF) << 24;
    }

    public void setGreen(int idx, int value) {
        data[idx] = data[idx] & 0xFF00FFFF | (value & 0xFF) << 16;
    }

    public void setBlue(int idx, int value) {
        data[idx] = data[idx] & 0xFFFF00FF | (value & 0xFF) << 8;
    }

    public void setSky(int idx, int value) {
        if (features.isFeatureEnabled(Features.IMPROVED_LIGHTING_SYSTEM))
            data[idx] = data[idx] & 0xFFFFFF00 | value & 0xFF;
        else
            data[idx] = data[idx] & 0xFFFFFF00 | 15 & 0xFF;
    }

    /**
     * Retrieves the RGBS value at the specified index in the internal data array.
     *
     * @param index the index of the value to retrieve from the internal data array
     * @return the RGBS value at the specified index in the internal data array
     * @throws IndexOutOfBoundsException if the specified index is out of the array bounds
     */
    public int get(int index) {
        return data[index];
    }

    /**
     * Sets the RGBS value at the specified index in the internal data array.
     * RGBS is a 32-bit integer containing the red, green, blue and sky values.
     *
     * @param index the index in the data array where the value will be set
     * @param rgbs  the RGBS value to set at the specified index
     */
    public void set(int index, int rgbs) {
        if (features.isFeatureEnabled(Features.IMPROVED_LIGHTING_SYSTEM))
            rgbs |= 0x000000FF;
        data[index] = rgbs;
    }

    /**
     * Clears all data in the internal array by setting every element to zero.
     * This effectively resets the state of the LightMap to an empty or default condition.
     */
    public void clear() {
        Arrays.fill(data, features.isFeatureEnabled(Features.IMPROVED_LIGHTING_SYSTEM) ? 0x00000FF : 0);
    }

    /**
     * Creates and returns a copy of this LightMap object. The clone includes a
     * copy of the internal data array, ensuring the original and cloned
     * objects maintain separate states.
     *
     * @return a new LightMap object that is a clone of this instance
     * @throws RuntimeException if the cloning process fails
     */
    @Override
    public LightMap clone() {
        try {
            LightMap clone = (LightMap) super.clone();
            clone.data = data.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the RGBS value at the specified coordinates in the internal data array.
     *
     * @param x the x-coordinate of the position
     * @param y the y-coordinate of the position
     * @param z the z-coordinate of the position
     * @return the RGBS value at the specified coordinates
     * @throws IndexOutOfBoundsException if the specified coordinates are out of the allowable range
     */
    public int getLight(int x, int y, int z) {
        if (features.isFeatureEnabled(Features.IMPROVED_LIGHTING_SYSTEM))
            return data[Chunk.getIndex(x, y, z)];
        else
            return data[Chunk.getIndex(x, y, z)] | 0xFF;
    }

    /**
     * Writes the internal data of the LightMap to the provided PacketIO instance.
     * The data is written as a variable-length integer followed by the individual
     * integer values from the internal data array.
     *
     * @param packetIO the PacketIO instance used to write the data from the LightMap
     */
    @Override
    public void write(PacketIO packetIO) {
        packetIO.writeVarInt(data.length);
        for (int value : data) {
            packetIO.writeInt(value);
        }
    }

    /**
     * Retrieves the red light component at the specified position.
     *
     * @param pos the position represented as a BlockVec object
     * @return the red light value at the given position
     */
    public int getRed(BlockVec pos) {
        return getRed(pos.x, pos.y, pos.z);
    }

    /**
     * Retrieves the green light component at the specified position.
     *
     * @param pos the position represented as a BlockVec object
     * @return the green light value at the given position
     */
    public int getGreen(BlockVec pos) {
        return getGreen(pos.x, pos.y, pos.z);
    }

    /**
     * Retrieves the blue light component at the specified position.
     *
     * @param pos the position represented as a BlockVec object
     * @return the blue light value at the given position
     */
    public int getBlue(BlockVec pos) {
        return getBlue(pos.x, pos.y, pos.z);
    }

    /**
     * Retrieves the skylight component at the specified position.
     *
     * @param pos the position represented as a BlockVec object
     * @return the skylight value at the given position
     */
    public int getSky(BlockVec pos) {
        if (!features.isFeatureEnabled(Features.IMPROVED_LIGHTING_SYSTEM))
            return 15;

        return getSky(pos.x, pos.y, pos.z);
    }

    public void set(int x, int y, int z, int r, int g, int b) {
        setRed(x, y, z, r);
        setGreen(x, y, z, g);
        setBlue(x, y, z, b);
    }

    public void load(int[] data) {
        this.data = data;
    }
}
