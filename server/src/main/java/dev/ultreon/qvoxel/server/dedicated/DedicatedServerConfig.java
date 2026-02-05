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

package dev.ultreon.qvoxel.server.dedicated;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.ultreon.qvoxel.CommonConstants;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class DedicatedServerConfig {
    public static final Path PATH = Paths.get("server_config.json");
    private static final int VERSION = 0;
    public String hostname;
    public int port;
    public String path;
    public long seed;
    public String levelName;
    public boolean allowCommands;
    public boolean allowCheats;

    private DedicatedServerConfig() {
        hostname = "localhost";
        port = 38800;
        path = null;
        seed = new Random().nextLong();
        levelName = "world";
        allowCommands = true;
        allowCheats = true;
    }

    private DedicatedServerConfig(String hostname, int port, String path, long seed, String levelName, boolean allowCommands, boolean allowCheats) {
        this.hostname = hostname;
        this.port = port;
        this.path = path;
        this.seed = seed;
        this.levelName = levelName;
        this.allowCommands = allowCommands;
        this.allowCheats = allowCheats;
    }

    public void save() throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("version", VERSION);

        JsonObject hosting = new JsonObject();
        hosting.addProperty("hostname", hostname);
        hosting.addProperty("port", port);
        hosting.addProperty("path", path);

        json.add("hosting", hosting);
        json.addProperty("seed", seed);
        json.addProperty("levelName", levelName);
        json.addProperty("allowCommands", allowCommands);
        json.addProperty("allowCheats", allowCheats);

        try (JsonWriter writer = new JsonWriter(Files.newBufferedWriter(PATH))) {
            CommonConstants.GSON.toJson(json, writer);
        }
    }

    public static DedicatedServerConfig load() {
        if (!Files.exists(PATH)) {
            return new DedicatedServerConfig();
        }

        try (JsonReader reader = new JsonReader(Files.newBufferedReader(PATH))) {
            JsonObject json = CommonConstants.GSON.fromJson(reader, JsonObject.class);

            if (json.get("version").getAsInt() > VERSION) {
                CommonConstants.LOGGER.warn("Server config version is newer than supported. Using defaults.");
                return new DedicatedServerConfig();
            }

            JsonObject hosting = json.getAsJsonObject("hosting");
            String hostname = "localhost";
            int port = 38800;
            String path = "qvoxel";
            if (hosting != null) {
                hostname = hosting.get("hostname").getAsString();
                port = hosting.get("port").getAsInt();
                path = hosting.get("path").getAsString();
            }

            return new DedicatedServerConfig(
                    hostname,
                    port,
                    path,
                    json.get("seed").getAsLong(),
                    json.get("levelName").getAsString(),
                    json.get("allowCommands").getAsBoolean(),
                    json.get("allowCheats").getAsBoolean()
            );
        } catch (FileNotFoundException e) {
            DedicatedServerConfig config = new DedicatedServerConfig();
            try {
                config.save();
            } catch (IOException e1) {
                CommonConstants.LOGGER.error("Failed to save server config: {}", e1.getMessage());
            }
            return config;
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to load server config: {}", e.getMessage());
            return new DedicatedServerConfig();
        }
    }
}
