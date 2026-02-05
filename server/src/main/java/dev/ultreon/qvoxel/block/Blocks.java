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

package dev.ultreon.qvoxel.block;

import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.FoliageBlock;
import dev.ultreon.qvoxel.fluid.Fluids;
import dev.ultreon.qvoxel.registry.Registries;
import dev.ultreon.qvoxel.sound.SoundEvent;

public class Blocks {
    // Air/Void
    public static final Block AIR = register("air", new Block(new Block.Settings().air(true).collision(false).resistance(Float.POSITIVE_INFINITY)));
    public static final Block CAVE_AIR = register("cave_air", new Block(new Block.Settings().air(true).collision(false).resistance(Float.POSITIVE_INFINITY)));
    public static final Block VOID_BARRIER = register("void_barrier", new Block(new Block.Settings().air(true).resistance(Float.POSITIVE_INFINITY)));

    // Grass/Soil
    public static final Block GRASS_BLOCK = register("grass", new Block(new Block.Settings().strength(0.6f).stepSound(SoundEvent.STEP_GRASS)));
    public static final Block SNOWY_GRASS_BLOCK = register("snowy_grass", new Block(new Block.Settings().strength(0.6f).stepSound(SoundEvent.STEP_SNOW)));
    public static final Block DIRT = register("dirt", new Block(new Block.Settings().strength(0.5f).stepSound(SoundEvent.STEP_GRASS)));
    public static final Block SAND = register("sand", new Block(new Block.Settings().strength(0.5f).stepSound(SoundEvent.STEP_SAND)));
    public static final Block RED_SAND = register("red_sand", new Block(new Block.Settings().strength(0.5f).stepSound(SoundEvent.STEP_SAND)));
    public static final Block GRAVEL = register("gravel", new Block(new Block.Settings().strength(0.6f).stepSound(SoundEvent.STEP_SAND)));

    // Stone/Rock
    public static final Block STONE = register("stone", new Block(new Block.Settings().hardness(1.5f).resistance(10f).stepSound(SoundEvent.STEP_STONE)));
    public static final Block COBBLESTONE = register("cobblestone", new Block(new Block.Settings().hardness(2f).resistance(10f).stepSound(SoundEvent.STEP_STONE)));
    public static final Block SANDSTONE = register("sandstone", new Block(new Block.Settings().strength(8f).stepSound(SoundEvent.STEP_STONE)));

    // Liquids
    public static final Block WATER = register("water", new Block(new Block.Settings().collision(false).fluid(true).fluidFor(_ -> Fluids.WATER.getState(1000)).hardness(Float.NaN).resistance(180000000f).stepSound(SoundEvent.STEP_STONE)));
    public static final Block LAVA = register("lava", new Block(new Block.Settings().hardness(Float.NaN).resistance(18000f).lightEmission(0xFF9020).stepSound(SoundEvent.STEP_STONE)));
    public static final Block LAVE = register("lave", new LaveBlock(new Block.Settings().hardness(Float.NaN).resistance(18000f).lightEmission(0x2090FF).stepSound(SoundEvent.STEP_STONE)));

    // Foliage
    public static final Block SHORT_GRASS = register("short_grass", new FoliageBlock(new Block.Settings().collision(false).replaceable(true).ambientOcclusion(false).strength(0f).stepSound(SoundEvent.STEP_GRASS)));
    public static final Block SNOWY_SHORT_GRASS = register("snowy_short_grass", new FoliageBlock(new Block.Settings().collision(false).replaceable(true).ambientOcclusion(false).strength(0f).stepSound(SoundEvent.STEP_GRASS)));

