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

public class LeafVortex implements ParticleEffectBehavior {

    private static final Particle[] LEAVES = {
            ParticleUtil.find("RED_POPLAR_LEAVES", Particle.TINTED_LEAVES),
            ParticleUtil.find("ORANGE_POPLAR_LEAVES", Particle.TINTED_LEAVES),
            ParticleUtil.find("YELLOW_POPLAR_LEAVES", Particle.TINTED_LEAVES)
    };
    private static final Color[] COLORS = {
            Color.fromRGB(0xD9412B),
            Color.fromRGB(0xF08A24),
            Color.fromRGB(0xF5C542)
    };
    private static final int ARMS = 3;
    private static final double MAX_HEIGHT = 2.4d;
    private static final int STEPS = 6;

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
            int index = ticks % LEAVES.length;
            ParticleUtil.spawn(location.add(0.0d, 0.3d, 0.0d), LEAVES[index], 1, 0.4d, 0.1d, 0.4d, 0.0d, COLORS[index]);
        }
    }

    private void spawnStaticEffect(Location location) {
        float rotation = ticks * 0.15f;

        // A few points per arm, climbing and widening like a whirlwind
        for (int arm = 0; arm < ARMS; arm++) {
            for (int step = 0; step < STEPS; step++) {
                double height = ((ticks * 0.04d) + step * (MAX_HEIGHT / STEPS)) % MAX_HEIGHT;
                float radius = (float) (0.25d + height * 0.3d);
                float angle = rotation + arm * (FastMathUtil.PI * 2.0f / ARMS) + (float) height * 2.0f;
                double x = radius * FastMathUtil.cos(angle);
                double z = radius * FastMathUtil.sin(angle);
                Color color = COLORS[(arm + step) % COLORS.length];

                location.add(x, height, z);
                ParticleUtil.spawn(location, Particle.DUST, 1, 0.0d, 0.0d, 0.0d, 0.0d, new Particle.DustOptions(color, 0.9f));
                location.subtract(x, height, z);
            }
        }

        if (ticks % 5 == 0) {
            int index = (ticks / 5) % LEAVES.length;
            ParticleUtil.spawn(location.add(0.0d, MAX_HEIGHT, 0.0d), LEAVES[index], 1, 0.6d, 0.1d, 0.6d, 0.0d, COLORS[index]);
        }
    }
}
