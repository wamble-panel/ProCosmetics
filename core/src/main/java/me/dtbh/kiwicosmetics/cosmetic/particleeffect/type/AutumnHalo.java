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

public class AutumnHalo implements ParticleEffectBehavior {

    // Poplar leaves are new in 26.3, older servers get tinted leaves in the same colors
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
    private static final Particle.DustOptions[] DUST = {
            new Particle.DustOptions(COLORS[0], 0.8f),
            new Particle.DustOptions(COLORS[1], 0.8f),
            new Particle.DustOptions(COLORS[2], 0.8f)
    };
    private static final int POINTS = 12;
    private static final float RADIUS = 0.45f;
    private static final double HEIGHT = 2.25d;

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
            location.add(0.0d, HEIGHT, 0.0d);
            ParticleUtil.spawn(location, LEAVES[index], 1, 0.3d, 0.05d, 0.3d, 0.0d, COLORS[index]);
        }
    }

    private void spawnStaticEffect(Location location) {
        location.add(0.0d, HEIGHT, 0.0d);
        float rotation = ticks * 0.06f;

        for (int i = 0; i < POINTS; i++) {
            float angle = rotation + i * (FastMathUtil.PI * 2.0f / POINTS);
            double x = RADIUS * FastMathUtil.cos(angle);
            double z = RADIUS * FastMathUtil.sin(angle);

            location.add(x, 0.0d, z);
            ParticleUtil.spawn(location, Particle.DUST, 1, 0.0d, 0.0d, 0.0d, 0.0d, DUST[i % DUST.length]);
            location.subtract(x, 0.0d, z);
        }

        // Every now and then the halo sheds a leaf
        if (ticks % 4 == 0) {
            int index = (ticks / 4) % LEAVES.length;
            float angle = rotation * 3.0f;
            double x = RADIUS * FastMathUtil.cos(angle);
            double z = RADIUS * FastMathUtil.sin(angle);
            ParticleUtil.spawn(location.add(x, 0.0d, z), LEAVES[index], 1, 0.0d, 0.0d, 0.0d, 0.0d, COLORS[index]);
        }
    }
}
