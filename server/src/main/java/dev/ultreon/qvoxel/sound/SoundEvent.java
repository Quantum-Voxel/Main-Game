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

package dev.ultreon.qvoxel.sound;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.network.PacketIO;
import dev.ultreon.qvoxel.network.packets.PacketCodec;
import dev.ultreon.qvoxel.network.packets.PacketId;
import dev.ultreon.qvoxel.registry.Registries;

public record SoundEvent(Identifier id) {
    public static final PacketCodec<SoundEvent> PACKET_CODEC = PacketCodec.of(PacketIO::readSoundEvent, PacketIO::writeSoundEvent);
    public static final SoundEvent LOGO_REVEAL = register(new SoundEvent(CommonConstants.id("sounds/logo_reveal.ogg")));
    public static final SoundEvent ENTITY_HIT = register(new SoundEvent(CommonConstants.id("sounds/entity/hit.ogg")));
    public static final SoundEvent ENTITY_PLAYER_HURT = register(new SoundEvent(CommonConstants.id("sounds/entity/player/hurt.ogg")));
    public static final SoundEvent ENTITY_PLAYER_SWORD_HIT = register(new SoundEvent(CommonConstants.id("sounds/entity/player/sword_hit.ogg")));
    public static final SoundEvent STEP_GRASS = register(new SoundEvent(CommonConstants.id("sounds/step/grass/1.ogg")));
    public static final SoundEvent STEP_STONE = register(new SoundEvent(CommonConstants.id("sounds/step/stone/1.ogg")));
    public static final SoundEvent STEP_WOOD = register(new SoundEvent(CommonConstants.id("sounds/step/wood.ogg")));
    public static final SoundEvent STEP_SAND = register(new SoundEvent(CommonConstants.id("sounds/step/sand.ogg")));
    public static final SoundEvent STEP_SNOW = register(new SoundEvent(CommonConstants.id("sounds/step/snow.ogg")));
    public static final SoundEvent UI_BUTTON_PRESS = register(new SoundEvent(CommonConstants.id("sounds/ui/button/press.ogg")));
    public static final SoundEvent UI_BUTTON_RELEASE = register(new SoundEvent(CommonConstants.id("sounds/ui/button/release.ogg")));
    public static final SoundEvent UI_MENU_TICK = register(new SoundEvent(CommonConstants.id("sounds/ui/menu/tick.ogg")));
    public static final SoundEvent UI_SCREENSHOT = register(new SoundEvent(CommonConstants.id("sounds/ui/screenshot.ogg")));

    public static void init() {

    }

    private static SoundEvent register(SoundEvent soundEvent) {
        Registries.SOUND_EVENT.register(soundEvent.id(), soundEvent);
        return soundEvent;
    }
}
