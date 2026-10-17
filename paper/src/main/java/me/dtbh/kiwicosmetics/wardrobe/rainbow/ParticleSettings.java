/*
 * This file is part of KiwiCosmetics, a fork of ProCosmetics - https://github.com/FilleDev/ProCosmetics
 * Copyright (C) 2026 FilleDev, dtbh and contributors
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
package me.dtbh.kiwicosmetics.wardrobe.rainbow;

import me.dtbh.kiwicosmetics.wardrobe.rainbow.ArmorPiece;
import org.bukkit.configuration.ConfigurationSection;

public record ParticleSettings(
        boolean enabled,
        ArmorPiece piece,
        int count,
        float size,
        double offset,
        double viewDistance,
        boolean hideForSpectators,
        boolean hideWhenInvisible
) {
    public static ParticleSettings load(ConfigurationSection section) {
        if (section == null) {
            return new ParticleSettings(true, ArmorPiece.BOOTS, 10, 1.0f, 0.2, 32, true, true);
        }
        ArmorPiece piece = ArmorPiece.fromKey(section.getString("piece", "boots"));
        return new ParticleSettings(
                section.getBoolean("enabled", true),
                piece == null ? ArmorPiece.BOOTS : piece,
                Math.max(0, section.getInt("count", 10)),
                (float) Math.max(0.01, section.getDouble("size", 1.0)),
                Math.max(0, section.getDouble("offset", 0.2)),
                Math.max(0, section.getDouble("view-distance", 32)),
                section.getBoolean("hide-for-spectators", true),
                section.getBoolean("hide-when-invisible", true)
        );
    }
}
