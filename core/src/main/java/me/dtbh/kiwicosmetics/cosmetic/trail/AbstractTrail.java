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
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticContext;
import me.dtbh.kiwicosmetics.api.cosmetic.particleeffect.ParticleEffectBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.particleeffect.ParticleEffectType;
import me.dtbh.kiwicosmetics.util.FastMathUtil;

/**
 * A trail only draws while the player walks, at their feet, so it can be worn together with a particle effect.
 */
public abstract class AbstractTrail implements ParticleEffectBehavior {

    protected int ticks;

    @Override
    public void onEquip(CosmeticContext<ParticleEffectType> context) {
    }

    @Override
    public void onUpdate(CosmeticContext<ParticleEffectType> context, Location location) {
        if (++ticks >= 72000) {
            ticks = 0;
        }
        if (context.getUser().isMoving()) {
            trail(location);
        } else {
            idle(location);
        }
    }

    @Override
    public void onUnequip(CosmeticContext<ParticleEffectType> context) {
    }

    /**
     * Called while the player moves. {@code location} is the player's feet and may be modified.
     */
    protected abstract void trail(Location location);

    /**
     * Called while the player stands still. Draws nothing by default.
     */
    protected void idle(Location location) {
    }

    /**
     * Moves {@code location} horizontally behind where the player is looking.
     */
    protected static Location behind(Location location, double distance) {
        float yaw = FastMathUtil.toRadians(location.getYaw());
        return location.add(FastMathUtil.sin(yaw) * distance, 0.0d, -FastMathUtil.cos(yaw) * distance);
    }
}
