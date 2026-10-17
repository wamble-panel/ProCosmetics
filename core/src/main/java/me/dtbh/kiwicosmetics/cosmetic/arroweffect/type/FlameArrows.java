/*
 * This file is part of KiwiCosmetics, a fork of ProCosmetics - https://github.com/FilleDev/ProCosmetics
 * Copyright (C) 2025-2026 FilleDev, dtbh and contributors
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
package me.dtbh.kiwicosmetics.cosmetic.arroweffect.type;

import org.bukkit.Location;
import org.bukkit.Particle;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticContext;
import me.dtbh.kiwicosmetics.api.cosmetic.arroweffect.ArrowEffectBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.arroweffect.ArrowEffectType;
import me.dtbh.kiwicosmetics.util.MathUtil;

public class FlameArrows implements ArrowEffectBehavior {

    private static final int HIT_AMOUNT = 30;
    private static final double HIT_SPREAD = 5.0d;

    @Override
    public void onEquip(CosmeticContext<ArrowEffectType> context) {
    }

    @Override
    public void onUpdate(CosmeticContext<ArrowEffectType> context, Location location) {
        location.getWorld().spawnParticle(Particle.FLAME, location, 0);
    }

    @Override
    public void onArrowHit(CosmeticContext<ArrowEffectType> context, Location location) {
        location.getWorld().spawnParticle(Particle.LAVA, location, 2);

        for (int i = 0; i < HIT_AMOUNT; i++) {
            location.getWorld().spawnParticle(Particle.FLAME,
                    location,
                    0,
                    MathUtil.randomRange(-HIT_SPREAD, HIT_SPREAD),
                    MathUtil.randomRange(-HIT_SPREAD, HIT_SPREAD),
                    MathUtil.randomRange(-HIT_SPREAD, HIT_SPREAD),
                    0.1d
            );
        }
    }

    @Override
    public void onUnequip(CosmeticContext<ArrowEffectType> context) {
    }
}
