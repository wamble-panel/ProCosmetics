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
package me.dtbh.kiwicosmetics.api.user;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Manages user instances and provides various methods for retrieving users.
 *
 * <p>Access patterns:
 * <ul>
 *   <li><b>Connecting</b> - Only returns users currently in the process of connecting (pre-login)</li>
 *   <li><b>Connected</b> - Only returns users currently online</li>
 *   <li><b>ConnectedOrCached</b> - Returns online users or recently cached offline users</li>
 *   <li><b>Get</b> - Loads from database if not found in memory (synchronous)</li>
 *   <li><b>GetAsync</b> - Loads from database if not found in memory (asynchronous)</li>
 * </ul>
 */
public interface UserManager {

    /**
     * Gets all currently connected users.
     *
     * @return an immutable collection of all online users
     */
    Collection<? extends User> getAllConnected();

    /**
     * Gets a connected user by their UUID.
     *
     * @param uuid the user's UUID
     * @return the connected user, or null if not online
     */
    @Nullable
    User getConnected(@Nullable UUID uuid);

    /**
     * Gets a connected user by their Player object.
     *
     * @param player the player
     * @return the connected user, or null if player is null or not online
     */
    @Nullable
    default User getConnected(@Nullable Player player) {
        if (player == null) {
            return null;
        }
        return getConnected(player.getUniqueId());
    }

    /**
     * Gets a connected user by their name (case-insensitive).
     * This method iterates through all connected users.
     *
     * @param name the user's name
     * @return the connected user, or null if name is null or user is not online
     */
    @Nullable
    default User getConnected(@Nullable String name) {
        if (name == null) {
            return null;
        }

        for (User user : getAllConnected()) {
            if (user.getName().equalsIgnoreCase(name)) {
                return user;
            }
        }
        return null;
    }

}
