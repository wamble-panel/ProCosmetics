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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.jetbrains.annotations.Nullable;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticContext;
import me.dtbh.kiwicosmetics.api.cosmetic.particleeffect.ParticleEffectBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.particleeffect.ParticleEffectType;
import me.dtbh.kiwicosmetics.util.MathUtil;
import me.dtbh.kiwicosmetics.util.ParticleUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns the ground you walk on into other blocks for a few seconds. The blocks are fake
 * (client-side, shown to everyone nearby), the real world never changes, and special blocks
 * like chests, doors and beds are skipped by the {@link me.dtbh.kiwicosmetics.util.block.FakeBlockManager}.
 * Slabs and stairs become a slab or stair of one of the trail's own blocks with the same shape;
 * trails whose blocks have no slabs or stairs (wool, concrete, ice) leave them alone.
 */
public class BlockTrail implements ParticleEffectBehavior {

    private static final KiwiCosmeticsPlugin PLUGIN = KiwiCosmeticsPlugin.getPlugin();
    private static final BlockFace[] SIDES = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
    private static final long DEFAULT_DURATION_TICKS = 60L;

    private final List<BlockData> blocks;
    private final List<Material> slabs = new ArrayList<>();
    private final List<Material> stairs = new ArrayList<>();
    private final boolean wide;
    private final boolean cycle;
    private final Particle particle;
    private final Location below = new Location(null, 0.0d, 0.0d, 0.0d);
    private long durationTicks = DEFAULT_DURATION_TICKS;
    private int index;
    private int ticks;

    /**
     * @param blocks     the blocks to show
     * @param shapeBases block names used on slabs and stairs, {@code <base>_slab} and {@code <base>_stairs}
     * @param wide       also change the four blocks around the one you stand on
     * @param cycle      go through the blocks in order (rainbows) instead of picking at random
     * @param particle   optional particle at your feet while walking
     */
    public BlockTrail(List<BlockData> blocks, List<String> shapeBases, boolean wide, boolean cycle, @Nullable Particle particle) {
        this.blocks = blocks;
        this.wide = wide;
        this.cycle = cycle;
        this.particle = particle;

        for (String base : shapeBases) {
            addShape(slabs, base + "_slab", Slab.class);
            addShape(stairs, base + "_stairs", Stairs.class);
        }
    }

    private static void addShape(List<Material> shapes, String name, Class<? extends BlockData> type) {
        Material material = Material.matchMaterial(name);

        if (material != null && material.isBlock() && !shapes.contains(material)
                && type.isInstance(material.createBlockData())) {
            shapes.add(material);
        }
    }

    @Override
    public void onEquip(CosmeticContext<ParticleEffectType> context) {
        long configured = context.getType().getCategory().getConfig().getInt("block_duration_ticks");
        durationTicks = configured > 0 ? configured : DEFAULT_DURATION_TICKS;
    }

    @Override
    public void onUpdate(CosmeticContext<ParticleEffectType> context, Location location) {
        if (blocks.isEmpty() || !context.getUser().isMoving()) {
            return;
        }
        below.setWorld(location.getWorld());
        below.setX(location.getX());
        below.setY(location.getY() - 0.2d);
        below.setZ(location.getZ());
        Block ground = below.getBlock();

        // In the air (jumping, flying): nothing to paint
        if (!ground.getType().isSolid()) {
            return;
        }
        place(ground);

        if (wide) {
            for (BlockFace side : SIDES) {
                place(ground.getRelative(side));
            }
        }

        if (particle != null && ++ticks % 2 == 0) {
            ParticleUtil.spawn(location.add(0.0d, 0.1d, 0.0d), particle, 2, 0.25d, 0.05d, 0.25d, 0.0d);
        }
    }

    private void place(Block block) {
        BlockData data = trailData(block.getBlockData());

        if (data != null && PLUGIN.getFakeBlockManager().setFakeBlock(block, data, false, durationTicks)) {
            index++;
        }
    }

    /**
     * The trail block for the ground: the same slab or stair shape when the ground is one.
     */
    @Nullable
    private BlockData trailData(BlockData ground) {
        if (ground instanceof Slab slab) {
            if (slabs.isEmpty()) {
                return null;
            }
            Slab data = (Slab) pick(slabs).createBlockData();
            data.setType(slab.getType());
            data.setWaterlogged(slab.isWaterlogged());
            return data;
        }
        if (ground instanceof Stairs stair) {
            if (stairs.isEmpty()) {
                return null;
            }
            Stairs data = (Stairs) pick(stairs).createBlockData();
            data.setFacing(stair.getFacing());
            data.setHalf(stair.getHalf());
            data.setShape(stair.getShape());
            data.setWaterlogged(stair.isWaterlogged());
            return data;
        }
        return pick(blocks);
    }

    private <T> T pick(List<T> list) {
        return cycle ? list.get(index % list.size()) : MathUtil.getRandomElement(list);
    }

    @Override
    public void onUnequip(CosmeticContext<ParticleEffectType> context) {
        // Fake blocks restore themselves after their duration
    }
}
