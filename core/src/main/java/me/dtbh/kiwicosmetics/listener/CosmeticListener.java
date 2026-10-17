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
package me.dtbh.kiwicosmetics.listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.api.cosmetic.Cosmetic;
import me.dtbh.kiwicosmetics.api.cosmetic.balloon.Balloon;
import me.dtbh.kiwicosmetics.api.cosmetic.gadget.Gadget;
import me.dtbh.kiwicosmetics.api.cosmetic.miniature.Miniature;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CosmeticCategory;
import me.dtbh.kiwicosmetics.api.user.User;

import java.util.List;

public class CosmeticListener implements Listener {

    private static final int MAX_DISTANCE_SQUARED = 128 * 128;

    private final KiwiCosmeticsPlugin plugin;

    public CosmeticListener(KiwiCosmeticsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        User user = plugin.getUserManager().getConnected(event.getEntity());

        if (user != null) {
            // Copy, removing music below modifies the map
            for (Cosmetic<?, ?> cosmetic : List.copyOf(user.getCosmetics().values())) {
                CosmeticCategory<?, ?, ?> category = cosmetic.getType().getCategory();

                if (category.equals(plugin.getCategoryRegistries().arrowEffects())
                        || category.equals(plugin.getCategoryRegistries().deathEffects())) {
                    continue;
                }

                if (category.equals(plugin.getCategoryRegistries().music())) {
                    user.removeCosmetic(plugin.getCategoryRegistries().music(), true, false);
                } else {
                    user.unequipCosmetic(category, true, false);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onTeleport(PlayerTeleportEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null || from.getWorld().equals(to.getWorld()) && from.distanceSquared(to) < MAX_DISTANCE_SQUARED) {
            return;
        }
        to = to.clone(); // Keep this for safety reasons as event.getTo() is not a copy of the loc

        Player player = event.getPlayer();
        User user = plugin.getUserManager().getConnected(player);

        if (user == null) {
            return;
        }
        Balloon balloon = (Balloon) user.getCosmetic(plugin.getCategoryRegistries().balloons());
        Gadget gadget = (Gadget) user.getCosmetic(plugin.getCategoryRegistries().gadgets());
        Miniature miniature = (Miniature) user.getCosmetic(plugin.getCategoryRegistries().miniatures());

        if (balloon != null && balloon.isEquipped()) {
            balloon.getTracker().respawnAt(to);
        }

        if (gadget != null && gadget.isEquipped() && gadget.getBehavior().shouldUnequipOnTeleport()) {
            gadget.unequip(false, true);
        }

        if (miniature != null && miniature.isEquipped()) {
            miniature.getNMSEntity().getTracker().respawnAt(to);
        }
    }
}
