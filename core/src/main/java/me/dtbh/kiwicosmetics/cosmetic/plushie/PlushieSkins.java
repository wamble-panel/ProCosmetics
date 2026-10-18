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
package me.dtbh.kiwicosmetics.cosmetic.plushie;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.Nullable;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Turns the {@code skin} of a plushie into a profile: {@code self}, a player name,
 * a skin texture URL or a base64 {@code textures} value.
 */
public final class PlushieSkins {

    public static final String SELF = "self";

    private static final String PROFILE_NAME = "KiwiPlushie";
    private static final Map<String, PlayerProfile> CACHE = new ConcurrentHashMap<>();

    private PlushieSkins() {
    }

    /**
     * Resolves a skin and hands the profile to the callback on the main thread.
     * Player names are looked up asynchronously the first time and cached afterwards.
     * The callback is not called when the skin cannot be resolved.
     */
    public static void resolve(KiwiCosmeticsPlugin plugin, Player owner, String skin, Consumer<PlayerProfile> callback) {
        if (skin == null || skin.isBlank() || skin.equalsIgnoreCase(SELF)) {
            callback.accept(owner.getPlayerProfile());
            return;
        }
        String key = skin.trim();
        PlayerProfile cached = CACHE.get(key.toLowerCase(Locale.ROOT));

        if (cached != null) {
            callback.accept(cached);
            return;
        }

        if (key.length() <= 16) {
            Player online = Bukkit.getPlayerExact(key);

            if (online != null) {
                callback.accept(online.getPlayerProfile());
                return;
            }
            lookup(plugin, key, callback);
            return;
        }
        PlayerProfile profile = fromTexture(plugin, key);

        if (profile != null) {
            CACHE.put(key.toLowerCase(Locale.ROOT), profile);
            callback.accept(profile);
        }
    }

    private static void lookup(KiwiCosmeticsPlugin plugin, String name, Consumer<PlayerProfile> callback) {
        PlayerProfile profile;

        try {
            profile = Bukkit.createPlayerProfile(name);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid plushie skin: " + name + ".");
            return;
        }
        profile.update().whenComplete((updated, throwable) -> {
            if (throwable != null || updated == null) {
                plugin.getLogger().log(Level.WARNING, "Could not look up the skin of " + name + " for a plushie.", throwable);
                return;
            }
            CACHE.put(name.toLowerCase(Locale.ROOT), updated);
            plugin.getSyncExecutor().execute(() -> callback.accept(updated));
        });
    }

    @Nullable
    private static PlayerProfile fromTexture(KiwiCosmeticsPlugin plugin, String skin) {
        try {
            String url = skin;
            PlayerTextures.SkinModel model = PlayerTextures.SkinModel.CLASSIC;

            if (!skin.startsWith("http")) {
                // A base64 "textures" value: {"textures":{"SKIN":{"url":"...","metadata":{"model":"slim"}}}}
                String json = new String(Base64.getDecoder().decode(skin), StandardCharsets.UTF_8);
                JsonObject skinObject = JsonParser.parseString(json).getAsJsonObject()
                        .getAsJsonObject("textures")
                        .getAsJsonObject("SKIN");
                url = skinObject.get("url").getAsString();

                JsonObject metadata = skinObject.getAsJsonObject("metadata");
                if (metadata != null && metadata.has("model") && metadata.get("model").getAsString().equalsIgnoreCase("slim")) {
                    model = PlayerTextures.SkinModel.SLIM;
                }
            }
            URL skinUrl = URI.create(url).toURL();
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.nameUUIDFromBytes(skin.getBytes(StandardCharsets.UTF_8)), PROFILE_NAME);
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(skinUrl, model);
            profile.setTextures(textures);
            return profile;
        } catch (RuntimeException | MalformedURLException e) {
            plugin.getLogger().warning("Invalid plushie skin texture: " + e.getMessage());
            return null;
        }
    }
}
