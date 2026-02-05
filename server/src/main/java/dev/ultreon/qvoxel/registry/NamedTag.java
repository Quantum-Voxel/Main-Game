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

package dev.ultreon.qvoxel.registry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.resource.ReloadContext;
import dev.ultreon.qvoxel.resource.Resource;
import dev.ultreon.qvoxel.resource.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class NamedTag<T> {
    private final Identifier name;
    private final Registry<T> registry;
    private final List<T> values;
    private boolean loaded;

    public NamedTag(Identifier name, Registry<T> registry) {
        this.name = name;
        this.registry = registry;
        values = new ArrayList<>();
    }

    @SuppressWarnings("D")
    public void reload(ReloadContext context) {
        ResourceManager resourceManager = context.getResourceManager();
        Identifier entry = name.mapPath(path -> {
            String domain = registry.id().location();
            if (domain.equals(CommonConstants.NAMESPACE))
                return "tags/" + registry.id().path() + "/" + path + ".json";
            return "tags/" + domain + "." + registry.id().path() + path + ".json";
        });
        @Nullable Resource res = resourceManager.getResource(entry);
        if (res == null) {
            CommonConstants.LOGGER.warn("Tag not found: {} for registry {}", name, registry.id());
            loaded = false;
            return;
        }
        JsonElement rootElem = res.loadJson();

        if (!(rootElem instanceof JsonObject rootObj)) {
            CommonConstants.LOGGER.warn("Invalid tag root element for registry {} in tag {}", registry.id(), name);
            return;
        }
        JsonElement root = rootObj.get("elements");

        if (!(root instanceof JsonArray rootArr)) {
            CommonConstants.LOGGER.warn("Invalid tag elements for registry {} in tag {}", registry.id(), name);
            return;
        }

        for (JsonElement elem : rootArr) {
            if (!elem.isJsonPrimitive() || !elem.getAsJsonPrimitive().isString()) {
                CommonConstants.LOGGER.warn("Invalid tag element for registry {} in tag {}", registry.id(), name);
                continue;
            }

            String element = elem.getAsString();
            if (!element.startsWith("#")) {
                T e = registry.get(new Identifier(element));
                if (e == null) {
                    throw new IllegalArgumentException("Element not found: " + element + " for registry " + registry.id() + " in tag " + name);
                }
                values.add(e);
                continue;
            }

            Optional<NamedTag<T>> tag = registry.getTag(new Identifier(element.substring(1)));
            values.addAll(tag.map(NamedTag::getValues).orElseGet(() -> {
                NamedTag<T> namedTag = new NamedTag<>(new Identifier(element.substring(1)), registry);
                namedTag.reload(context);
                return namedTag.getValues();
            }));
        }

        loaded = true;
    }

    public Identifier getName() {
        return name;
    }

    public Collection<T> getValues() {
        if (!loaded) {
            throw new IllegalStateException("Tag not loaded or failed to load: " + name);
        }
        return Collections.unmodifiableCollection(values);
    }

    public boolean contains(T value) {
        if (!loaded) {
            throw new IllegalStateException("Tag not loaded or failed to load: " + name);
        }
        return values.contains(value);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamedTag<?> that = (NamedTag<?>) o;
        return name.equals(that.name) && values.equals(that.values);
    }

    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + values.hashCode();
        return result;
    }

    public String toString() {
        return "Tag[" + name + "]";
    }
}
