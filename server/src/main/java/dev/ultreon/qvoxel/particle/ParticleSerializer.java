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

import dev.ultreon.qvoxel.network.PacketIO;

public interface ParticleSerializer<T extends ParticleData> {
    /**
     * Serializes the particle data into a byte buffer.
     *
     * @param buffer The {@link PacketIO} object that will contain the serialized particle data.
     */
    void toBytes(T data, PacketIO buffer);

    /**
     * Deserializes the particle data from a byte buffer.
     *
     * @param buffer The {@link PacketIO} object that contains the serialized particle data.
     */
    T fromBytes(PacketIO buffer);
}
