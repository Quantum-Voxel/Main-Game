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

package dev.ultreon.qvoxel.particle;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.registry.Registries;

public class ParticleTypes {
    public static final ParticleType<BlockParticleData> BLOCK = register("block", new ParticleType<>(BlockParticleData.SERIALIZER));
    public static final ParticleType<ParticleData> ENTITY_HIT = register("entity_hit", new ParticleType<>(ParticleData.SERIALIZER));

    private static <T extends ParticleData> ParticleType<T> register(String name, ParticleType<T> particleType) {
        Registries.PARTICLE_TYPE.register(CommonConstants.id(name), particleType);
        return particleType;
    }

    public static void init() {
        // no-op
    }
}
