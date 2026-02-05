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

package dev.ultreon.qvoxel.client.world;

import dev.ultreon.qvoxel.client.render.Color;
import dev.ultreon.qvoxel.client.shader.ShaderProgram;
import dev.ultreon.qvoxel.resource.GameObject;
import org.joml.Math;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class SkyboxRenderer extends GameObject {
    private final WorldRenderer worldRenderer;
    public final Color topColorDay = new Color(0.5f, 0.64f, 0.985f);
    public final Color midColorDay = new Color(0.75f, 0.825f, 0.945f);
    public final Color bottomColorDay = new Color(0.75f, 0.825f, 0.945f);
    public final Color topColorNight = new Color(0.01f, 0.02f, 0.053f);
    public final Color midColorNight = new Color(0.01f, 0.01f, 0.01f);
    public final Color bottomColorNight = new Color(0.01f, 0.01f, 0.01f);
    public final Color topColor = new Color();
    public final Color midColor = new Color();
    public final Color bottomColor = new Color();
    private final List<CelestialBody> celestialBodies = new ArrayList<>();
    private final Color oldTopColor = new Color(topColor);
    private final Color oldMidColor = new Color(midColor);
    private final Color oldBottomColor = new Color(bottomColor);
    private final Vector3d playerPos = new Vector3d();

    public SkyboxRenderer(WorldRenderer worldRenderer) {
        this.worldRenderer = worldRenderer;

        celestialBodies.add(new Sun());
        celestialBodies.add(new Moon());

        scale.set(20, 20, 20);
    }

    public void render(ClientPlayerEntity player, float partialTicks) {
        ClientWorld world = worldRenderer.getWorld();
        if (world == null) {
            return;
        }
        float globalSunLight = world.getGlobalSunLight(partialTicks);

        lerp(topColor, topColorNight, topColorDay, globalSunLight);
        lerp(midColor, midColorNight, midColorDay, globalSunLight);
        lerp(bottomColor, bottomColorNight, bottomColorDay, globalSunLight);

        if (player != null) {
            super.update(player.getPosition(playerPos, partialTicks));
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glFrontFace(GL11.GL_CCW);
        GL11.glDepthMask(false);
        modelMatrix.identity();
        modelMatrix.translate(worldRenderer.getCamera().position);
        if (player != null) {
            modelMatrix.rotateY(Math.toRadians(-player.yawHead));
        }
        modelMatrix.translate(-0.5f, -0.5f, -0.5f);

        ShaderProgram skyboxShader = worldRenderer.getSkyboxProgram();
        ShaderProgram celestialBodyShader = worldRenderer.getCelestialBodyProgram();
        if (skyboxShader == null || celestialBodyShader == null) {
            return;
        }
        skyboxShader.use();
        skyboxShader.setUniform("view", worldRenderer.getCamera().getViewMatrix());
        skyboxShader.setUniform("projection", worldRenderer.getCamera().getProjectionMatrix());
        skyboxShader.setUniform("model", modelMatrix);
        skyboxShader.setUniform("topColor", topColor);
        skyboxShader.setUniform("midColor", midColor);
        skyboxShader.setUniform("bottomColor", bottomColor);
        worldRenderer.getSkyboxMesh().render(skyboxShader);

        GL11.glFrontFace(GL11.GL_CW);
        celestialBodyShader.use();
        celestialBodyShader.setUniform("viewMatrix", worldRenderer.getCamera().getViewMatrix());
        celestialBodyShader.setUniform("projectionMatrix", worldRenderer.getCamera().getProjectionMatrix());
        for (var celestialBody : celestialBodies) {
            celestialBody.render(celestialBodyShader, worldRenderer.getCelestialMesh());
        }

        GL11.glDepthMask(true);
        GL11.glFrontFace(GL11.GL_CW);
    }

    private void lerp(Color out, Color zero, Color one, float value) {
        out.set(
                Math.lerp(zero.r, one.r, value),
                Math.lerp(zero.g, one.g, value),
                Math.lerp(zero.b, one.b, value),
                Math.lerp(zero.a, one.a, value)
        );
    }
}
