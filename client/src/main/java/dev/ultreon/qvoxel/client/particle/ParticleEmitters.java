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

import dev.ultreon.qvoxel.client.particle.emitter.BlockParticleEmitter;
import dev.ultreon.qvoxel.client.particle.emitter.SimpleParticleEmitter;
import dev.ultreon.qvoxel.client.particle.types.BlockParticle;
import dev.ultreon.qvoxel.client.particle.types.SimpleParticle;
import dev.ultreon.qvoxel.client.render.Mesh;
import dev.ultreon.qvoxel.client.world.MeshBuilder;
import dev.ultreon.qvoxel.client.world.RenderType;
import dev.ultreon.qvoxel.particle.ParticleTypes;
import org.joml.Vector3f;

public class ParticleEmitters {

    public static void init(ParticleSystem system) {
        system.addEmitter(new BlockParticleEmitter(new BlockParticle(1000L, createParticleMesh(), new Vector3f(0.5f, 0.5f, 0.5f)), ParticleTypes.BLOCK));
        system.addEmitter(new SimpleParticleEmitter(new SimpleParticle(0L, createParticleMesh(), new Vector3f(0.5f, 0.5f, 0.5f)), ParticleTypes.ENTITY_HIT, RenderType.CUTOUT));
    }

    public static Mesh createParticleMesh() {
        MeshBuilder builder = new MeshBuilder(RenderType.PARTICLE.attributes());
        var v00 = builder.vertex().setPosition(0, 0, 0).setUV(0, 0);
        var v01 = builder.vertex().setPosition(0, 1, 0).setUV(0, 1);
        var v10 = builder.vertex().setPosition(1, 0, 0).setUV(1, 0);
        var v11 = builder.vertex().setPosition(1, 1, 0).setUV(1, 1);
        builder.face(v00, v10, v11, v01);
        Mesh build = builder.build();
        if (build == null) {
            throw new RuntimeException("Failed to create particle mesh");
        }
        return build;
    }

}
