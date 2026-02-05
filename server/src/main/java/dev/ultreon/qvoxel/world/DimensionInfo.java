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

package dev.ultreon.qvoxel.world;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.registry.RegistryKey;
import dev.ultreon.qvoxel.registry.RegistryKeys;
import dev.ultreon.qvoxel.world.gen.chunk.ChunkGenerator;

import java.util.Objects;
import java.util.Optional;

/**
 * This class represents information about a dimension in a game world.
 * It includes the dimension's unique identifier, an optional seed value,
 * and a key for the chunk generator.
 *
 */
public final class DimensionInfo {
    public static final RegistryKey<DimensionInfo> OVERWORLD = RegistryKey.of(RegistryKeys.DIMENSION, CommonConstants.id("overworld"));
    public static final RegistryKey<DimensionInfo> TEST = RegistryKey.of(RegistryKeys.DIMENSION, CommonConstants.id("test"));
    public static final RegistryKey<DimensionInfo> SPACE = RegistryKey.of(RegistryKeys.DIMENSION, CommonConstants.id("space"));

    public Identifier id() {
        return id;
    }

    public Optional<Long> seed() {
        return seed;
    }

    public RegistryKey<ChunkGenerator> generatorKey() {
        return generatorKey;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        var that = (DimensionInfo) obj;
        return Objects.equals(id, that.id) &&
                Objects.equals(seed, that.seed) &&
                Objects.equals(generatorKey, that.generatorKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, seed, generatorKey);
    }

    @Override
    public String toString() {
        return "DimensionInfo[" +
                "id=" + id + ", " +
                "seed=" + seed + ", " +
                "generatorKey=" + generatorKey + ']';
    }


    public static DimensionInfo fromJson(JsonObject json) {
        Identifier id = CommonConstants.id(json.get("id").getAsString());
        Optional<Long> seed = json.has("seed") ? Optional.of(json.get("seed").getAsLong()) : Optional.empty();
        RegistryKey<ChunkGenerator> generatorKey = RegistryKey.of(RegistryKeys.CHUNK_GENERATOR, Identifier.parse(json.get("generator").getAsString()));
        return new DimensionInfo(id, seed, generatorKey);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.add("id", new JsonPrimitive(id.toString()));
        seed.ifPresent(s -> json.add("seed", new JsonPrimitive(s)));
        json.add("generator", new JsonPrimitive(generatorKey.id().toString()));
        return json;
    }

    private final Identifier id;
    private final Optional<Long> seed;
    private final RegistryKey<ChunkGenerator> generatorKey;

    /**
     * @param id           The unique namespace ID of the dimension.
     * @param seed         An optional seed value for the dimension.
     * @param generatorKey The key for the chunk generator associated with the dimension.
     */
    public DimensionInfo(Identifier id, Optional<Long> seed, RegistryKey<ChunkGenerator> generatorKey) {
        this.id = id;
        this.seed = seed;
        this.generatorKey = generatorKey;
    }
}
