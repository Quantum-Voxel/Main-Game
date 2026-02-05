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

import dev.ultreon.qvoxel.CommonConstants;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AssetStore implements AutoCloseable {
    private static final AssetStore instance = new AssetStore(System.getProperty("quantum.launch.version"));
    private String version;
    private List<Asset> assets;
    private Path assetsDir;
    private List<String> paths;

    public AssetStore(String version) {
        this.version = version;
    }

    public void create(Path assetsDir, String version) {
        this.version = version;
        this.assetsDir = assetsDir;
        Assets assets;
        try {
            assets = CommonConstants.GSON.fromJson(Files.readString(assetsDir.resolve(this.version + ".json")), Assets.class);
        } catch (IOException e) {
            throw new AssetStoreException(e);
        }
        this.assets = assets.getAssets();
    }

    @Override
    public void close() {
        assets = null;
        assetsDir = null;
        paths = null;
    }

    public static class Asset {
        private final String hash;
        private final String path;

        public Asset(String hash, String path) {
            this.hash = hash;
            this.path = path;
        }

        public String getHash() {
            return hash;
        }

        public String getPath() {
            return path;
        }
    }

    public static class Assets {
        private List<Asset> assets = new ArrayList<>();

        public Assets() {
        }

        public List<Asset> getAssets() {
            return assets;
        }

        public void setAssets(List<Asset> assets) {
            this.assets = assets;
        }
    }

    public List<String> getPaths() {
        if (paths != null) {
            return paths;
        }

        paths = new ArrayList<>();
        for (Asset asset : assets) {
            String path = asset.getPath();
            paths.add(path);
        }
        return paths;
    }

    public InputStream openResourceStream(String path) throws IOException {
        String replace = path.replace(File.separatorChar, '/');
        if (replace.startsWith("/")) {
            replace = replace.substring(1);
        }

        for (Asset asset : assets) {
            if (asset.getPath().equals(replace)) {
                String hash = asset.getHash();
                String firstTwo = hash.substring(0, 2);

                InputStream objects = Files.newInputStream(assetsDir.resolve("objects").resolve(firstTwo).resolve(hash));
                return new AutoClosingInputStream(objects);
            }
        }

        throw new IOException("Asset not found: " + path);
    }

    public static AssetStore get() {
        return instance;
    }

    private static class AutoClosingInputStream extends InputStream {
        private final InputStream objects;

        public AutoClosingInputStream(InputStream objects) {
            this.objects = objects;

            CommonConstants.CLEANER.register(this, () -> {
                try {
                    objects.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        @Override
        public int read(byte @NotNull [] b) throws IOException {
            return objects.read(b);
        }

        @Override
        public int read(byte @NotNull [] b, int off, int len) throws IOException {
            return objects.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return objects.skip(n);
        }

        @Override
        public int available() throws IOException {
            return objects.available();
        }

        @Override
        public void close() throws IOException {
            objects.close();
        }

        @Override
        public synchronized void mark(int readlimit) {
            objects.mark(readlimit);
        }

        @Override
        public synchronized void reset() throws IOException {
            objects.reset();
        }

        @Override
        public boolean markSupported() {
            return objects.markSupported();
        }

        @Override
        public int read() {
            return 0;
        }
    }
}
