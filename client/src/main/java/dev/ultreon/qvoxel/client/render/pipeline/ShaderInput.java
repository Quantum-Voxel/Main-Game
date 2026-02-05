package dev.ultreon.qvoxel.client.render.pipeline;

import dev.ultreon.qvoxel.client.shader.ShaderProgram;
import dev.ultreon.qvoxel.client.world.ClientPlayerEntity;

@FunctionalInterface
public interface ShaderInput {
    void apply(ClientPlayerEntity player, ShaderProgram program, String uniformName);
}
