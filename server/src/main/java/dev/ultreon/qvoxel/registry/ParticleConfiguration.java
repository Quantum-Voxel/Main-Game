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

package dev.ultreon.qvoxel.registry;

import dev.ultreon.qvoxel.ValueSource;

public class ParticleConfiguration {
    public ValueSource quantity = ValueSource.fixed(1);
    public ValueSource x = ValueSource.fixed(0);
    public ValueSource y = ValueSource.fixed(0);
    public ValueSource z = ValueSource.fixed(0);
    public ValueSource deltaX = ValueSource.fixed(0);
    public ValueSource deltaY = ValueSource.fixed(0);
    public ValueSource deltaZ = ValueSource.fixed(0);
    public ValueSource velocityX = ValueSource.fixed(0);
    public ValueSource velocityY = ValueSource.fixed(0);
    public ValueSource velocityZ = ValueSource.fixed(0);
    public ValueSource scaleX = ValueSource.fixed(1);
    public ValueSource scaleY = ValueSource.fixed(1);
    public ValueSource scaleZ = ValueSource.fixed(1);
    public ValueSource ttl = ValueSource.fixed(1000);
}
