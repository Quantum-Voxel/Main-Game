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

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.particle.types.BlockParticle;
import dev.ultreon.qvoxel.client.render.Mesh;
import dev.ultreon.qvoxel.client.texture.TextureAtlas;
import dev.ultreon.qvoxel.client.world.RenderType;
import dev.ultreon.qvoxel.particle.BlockParticleData;
import dev.ultreon.qvoxel.particle.ParticleData;
import dev.ultreon.qvoxel.particle.ParticleType;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class BlockParticleEmitter extends ParticleEmitter<BlockParticle> {
    public BlockParticleEmitter(BlockParticle particle, ParticleType<?> particleType) {
        super(particle, particleType, RenderType.PARTICLE);
    }

    @Override
    protected void configureParticle(BlockParticle particle, ParticleData data) {
        if (data.userData == null) {
            throw new IllegalArgumentException("Particle data must have a user data!");
        }
        particle.setAtlasRegion((TextureAtlas.AtlasRegion) data.userData);
        particle.velocity.set(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5);
        particle.velocity.normalize();
        particle.velocity.mul(random(0.05f, 0.1f));
        particle.scale.set(random(data.minSize, data.maxSize));
        particle.setTtl(data.ttl + Math.round(Math.random() * 1000));
    }

    @Override
    protected BlockParticle obtain(BlockParticle baseParticle) {
        return new BlockParticle(baseParticle);
    }

    @Override
    public Mesh getMesh() {
        return baseParticle.getMesh();
    }

    @Override
    public void addParticle(int quantity, ParticleData data) {
        TextureAtlas.AtlasRegion particle = QuantumClient.get().getBlockModel(((BlockParticleData) data).getBlockState()).getParticle();
        if (particle == null) {
            return; // No particle for this block state :(
        }
        data.userData = particle;
        CommonConstants.LOGGER.debug("Adding {} block particle(s) at {} using speed {}..{}, size {}..{} and TTL {}", quantity, data.position, data.minSpeed, data.maxSpeed, data.minSize, data.maxSize, data.ttl);
        createParticles(data, quantity, data.position, data.delta, data.minSpeed, data.maxSpeed, data.minSize, data.maxSize, data.ttl);
    }

    @Override
    public void createParticles(ParticleData data, int quantity, Vector3d position, Vector3f delta, float minSpeed, float maxSpeed, float minScale, float maxScale, long ttl) {
        Vector3d aux = new Vector3d();
        for (float dx = -0.5f; dx <= 0.5f; dx += 1.0f / (float) Math.cbrt(quantity)) {
            for (float dy = -0.5f; dy <= 0.5f; dy += 1.0f / (float) Math.cbrt(quantity)) {
                for (float dz = -0.5f; dz <= 0.5f; dz += 1.0f / (float) Math.cbrt(quantity)) {
                    createParticle(data, aux.set(position).add(dx * 0.333, dy * 0.333, dz * 0.333), new Vector3f((float) (dx * 0.333), (float) (dy * 0.333), (float) (dz * 0.333)), random(minSpeed, maxSpeed), random(minScale, maxScale), ttl);
                }
            }
        }
    }
}
