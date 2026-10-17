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
import me.dtbh.kiwicosmetics.util.MathUtil;
import me.dtbh.kiwicosmetics.util.ParticleUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;

public class CometTrail extends AbstractTrail {

    private static final Color HEAD = Color.fromRGB(0xFFC8DD);
    private static final Color TAIL = Color.fromRGB(0xA2D2FF);
    private static final int DURATION = 18;

    private final Location target = new Location(null, 0.0d, 0.0d, 0.0d);

    @Override
    protected void trail(Location location) {
        // Streaks fly from the feet to a point behind the player, the color slowly shifts along the way
        float phase = (ticks % 80) / 40.0f;
        Color color = ParticleUtil.blend(HEAD, TAIL, phase > 1.0f ? 2.0f - phase : phase);

        location.add(0.0d, 0.2d, 0.0d);
        target.setWorld(location.getWorld());
        target.setX(location.getX());
        target.setY(location.getY() + 0.3d);
        target.setZ(location.getZ());
        target.setYaw(location.getYaw());
        behind(target, 1.6d);

        for (int i = 0; i < 3; i++) {
            double x = MathUtil.randomRange(-0.25d, 0.25d);
            double y = MathUtil.randomRange(-0.1d, 0.2d);
            double z = MathUtil.randomRange(-0.25d, 0.25d);

            target.add(x, y, z);
            ParticleUtil.spawn(location, Particle.TRAIL, 1, 0.1d, 0.05d, 0.1d, 0.0d, new Particle.Trail(target, color, DURATION));
            target.subtract(x, y, z);
        }
    }
}
