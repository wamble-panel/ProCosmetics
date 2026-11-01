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

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helpers for particles that only exist on newer Minecraft versions.
 * <p>
 * The plugin compiles against 26.2 but runs on 26.1+, so particles added in 26.2 (sulfur, noxious gas, geysers)
 * and 26.3 (poplar leaves) are looked up by name at runtime and replaced by a fallback when missing.
 * <p>
 * Players joining with an older client through ViaVersion can't read those particles (their client
 * disconnects with "Failed to decode packet 'level_particles'"), so they get the fallback instead.
 */
public final class ParticleUtil {

    // Vanilla only sends particles to players within 32 blocks
    private static final double VIEW_DISTANCE_SQUARED = 32.0d * 32.0d;

    private static final Map<String, Particle> CACHE = new ConcurrentHashMap<>();
    // Particles newer than some clients, with what older clients get instead
    private static final Map<Particle, Particle> FALLBACKS = new ConcurrentHashMap<>();

    private ParticleUtil() {
    }

    /**
     * @return the particle with this enum name, or the fallback when this server version doesn't have it
     */
    public static Particle find(String name, Particle fallback) {
        return CACHE.computeIfAbsent(name, key -> {
            try {
                Particle particle = Particle.valueOf(key);
                FALLBACKS.put(particle, fallback);
                return particle;
            } catch (IllegalArgumentException e) {
                return fallback;
            }
        });
    }

    /**
     * Spawns a particle, passing {@code data} only when the particle accepts that type.
     * Particles that need data we don't have are skipped instead of throwing.
     *
     * @param data the particle data (Color, DustOptions, Trail...), or null
     */
    public static void spawn(Location location, Particle particle, int count,
                             double offsetX, double offsetY, double offsetZ, double speed, Object data) {
        Particle fallback = FALLBACKS.get(particle);

        if (fallback != null && ViaVersionHook.isActive()) {
            spawnPerPlayer(location, particle, fallback, count, offsetX, offsetY, offsetZ, speed, data);
            return;
        }
        Object particleData = dataFor(particle, data);

        if (particleData == SKIP) {
            return;
        }
        if (particleData == null) {
            location.getWorld().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
        } else {
            location.getWorld().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed, particleData);
        }
    }

    /**
     * Sends a new particle to players whose client knows it and the fallback to older clients.
     */
    private static void spawnPerPlayer(Location location, Particle particle, Particle fallback, int count,
                                       double offsetX, double offsetY, double offsetZ, double speed, Object data) {
        Location tmp = location.clone();

        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation(tmp).distanceSquared(location) > VIEW_DISTANCE_SQUARED) {
                continue;
            }
            Particle shown = ViaVersionHook.isOlderClient(player) ? fallback : particle;
            Object particleData = dataFor(shown, data);

            if (particleData == SKIP) {
                continue;
            }
            if (particleData == null) {
                player.spawnParticle(shown, location, count, offsetX, offsetY, offsetZ, speed);
            } else {
                player.spawnParticle(shown, location, count, offsetX, offsetY, offsetZ, speed, particleData);
            }
        }
    }

    private static final Object SKIP = new Object();

    /**
     * The data to send with a particle: null for particles without data, {@link #SKIP} when the
     * particle needs data we don't have.
     */
    private static Object dataFor(Particle particle, Object data) {
        Class<?> dataType = particle.getDataType();

        if (dataType == Void.class) {
            return null;
        }
        if (data != null && dataType.isInstance(data)) {
            return data;
        }
        // A few particles take a color, give them one so a fallback like TINTED_LEAVES still shows
        if (dataType == Color.class) {
            return data instanceof Particle.DustOptions dust ? dust.getColor() : Color.WHITE;
        }
        // DUST (the NOXIOUS_GAS fallback) needs dust options
        if (dataType == Particle.DustOptions.class && data instanceof Color color) {
            return new Particle.DustOptions(color, 1.0f);
        }
        return SKIP;
    }

    public static void spawn(Location location, Particle particle, int count,
                             double offsetX, double offsetY, double offsetZ, double speed) {
        spawn(location, particle, count, offsetX, offsetY, offsetZ, speed, null);
    }

    /**
     * Hue (0-1) to a fully saturated, bright color.
     */
    public static Color hue(float hue) {
        return hsv(hue, 1.0f, 1.0f);
    }

    public static Color hsv(float hue, float saturation, float value) {
        float h = (hue - (float) Math.floor(hue)) * 6.0f;
        int sector = (int) h;
        float f = h - sector;
        float p = value * (1.0f - saturation);
        float q = value * (1.0f - saturation * f);
        float t = value * (1.0f - saturation * (1.0f - f));
        float r = value;
        float g = value;
        float b = value;

        switch (sector) {
            case 0 -> { r = value; g = t; b = p; }
            case 1 -> { r = q; g = value; b = p; }
            case 2 -> { r = p; g = value; b = t; }
            case 3 -> { r = p; g = q; b = value; }
            case 4 -> { r = t; g = p; b = value; }
            default -> { r = value; g = p; b = q; }
        }
        return Color.fromRGB((int) (r * 255.0f), (int) (g * 255.0f), (int) (b * 255.0f));
    }

    /**
     * Linear blend between two colors, {@code t} from 0 (a) to 1 (b).
     */
    public static Color blend(Color a, Color b, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        return Color.fromRGB(
                (int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t)
        );
    }
}
