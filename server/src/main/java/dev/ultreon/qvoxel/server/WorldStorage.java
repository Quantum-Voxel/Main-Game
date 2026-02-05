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

package dev.ultreon.qvoxel.server;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.ubo.DataIo;
import dev.ultreon.ubo.types.DataType;
import dev.ultreon.ubo.types.MapType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.stream.Stream;

import static java.util.Comparator.reverseOrder;

/**
 * The WorldStorage class represents a storage system for world data.
 * It provides methods to read, write, and manage data within a given world directory.
 */
public final class WorldStorage {
    private final Path directory;
    private final Path infoFile;
    private boolean infoLoaded;
    private WorldSaveInfo info;
    private String md5Name;
    private String name;

    /**
     * Creates a new world storage instance from the given directory.
     *
     * @param path the world directory.
     */
    public WorldStorage(Path path) {
        directory = path;
        infoFile = getDirectory().resolve("info.ubo");
    }

    /**
     * Read a UBO object from the given path.
     *
     * @param path       the path to the UBO object.
     * @param typeGetter the type getter. <span style="color: red;">NOTE: do not use this parameter! Leave it empty.</span>
     * @param <T>        the type of the UBO object.
     * @return the UBO object.
     * @throws IOException if an I/O error occurs.
     */
    @SafeVarargs
    public final <T extends DataType<?>> T read(String path, T... typeGetter) throws IOException {
        try (InputStream stream = Files.newInputStream(directory.resolve(path))) {
            return DataIo.read(stream, typeGetter);
        }
    }

    /**
     * Write a UBO object to the given path.
     *
     * @param data the UBO object to write.
     * @param path the path to the UBO object.
     * @throws IOException if an I/O error occurs.
     */
    public void write(DataType<?> data, String path) throws IOException {
        Path child = directory.resolve(path);
        Path parent = child.getParent();
        if (Files.notExists(parent)) Files.createDirectories(parent);
        DataIo.write(data, Files.newOutputStream(child, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
    }

    /**
     * Check if the given path exists.
     *
     * @param path the path to the UBO object.
     * @return {@code true} if the path exists, {@code false} otherwise.
     */
    public boolean exists(String path) {
        return Files.exists(directory.resolve(path));
    }

    /**
     * Creates a new subdirectory in the world directory.
     *
     * @param path the relative path to the subdirectory.
     */
    public void createDir(String path) {
        // Create the directory if it doesn't exist
        if (exists(path)) {
            return;
        }

        try {
            Files.createDirectories(getDirectory().resolve(path));
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Check if the given region file exists.
     *
     * @param x the x coordinate of the region.
     * @param z the z coordinate of the region.
     * @return {@code true} if the region file exists, {@code false} otherwise.
     */
    public boolean regionExists(int x, int y, int z) {
        return exists("regions/" + x + "," + z + "/section." + y + ".qvr");
    }

    /**
     * Get the region file for the given coordinates.
     *
     * @param x the x coordinate of the region.
     * @param z the z coordinate of the region.
     * @return the region file.
     */
    public Path regionFile(int x, int y, int z) {
        return getDirectory().resolve("regions/" + x + "," + z + "/section." + y + ".qvr");
    }

    public boolean delete() {
        WorldSaveInfo worldSaveInfo = loadInfo();
        worldSaveInfo.delete();
        try {
            saveInfo(worldSaveInfo);
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to trash world {}", name, e);
            return false;
        }
        return true;
    }

    public boolean restore() {
        WorldSaveInfo worldSaveInfo = loadInfo();
        worldSaveInfo.restore();
        try {
            saveInfo(worldSaveInfo);
        } catch (IOException _) {
            return false;
        }
        return true;
    }

    /**
     * Delete the world directory.
     *
     * @return {@code true} if the world directory existed before, {@code false} otherwise.
     * @throws IOException if an I/O error occurs.
     */
    public boolean deletePermanently() throws IOException {
        try (Stream<Path> walk = Files.walk(getDirectory())) {
            walk.sorted(reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    throw new StorageException("Failed to delete file: " + path + " (" + e.getMessage());
                }
            });
        } catch (StorageException e) {
            CommonConstants.LOGGER.error("Failed to delete world directory: {} ({})", getDirectory().toAbsolutePath(), e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Get the region file for the given coordinates.
     *
     * @param pos the relativePos of the region.
     * @return the region file.
     */
    public Path regionFile(Vector3i pos) {
        return regionFile(pos.x, pos.y, pos.z);
    }

    /**
     * Create the world directory if it doesn't exist.
     *
     */
    public void createWorld() {
        createDir("regions");
        createDir("data");
    }

    public WorldSaveInfo loadInfo() {
        if (!infoLoaded) {
            infoLoaded = true;
            MapType infoData;
            try (InputStream stream = Files.newInputStream(infoFile)) {
                infoData = DataIo.read(stream);
            } catch (Exception e) {
                CommonConstants.LOGGER.error("Failed to load world info for {}", name, e);
                infoData = new MapType();
            }

            info = WorldSaveInfo.fromMap(infoData);
            name = info.getName();
        }

        return info;
    }

    public boolean hasInfo() {
        return Files.exists(getDirectory().resolve("info.ubo"));
    }

    /**
     * Retrieves the MD5 hash of the world storage directory name.
     * If the hash has not been computed yet, it generates the hash and stores it.
     *
     * @return The MD5 hash of the world storage directory name in hexadecimal format.
     */
    public String getHashName() {
        if (md5Name == null) {
            String string = getDirectory().getFileName().toString();
            md5Name = hashSHA256(string.getBytes(StandardCharsets.UTF_8));
        }

        return md5Name;
    }

    /**
     * Generates a unique folder name using the current system time and MD5 hashing.
     *
     * @return A string representing the generated folder name in hexadecimal format.
     */
    public static String createFolderName() {
        return hashSHA256(String.valueOf(System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Converts an array of bytes into its hexadecimal string representation.
     *
     * @param bytes an array of bytes to be converted to a hexadecimal string.
     * @return a string representing the hexadecimal value of the byte array.
     */
    public static String bytes2hex(byte[] bytes) {
        char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Computes the MD5 hash of the given input byte array.
     *
     * @param input the byte array to be hashed
     * @return a byte array containing the MD5 hash of the input
     */
    public static String hashSHA256(byte @NotNull [] input) {
        return Base64.getEncoder().encodeToString(input).replace("/", "_").replace("+", "-").replace("=", "");
    }

    /**
     * Retrieves the name associated with the world storage.
     * If the name is not already known, it attempts to load this information.
     * If the information cannot be loaded, it defaults to the name of the directory.
     *
     * @return The name of the world storage.
     */
    public String getName() {
        if (name != null) return name;
        if (hasInfo()) {
            info = loadInfo();
        } else {
            name = getDirectory().getFileName().toString();
        }
        return name;
    }

    /**
     * Saves the given WorldSaveInfo object and persists the changes.
     *
     * @param worldSaveInfo the WorldSaveInfo object that contains the information to be saved
     * @throws IOException if an I/O error occurs during the saving process
     */
    public void saveInfo(WorldSaveInfo worldSaveInfo) throws IOException {
        info = worldSaveInfo;
        name = worldSaveInfo.getName();
        infoLoaded = true;
        worldSaveInfo.save(this);
    }

    public Path getDirectory() {
        return directory;
    }

    public int compareTo(WorldStorage worldStorage) {
        return worldStorage.loadInfo().getLastSave().compareTo(loadInfo().getLastSave());
    }
}
