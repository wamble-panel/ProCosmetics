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
import org.bukkit.Location;
import org.bukkit.Particle;

public class StarlightTrail extends AbstractTrail {

    @Override
    protected void trail(Location location) {
        behind(location, 0.3d).add(0.0d, 0.15d, 0.0d);
        ParticleUtil.spawn(location, Particle.END_ROD, 1, 0.1d, 0.05d, 0.1d, 0.01d);
        ParticleUtil.spawn(location, Particle.ELECTRIC_SPARK, 2, 0.2d, 0.1d, 0.2d, 0.0d);
    }

    @Override
    protected void idle(Location location) {
        if (ticks % 10 == 0) {
            ParticleUtil.spawn(location.add(0.0d, 0.1d, 0.0d), Particle.ELECTRIC_SPARK, 1, 0.3d, 0.0d, 0.3d, 0.0d);
        }
    }
}
