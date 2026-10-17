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
package me.dtbh.kiwicosmetics.placeholder.outgoing;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.api.cosmetic.Cosmetic;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CosmeticCategory;
import me.dtbh.kiwicosmetics.api.user.User;

import javax.annotation.Nonnull;
import java.util.List;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {

    private final KiwiCosmeticsPlugin plugin;

    public PlaceholderAPIExpansion(KiwiCosmeticsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String identifier) {
        if (identifier.startsWith("equipped_")) {
            for (CosmeticCategory<?, ?, ?> category : plugin.getCategoryRegistries().getCategories()) {
                if (identifier.equals("equipped_" + category.getKey())) {
                    if (offlinePlayer == null) {
                        return "None";
                    }
                    User user = plugin.getUserManager().getConnected(offlinePlayer.getUniqueId());

                    if (user == null) {
                        return "None";
                    }
                    Cosmetic<?, ?> cosmetic = user.getCosmetic(category);

                    if (cosmetic == null || !cosmetic.isEquipped()) {
                        return "None";
                    }
                    return cosmetic.getType().getName(plugin.getLanguageManager());
                }
            }
        } else if (identifier.startsWith("unlocked_")) {
            for (CosmeticCategory<?, ?, ?> category : plugin.getCategoryRegistries().getCategories()) {
                if (identifier.equals("unlocked_" + category.getKey())) {
                    int total = category.getCosmeticRegistry().getEnabledTypes().size();

                    if (offlinePlayer == null) {
                        return "0/" + total;
                    }
                    Player player = offlinePlayer.getPlayer();

                    if (player == null) {
                        return "0/" + total;
                    }
                    return category.getUnlockedCosmetics(player) + "/" + total;
                }
            }
        }
        // Unknown placeholder, let PlaceholderAPI leave it untouched
        return null;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Nonnull
    @Override
    public String getAuthor() {
        List<String> authors = plugin.getJavaPlugin().getDescription().getAuthors();

        if (authors.isEmpty()) {
            return "unknown";
        }
        return authors.getFirst();
    }

    @Nonnull
    @Override
    public String getIdentifier() {
        return "kiwicosmetics";
    }

    @Nonnull
    @Override
    public String getVersion() {
        return "1.0.0";
    }
}
