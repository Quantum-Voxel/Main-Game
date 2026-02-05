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

package dev.ultreon.qvoxel.network;

import dev.ultreon.qvoxel.Env;

public enum PacketDestination {
    SERVER, CLIENT;

    public PacketDestination opposite() {
        return switch (this) {
            case SERVER -> PacketDestination.CLIENT;
            case CLIENT -> PacketDestination.SERVER;
        };
    }

    public Env getSourceEnv() {
        return switch (this) {
            case SERVER -> Env.CLIENT;
            case CLIENT -> Env.SERVER;
        };
    }

    public Env getDestinationEnv() {
        return switch (this) {
            case SERVER -> Env.SERVER;
            case CLIENT -> Env.CLIENT;
        };
    }
}
