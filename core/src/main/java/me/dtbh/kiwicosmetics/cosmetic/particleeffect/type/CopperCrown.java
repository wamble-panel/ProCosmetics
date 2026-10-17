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

public class CopperCrown implements ParticleEffectBehavior {

    private static final Particle.DustOptions COPPER = new Particle.DustOptions(Color.fromRGB(0xC77B4B), 0.7f);
    private static final Particle.DustOptions PATINA = new Particle.DustOptions(Color.fromRGB(0x5FB89B), 0.7f);
    private static final int RING_POINTS = 16;
    private static final int SPIKES = 8;
    private static final float RADIUS = 0.3f;
    private static final double HEIGHT = 2.1d;

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
        // A crown stays on the head while walking, just without the flames
        drawCrown(location.add(0.0d, HEIGHT, 0.0d), false);
    }

    private void spawnStaticEffect(Location location) {
        drawCrown(location.add(0.0d, HEIGHT, 0.0d), ticks % 4 == 0);
    }

    private void drawCrown(Location location, boolean flames) {
        float rotation = ticks * 0.03f;

        for (int i = 0; i < RING_POINTS; i++) {
            float angle = rotation + i * (FastMathUtil.PI * 2.0f / RING_POINTS);
            double x = RADIUS * FastMathUtil.cos(angle);
            double z = RADIUS * FastMathUtil.sin(angle);

            location.add(x, 0.0d, z);
            ParticleUtil.spawn(location, Particle.DUST, 1, 0.0d, 0.0d, 0.0d, 0.0d, i % 4 == 0 ? PATINA : COPPER);
            location.subtract(x, 0.0d, z);
        }

        for (int i = 0; i < SPIKES; i++) {
            float angle = rotation + i * (FastMathUtil.PI * 2.0f / SPIKES);
            double x = RADIUS * FastMathUtil.cos(angle);
            double z = RADIUS * FastMathUtil.sin(angle);
            double spike = i % 2 == 0 ? 0.25d : 0.15d;

            location.add(x, spike, z);
            ParticleUtil.spawn(location, Particle.DUST, 1, 0.0d, 0.0d, 0.0d, 0.0d, COPPER);

            if (flames && i % 2 == 0) {
                ParticleUtil.spawn(location.add(0.0d, 0.08d, 0.0d), Particle.COPPER_FIRE_FLAME, 1, 0.0d, 0.0d, 0.0d, 0.0d);
                location.subtract(0.0d, 0.08d, 0.0d);
            }
            location.subtract(x, spike, z);
        }
    }
}
