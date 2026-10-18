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
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Nullable;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticContext;
import me.dtbh.kiwicosmetics.api.cosmetic.particleeffect.ParticleEffectBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.particleeffect.ParticleEffectType;
import me.dtbh.kiwicosmetics.util.MathUtil;
import me.dtbh.kiwicosmetics.util.ParticleUtil;

import java.util.List;

/**
 * Turns the ground you walk on into other blocks for a few seconds. The blocks are fake
 * (client-side, shown to everyone nearby), the real world never changes, and special blocks
 * like chests, doors and beds are skipped by the {@link me.dtbh.kiwicosmetics.util.block.FakeBlockManager}.
 */
public class BlockTrail implements ParticleEffectBehavior {

    private static final KiwiCosmeticsPlugin PLUGIN = KiwiCosmeticsPlugin.getPlugin();
    private static final BlockFace[] SIDES = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
    private static final long DEFAULT_DURATION_TICKS = 60L;

    private final List<BlockData> blocks;
    private final boolean wide;
    private final boolean cycle;
    private final Particle particle;
    private final Location below = new Location(null, 0.0d, 0.0d, 0.0d);
    private long durationTicks = DEFAULT_DURATION_TICKS;
    private int index;
    private int ticks;

    /**
     * @param blocks   the blocks to show
     * @param wide     also change the four blocks around the one you stand on
     * @param cycle    go through the blocks in order (rainbows) instead of picking at random
     * @param particle optional particle at your feet while walking
     */
    public BlockTrail(List<BlockData> blocks, boolean wide, boolean cycle, @Nullable Particle particle) {
        this.blocks = blocks;
        this.wide = wide;
        this.cycle = cycle;
        this.particle = particle;
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
        BlockData data = cycle ? blocks.get(index % blocks.size()) : MathUtil.getRandomElement(blocks);

        if (data != null && PLUGIN.getFakeBlockManager().setFakeBlock(block, data, false, durationTicks)) {
            index++;
        }
    }

    @Override
    public void onUnequip(CosmeticContext<ParticleEffectType> context) {
        // Fake blocks restore themselves after their duration
    }
}
