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

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import se.filledev.procosmetics.ProCosmeticsPlugin;
import se.filledev.procosmetics.api.cosmetic.CosmeticType;
import se.filledev.procosmetics.api.cosmetic.registry.CosmeticCategory;
import se.filledev.procosmetics.cosmetic.CosmeticTypeImpl;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Remembers which cosmetics a player had equipped, stored on the player itself
 * (persistent data container) so no database is needed.
 * Format: {@code category=cosmetic;category=cosmetic}
 */
public class UserStorage {

    private final ProCosmeticsPlugin plugin;
    private final NamespacedKey equippedKey;

    public UserStorage(ProCosmeticsPlugin plugin) {
        this.plugin = plugin;
        this.equippedKey = new NamespacedKey(plugin, "equipped");
    }

    public void loadEquipped(Player player, UserImpl user) {
        for (Map.Entry<String, String> entry : read(player).entrySet()) {
            CosmeticCategory<?, ?, ?> category = plugin.getCategoryRegistries().getCategoryRaw(entry.getKey());

            if (category == null || !category.isEnabled()) {
                continue;
            }
            CosmeticType<?, ?> type = category.getCosmeticRegistry().getType(entry.getValue());

            if (type == null || !type.isEnabled()) {
                continue;
            }
            user.setCosmetic(category, ((CosmeticTypeImpl<?, ?>) type).create(plugin, user));
        }
    }

    public void saveEquipped(Player player, CosmeticType<?, ?> type) {
        Map<String, String> equipped = read(player);
        equipped.put(type.getCategory().getKey(), type.getKey());
        write(player, equipped);
    }

    public void removeEquipped(Player player, CosmeticCategory<?, ?, ?> category) {
        Map<String, String> equipped = read(player);

        if (equipped.remove(category.getKey()) != null) {
            write(player, equipped);
        }
    }

    private Map<String, String> read(Player player) {
        Map<String, String> equipped = new LinkedHashMap<>();
        String raw = player.getPersistentDataContainer().get(equippedKey, PersistentDataType.STRING);

        if (raw == null || raw.isEmpty()) {
            return equipped;
        }
        for (String part : raw.split(";")) {
            int index = part.indexOf('=');

            if (index > 0 && index < part.length() - 1) {
                equipped.put(part.substring(0, index), part.substring(index + 1));
            }
        }
        return equipped;
    }

    private void write(Player player, Map<String, String> equipped) {
        PersistentDataContainer container = player.getPersistentDataContainer();

        if (equipped.isEmpty()) {
            container.remove(equippedKey);
            return;
        }
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, String> entry : equipped.entrySet()) {
            if (!builder.isEmpty()) {
                builder.append(';');
            }
            builder.append(entry.getKey()).append('=').append(entry.getValue());
        }
        container.set(equippedKey, PersistentDataType.STRING, builder.toString());
    }
}
