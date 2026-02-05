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

package dev.ultreon.qvoxel.registry;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.block.Block;
import dev.ultreon.qvoxel.block.actor.BlockActor;
import dev.ultreon.qvoxel.block.actor.BlockActorFactory;
import dev.ultreon.qvoxel.fluid.Fluid;
import dev.ultreon.qvoxel.item.Item;
import dev.ultreon.qvoxel.menu.MenuType;
import dev.ultreon.qvoxel.particle.ParticleType;
import dev.ultreon.qvoxel.sound.SoundEvent;
import dev.ultreon.qvoxel.world.DimensionInfo;
import dev.ultreon.qvoxel.world.gen.biome.Biome;
import dev.ultreon.qvoxel.world.gen.chunk.ChunkGenerator;
import dev.ultreon.qvoxel.world.gen.noise.NoiseConfig;

public class RegistryKeys {
    public static final RegistryKey<Registry<Registry<?>>> REGISTRY = RegistryKey.registry(CommonConstants.id("registry"));
    public static final RegistryKey<Registry<Block>> BLOCK = RegistryKey.registry(CommonConstants.id("block"));
    public static final RegistryKey<Registry<BlockActorFactory<?>>> BLOCK_ACTOR = RegistryKey.registry(CommonConstants.id("block_actor"));
    public static final RegistryKey<Registry<Item>> ITEM = RegistryKey.registry(CommonConstants.id("item"));
    public static final RegistryKey<Registry<Biome>> BIOME = RegistryKey.registry(CommonConstants.id("biome"));
    public static final RegistryKey<Registry<ChunkGenerator>> CHUNK_GENERATOR = RegistryKey.registry(CommonConstants.id("chunk_generator"));
    public static final RegistryKey<Registry<NoiseConfig>> NOISE_CONFIG = RegistryKey.registry(CommonConstants.id("noise_config"));
    public static final RegistryKey<Registry<DimensionInfo>> DIMENSION = RegistryKey.registry(CommonConstants.id("dimension"));
    public static final RegistryKey<Registry<MenuType<?>>> MENU_TYPE = RegistryKey.registry(CommonConstants.id("menu_type"));
    public static final RegistryKey<Registry<SoundEvent>> SOUND_EVENT = RegistryKey.registry(CommonConstants.id("sound_event"));
    public static final RegistryKey<Registry<ParticleType<?>>> PARTICLE_TYPE = RegistryKey.registry(CommonConstants.id("particle_type"));
    public static final RegistryKey<Registry<Fluid>> FLUID = RegistryKey.registry(CommonConstants.id("fluid"));
}
