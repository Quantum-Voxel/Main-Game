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

import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.render.GuiRenderer;
import dev.ultreon.qvoxel.client.world.ClientPlayerEntity;
import dev.ultreon.qvoxel.resource.GameObject;

public abstract class Overlay extends GameObject {
    public static int leftY = 0;
    public static int rightY = 0;

    protected int width;
    protected int height;
    protected final QuantumClient client = QuantumClient.get();

    public abstract void render(ClientPlayerEntity player, GuiRenderer renderer, float partialTick);

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
