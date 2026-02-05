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

package dev.ultreon.qvoxel.client.particle.emitter;

import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.particle.types.Particle;
import dev.ultreon.qvoxel.client.render.Mesh;
import dev.ultreon.qvoxel.client.shader.ShaderProgram;
import dev.ultreon.qvoxel.client.world.Camera;
import dev.ultreon.qvoxel.client.world.ClientWorld;
import dev.ultreon.qvoxel.client.world.RenderType;
import dev.ultreon.qvoxel.particle.ParticleData;
import dev.ultreon.qvoxel.particle.ParticleType;
import dev.ultreon.qvoxel.resource.GameObject;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class ParticleEmitter<T extends Particle> implements AutoCloseable {

    protected boolean active;

    protected final List<T> particles;

    protected final T baseParticle;
    private final ParticleType<?> particleType;
    private final RenderType renderType;
    private final ShaderProgram program;

    public ParticleEmitter(T baseParticle, ParticleType<?> particleType, RenderType renderType) {
        this.baseParticle = baseParticle;
        this.particleType = particleType;
        this.renderType = renderType;
        program = renderType.shaderProgram();
        particles = new CopyOnWriteArrayList<>();
    }

    public T getBaseParticle() {
        return baseParticle;
    }


    public List<T> getParticles() {
        return particles;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void update(long ellapsedTime, Vector3d playerPos, Matrix4f modelViewMatrix, Camera camera, float partialTick) {
        particles.removeIf(p -> p.updateTtl(ellapsedTime) < 0);
        particles.forEach(particle -> {
            // Update the particle positioning
            updatePosition(particle, ellapsedTime);
            particle.rotation.identity();

            particle.update(playerPos);

            // Look at the camera
            particle.modelMatrix
                    .rotateY((float) Math.toRadians(-camera.yaw + 180))
                    .rotateX((float) Math.toRadians(camera.pitch));

            // Apply the modelViewMatrix to the particle shader
            modelViewMatrix.set(camera.getViewMatrix());
            modelViewMatrix.mul(particle.modelMatrix);
            if (program != null) {
                program.setUniform("modelViewMatrix", modelViewMatrix);
                particle.draw(getMesh(), program);
            }
        });
    }

    private void lerp(Vector3d tmp, Vector3f position, float v) {
        tmp.x = position.x * v + tmp.x * (1.0f - v);
        tmp.y = position.y * v + tmp.y * (1.0f - v);
        tmp.z = position.z * v + tmp.z * (1.0f - v);
    }

    public void createParticles(ParticleData data, int quantity, Vector3d position, Vector3f delta, float minSpeed, float maxSpeed, float minScale, float maxScale, long ttl) {
        for (int i = 0; i < quantity; i++) {
            createParticle(data, position, random(delta), random(minSpeed, maxSpeed), random(minScale, maxScale), ttl);
        }
    }

    private Vector3f random(Vector3f delta) {
        return new Vector3f(random(-delta.x, delta.x), random(-delta.y, delta.y), random(-delta.z, delta.z));
    }

    protected float random(float min, float max) {
        return (float) (min + Math.random() * (max - min));
    }

    protected void createParticle(ParticleData data, Vector3d position, Vector3f delta, float speed, float scale, long ttl) {
        T particle = obtain(getBaseParticle());

        particle.position.set(position);
        particle.position.add(delta);
        particle.velocity.set(delta);
        particle.getSpeed().set(speed);
        particle.scale.mul(scale, scale, scale);
        particle.setTtl(ttl);
        configureParticle(particle, data);
        particles.add(particle);
    }

    protected abstract void configureParticle(T particle, ParticleData data);

    protected abstract T obtain(T baseParticle);

    /**
     * Updates a particle position
     *
     * @param particle    The particle to update
     * @param elapsedTime Elapsed time in milliseconds
     */
    public void updatePosition(T particle, long elapsedTime) {

    }

    @Override
    public void close() {
        for (T particle : getParticles()) {
            particle.close();
        }
    }

    public abstract Mesh getMesh();

    public ParticleType<?> getParticleType() {
        return particleType;
    }

    public void render(QuantumClient client, Vector3d playerPos, Matrix4f modelViewMatrix, Camera camera, float partialTick) {
        if (particles.isEmpty()) return;
        update(client.deltaTime, playerPos, modelViewMatrix, camera, partialTick);
    }

    public abstract void addParticle(int quantity, ParticleData data);

    public void tick(ClientWorld world) {
        for (var particle : getParticles()) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            particle.tick(world);
        }
    }
}