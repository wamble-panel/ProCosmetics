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
package me.dtbh.kiwicosmetics.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Reads the client version of players from ViaVersion, without depending on it.
 * Everything is looked up once, on first use, after all plugins are enabled.
 */
public final class ViaVersionHook {

    private static boolean initialized;
    private static Object api;
    private static Method getPlayerVersion;
    private static int serverVersion = -1;

    private ViaVersionHook() {
    }

    /**
     * @return whether ViaVersion is installed and its API could be read
     */
    public static boolean isActive() {
        initialize();
        return api != null && serverVersion > 0;
    }

    /**
     * @return whether the player joined with a client older than the server
     */
    public static boolean isOlderClient(Player player) {
        if (!isActive()) {
            return false;
        }
        try {
            int version = (int) getPlayerVersion.invoke(api, player.getUniqueId());
            return version > 0 && version < serverVersion;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }

    private static synchronized void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        if (Bukkit.getPluginManager().getPlugin("ViaVersion") == null) {
            return;
        }
        try {
            Class<?> via = Class.forName("com.viaversion.viaversion.api.Via");
            Object viaApi = via.getMethod("getAPI").invoke(null);
            Method playerVersion = viaApi.getClass().getMethod("getPlayerVersion", UUID.class);
            Object server = viaApi.getClass().getMethod("getServerVersion").invoke(viaApi);
            Method lowest = server.getClass().getMethod("lowestSupportedVersion");
            lowest.setAccessible(true);
            int version = (int) lowest.invoke(server);

            getPlayerVersion = playerVersion;
            getPlayerVersion.setAccessible(true);
            serverVersion = version;
            api = viaApi;
            KiwiCosmeticsPlugin.getPlugin().getLogger().info("ViaVersion found: players on older clients get older particles.");
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            KiwiCosmeticsPlugin.getPlugin().getLogger().warning("Could not read client versions from ViaVersion, new particles are sent to everyone: " + e);
        }
    }
}
