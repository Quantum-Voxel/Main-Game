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

import dev.ultreon.qvoxel.block.Block;
import dev.ultreon.qvoxel.block.actor.BlockActorFactory;
import dev.ultreon.qvoxel.fluid.Fluid;
import dev.ultreon.qvoxel.item.Item;
import dev.ultreon.qvoxel.menu.MenuType;
import dev.ultreon.qvoxel.particle.ParticleType;
import dev.ultreon.qvoxel.sound.SoundEvent;

public class Registries {
    public static final Registry<Registry<?>> REGISTRY = new SimpleRegistry.Builder<>(RegistryKeys.REGISTRY).build();
    public static final Registry<Block> BLOCK = new SimpleRegistry.Builder<>(RegistryKeys.BLOCK).build();
    public static final Registry<BlockActorFactory<?>> BLOCK_ACTOR = new SimpleRegistry.Builder<>(RegistryKeys.BLOCK_ACTOR).build();
    public static final Registry<Item> ITEM = new SimpleRegistry.Builder<>(RegistryKeys.ITEM).build();
    public static final Registry<MenuType<?>> MENU_TYPE = new SimpleRegistry.Builder<>(RegistryKeys.MENU_TYPE).build();
    public static final Registry<SoundEvent> SOUND_EVENT = new SimpleRegistry.Builder<>(RegistryKeys.SOUND_EVENT).build();
    public static final Registry<ParticleType<?>> PARTICLE_TYPE = new SimpleRegistry.Builder<>(RegistryKeys.PARTICLE_TYPE).build();
    public static final Registry<Fluid> FLUID = new SimpleRegistry.Builder<>(RegistryKeys.FLUID).build();

    static {
        REGISTRY.register((RegistryKey) RegistryKeys.REGISTRY, REGISTRY);
        REGISTRY.register((RegistryKey) RegistryKeys.BLOCK, BLOCK);
        REGISTRY.register((RegistryKey) RegistryKeys.BLOCK_ACTOR, BLOCK_ACTOR);
        REGISTRY.register((RegistryKey) RegistryKeys.ITEM, ITEM);
        REGISTRY.register((RegistryKey) RegistryKeys.MENU_TYPE, MENU_TYPE);
        REGISTRY.register((RegistryKey) RegistryKeys.SOUND_EVENT, SOUND_EVENT);
        REGISTRY.register((RegistryKey) RegistryKeys.PARTICLE_TYPE, PARTICLE_TYPE);
        REGISTRY.register((RegistryKey) RegistryKeys.FLUID, FLUID);
    }
}
