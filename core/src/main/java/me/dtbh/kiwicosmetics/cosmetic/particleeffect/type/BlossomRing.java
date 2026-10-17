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

public class BlossomRing implements ParticleEffectBehavior {

    private static final Particle.DustOptions PINK = new Particle.DustOptions(Color.fromRGB(0xFFC8DD), 0.7f);
    private static final Particle.DustOptions WHITE = new Particle.DustOptions(Color.fromRGB(0xF4EFE9), 0.7f);
    private static final int RING_POINTS = 14;
    private static final float RADIUS = 0.8f;
    private static final double HEIGHT = 1.0d;

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
        if (ticks % 3 == 0) {
            Particle leaf = ticks % 2 == 0 ? Particle.CHERRY_LEAVES : Particle.PALE_OAK_LEAVES;
            ParticleUtil.spawn(location.add(0.0d, 2.3d, 0.0d), leaf, 1, 0.3d, 0.05d, 0.3d, 0.0d);
        }
    }

    private void spawnStaticEffect(Location location) {
        float rotation = ticks * 0.07f;
        location.add(0.0d, HEIGHT, 0.0d);

        for (int i = 0; i < RING_POINTS; i++) {
            float angle = rotation + i * (FastMathUtil.PI * 2.0f / RING_POINTS);
            double x = RADIUS * FastMathUtil.cos(angle);
            double z = RADIUS * FastMathUtil.sin(angle);

            location.add(x, 0.0d, z);
            ParticleUtil.spawn(location, Particle.DUST, 1, 0.0d, 0.0d, 0.0d, 0.0d, i % 2 == 0 ? PINK : WHITE);
            location.subtract(x, 0.0d, z);
        }

        // Petals drift off the ring, cherry and pale oak taking turns
        if (ticks % 3 == 0) {
            float angle = rotation * 2.0f;
            double x = RADIUS * FastMathUtil.cos(angle);
            double z = RADIUS * FastMathUtil.sin(angle);
            Particle leaf = (ticks / 3) % 2 == 0 ? Particle.CHERRY_LEAVES : Particle.PALE_OAK_LEAVES;

            ParticleUtil.spawn(location.add(x, 0.0d, z), leaf, 1, 0.0d, 0.0d, 0.0d, 0.0d);
        }
    }
}
