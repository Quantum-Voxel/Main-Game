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

package dev.ultreon.qvoxel.client.world;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.render.Mesh;
import dev.ultreon.qvoxel.client.shader.ShaderProgram;
import dev.ultreon.qvoxel.world.World;
import org.joml.Math;

public final class Sun extends CelestialBody {
    public Sun() {
        super(CommonConstants.id("sun"));
    }

    @Override
    public void render(ShaderProgram program, Mesh mesh) {
        ClientWorld world = QuantumClient.get().getWorld();
        if (world == null) return;

        float rotation = world.getTimeOfDay() * 360f / World.DAY_TIME;
        modelMatrix.identity();
        modelMatrix.rotateX(Math.toRadians(rotation));
        modelMatrix.translate(-7.5f, -7.5f, -50);
        modelMatrix.scale(15, 15, 0);

        super.render(program, mesh);
    }
}
