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
import me.dtbh.kiwicosmetics.util.MathUtil;
import me.dtbh.kiwicosmetics.util.ParticleUtil;
import org.bukkit.Location;
import org.bukkit.Particle;

public class GeyserFountain implements ParticleEffectBehavior {

    // Geyser particles are new in 26.2
    private static final Particle PLUME = ParticleUtil.find("GEYSER_PLUME", Particle.CLOUD);
    private static final Particle POOF = ParticleUtil.find("GEYSER_POOF", Particle.POOF);
    private static final int ERUPTION_INTERVAL = 30;
    private static final int RING_POINTS = 8;

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
        ParticleUtil.spawn(location.add(0.0d, 0.1d, 0.0d), Particle.SPLASH, 3, 0.2d, 0.0d, 0.2d, 0.0d);
    }

    private void spawnStaticEffect(Location location) {
        int phase = ticks % ERUPTION_INTERVAL;

        if (phase == 0) {
            // Eruption: a ring of steam bursts around the feet and a column shoots up
            for (int i = 0; i < RING_POINTS; i++) {
                float angle = i * (FastMathUtil.PI * 2.0f / RING_POINTS);
                double x = 0.7d * FastMathUtil.cos(angle);
                double z = 0.7d * FastMathUtil.sin(angle);

                location.add(x, 0.1d, z);
                ParticleUtil.spawn(location, POOF, 1, 0.0d, 0.0d, 0.0d, 0.02d);
                location.subtract(x, 0.1d, z);
            }
        }

        if (phase < 8) {
            // count 0 makes the offsets a direction: straight up, faster at the start of the eruption
            double speed = 0.5d - phase * 0.05d;
            location.add(MathUtil.randomRange(-0.15d, 0.15d), 0.1d, MathUtil.randomRange(-0.15d, 0.15d));
            ParticleUtil.spawn(location, PLUME, 0, 0.0d, 1.0d, 0.0d, speed);
            location.add(0.0d, 2.4d, 0.0d);
            ParticleUtil.spawn(location, Particle.FALLING_WATER, 4, 0.5d, 0.1d, 0.5d, 0.0d);
        } else if (ticks % 4 == 0) {
            ParticleUtil.spawn(location.add(0.0d, 0.1d, 0.0d), Particle.SPLASH, 2, 0.3d, 0.0d, 0.3d, 0.0d);
        }
    }
}
