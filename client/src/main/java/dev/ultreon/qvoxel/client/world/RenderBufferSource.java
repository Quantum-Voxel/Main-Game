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

package dev.ultreon.qvoxel.client.world;

import dev.ultreon.qvoxel.resource.GameNode;
import org.joml.Matrix4fStack;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RenderBufferSource extends GameNode implements AutoCloseable {
    private final Map<RenderType, RenderBuffer> background = new HashMap<>();
    private final Map<RenderType, RenderBuffer> buffers = new LinkedHashMap<>();
    private Camera camera;
    private Matrix4fStack matrixStack;
    private boolean opened;
    private Vector3d origin;
    private float partialTicks;

    public RenderBufferSource open(Camera camera, Matrix4fStack matrixStack, Vector3d origin, float partialTicks) {
        this.camera = camera;
        this.matrixStack = matrixStack;
        this.origin = origin;
        this.partialTicks = partialTicks;
        opened = true;
        return this;
    }


    public RenderBuffer get(RenderType renderType) {
        if (!opened) throw new IllegalStateException("Render buffer source is closed");

        return buffers.computeIfAbsent(renderType, rt -> background.computeIfAbsent(rt, RenderBuffer::new).open(camera, matrixStack, origin, partialTicks));
    }

    @Override
    public void close() {
        opened = false;
        for (RenderBuffer buffer : buffers.values()) buffer.close();
        background.putAll(buffers);
        buffers.clear();
    }
}
