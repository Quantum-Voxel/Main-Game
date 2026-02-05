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

package dev.ultreon.qvoxel.world.gen.noise;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.registry.RegistryKey;
import dev.ultreon.qvoxel.registry.RegistryKeys;
import dev.ultreon.qvoxel.resource.GameNode;
import dev.ultreon.qvoxel.server.QuantumServer;
import org.joml.Vector2f;

public final class NoiseConfigs extends GameNode {
    public static final RegistryKey<NoiseConfig> EMPTY = RegistryKey.of(RegistryKeys.NOISE_CONFIG, new Identifier("empty"));
    public static final RegistryKey<NoiseConfig> BIOME_MAP = RegistryKey.of(RegistryKeys.NOISE_CONFIG, new Identifier("biome_map"));

    public final NoiseConfig empty;
    public final NoiseConfig genericNoise;
    public final NoiseConfig tree;
    public final NoiseConfig cacti;
    public final NoiseConfig rock;
    public final NoiseConfig patch;
    public final NoiseConfig waterPatch1;
    public final NoiseConfig waterPatch2;
    public final NoiseConfig biomeX;
    public final NoiseConfig biomeY;
    public final NoiseConfig layerX;
    public final NoiseConfig layerY;
    public final NoiseConfig biomeMap;
    public final NoiseConfig foliage;
    public final NoiseConfig ore;
    private final QuantumServer server;

    public NoiseConfigs(QuantumServer server) {
        this.server = server;

        empty = register(EMPTY,
                new NoiseConfig(0, 0, new Vector2f(), 0, 0, 0, 0, 0, 0));
        genericNoise = register("plains",
                new NoiseConfig(0.1f, 3, new Vector2f(330462, 631774), 196977, .35f, .001f, 1f, 30, 64));
        tree = register("tree",
                new NoiseConfig(0.01f, 1, new Vector2f(946664, 61722), 497395, 0.01f, 1.2f, 4f, 1, 0));
        cacti = register("cacti",
                new NoiseConfig(0.01f, 1, new Vector2f(832630, 85618), 343406, 0.01f, 1.2f, 4f, 1, 0));
        rock = register("rock",
                new NoiseConfig(0.01f, 1, new Vector2f(946664, 61722), 581404, 0.01f, 1.2f, 4f, 1, 0));
        patch = register("patch",
                new NoiseConfig(0.01f, 5, new Vector2f(680875, 914213), 425023, 0.5f, 0.25f, 0.635f, 1, 0.5f));
        waterPatch1 = register("water_patch_1",
                new NoiseConfig(0.01f, 5, new Vector2f(680875, 914213), 789874, 0.5f, 0.25f, 0.635f, 1, 0.5f));
        waterPatch2 = register("water_patch_2",
                new NoiseConfig(0.01f, 5, new Vector2f(680875, 914213), 266785, 0.5f, 0.25f, 0.635f, 1, 0.5f));
        biomeX = register("biome_x",
                new NoiseConfig(0.001f, 4, new Vector2f(69400, 35350), 998334, 0.6f, 1.2f, 5f, 30, 60));
        biomeY = register("biome_y",
                new NoiseConfig(0.001f, 4, new Vector2f(35900, 15900), 985449, 0.6f, 1.2f, 5f, 30, 60));
        layerX = register("layer_x",
                new NoiseConfig(0.01f, 4, new Vector2f(69400, 35350), 998334, 0.6f, 1.2f, 5f, 30, 60));
        layerY = register("layer_y",
                new NoiseConfig(0.01f, 4, new Vector2f(35900, 15900), 985449, 0.6f, 1.2f, 5f, 30, 60));
        biomeMap = register(BIOME_MAP,
                new NoiseConfig(0.02f, 8, new Vector2f(903852, 493382), 137339, 0.6f, 2.0f, 5f, 10, 0));
        foliage = register("foliage",
                new NoiseConfig(1f, 1, new Vector2f(652748, 695825), 297418, 0.5f, 2.0f, 1f, 2, 0));
        ore = register("ore",
                new NoiseConfig(1f, 3, new Vector2f(867470, 115558), 930208, 0.5f, 2.0f, 1f, 2, 0));
    }

    private <T extends NoiseConfig> T register(RegistryKey<NoiseConfig> key, T settings) {
        server.getRegistries().get(RegistryKeys.NOISE_CONFIG).register(key, settings);
        return settings;
    }

    private <T extends NoiseConfig> T register(String name, T settings) {
        server.getRegistries().get(RegistryKeys.NOISE_CONFIG).register(CommonConstants.id(name), settings);
        return settings;
    }
}
