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
package se.filledev.procosmetics.user;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;
import se.filledev.procosmetics.ProCosmeticsPlugin;
import se.filledev.procosmetics.api.user.User;
import se.filledev.procosmetics.api.user.UserManager;
import se.filledev.procosmetics.util.AbstractRunnable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserManagerImpl implements UserManager {

    private final ProCosmeticsPlugin plugin;
    private final UserStorage storage;
    private final Map<UUID, User> connected = new ConcurrentHashMap<>();

    public UserManagerImpl(ProCosmeticsPlugin plugin) {
        this.plugin = plugin;
        this.storage = new UserStorage(plugin);
    }

    public UserStorage getStorage() {
        return storage;
    }

    public void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(new Listeners(), plugin);
        plugin.getServer().getScheduler().runTaskTimer(plugin, new MovementRunnable(), 1L, 2L); // Keep this at least 2 ticks
    }

    public void loadOnlinePlayers() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            User user = load(player);

            if (player.getGameMode() != GameMode.SPECTATOR) {
                user.equipSavedCosmetics(true);
            }
        }
    }

    private User load(Player player) {
        UserImpl user = new UserImpl(plugin, player.getUniqueId(), player.getName());

        if (plugin.getConfigManager().getMainConfig().getBoolean("settings.restore_cosmetics")) {
            storage.loadEquipped(player, user);
        }
        connected.put(player.getUniqueId(), user);
        return user;
    }

    private final class Listeners implements Listener {

        @EventHandler(priority = EventPriority.LOWEST)
        private void onJoin(PlayerJoinEvent event) {
            load(event.getPlayer());
        }

        @EventHandler(priority = EventPriority.HIGH)
        private void onLateJoin(PlayerJoinEvent event) {
            Player player = event.getPlayer();
            User user = getConnected(player);

            if (user != null && player.getGameMode() != GameMode.SPECTATOR) {
                user.equipSavedCosmetics(true);
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        private void onQuit(PlayerQuitEvent event) {
            User user = connected.remove(event.getPlayer().getUniqueId());

            if (user != null) {
                user.clearAllCosmetics(true, false);
            }
        }
    }

    @Override
    public Collection<User> getAllConnected() {
        return connected.values();
    }

    @Override
    @Nullable
    public User getConnected(@Nullable UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return connected.get(uuid);
    }

    private final class MovementRunnable extends AbstractRunnable {

        private final Location reuseableLocation = new Location(null, 0.0d, 0.0d, 0.0d);

        @Override
        public void run() {
            for (User user : connected.values()) {
                Player player = user.getPlayer();

                if (player != null) {
                    Location location = player.getLocation(reuseableLocation);
                    boolean moving = hasDifference(location, user.getLastLocation());
                    user.setMoving(moving);

                    if (moving) {
                        user.setLastLocation(location);
                    }
                }
            }
        }

        private boolean hasDifference(@Nullable Location from, @Nullable Location to) {
            if (from == null || to == null) {
                return true;
            }

            if (from.getWorld() != to.getWorld()) {
                return true;
            }
            return Double.doubleToLongBits(from.getX()) != Double.doubleToLongBits(to.getX())
                    || Double.doubleToLongBits(from.getY()) != Double.doubleToLongBits(to.getY())
                    || Double.doubleToLongBits(from.getZ()) != Double.doubleToLongBits(to.getZ());
        }
    }
}
