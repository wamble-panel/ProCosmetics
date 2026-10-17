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

import org.bukkit.Material;

import java.util.Locale;

public enum ArmorPiece {
    // armorIndex is the position in PlayerInventory#getArmorContents()
    HELMET(Material.LEATHER_HELMET, 3),
    CHESTPLATE(Material.LEATHER_CHESTPLATE, 2),
    LEGGINGS(Material.LEATHER_LEGGINGS, 1),
    BOOTS(Material.LEATHER_BOOTS, 0);

    public static final ArmorPiece[] VALUES = values();

    private final Material material;
    private final int armorIndex;

    ArmorPiece(Material material, int armorIndex) {
        this.material = material;
        this.armorIndex = armorIndex;
    }

    public Material material() {
        return material;
    }

    public int armorIndex() {
        return armorIndex;
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static ArmorPiece fromKey(String key) {
        for (ArmorPiece piece : VALUES) {
            if (piece.key().equalsIgnoreCase(key)) {
                return piece;
            }
        }
        return null;
    }
}
