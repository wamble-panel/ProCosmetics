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
package me.dtbh.kiwicosmetics.cosmetic.plushie;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Every plushie item handed out is a claim, saved in plushie_claims.yml. Items carry their claim id,
 * so a player can only claim a plushie as often as their limit allows, and copied or old items
 * (no claim, or a claim that is already placed) can't be placed.
 */
public class PlushieClaims {

    private final KiwiCosmeticsPlugin plugin;
    private final File file;
    private final Map<String, Claim> claims = new HashMap<>();
    private boolean saveScheduled;

    /**
     * @param owner  the player who claimed it
     * @param type   the plushie key
     * @param placed the mannequin it is placed as, or null while it is an item
     */
    public record Claim(UUID owner, String type, @Nullable UUID placed) {
    }

    public PlushieClaims(KiwiCosmeticsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "plushie_claims.yml");
        load();
    }

    private void load() {
        if (!file.exists()) {
            return;
        }
        ConfigurationSection section = YamlConfiguration.loadConfiguration(file).getConfigurationSection("claims");

        if (section == null) {
            return;
        }
        for (String id : section.getKeys(false)) {
            try {
                UUID owner = UUID.fromString(section.getString(id + ".owner", ""));
                String type = section.getString(id + ".plushie", "");
                String placed = section.getString(id + ".placed", "");
                claims.put(id, new Claim(owner, type, placed.isEmpty() ? null : UUID.fromString(placed)));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Skipping broken plushie claim " + id + " in plushie_claims.yml.");
            }
        }
    }

    @Nullable
    public Claim get(@Nullable String id) {
        return id == null ? null : claims.get(id);
    }

    public int count(UUID owner, String type) {
        int count = 0;

        for (Claim claim : claims.values()) {
            if (claim.owner().equals(owner) && claim.type().equals(type)) {
                count++;
            }
        }
        return count;
    }

    public String create(UUID owner, String type) {
        String id = UUID.randomUUID().toString();
        claims.put(id, new Claim(owner, type, null));
        scheduleSave();
        return id;
    }

    public void setPlaced(String id, @Nullable UUID placed) {
        Claim claim = claims.get(id);

        if (claim != null) {
            claims.put(id, new Claim(claim.owner(), claim.type(), placed));
            scheduleSave();
        }
    }

    /**
     * Forgets a player's claims (all plushies, or one), so they can claim again. Items of those
     * claims stop working; plushies already placed stay where they are.
     *
     * @return how many claims were removed
     */
    public int reset(UUID owner, @Nullable String type) {
        int removed = 0;
        Iterator<Claim> iterator = claims.values().iterator();

        while (iterator.hasNext()) {
            Claim claim = iterator.next();

            if (claim.owner().equals(owner) && (type == null || claim.type().equals(type))) {
                iterator.remove();
                removed++;
            }
        }
        if (removed > 0) {
            scheduleSave();
        }
        return removed;
    }

    /**
     * Saves a second later, so a burst of changes is written once.
     */
    private void scheduleSave() {
        if (saveScheduled || !plugin.isEnabled()) {
            save();
            return;
        }
        saveScheduled = true;
        plugin.getServer().getScheduler().runTaskLater(plugin, this::save, 20L);
    }

    public void save() {
        saveScheduled = false;
        YamlConfiguration config = new YamlConfiguration();

        for (Map.Entry<String, Claim> entry : claims.entrySet()) {
            String path = "claims." + entry.getKey();
            Claim claim = entry.getValue();
            config.set(path + ".owner", claim.owner().toString());
            config.set(path + ".plushie", claim.type());

            if (claim.placed() != null) {
                config.set(path + ".placed", claim.placed().toString());
            }
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save plushie_claims.yml.", e);
        }
    }
}
