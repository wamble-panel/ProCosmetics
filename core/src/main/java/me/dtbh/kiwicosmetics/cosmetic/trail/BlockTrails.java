/*
 * This file is part of KiwiCosmetics, a fork of ProCosmetics - https://github.com/FilleDev/ProCosmetics
 * Copyright (C) 2026 FilleDev and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.dtbh.kiwicosmetics.cosmetic.trail;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.util.ParticleUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * All block trails. Blocks are looked up by name so trails using newer blocks (cinnabar, sulfur,
 * resin, pale garden) fall back to similar blocks on versions that don't have them.
 */
public final class BlockTrails {

    private static final List<BlockData> RAINBOW = blocks("red_wool", "orange_wool", "yellow_wool", "lime_wool",
            "light_blue_wool", "blue_wool", "purple_wool", "magenta_wool");
    private static final List<BlockData> PASTEL = blocks("pink_wool", "white_wool", "light_blue_wool", "lime_wool",
            "yellow_wool", "magenta_wool");
    private static final List<BlockData> CANDY_CANE = blocks("red_concrete", "white_concrete");
    private static final List<BlockData> FROST = blocks("packed_ice", "blue_ice", "snow_block", "ice");
    private static final List<BlockData> MAGMA = blocks("magma_block", "magma_block", "blackstone");
    private static final List<BlockData> GOLDEN = blocks("gold_block", "raw_gold_block", "yellow_glazed_terracotta");
    private static final List<BlockData> GEMSTONE = blocks("diamond_block", "emerald_block", "lapis_block", "amethyst_block");
    private static final List<BlockData> NETHER = blocks("crimson_nylium", "warped_nylium", "nether_wart_block", "warped_wart_block");
    private static final List<BlockData> OCEAN = blocks("prismarine", "prismarine_bricks", "dark_prismarine", "sea_lantern");
    private static final List<BlockData> GALAXY = blocks("black_concrete", "purple_concrete", "obsidian", "crying_obsidian");
    private static final List<BlockData> SCULK = blocks("sculk", "sculk", "sculk_catalyst");
    private static final List<BlockData> COPPER = blocks("copper_block", "exposed_copper", "weathered_copper", "oxidized_copper");
    private static final List<BlockData> CHERRY = blocks("cherry_leaves", "pink_concrete_powder", "cherry_planks");
    private static final List<BlockData> AMETHYST = blocks("amethyst_block", "budding_amethyst", "purpur_block");
    private static final List<BlockData> HONEY = blocks("honeycomb_block", "orange_terracotta", "yellow_terracotta");
    private static final List<BlockData> CINNABAR = blocksNamed("cinnabar", "red_nether_bricks", "redstone_block", "red_terracotta");
    private static final List<BlockData> SULFUR = blocksNamed("sulfur", "yellow_terracotta", "yellow_concrete_powder", "end_stone");
    private static final List<BlockData> RESIN = orElse(blocks("resin_block", "resin_bricks", "chiseled_resin_bricks"),
            "orange_terracotta", "orange_concrete");
    private static final List<BlockData> PALE_GARDEN = orElse(blocks("pale_moss_block", "pale_oak_planks", "pale_oak_leaves"),
            "white_concrete", "light_gray_concrete");

    private BlockTrails() {
    }

    /**
     * Looks up the blocks now (on startup) so the log shows which cinnabar/sulfur blocks were found.
     */
    public static void load() {
        // Loading the class runs the static block lookups above
    }

    public static BlockTrail rainbow() {
        return new BlockTrail(RAINBOW, shapes(RAINBOW), false, true, null);
    }

    public static BlockTrail pastel() {
        return new BlockTrail(PASTEL, shapes(PASTEL), false, true, Particle.HAPPY_VILLAGER);
    }

    public static BlockTrail candyCane() {
        return new BlockTrail(CANDY_CANE, shapes(CANDY_CANE), false, true, null);
    }

    public static BlockTrail frost() {
        return new BlockTrail(FROST, shapes(FROST), true, false, Particle.SNOWFLAKE);
    }

    public static BlockTrail magma() {
        return new BlockTrail(MAGMA, shapes(MAGMA), false, false, Particle.SMALL_FLAME);
    }

    public static BlockTrail golden() {
        return new BlockTrail(GOLDEN, shapes(GOLDEN), false, false, Particle.WAX_ON);
    }

    public static BlockTrail gemstone() {
        return new BlockTrail(GEMSTONE, shapes(GEMSTONE), false, false, Particle.ELECTRIC_SPARK);
    }

    public static BlockTrail nether() {
        return new BlockTrail(NETHER, shapes(NETHER), true, false, Particle.CRIMSON_SPORE);
    }

