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

import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public final class ColorPattern {
    private final String name;
    private final Color[] colors;
    private final long interval;
    private final ItemStack[] armorSet;

    public ColorPattern(String name, Color[] colors, long interval, ItemStack[] armorSet) {
        this.name = name;
        this.colors = colors;
        this.interval = Math.max(1L, interval);
        this.armorSet = armorSet;
    }

    public String name() {
        return name;
    }

    public long interval() {
        return interval;
    }

    /**
     * The color for a given tick. Derived from the tick alone, so every player
     * wearing the same pattern stays in sync and no shared counter has to be advanced.
     */
    public Color colorAt(long tick) {
        if (colors.length == 0) {
            return Color.WHITE;
        }
        return colors[(int) ((tick / interval) % colors.length)];
    }

    /**
     * A fresh copy of the configured item for this piece, already dyed with the current color.
     */
    public ItemStack createItem(ArmorPiece piece, long tick) {
        ItemStack template = armorSet[piece.ordinal()];
        if (template == null) {
            return null;
        }
        ItemStack item = template.clone();
        Color color = colorAt(tick);
        item.editMeta(LeatherArmorMeta.class, meta -> meta.setColor(color));
        return item;
    }
}
