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
package me.dtbh.kiwicosmetics.cosmetic.particleeffect.type;

import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticContext;
import me.dtbh.kiwicosmetics.api.cosmetic.particleeffect.ParticleEffectBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.particleeffect.ParticleEffectType;
import me.dtbh.kiwicosmetics.util.FastMathUtil;
import me.dtbh.kiwicosmetics.util.ParticleUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;

public class CometOrbit implements ParticleEffectBehavior {

    private static final Color[] COLORS = {
            Color.fromRGB(0xFFB5A7),
            Color.fromRGB(0xCDB4DB),
            Color.fromRGB(0xA2D2FF)
    };
    private static final float[] TILTS = {0.0f, 0.6f, -0.6f};
    private static final float RADIUS = 0.95f;
    private static final float SPEED = 0.18f;
    private static final int DURATION = 10;

    private final Location target = new Location(null, 0.0d, 0.0d, 0.0d);
    private int ticks;

    @Override
    public void onEquip(CosmeticContext<ParticleEffectType> context) {
    }

    @Override
    public void onUpdate(CosmeticContext<ParticleEffectType> context, Location location) {
        if (++ticks >= 72000) {
            ticks = 0;
        }
        if (context.getUser().isMoving()) {
            spawnMovingEffect(location);
        } else {
            spawnStaticEffect(location);
        }
    }

    @Override
    public void onUnequip(CosmeticContext<ParticleEffectType> context) {
    }

    private void spawnMovingEffect(Location location) {
        spawnComet(location, 0, 0.0f);
    }

    private void spawnStaticEffect(Location location) {
        for (int i = 0; i < COLORS.length; i++) {
            spawnComet(location, i, i * (FastMathUtil.PI * 2.0f / COLORS.length));
        }
    }

    /**
     * A trail particle at the comet that flies back to where the comet just was, drawing its tail.
     */
    private void spawnComet(Location location, int index, float offset) {
        float angle = ticks * SPEED + offset;
        float previous = angle - SPEED * 3.0f;

        target.setWorld(location.getWorld());
        setOrbit(target, location, index, previous);
        setOrbit(location, location, index, angle);
        ParticleUtil.spawn(location, Particle.TRAIL, 1, 0.0d, 0.0d, 0.0d, 0.0d, new Particle.Trail(target, COLORS[index], DURATION));
        resetOrbit(location, index, angle);
    }

    private static void setOrbit(Location result, Location center, int index, float angle) {
        double x = RADIUS * FastMathUtil.cos(angle);
        double z = RADIUS * FastMathUtil.sin(angle);
        double y = 1.0d + TILTS[index] * x;

        if (result == center) {
            result.add(x, y, z);
        } else {
            result.setX(center.getX() + x);
            result.setY(center.getY() + y);
            result.setZ(center.getZ() + z);
        }
    }

    private static void resetOrbit(Location location, int index, float angle) {
        double x = RADIUS * FastMathUtil.cos(angle);
        double z = RADIUS * FastMathUtil.sin(angle);
        location.subtract(x, 1.0d + TILTS[index] * x, z);
    }
}
