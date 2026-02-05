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

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.libs.commons.v0.exceptions.SyntaxException;
import dev.ultreon.libs.commons.v0.util.IOUtils;
import dev.ultreon.qvoxel.CommonConstants;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ResourceManager extends GameNode {
    protected final List<ResourcePackage> resourcePackages = new CopyOnWriteArrayList<>();
    private final String root;
    private final Map<Identifier, Resource> fakeResources = new ConcurrentHashMap<>();

    public ResourceManager(String root) {
        this.root = root;
    }

    public boolean canScanFiles() {
        return true;
    }

    public InputStream openResourceStream(Identifier entry) throws IOException {
        @Nullable Resource resource = getResource(entry);
        return resource == null ? null : resource.openStream();
    }

    @Nullable
    public Resource getResource(Identifier entry) {
        Resource resource = fakeResources.get(entry);
        if (resource != null) {
            return resource;
        }

        for (ResourcePackage resourcePackage : resourcePackages) {
            if (resourcePackage.has(entry)) {
                return resourcePackage.get(entry);
            }
        }

        return null;
    }

    public void importPackage(URI uri) throws IOException {
        URL url = uri.toURL();
        if (url.getProtocol().equals("file")) {
            importPackage(new File(uri).toPath());
        } else if (url.getProtocol().equals("jar")) {
            try {
                importFilePackage(new ZipInputStream(new URI(uri.toURL().getPath().split("!/", 2)[0]).toURL().openStream()), uri.toASCIIString());
            } catch (URISyntaxException e) {
                throw new IOException("Invalid URI: " + uri, e);
            }
        } else {
            importFilePackage(new ZipInputStream(uri.toURL().openStream()), uri.toASCIIString());
        }
    }

    public void importPackage(Path file) throws IOException {
        if (Files.notExists(file)) {
            throw new IOException("Resource package doesn't exists: " + file.toAbsolutePath());
        }

        if (!Files.isDirectory(file)) {
            CommonConstants.LOGGER.info("Importing file resource package: {}", file.toAbsolutePath());
            if (file.getFileName().toString().endsWith(".jar") || file.getFileName().toString().endsWith(".zip")) {
                importFilePackage(new ZipInputStream(Files.newInputStream(file)), file.toAbsolutePath().toString());
            } else {
                CommonConstants.LOGGER.warn("Resource package isn't a .jar or .zip file: {}", file.toAbsolutePath());
            }
        } else if (Files.isDirectory(file)) {
            CommonConstants.LOGGER.info("Importing directory resource package: {}", file.toUri());
            importDirectoryPackage(file);
        } else {
            throw new IOException("Resource package isn't a directory or a .jar or .zip file: " + file.toAbsolutePath());
        }
    }

    public void loadFromAssetStore(AssetStore store) {
        List<String> fileList = store.getPaths();

        // Prepare mappings
        Map<Identifier, StaticResource> map = new HashMap<>();
        Map<String, ResourceCategory> categories = new HashMap<>();

        for (String file : fileList) {
            char str = File.separatorChar;
            if (file.startsWith(root + str)) {
                String domain = file.substring(root.length() + 1);
                String domainId = domain.substring(0, domain.indexOf(str));
                String[] path = domain.substring(domain.indexOf(str) + 1).split("\\\\" + str);
                String[] categoryParts = Arrays.copyOf(path, path.length - 1);
                String filename = path[path.length - 1];

                String categoryPath = categoryParts.length > 0 ? String.join(String.valueOf(str).replace("\\", "\\\\"), categoryParts) + str : "";
                String filePath = categoryPath + filename;

                StaticResource resource = new StaticResource(
                        new Identifier(domainId, filePath.replace(File.separatorChar, '/')),
                        () -> store.openResourceStream(file)
                );

                // Add to the categories' map
                if (categoryParts.length > 0) {
                    String category = categoryParts[0];
                    categories.computeIfAbsent(category, ResourceCategory::new)
                            .set(new Identifier(domainId, filePath.replace(File.separatorChar, '/')), resource);
                }

                // Add to resources' map
                map.put(new Identifier(domainId, filePath.replace(File.separatorChar, '/')), resource);
            }
        }

        addImported(new ResourcePackage(map, categories));
    }

    @SuppressWarnings({"unused"})
    private void importDirectoryPackage(Path file) throws IOException {
        // Check if it's a directory.
        assert Files.isDirectory(file);

        // Prepare (entry -> resource) mappings
        Map<Identifier, StaticResource> map = new HashMap<>();

        // Resource categories
        Map<String, ResourceCategory> categories = new HashMap<>();

        // Get assets directory.
        Path assets = file.resolve(root);

        // Check if the assets directory exists.
        if (Files.exists(assets)) {
            // List files in assets dir.
            Path[] files;
            try (Stream<Path> list = Files.list(assets)) {
                files = list.toArray(Path[]::new);
            }

            // Loop listed files.
            for (Path resPackage : files != null ? files : new Path[0]) {
                // Get assets-package namespace from the name of the listed file (that's a dir).
                String namespace = resPackage.getFileName().toString();

                // Walk assets package.
                try (Stream<Path> walk = walk(resPackage)) {
                    for (Path assetPath : walk.collect(Collectors.toList())) {
                        // Convert to a file object.

                        // Check if it's a file, if not,
                        // we will walk to the next file / folder in the Files.walk(...)
                        // list.
                        if (Files.isDirectory(assetPath)) {
                            continue;
                        }

                        // Continue to the next file / folder
                        // if the asset path is the same path as the resource package.
                        if (assetPath.equals(resPackage)) {
                            CommonConstants.LOGGER.warn("Skipping resource package: {}", resPackage.toAbsolutePath());
                            continue;
                        }

                        // Calculate resource path.
                        String s = resPackage.relativize(assetPath).toString();
                        s = s.replace('\\', '/');

                        // Create resource entry/
                        Identifier entry;
                        try {
                            entry = new Identifier(namespace, s);
                        } catch (SyntaxException e) {
                            CommonConstants.LOGGER.error("Invalid resource identifier:", e);
                            continue;
                        }

                        // Create resource with file input stream.
                        ThrowingSupplier<InputStream, IOException> sup = () -> Files.newInputStream(assetPath);
                        StaticResource resource = new StaticResource(entry, sup);

                        String path = entry.toString();
                        String[] split = path.split("/");
                        String category = split[0];
                        if (split.length > 1) {
                            categories.computeIfAbsent(category, ResourceCategory::new).set(entry, resource);
                        }

                        // Add resource mapping for (entry -> resource).
                        map.put(entry, resource);
                    }
                }
            }

            addImported(new ResourcePackage(map, categories));
        }
    }

    private Stream<Path> walk(Path resPackage) throws IOException {
        if (Files.notExists(resPackage)) {
            return Stream.empty();
        }

        List<Path> files = new ArrayList<>();
        files.add(resPackage);

        if (Files.isDirectory(resPackage)) {
            try (Stream<Path> list = Files.list(resPackage)) {
                for (Path child : list.toList()) {
                    files.addAll(walk(child).toList());
                }
            }
        }

        return files.stream();
    }

    private void importFilePackage(ZipInputStream stream, String filePath) throws IOException {
        // Check for .jar files.
        // Prepare (entry -> resource) mappings.
        Map<Identifier, StaticResource> map = new HashMap<>();

        // Resource categories
        Map<String, ResourceCategory> categories = new HashMap<>();

        try {
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                String name = entry.getName();
                byte[] bytes = IOUtils.readAllBytes(stream);
                ThrowingSupplier<InputStream, IOException> sup = () -> new ByteArrayInputStream(bytes);

                // Check if it isn't a directory because we want a file.
                if (!entry.isDirectory()) {
                    addEntry(map, categories, name, sup);
                }
                stream.closeEntry();
            }
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to load resource package: {}", filePath, e);
        }

        addImported(new ResourcePackage(map, categories));

        stream.close();
    }

    private void addImported(ResourcePackage pkg) {
        resourcePackages.add(pkg);
        add(pkg.getName(), pkg);
    }

    private void addEntry(Map<Identifier, StaticResource> map, Map<String, ResourceCategory> categories, String name, ThrowingSupplier<InputStream, IOException> sup) {
        String[] splitPath = name.split("/", 3);

        if (splitPath.length >= 3) {
            if (name.startsWith(root + "/")) {
                // Get namespace and path from the split path
                String namespace = splitPath[1];
                String path = splitPath[2];

                // Entry
                Identifier entry = new Identifier(namespace, path);

                // Resource
                StaticResource resource = new StaticResource(entry, sup);

                // Category
                String[] split = path.split("/");
                String category = split[0];
                if (split.length > 1) {
                    categories.computeIfAbsent(category, ResourceCategory::new).set(entry, resource);
                }

                try {

                    // Add (entry -> resource) mapping.
                    map.put(entry, resource);
                } catch (Throwable ignored) {

                }
            }
        }
    }

    public String getRoot() {
        return root;
    }

    public List<ResourceCategory> getResourceCategory(String category) {
        return resourcePackages.stream().map(resourcePackage -> {
            if (!resourcePackage.hasCategory(category)) {
                return null;
            }

            return resourcePackage.getCategory(category);
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public List<ResourcePackage> getResourcePackages() {
        return Collections.unmodifiableList(resourcePackages);
    }

    public List<ResourceCategory> getResourceCategories() {
        return resourcePackages.stream().flatMap(resourcePackage -> resourcePackage.getCategories().stream()).collect(Collectors.toList());
    }

    public void reload() {
        for (ResourcePackage resourcePackage : resourcePackages) {
            remove(resourcePackage);
        }

        resourcePackages.clear();

        importAll();
    }

    public void importAll() {
        importGameResources();
        importModResources();

        try {
            discover();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void discover() throws IOException {
        Path dir;
        try {
            dir = FabricLoader.getInstance().getGameDir().resolve("resource-packages");
        } catch (IllegalStateException e) {
            dir = Path.of("resource-packages");
        }
        if (Files.notExists(dir)) {
            Files.createDirectories(dir);
            return;
        }
        Path[] list;
        try (Stream<Path> list1 = Files.list(dir)) {
            list = list1.toArray(Path[]::new);
        }
        for (Path fileHandle : list) {
            importFrom(fileHandle);
        }
    }

    private void importFrom(Path list) {
        try {
            importPackage(list);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void importGameResources() {
        FabricLoader.getInstance().getModContainer(CommonConstants.NAMESPACE).ifPresent(modContainer -> {
            List<Path> rootPaths = modContainer.getRootPaths();
            for (Path rootPath : rootPaths) {
                if (Files.notExists(rootPath)) continue;
                try {
                    importPackage(rootPath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void importModResources() {
        FabricLoader.getInstance().getAllMods().forEach(modContainer -> {
            if (modContainer.getMetadata().getId().equals(CommonConstants.NAMESPACE)) {
                return;
            }

            List<Path> rootPaths = modContainer.getRootPaths();
            for (Path rootPath : rootPaths) {
                if (Files.notExists(rootPath)) continue;
                try {
                    importPackage(rootPath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void setFakeResource(Identifier identifier, byte[] dataAsPng) {
        this.fakeResources.put(identifier, new StaticResource(identifier, () -> new ByteArrayInputStream(dataAsPng)));
    }
}
