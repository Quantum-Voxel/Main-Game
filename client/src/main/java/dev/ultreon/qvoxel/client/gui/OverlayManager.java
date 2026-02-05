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

package dev.ultreon.qvoxel.client.gui;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.client.world.ClientPlayerEntity;
import dev.ultreon.qvoxel.resource.GameObject;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public class OverlayManager extends GameObject {
    private final ListOrderedMap<Identifier, Overlay> registry = new ListOrderedMap<>();

    public <T extends Overlay> T registerTop(Identifier id, T overlay) {
        synchronized (registry) {
            registry.put(registry.size(), id, overlay);
            add(id.toString(), overlay);
            return overlay;
        }

    }

    public <T extends Overlay> T registerAbove(Identifier above, Identifier id, T overlay) {
        synchronized (registry) {
            int idx = registry.indexOf(above);
            registry.put(idx + 1, id, overlay);
            add(id.toString(), overlay);
            return overlay;
        }
    }

    public <T extends Overlay> T registerBelow(Identifier below, Identifier id, T overlay) {
        synchronized (registry) {
            int idx = registry.indexOf(below);
            registry.put(idx, id, overlay);
            add(id.toString(), overlay);
            return overlay;
        }
    }

    public <T extends Overlay> T registerBottom(Identifier id, T overlay) {
        synchronized (registry) {
            registry.put(0, id, overlay);
            add(id.toString(), overlay);
            return overlay;
        }
    }

    public List<Overlay> getOverlays() {
        return registry.valueList();
    }

    public void render(ClientPlayerEntity player, GuiRenderer renderer, float partialTick) {
        if (QuantumClient.get().hideHud) return;

        synchronized (registry) {
            int height = renderer.getHeight();
            Overlay.leftY = height;
            Overlay.rightY = height;

            for (Overlay overlay : getOverlays()) {
                overlay.render(player, renderer, partialTick);
            }
        }
    }

    @ApiStatus.Internal
    public void resize(int width, int height) {
        synchronized (registry) {
            for (Overlay overlay : getOverlays()) {
                overlay.resize(width, height);
            }
        }
    }
}
