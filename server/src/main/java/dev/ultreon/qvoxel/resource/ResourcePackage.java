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
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ResourcePackage extends GameNode {
    protected final Map<Identifier, StaticResource> resources;
    protected final Map<String, ResourceCategory> categories;
    private boolean locked;

    public ResourcePackage(Map<Identifier, StaticResource> resources, Map<String, ResourceCategory> categories) {
        this.resources = resources;
        this.categories = categories;
    }

    public ResourcePackage() {
        resources = new HashMap<>();
        categories = new HashMap<>();
    }

    public boolean has(Identifier entry) {
        return resources.containsKey(entry);
    }

    public Set<Identifier> entries() {
        return resources.keySet();
    }

    public StaticResource get(Identifier entry) {
        return resources.get(entry);
    }

    public Map<Identifier, StaticResource> mapEntries() {
        return Collections.unmodifiableMap(resources);
    }

    public boolean hasCategory(String name) {
        return categories.containsKey(name);
    }

    public ResourceCategory getCategory(String name) {
        return categories.get(name);
    }

    public List<ResourceCategory> getCategories() {
        return List.copyOf(categories.values());
    }

    public @NotNull String getName() {
        return getClass().getSimpleName();
    }
}
