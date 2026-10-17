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

public class SulfurSpiral implements ParticleEffectBehavior {

    // Sulfur bubbles are new in 26.2
    private static final Particle BUBBLES = ParticleUtil.find("SULFUR_BUBBLES", Particle.BUBBLE_POP);
    private static final Particle.DustOptions SULFUR = new Particle.DustOptions(Color.fromRGB(0xE8E05A), 0.8f);
    private static final double HEIGHT = 2.2d;
    private static final float RADIUS = 0.6f;

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
        if (ticks % 2 == 0) {
            ParticleUtil.spawn(location.add(0.0d, 0.2d, 0.0d), BUBBLES, 2, 0.25d, 0.05d, 0.25d, 0.02d);
        }
    }

    private void spawnStaticEffect(Location location) {
        float angle = ticks * 0.25f;

        // Two strands climbing opposite each other, each leaving a short yellow tail
        for (int strand = 0; strand < 2; strand++) {
            for (int tail = 0; tail < 3; tail++) {
                double height = ((ticks - tail) * 0.06d) % HEIGHT;

                if (height < 0) {
                    height += HEIGHT;
                }
                float strandAngle = angle - tail * 0.25f + strand * FastMathUtil.PI;
                double x = RADIUS * FastMathUtil.cos(strandAngle);
                double z = RADIUS * FastMathUtil.sin(strandAngle);

                location.add(x, height, z);
                if (tail == 0) {
                    ParticleUtil.spawn(location, BUBBLES, 1, 0.0d, 0.0d, 0.0d, 0.0d);
                }
                ParticleUtil.spawn(location, Particle.DUST, 1, 0.0d, 0.0d, 0.0d, 0.0d, SULFUR);
                location.subtract(x, height, z);
            }
        }
    }
}
