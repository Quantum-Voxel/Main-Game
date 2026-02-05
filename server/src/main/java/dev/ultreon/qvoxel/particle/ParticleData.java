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
import org.joml.Vector3d;
import org.joml.Vector3f;

public class ParticleData {
    public static final ParticleSerializer<ParticleData> SERIALIZER = new ParticleSerializer<>() {
        @Override
        public void toBytes(ParticleData data, PacketIO buffer) {
            buffer.writeDouble(data.position.x)
                    .writeDouble(data.position.y)
                    .writeDouble(data.position.z)
                    .writeFloat(data.minSpeed)
                    .writeFloat(data.maxSpeed)
                    .writeFloat(data.minSize)
                    .writeFloat(data.maxSize);
        }

        @Override
        public ParticleData fromBytes(PacketIO buffer) {
            return new ParticleData(buffer);
        }
    };

    public final Vector3d position = new Vector3d();
    public Vector3f delta = new Vector3f(0.1f, 0.1f, 0.1f);
    public float minSpeed = 0.01f;
    public float maxSpeed = 0.1f;
    public float minSize = 0.1f;
    public float maxSize = 0.1f;
    public long ttl = 1000;
    public Object userData = null;

    public ParticleData() {

    }

    public ParticleData(PacketIO buffer) {
        position.set(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        minSpeed = buffer.readFloat();
        maxSpeed = buffer.readFloat();
        minSize = buffer.readFloat();
        maxSize = buffer.readFloat();
    }
}