    // Wood types
    public static final Block MAPLE_LOG = register("maple_log", new Block(new Block.Settings().strength(2f).stepSound(SoundEvent.STEP_WOOD)));
    public static final Block MAPLE_LEAVES = register("maple_leaves", new Block(new Block.Settings().strength(0.2f).stepSound(SoundEvent.STEP_GRASS).collision(false).climbable()));
    public static final Block MAPLE_PLANKS = register("maple_planks", new Block(new Block.Settings().strength(2f).stepSound(SoundEvent.STEP_WOOD)));
    public static final Block MESQUITE_LOG = register("mesquite_log", new Block(new Block.Settings().strength(2f).stepSound(SoundEvent.STEP_WOOD)));
    public static final Block MESQUITE_LEAVES = register("mesquite_leaves", new Block(new Block.Settings().strength(0.2f).stepSound(SoundEvent.STEP_GRASS).collision(false).climbable()));
    public static final Block MESQUITE_PLANKS = register("mesquite_planks", new Block(new Block.Settings().strength(2f).stepSound(SoundEvent.STEP_WOOD)));
    public static final Block PINE_LOG = register("pine_log", new Block(new Block.Settings().strength(2f).stepSound(SoundEvent.STEP_WOOD)));
    public static final Block PINE_LEAVES = register("pine_leaves", new Block(new Block.Settings().strength(0.2f).stepSound(SoundEvent.STEP_GRASS).collision(false).climbable()));
    public static final Block PINE_PLANKS = register("pine_planks", new Block(new Block.Settings().strength(2f).stepSound(SoundEvent.STEP_WOOD)));
    public static final Block ASPEN_LOG = register("aspen_log", new Block(new Block.Settings().strength(2f).stepSound(SoundEvent.STEP_WOOD)));
    public static final Block ASPEN_LEAVES = register("aspen_leaves", new Block(new Block.Settings().strength(0.2f).stepSound(SoundEvent.STEP_GRASS).collision(false).climbable()));
    public static final Block ASPEN_PLANKS = register("aspen_planks", new Block(new Block.Settings().strength(2f).stepSound(SoundEvent.STEP_WOOD)));
    public static final Block MAHOGANY_LOG = register("mahogany_log", new Block(new Block.Settings().strength(2f).stepSound(SoundEvent.STEP_WOOD)));
    public static final Block MAHOGANY_LEAVES = register("mahogany_leaves", new Block(new Block.Settings().strength(0.2f).stepSound(SoundEvent.STEP_GRASS).collision(false).climbable()));
    public static final Block MAHOGANY_PLANKS = register("mahogany_planks", new Block(new Block.Settings().strength(2f).stepSound(SoundEvent.STEP_WOOD)));
    public static final Block CYPRESS_LOG = register("cypress_log", new Block(new Block.Settings().strength(2f).stepSound(SoundEvent.STEP_WOOD)));
    public static final Block CYPRESS_LEAVES = register("cypress_leaves", new Block(new Block.Settings().strength(0.2f).stepSound(SoundEvent.STEP_GRASS).collision(false).climbable()));
    public static final Block CYPRESS_PLANKS = register("cypress_planks", new Block(new Block.Settings().strength(2f).stepSound(SoundEvent.STEP_WOOD)));
    public static final Block DOGWOOD_LOG = register("dogwood_log", new Block(new Block.Settings().strength(2f).stepSound(SoundEvent.STEP_WOOD)));
    public static final Block DOGWOOD_LEAVES = register("dogwood_leaves", new Block(new Block.Settings().strength(0.2f).stepSound(SoundEvent.STEP_GRASS).collision(false).climbable()));
    public static final Block DOGWOOD_PLANKS = register("dogwood_planks", new Block(new Block.Settings().strength(2f).stepSound(SoundEvent.STEP_WOOD)));
    public static final Block WILLOW_LOG = register("willow_log", new Block(new Block.Settings().strength(2f).stepSound(SoundEvent.STEP_WOOD)));
    public static final Block WILLOW_LEAVES = register("willow_leaves", new Block(new Block.Settings().strength(0.2f).stepSound(SoundEvent.STEP_GRASS).collision(false).climbable()));
    public static final Block WILLOW_PLANKS = register("willow_planks", new Block(new Block.Settings().strength(2f).stepSound(SoundEvent.STEP_WOOD)));
    public static final Block EUCALYPTUS_LOG = register("eucalyptus_log", new Block(new Block.Settings().strength(2f).stepSound(SoundEvent.STEP_WOOD)));
    public static final Block EUCALYPTUS_LEAVES = register("eucalyptus_leaves", new Block(new Block.Settings().strength(0.2f).stepSound(SoundEvent.STEP_GRASS).collision(false).climbable()));
    public static final Block EUCALYPTUS_PLANKS = register("eucalyptus_planks", new Block(new Block.Settings().strength(2f).stepSound(SoundEvent.STEP_WOOD)));
    public static final Block CEDAR_LOG = register("cedar_log", new Block(new Block.Settings().strength(2f).stepSound(SoundEvent.STEP_WOOD)));
    public static final Block CEDAR_LEAVES = register("cedar_leaves", new Block(new Block.Settings().strength(0.2f).stepSound(SoundEvent.STEP_GRASS).collision(false).climbable()));
    public static final Block CEDAR_PLANKS = register("cedar_planks", new Block(new Block.Settings().strength(2f).stepSound(SoundEvent.STEP_WOOD)));
    public static final Block OAK_LOG = register("oak_log", new Block(new Block.Settings().strength(2f).stepSound(SoundEvent.STEP_WOOD)));
    public static final Block OAK_LEAVES = register("oak_leaves", new Block(new Block.Settings().strength(0.2f).stepSound(SoundEvent.STEP_GRASS).collision(false).climbable()));
    public static final Block OAK_PLANKS = register("oak_planks", new Block(new Block.Settings().strength(2f).stepSound(SoundEvent.STEP_WOOD)));

