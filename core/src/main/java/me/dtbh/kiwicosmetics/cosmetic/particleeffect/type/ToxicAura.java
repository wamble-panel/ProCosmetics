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

public class ToxicAura implements ParticleEffectBehavior {

    // Noxious gas is new in 26.2, older servers get green dust
    private static final Particle GAS = ParticleUtil.find("NOXIOUS_GAS", Particle.DUST);
    private static final Particle.DustOptions GAS_FALLBACK = new Particle.DustOptions(Color.fromRGB(0x8FD14F), 1.4f);
    private static final Particle.DustTransition RING = new Particle.DustTransition(Color.fromRGB(0xB8F28C), Color.fromRGB(0x3E7D2A), 0.9f);
    private static final int ARMS = 2;
    private static final int RING_POINTS = 10;

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
            ParticleUtil.spawn(location.add(0.0d, 0.2d, 0.0d), GAS, 1, 0.2d, 0.05d, 0.2d, 0.0d, GAS_FALLBACK);
        }
    }

    private void spawnStaticEffect(Location location) {
        float rotation = ticks * 0.2f;
        double height = (ticks * 0.06d) % 2.0d;

        // Gas spirals rising around the body
        for (int arm = 0; arm < ARMS; arm++) {
            float angle = rotation + arm * FastMathUtil.PI;
            double x = 0.7d * FastMathUtil.cos(angle);
            double z = 0.7d * FastMathUtil.sin(angle);

            location.add(x, height, z);
            ParticleUtil.spawn(location, GAS, 1, 0.0d, 0.0d, 0.0d, 0.0d, GAS_FALLBACK);
            location.subtract(x, height, z);
        }

        // A bubbling ring around the waist
        location.add(0.0d, 0.9d, 0.0d);
        for (int i = 0; i < RING_POINTS; i++) {
            float angle = -rotation * 0.5f + i * (FastMathUtil.PI * 2.0f / RING_POINTS);
            double x = 0.9d * FastMathUtil.cos(angle);
            double z = 0.9d * FastMathUtil.sin(angle);

            location.add(x, 0.0d, z);
            ParticleUtil.spawn(location, Particle.DUST_COLOR_TRANSITION, 1, 0.0d, 0.0d, 0.0d, 0.0d, RING);
            location.subtract(x, 0.0d, z);
        }
    }
}
