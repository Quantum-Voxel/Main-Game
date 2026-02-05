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

package dev.ultreon.qvoxel.client.gui.screen;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.client.gui.Screen;

import java.util.Set;

public class MissingRegistriesScreen extends Screen {
    public MissingRegistriesScreen(Set<Identifier> client, Set<Identifier> server) {
        super("Missing Registries");
    }

    @Override
    public void init() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void resized() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
