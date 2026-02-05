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

package dev.ultreon.qvoxel.resource;

import com.google.gson.JsonElement;
import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.util.Result;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public interface Resource {
    default byte @Nullable [] readBytes() {
        try (InputStream inputStream = openStream()) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to load resource: {}", id(), e);
            return null;
        }
    }

    default @Nullable String readString() {
        byte[] bytes = readBytes();
        if (bytes == null) return null;
        return new String(bytes, StandardCharsets.UTF_8);
    }

    default Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = openStream()) {
            properties.load(inputStream);
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to load properties resource: {}", id(), e);
        }
        return properties;
    }

    default Reader openReader() throws IOException {
        return new InputStreamReader(openStream());
    }

    default <T> Result<T> loadJson(Class<T> jsonObjectClass) {
        try (Reader reader = openReader()) {
            return Result.ok(CommonConstants.GSON.fromJson(reader, jsonObjectClass));
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to load JSON resource: {}", id(), e);
            return Result.failure(e);
        }
    }

    default JsonElement loadJson() {
        try (Reader reader = openReader()) {
            return CommonConstants.GSON.fromJson(reader, JsonElement.class);
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to load JSON resource: {}", id(), e);
            return null;
        }
    }

    InputStream openStream() throws IOException;

    Identifier id();
}
