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

package dev.ultreon.qvoxel.client.particle;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.particle.emitter.ParticleEmitter;
import dev.ultreon.qvoxel.client.world.ClientPlayerEntity;
import dev.ultreon.qvoxel.client.world.WorldRenderer;
import dev.ultreon.qvoxel.particle.ParticleData;
import dev.ultreon.qvoxel.particle.ParticleType;
import dev.ultreon.qvoxel.resource.GameNode;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class ParticleSystem extends GameNode {
    private final QuantumClient client;
    private final List<ParticleEmitter<?>> emitters = new ArrayList<>();
    private final Matrix4f modelViewMatrix = new Matrix4f();
    private Thread thread;
    private long lastTime;
    private float partialTick;

    public ParticleSystem(QuantumClient client) {
        this.client = client;
    }

    public void addEmitter(ParticleEmitter<?> emitter) {
        emitters.add(emitter);
    }

    public void render(ClientPlayerEntity player, Vector3d playerPos) {
        WorldRenderer worldRenderer = player.getWorldRenderer();
        if (worldRenderer == null) return;

        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_CULL_FACE);
        for (ParticleEmitter<?> emitter : emitters) {
            emitter.render(client, playerPos, modelViewMatrix, worldRenderer.getCamera(), partialTick);
        }
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    public void start() {
        thread = new Thread(this::tick);
        thread.start();
    }

    public void stop() {
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void tick() {
        try {
            while (true) {
                long currentTime = System.currentTimeMillis();
                partialTick = (float) (currentTime - lastTime) / (1000 / 20f);
                if (currentTime - lastTime >= 1000 / 20) {
                    lastTime = currentTime;
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    for (ParticleEmitter<?> emitter : emitters) {
                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }
                        emitter.tick(client.getWorld());
                    }
                }

            }
        } catch (Exception e) {
            CommonConstants.LOGGER.error("Failed to tick particle system", e);
        }
    }

    public <T extends ParticleData> void handle(ParticleType<T> type, int quantity, ParticleData data) {
        emitters.forEach(emitter -> {
            if (emitter.getParticleType() == type) {
                emitter.addParticle(quantity, data);
            }
        });
    }
}
