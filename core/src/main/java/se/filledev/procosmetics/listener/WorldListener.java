/*
 * This file is part of ProCosmetics - https://github.com/FilleDev/ProCosmetics
 * Copyright (C) 2025-2026 FilleDev and contributors
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
package se.filledev.procosmetics.listener;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import se.filledev.procosmetics.ProCosmeticsPlugin;
import se.filledev.procosmetics.api.config.Config;
import se.filledev.procosmetics.api.user.User;
import se.filledev.procosmetics.util.InventoryUtil;
import se.filledev.procosmetics.util.item.ItemIdentifier;

import java.util.List;

/**
 * Handles cosmetics when players move between worlds or respawn.
 * The cosmetics menu is opened with /cosmetics, there is no menu item anymore.
 */
public class WorldListener implements Listener {

    // Menu item handed out by older versions, cleaned up when players join
    private static final ItemIdentifier LEGACY_MENU_ITEM = new ItemIdentifier("COSMETIC_ITEM");

    private final ProCosmeticsPlugin plugin;
    private final Config config;

    public WorldListener(ProCosmeticsPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager().getMainConfig();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        InventoryUtil.remove(event.getPlayer().getInventory(), LEGACY_MENU_ITEM);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        User user = plugin.getUserManager().getConnected(player);

        if (user == null) {
            return;
        }

        if (!isCosmeticAllowed(player.getWorld())) {
            user.unequipCosmetics(true, false);
        } else if (!isCosmeticAllowed(event.getFrom())) {
            user.equipSavedCosmetics(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        World respawnWorld = event.getRespawnLocation().getWorld();

        if (respawnWorld == null || !isCosmeticAllowed(respawnWorld)) {
            return;
        }
        Player player = event.getPlayer();

        // Run 1 tick later, player.getLocation() is still the death location here
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            User user = plugin.getUserManager().getConnected(player);

            if (user != null && player.isOnline()) {
                user.equipSavedCosmetics(true);
            }
        });
    }

    private boolean isCosmeticAllowed(World world) {
        boolean blacklist = config.getBoolean("multi_world.blacklisted_worlds");
        List<String> worlds = config.getStringList("multi_world.worlds");
        return blacklist != worlds.contains(world.getName());
    }
}
