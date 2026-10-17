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
package me.dtbh.kiwicosmetics.wardrobe;

import me.dtbh.kiwicosmetics.wardrobe.Wardrobe;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class AnvilListener implements Listener {
    private final Wardrobe plugin;

    public AnvilListener(Wardrobe plugin) {
        this.plugin = plugin;
    }

    /**
     * Rainbow armor and hats can be repaired and enchanted, but not renamed.
     * <p>
     * The rename box is pre-filled with the item's current name, so instead of looking at the
     * rename text we compare the name before and after: only an actual change is blocked.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepare(PrepareAnvilEvent event) {
        ItemStack input = event.getInventory().getFirstItem();
        ItemStack result = event.getResult();
        if (result == null || (!plugin.isRainbow(input) && plugin.hats().match(input) == null)) {
            return;
        }
        Component before = input.getItemMeta().displayName();
        Component after = result.hasItemMeta() ? result.getItemMeta().displayName() : null;
        if (!Objects.equals(before, after)) {
            event.setResult(null);
        }
    }
}