    // Ores
    public static final Block IRON_ORE = register("iron_ore", new Block(new Block.Settings().strength(15f).stepSound(SoundEvent.STEP_STONE)));
    public static final Block SILVER_ORE = register("silver_ore", new Block(new Block.Settings().strength(15f).stepSound(SoundEvent.STEP_STONE)));
    public static final Block TIN_ORE = register("tin_ore", new Block(new Block.Settings().strength(15f).stepSound(SoundEvent.STEP_STONE)));
    public static final Block LEAD_ORE = register("lead_ore", new Block(new Block.Settings().strength(28f).stepSound(SoundEvent.STEP_STONE)));
    public static final Block ALUMINUM_ORE = register("aluminum_ore", new Block(new Block.Settings().strength(20f).stepSound(SoundEvent.STEP_STONE)));
    public static final Block NICKEL_ORE = register("nickel_ore", new Block(new Block.Settings().strength(18f).stepSound(SoundEvent.STEP_STONE)));
    public static final Block COPPER_ORE = register("copper_ore", new Block(new Block.Settings().strength(14f).stepSound(SoundEvent.STEP_STONE)));
    public static final Block GOLD_ORE = register("gold_ore", new Block(new Block.Settings().strength(12f).stepSound(SoundEvent.STEP_STONE)));
    public static final Block DIAMOND_ORE = register("diamond_ore", new Block(new Block.Settings().strength(38f).stepSound(SoundEvent.STEP_STONE)));
    public static final Block TUNGSTEN_ORE = register("tungsten_ore", new Block(new Block.Settings().strength(45f).stepSound(SoundEvent.STEP_STONE)));
    public static final Block PLATINUM_ORE = register("platinum_ore", new Block(new Block.Settings().strength(42f).stepSound(SoundEvent.STEP_STONE)));
    public static final Block QUARTZ_ORE = register("quartz_ore", new Block(new Block.Settings().strength(30f).stepSound(SoundEvent.STEP_STONE)));
    public static final Block OSMIUM_ORE = register("osmium_ore", new Block(new Block.Settings().strength(35f).stepSound(SoundEvent.STEP_STONE)));
    public static final Block RUBY_ORE = register("ruby_ore", new Block(new Block.Settings().strength(23f).stepSound(SoundEvent.STEP_STONE)));
    public static final Block EMERALD_ORE = register("emerald_ore", new Block(new Block.Settings().strength(23f).stepSound(SoundEvent.STEP_STONE)));
    public static final Block AMETHYST_ORE = register("amethyst_ore", new Block(new Block.Settings().strength(23f).stepSound(SoundEvent.STEP_STONE)));

    // Misc
    public static final Block ICE = register("ice", new Block(new Block.Settings().strength(0.1f).stepSound(SoundEvent.STEP_STONE)));
    public static final Block SNOW_BLOCK = register("snow_block", new Block(new Block.Settings().strength(0.1f).stepSound(SoundEvent.STEP_SNOW)));
    public static final Block CACTUS = register("cactus", new Block(new Block.Settings().transparent(true).strength(0.4f).stepSound(SoundEvent.STEP_GRASS)));

    private static Block register(String name, Block block) {
        return Registries.BLOCK.register(CommonConstants.id(name), block);
    }

    public static void init() {

    }
}
