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

package dev.ultreon.qvoxel.server;

import dev.ultreon.qvoxel.player.PlayerAbilities;
import org.jetbrains.annotations.Nullable;

public enum GameMode {
    SURVIVAL {
        @Override
        public void apply(PlayerAbilities abilities) {
            abilities.setFlying(false);
            abilities.setInvulnerable(false);
            abilities.setCanFly(false);
        }
    }, BUILDING {
        @Override
        public void apply(PlayerAbilities abilities) {
            abilities.setInvulnerable(true);
            abilities.setCanFly(true);
        }
    }, SPECTATING {
        @Override
        public void apply(PlayerAbilities abilities) {
            abilities.setFlying(true);
            abilities.setInvulnerable(true);
            abilities.setCanFly(true);
        }
    };

    public static @Nullable GameMode byOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= values().length) {
            return null;
        }

        return values()[ordinal];
    }

    public static GameMode fromName(String name) {
        for (GameMode value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }

    public abstract void apply(PlayerAbilities abilities);
}
