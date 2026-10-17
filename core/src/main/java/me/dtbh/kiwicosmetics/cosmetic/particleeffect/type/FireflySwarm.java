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

public class FireflySwarm implements ParticleEffectBehavior {

    private static final int FIREFLIES = 5;
    private static final float RADIUS = 1.1f;
    private static final Particle.DustOptions GLOW = new Particle.DustOptions(Color.fromRGB(0xF4F18A), 0.6f);

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
            ParticleUtil.spawn(location.add(0.0d, 1.6d, 0.0d), Particle.FIREFLY, 1, 0.5d, 0.3d, 0.5d, 0.0d);
        }
    }

    private void spawnStaticEffect(Location location) {
        float time = ticks * 0.08f;
        float yaw = FastMathUtil.toRadians(location.getYaw());

        // Fireflies chase each other on a figure eight around the player
        for (int i = 0; i < FIREFLIES; i++) {
            float t = time + i * (FastMathUtil.PI * 2.0f / FIREFLIES);
            float sin = FastMathUtil.sin(t);
            float forward = RADIUS * sin;
            float side = RADIUS * sin * FastMathUtil.cos(t);
            double x = forward * FastMathUtil.cos(yaw) - side * FastMathUtil.sin(yaw);
            double z = forward * FastMathUtil.sin(yaw) + side * FastMathUtil.cos(yaw);
            double y = 1.1d + 0.35d * FastMathUtil.sin(t * 2.0f + i);

            location.add(x, y, z);
            ParticleUtil.spawn(location, Particle.DUST, 1, 0.0d, 0.0d, 0.0d, 0.0d, GLOW);

            if ((ticks + i) % 6 == 0) {
                ParticleUtil.spawn(location, Particle.FIREFLY, 1, 0.0d, 0.0d, 0.0d, 0.0d);
            }
            location.subtract(x, y, z);
        }
    }
}
