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

package dev.ultreon.qvoxel.item;

import dev.ultreon.libs.commons.v0.Identifier;
import dev.ultreon.qvoxel.CommonConstants;
import dev.ultreon.qvoxel.block.Blocks;
import dev.ultreon.qvoxel.registry.Registries;
import dev.ultreon.qvoxel.registry.RegistryHandle;
import dev.ultreon.qvoxel.registry.RegistryKeys;
import org.jetbrains.annotations.NotNull;

public class Items {
    public static final Item AIR = register("air", new Item(new Item.Settings()));
    public static final BlockItem GRASS_BLOCK = register("grass", new BlockItem(new Item.Settings(), () -> Blocks.GRASS_BLOCK));
    public static final BlockItem SNOWY_GRASS_BLOCK = register("snowy_grass", new BlockItem(new Item.Settings(), () -> Blocks.SNOWY_GRASS_BLOCK));
    public static final BlockItem DIRT = register("dirt", new BlockItem(new Item.Settings(), () -> Blocks.DIRT));
    public static final BlockItem SAND = register("sand", new BlockItem(new Item.Settings(), () -> Blocks.SAND));
    public static final BlockItem GRAVEL = register("gravel", new BlockItem(new Item.Settings(), () -> Blocks.GRAVEL));
    public static final BlockItem STONE = register("stone", new BlockItem(new Item.Settings(), () -> Blocks.STONE));
    public static final BlockItem COBBLESTONE = register("cobblestone", new BlockItem(new Item.Settings(), () -> Blocks.COBBLESTONE));
    public static final BlockItem SANDSTONE = register("sandstone", new BlockItem(new Item.Settings(), () -> Blocks.SANDSTONE));
    public static final BlockItem WATER = register("water", new BlockItem(new Item.Settings(), () -> Blocks.WATER));
    public static final BlockItem LAVA = register("lava", new BlockItem(new Item.Settings(), () -> Blocks.LAVA));
    public static final BlockItem LAVE = register("lave", new BlockItem(new Item.Settings(), () -> Blocks.LAVE));
    public static final BlockItem SHORT_GRASS = register("short_grass", new BlockItem(new Item.Settings(), () -> Blocks.SHORT_GRASS));
    public static final BlockItem SNOWY_SHORT_GRASS = register("snowy_short_grass", new BlockItem(new Item.Settings(), () -> Blocks.SNOWY_SHORT_GRASS));
    public static final BlockItem MAPLE_LOG = register("maple_log", new BlockItem(new Item.Settings(), () -> Blocks.MAPLE_LOG));
    public static final BlockItem MAPLE_LEAVES = register("maple_leaves", new BlockItem(new Item.Settings(), () -> Blocks.MAPLE_LEAVES));
    public static final BlockItem MAPLE_PLANKS = register("maple_planks", new BlockItem(new Item.Settings(), () -> Blocks.MAPLE_PLANKS));
    public static final BlockItem MESQUITE_LOG = register("mesquite_log", new BlockItem(new Item.Settings(), () -> Blocks.MESQUITE_LOG));
    public static final BlockItem MESQUITE_LEAVES = register("mesquite_leaves", new BlockItem(new Item.Settings(), () -> Blocks.MESQUITE_LEAVES));
    public static final BlockItem MESQUITE_PLANKS = register("mesquite_planks", new BlockItem(new Item.Settings(), () -> Blocks.MESQUITE_PLANKS));
    public static final BlockItem PINE_LOG = register("pine_log", new BlockItem(new Item.Settings(), () -> Blocks.PINE_LOG));
    public static final BlockItem PINE_LEAVES = register("pine_leaves", new BlockItem(new Item.Settings(), () -> Blocks.PINE_LEAVES));
    public static final BlockItem PINE_PLANKS = register("pine_planks", new BlockItem(new Item.Settings(), () -> Blocks.PINE_PLANKS));
    public static final BlockItem ASPEN_LOG = register("aspen_log", new BlockItem(new Item.Settings(), () -> Blocks.ASPEN_LOG));
    public static final BlockItem ASPEN_LEAVES = register("aspen_leaves", new BlockItem(new Item.Settings(), () -> Blocks.ASPEN_LEAVES));
    public static final BlockItem ASPEN_PLANKS = register("aspen_planks", new BlockItem(new Item.Settings(), () -> Blocks.ASPEN_PLANKS));
    public static final BlockItem MAHOGANY_LOG = register("mahogany_log", new BlockItem(new Item.Settings(), () -> Blocks.MAHOGANY_LOG));
    public static final BlockItem MAHOGANY_LEAVES = register("mahogany_leaves", new BlockItem(new Item.Settings(), () -> Blocks.MAHOGANY_LEAVES));
    public static final BlockItem MAHOGANY_PLANKS = register("mahogany_planks", new BlockItem(new Item.Settings(), () -> Blocks.MAHOGANY_PLANKS));
    public static final BlockItem CYPRESS_LOG = register("cypress_log", new BlockItem(new Item.Settings(), () -> Blocks.CYPRESS_LOG));
    public static final BlockItem CYPRESS_LEAVES = register("cypress_leaves", new BlockItem(new Item.Settings(), () -> Blocks.CYPRESS_LEAVES));
    public static final BlockItem CYPRESS_PLANKS = register("cypress_planks", new BlockItem(new Item.Settings(), () -> Blocks.CYPRESS_PLANKS));
    public static final BlockItem DOGWOOD_LOG = register("dogwood_log", new BlockItem(new Item.Settings(), () -> Blocks.DOGWOOD_LOG));
    public static final BlockItem DOGWOOD_LEAVES = register("dogwood_leaves", new BlockItem(new Item.Settings(), () -> Blocks.DOGWOOD_LEAVES));
    public static final BlockItem DOGWOOD_PLANKS = register("dogwood_planks", new BlockItem(new Item.Settings(), () -> Blocks.DOGWOOD_PLANKS));
    public static final BlockItem WILLOW_LOG = register("willow_log", new BlockItem(new Item.Settings(), () -> Blocks.WILLOW_LOG));
    public static final BlockItem WILLOW_LEAVES = register("willow_leaves", new BlockItem(new Item.Settings(), () -> Blocks.WILLOW_LEAVES));
    public static final BlockItem WILLOW_PLANKS = register("willow_planks", new BlockItem(new Item.Settings(), () -> Blocks.WILLOW_PLANKS));
    public static final BlockItem EUCALYPTUS_LOG = register("eucalyptus_log", new BlockItem(new Item.Settings(), () -> Blocks.EUCALYPTUS_LOG));
    public static final BlockItem EUCALYPTUS_LEAVES = register("eucalyptus_leaves", new BlockItem(new Item.Settings(), () -> Blocks.EUCALYPTUS_LEAVES));
    public static final BlockItem EUCALYPTUS_PLANKS = register("eucalyptus_planks", new BlockItem(new Item.Settings(), () -> Blocks.EUCALYPTUS_PLANKS));
    public static final BlockItem CEDAR_LOG = register("cedar_log", new BlockItem(new Item.Settings(), () -> Blocks.CEDAR_LOG));
    public static final BlockItem CEDAR_LEAVES = register("cedar_leaves", new BlockItem(new Item.Settings(), () -> Blocks.CEDAR_LEAVES));
    public static final BlockItem CEDAR_PLANKS = register("cedar_planks", new BlockItem(new Item.Settings(), () -> Blocks.CEDAR_PLANKS));
    public static final BlockItem IRON_ORE = register("iron_ore", new BlockItem(new Item.Settings(), () -> Blocks.IRON_ORE));
    public static final BlockItem SILVER_ORE = register("silver_ore", new BlockItem(new Item.Settings(), () -> Blocks.SILVER_ORE));
    public static final BlockItem TIN_ORE = register("tin_ore", new BlockItem(new Item.Settings(), () -> Blocks.TIN_ORE));
    public static final BlockItem LEAD_ORE = register("lead_ore", new BlockItem(new Item.Settings(), () -> Blocks.LEAD_ORE));
    public static final BlockItem ALUMINUM_ORE = register("aluminum_ore", new BlockItem(new Item.Settings(), () -> Blocks.ALUMINUM_ORE));
    public static final BlockItem NICKEL_ORE = register("nickel_ore", new BlockItem(new Item.Settings(), () -> Blocks.NICKEL_ORE));
    public static final BlockItem COPPER_ORE = register("copper_ore", new BlockItem(new Item.Settings(), () -> Blocks.COPPER_ORE));
    public static final BlockItem GOLD_ORE = register("gold_ore", new BlockItem(new Item.Settings(), () -> Blocks.GOLD_ORE));
    public static final BlockItem DIAMOND_ORE = register("diamond_ore", new BlockItem(new Item.Settings(), () -> Blocks.DIAMOND_ORE));
    public static final BlockItem TUNGSTEN_ORE = register("tungsten_ore", new BlockItem(new Item.Settings(), () -> Blocks.TUNGSTEN_ORE));
    public static final BlockItem PLATINUM_ORE = register("platinum_ore", new BlockItem(new Item.Settings(), () -> Blocks.PLATINUM_ORE));
    public static final BlockItem QUARTZ_ORE = register("quartz_ore", new BlockItem(new Item.Settings(), () -> Blocks.QUARTZ_ORE));
    public static final BlockItem OSMIUM_ORE = register("osmium_ore", new BlockItem(new Item.Settings(), () -> Blocks.OSMIUM_ORE));
    public static final BlockItem RUBY_ORE = register("ruby_ore", new BlockItem(new Item.Settings(), () -> Blocks.RUBY_ORE));
    public static final BlockItem EMERALD_ORE = register("emerald_ore", new BlockItem(new Item.Settings(), () -> Blocks.EMERALD_ORE));
    public static final BlockItem AMETHYST_ORE = register("amethyst_ore", new BlockItem(new Item.Settings(), () -> Blocks.AMETHYST_ORE));
    public static final BlockItem ICE = register("ice", new BlockItem(new Item.Settings(), () -> Blocks.ICE));
    public static final BlockItem SNOW_BLOCK = register("snow_block", new BlockItem(new Item.Settings(), () -> Blocks.SNOW_BLOCK));
    public static final BlockItem CACTUS = register("cactus", new BlockItem(new Item.Settings(), () -> Blocks.CACTUS));

    private static <T extends Item> T register(String name, T item) {
        Registries.ITEM.register(CommonConstants.id(name), item);
        return item;
    }

    public static Item get(@NotNull Identifier item) {
        return Registries.ITEM.get(item);
    }

    public static Item get(int rawId, RegistryHandle registryHandle) {
        return registryHandle.get(RegistryKeys.ITEM).byRawId(rawId);
    }

    public static void init() {

    }
}