    public static BlockTrail ocean() {
        return new BlockTrail(OCEAN, shapes(OCEAN), false, false, Particle.DRIPPING_WATER);
    }

    public static BlockTrail galaxy() {
        return new BlockTrail(GALAXY, shapes(GALAXY), false, false, Particle.END_ROD);
    }

    public static BlockTrail sculk() {
        return new BlockTrail(SCULK, shapes(SCULK), true, false, Particle.SCULK_SOUL);
    }

    public static BlockTrail copper() {
        return new BlockTrail(COPPER, shapes(COPPER, "cut_copper", "exposed_cut_copper", "weathered_cut_copper", "oxidized_cut_copper"), false, true, null);
    }

    public static BlockTrail cherry() {
        return new BlockTrail(CHERRY, shapes(CHERRY), true, false, Particle.CHERRY_LEAVES);
    }

    public static BlockTrail amethyst() {
        return new BlockTrail(AMETHYST, shapes(AMETHYST), false, false, Particle.END_ROD);
    }

    public static BlockTrail honey() {
        return new BlockTrail(HONEY, shapes(HONEY), false, false, Particle.FALLING_HONEY);
    }

    public static BlockTrail cinnabar() {
        return new BlockTrail(CINNABAR, shapes(CINNABAR), false, false, Particle.CRIMSON_SPORE);
    }

    public static BlockTrail sulfur() {
        return new BlockTrail(SULFUR, shapes(SULFUR), false, false, ParticleUtil.find("SULFUR_BUBBLES", Particle.WAX_OFF));
    }

    public static BlockTrail resin() {
        return new BlockTrail(RESIN, shapes(RESIN), false, false, null);
    }

    public static BlockTrail paleGarden() {
        return new BlockTrail(PALE_GARDEN, shapes(PALE_GARDEN), true, false, Particle.WHITE_ASH);
    }

    /**
     * Blocks by name, skipping names this server version doesn't know.
     */
    private static List<BlockData> blocks(String... names) {
        List<BlockData> result = new ArrayList<>();

        for (String name : names) {
            Material material = Material.matchMaterial(name);

            if (material != null && material.isBlock()) {
                result.add(material.createBlockData());
            }
        }
        return List.copyOf(result);
    }

    /**
     * Slab and stair names for a trail, only from its own blocks ({@code cherry_planks} gives
     * {@code cherry}, {@code prismarine_bricks} gives {@code prismarine_brick}), plus other cuts of the
     * same block. Trails of wool, concrete or ice have no slabs or stairs in vanilla, so they leave
     * slabs and stairs alone; if a version adds e.g. {@code red_wool_slab}, it is used automatically.
     */
    private static List<String> shapes(List<BlockData> blocks, String... sameBlock) {
        List<String> result = new ArrayList<>();

        for (BlockData data : blocks) {
            String name = data.getMaterial().getKey().getKey();

            if (name.endsWith("_planks")) {
                name = name.substring(0, name.length() - "_planks".length());
            } else if (name.endsWith("_block")) {
                name = name.substring(0, name.length() - "_block".length());
            } else if (name.endsWith("bricks") || name.endsWith("tiles")) {
                name = name.substring(0, name.length() - 1);
            }
            if (!result.contains(name)) {
                result.add(name);
            }
        }
        result.addAll(List.of(sameBlock));
        return result;
    }

    private static List<BlockData> orElse(List<BlockData> blocks, String... fallback) {
        return blocks.isEmpty() ? blocks(fallback) : blocks;
    }

    /**
     * Every full block whose name contains {@code keyword} (e.g. all cinnabar blocks of the version),
     * or the fallback blocks when there are none.
     */
    private static List<BlockData> blocksNamed(String keyword, String... fallback) {
        String upper = keyword.toUpperCase(Locale.ROOT);
        List<BlockData> result = new ArrayList<>();

        for (Material material : Material.values()) {
            if (!material.name().startsWith("LEGACY_") && material.name().contains(upper)
                    && material.isBlock() && material.isSolid() && material.isOccluding()) {
                result.add(material.createBlockData());
            }
        }

        if (result.isEmpty()) {
            KiwiCosmeticsPlugin.getPlugin().getLogger().info("No " + keyword + " blocks on this version, the " + keyword + " trail uses similar blocks instead.");
            return blocks(fallback);
        }
        KiwiCosmeticsPlugin.getPlugin().getLogger().info("The " + keyword + " trail uses: " + result.stream()
                .map(data -> data.getMaterial().getKey().getKey()).toList());
        return List.copyOf(result);
    }
}
