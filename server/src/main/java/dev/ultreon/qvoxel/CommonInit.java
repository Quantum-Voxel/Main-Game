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

package dev.ultreon.qvoxel;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.block.Blocks;
import dev.ultreon.qvoxel.block.actor.BlockActors;
import dev.ultreon.qvoxel.featureflags.Features;
import dev.ultreon.qvoxel.item.Items;
import dev.ultreon.qvoxel.particle.ParticleTypes;
import dev.ultreon.qvoxel.sound.SoundEvent;

public class CommonInit {
    public static void init() {
        Identifier.setDefaultNamespace(CommonConstants.NAMESPACE);

        Features.init();
        Blocks.init();
        BlockActors.init();
        Items.init();
        SoundEvent.init();
        ParticleTypes.init();
    }
}
