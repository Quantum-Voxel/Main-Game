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

package dev.ultreon.qvoxel.client.registry;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.sound.Sound;
import dev.ultreon.qvoxel.resource.GameNode;
import dev.ultreon.qvoxel.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SoundRegistry extends GameNode {
    private Map<SoundEvent, Sound> sounds = new HashMap<>();
    private Set<SoundEvent> errored = new HashSet<>();

    public @Nullable Sound get(SoundEvent soundEvent) {
        if (errored.contains(soundEvent)) {
            return null;
        }
        Sound sound = sounds.get(soundEvent);
        if (sound == null) {
            errored.add(soundEvent);
            CommonConstants.LOGGER.error("Failed to find sound {}", soundEvent);
        }
        return sound;
    }

    public void register(SoundEvent soundEvent) {
        try {
            Sound sound = QuantumClient.get().soundSystem.createSound(soundEvent.id());
            if (sound == null) {
                errored.add(soundEvent);
                CommonConstants.LOGGER.error("Failed to load sound {}", soundEvent);
                return;
            }
            sounds.put(soundEvent, sound);
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to load sound {}", soundEvent, e);
        }
    }
}
