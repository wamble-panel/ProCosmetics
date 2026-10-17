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
package me.dtbh.kiwicosmetics.cosmetic.trail.type;

import me.dtbh.kiwicosmetics.cosmetic.trail.AbstractTrail;
import me.dtbh.kiwicosmetics.util.ParticleUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;

public class AutumnLeavesTrail extends AbstractTrail {

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

    @Override
    protected void trail(Location location) {
        int index = ticks % LEAVES.length;
        behind(location, 0.3d).add(0.0d, 0.7d, 0.0d);
        ParticleUtil.spawn(location, LEAVES[index], 1, 0.3d, 0.2d, 0.3d, 0.0d, COLORS[index]);
    }
}
