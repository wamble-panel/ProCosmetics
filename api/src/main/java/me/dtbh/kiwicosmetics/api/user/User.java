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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import me.dtbh.kiwicosmetics.api.cosmetic.Cosmetic;
import me.dtbh.kiwicosmetics.api.cosmetic.gadget.GadgetType;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CosmeticCategory;
import me.dtbh.kiwicosmetics.api.locale.Translator;

import java.util.Map;
import java.util.UUID;

/**
 * Represents a player in the KiwiCosmetics system.
 * <p>
 * Each online player has a corresponding {@code User} instance that is used for
 * both runtime cosmetic state and persistent database synchronization.
 *
 * @see UserManager
 * @see Cosmetic
 * @see CosmeticCategory
 * @see TreasureChest
 */
public interface User extends Translator {

    /**
     * Displays a title to the player.
     *
     * @param title    the main title component
     * @param subTitle the subtitle component
     * @param times    the display timings for the title
     */
    void showTitle(Component title, Component subTitle, Title.Times times);

    /**
     * Sends an action bar message to the player.
     *
     * @param component the message to display
     */
    void sendActionBar(Component component);

    /**
     * Sends a chat message to the player.
     *
     * @param component the message to send
     */
    void sendMessage(Component component);

    /**
     * Gets the unique Minecraft UUID of this player.
     *
     * @return the player’s UUID
     */
    UUID getUniqueId();

    /**
     * Gets the name of this player.
     *
     * @return the player’s name
     */
    String getName();

    /**
     * Gets the underlying Bukkit {@link Player} instance, if online.
     *
     * @return the {@link Player}, or {@code null} if the player is offline
     */
    @Nullable
    Player getPlayer();

    /**
     * Checks whether the player is currently moving.
     *
     * @return {@code true} if the player is moving
     */
    boolean isMoving();

    /**
     * Sets the player's movement state.
     *
     * @param moving {@code true} if the player is moving
     */
    @ApiStatus.Internal
    void setMoving(boolean moving);

    /**
     * Gets the player’s last recorded location.
     *
     * @return the last known {@link Location}
     */
    @ApiStatus.Internal
    Location getLastLocation();

    /**
     * Updates the player’s last recorded location.
     *
     * @param location the new last known {@link Location}
     */
    @ApiStatus.Internal
    void setLastLocation(Location location);

    /**
     * Checks if the player currently has fall damage protection.
     *
     * @return {@code true} if fall damage is prevented
     */
    boolean hasFallDamageProtection();

    /**
     * Applies temporary fall damage protection to the player.
     *
     * @param seconds the duration of protection in seconds
     */
    void setFallDamageProtection(int seconds);

    /**
     * Checks whether the player has an equipped cosmetic in the given category.
     *
     * @param category the cosmetic category to check
     * @return {@code true} if the player has a cosmetic equipped in the category
     */
    boolean hasCosmetic(CosmeticCategory<?, ?, ?> category);

    /**
     * Gets the cosmetic currently equipped in the given category.
     *
     * @param category the cosmetic category
     * @return the equipped cosmetic, or {@code null} if none
     */
    @Nullable
    Cosmetic<?, ?> getCosmetic(CosmeticCategory<?, ?, ?> category);

    /**
     * Sets the equipped cosmetic for the given category.
     *
     * @param category the category of the cosmetic
     * @param cosmetic the cosmetic to equip
     */
    @ApiStatus.Internal
    void setCosmetic(CosmeticCategory<?, ?, ?> category, Cosmetic<?, ?> cosmetic);

    /**
     * Gets all currently equipped cosmetics across all categories.
     *
     * @return a map of categories to their equipped cosmetics
     */
    @ApiStatus.Internal
    Map<CosmeticCategory<?, ?, ?>, Cosmetic<?, ?>> getCosmetics();

    /**
     * Equips all cosmetics previously saved for this player.
     *
     * @param silent {@code true} to suppress messages or sounds
     */
    void equipSavedCosmetics(boolean silent);

    /**
     * Unequips a cosmetic in a specific category.
     *
     * @param category       the category to unequip
     * @param silent         {@code true} to suppress messages or sounds
     * @param saveToDatabase {@code true} to update the database
     */
    void unequipCosmetic(CosmeticCategory<?, ?, ?> category, boolean silent, boolean saveToDatabase);

    /**
     * Unequips all equipped cosmetics.
     *
     * @param silent         {@code true} to suppress messages or sounds
     * @param saveToDatabase {@code true} to update the database
     */
    void unequipCosmetics(boolean silent, boolean saveToDatabase);

    /**
     * Removes a cosmetic from the player’s data entirely.
     *
     * @param category       the category to remove
     * @param silent         {@code true} to suppress messages or sounds
     * @param saveToDatabase {@code true} to update the database
     */
    void removeCosmetic(CosmeticCategory<?, ?, ?> category, boolean silent, boolean saveToDatabase);

    /**
     * Clears all cosmetics from the player.
     *
     * @param silent         {@code true} to suppress messages or sounds
     * @param saveToDatabase {@code true} to update the database
     */
    void clearAllCosmetics(boolean silent, boolean saveToDatabase);

    /**
     * Checks whether a gadget is currently on cooldown for this player.
     *
     * @param gadget the gadget type
     * @return {@code true} if the gadget is on cooldown
     */
    boolean hasCooldown(GadgetType gadget);

    /**
     * Gets the cooldown time for a gadget.
     *
     * @param gadget the gadget type
     * @return the remaining cooldown in seconds
     */
    double getCooldown(GadgetType gadget);

    /**
     * Sets a cooldown for a gadget.
     *
     * @param gadget the gadget type
     * @param time   the cooldown duration in seconds
     */
    void setCooldown(GadgetType gadget, double time);

}
