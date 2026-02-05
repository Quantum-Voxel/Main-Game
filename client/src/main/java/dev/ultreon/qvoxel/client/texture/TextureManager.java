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

package dev.ultreon.qvoxel.client.texture;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.shader.Reloadable;
import dev.ultreon.qvoxel.resource.GameNode;
import dev.ultreon.qvoxel.resource.ReloadContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TextureManager extends GameNode implements AutoCloseable, Reloadable {
    private final Texture fallbackTexture = Texture.draw(16, 16, painter -> {
        painter.fillColor(0xFF000000);
        painter.fillRect(0, 0, 8, 8, 0xFFFFB000);
        painter.fillRect(8, 8, 8, 8, 0xFFFFB000);
    });
    private final Map<Identifier, Texture> textures = new HashMap<>();

    public void loadTexture(Identifier id, Texture texture) {
        Texture old = textures.get(id);
        if (old != null) old.delete();
        textures.put(id, texture);
    }

    public void loadTexture(Identifier id) {
        try {
            loadTexture(id, new Texture(id));
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to load texture {}", id, e);
        }
    }

    public void unloadTexture(Identifier id) {
        Texture texture = textures.remove(id);
        if (texture != null) texture.delete();
    }

    public Texture getTexture(Identifier id) {
        Texture texture = textures.get(id);
        if (texture != null) return texture;
        if (id.path().isEmpty()) return fallbackTexture;

        try {
            Texture texture1 = new Texture(id);
            textures.put(id, texture1);
            return texture1;
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to load texture {}", id, e);
            return fallbackTexture;
        } catch (TextureException e) {
            CommonConstants.LOGGER.error("Failed to load texture {}", id, e);
            textures.put(id, fallbackTexture);
            return fallbackTexture;
        }
    }

    @Override
    public void close() {
        for (Texture texture : textures.values()) {
            texture.delete();
        }

        textures.clear();
        fallbackTexture.delete();
    }

    public CompletableFuture<?> reload(ReloadContext context) {
        context.log("Deleting all textures...");
        return context.submitSafe(() -> {
            for (Texture value : textures.values()) {
                value.delete();
            }
            textures.clear();
        });
    }
}
