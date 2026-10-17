/*
 * This file is part of KiwiCosmetics, a fork of ProCosmetics - https://github.com/FilleDev/ProCosmetics
 * Copyright (C) 2026 FilleDev, dtbh and contributors
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
package me.dtbh.kiwicosmetics.wardrobe.rainbow;

import me.dtbh.kiwicosmetics.wardrobe.rainbow.ArmorPiece;
import me.dtbh.kiwicosmetics.wardrobe.rainbow.ColorPattern;
import me.dtbh.kiwicosmetics.wardrobe.Wardrobe;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Runs every tick and recolors worn rainbow armor.
 * <p>
 * Reading item meta is the expensive part, so each player's armor is only inspected every
 * {@code scan-interval} ticks, and only leather pieces are ever opened. The result is cached
 * per player, so between scans players without rainbow armor cost nothing at all.
 */
public class RainbowTask implements Runnable {
    private final Wardrobe plugin;
    private final Map<UUID, WornPatterns> worn = new HashMap<>();
    private long tick;

    public RainbowTask(Wardrobe plugin) {
        this.plugin = plugin;
    }

    public long tick() {
        return tick;
    }

    /** Forget all cached armor, e.g. after a reload replaced the pattern objects. */
    public void reset() {
        worn.clear();
    }

    public void forget(UUID player) {
        worn.remove(player);
    }

    @Override
    public void run() {
        long now = tick++;
        long scanInterval = plugin.scanInterval();
        ParticleSettings particles = plugin.particleSettings();

        for (Player player : Bukkit.getOnlinePlayers()) {
            WornPatterns state = worn.computeIfAbsent(player.getUniqueId(), id -> new WornPatterns());
            if (now >= state.nextScan) {
                scan(player, state);
                state.nextScan = now + scanInterval;
            }
            if (!state.any) {
                continue;
            }

            ItemStack[] armor = null;
            Color particleColor = null;
            for (ArmorPiece piece : ArmorPiece.VALUES) {
                ColorPattern pattern = state.patterns[piece.ordinal()];
                if (pattern == null || now % pattern.interval() != 0) {
                    continue;
                }
                if (armor == null) {
                    armor = player.getInventory().getArmorContents();
                }
                Color color = pattern.colorAt(now);
                if (!applyColor(armor[piece.armorIndex()], piece, pattern, color)) {
                    // The piece was swapped out since the last scan; rescan next tick.
                    state.patterns[piece.ordinal()] = null;
                    state.nextScan = now + 1;
                    continue;
                }
                if (piece == particles.piece()) {
                    particleColor = color;
                }
            }

            if (particleColor != null && particles.enabled()) {
                spawnParticles(player, particleColor, particles);
            }
        }
    }

    private void scan(Player player, WornPatterns state) {
        ItemStack[] armor = player.getInventory().getArmorContents();
        boolean any = false;
        for (ArmorPiece piece : ArmorPiece.VALUES) {
            ItemStack item = armor[piece.armorIndex()];
            // Cheap type check first so non-leather armor never has its meta read.
            ColorPattern pattern = item != null && item.getType() == piece.material()
                    ? plugin.pattern(item)
                    : null;
            state.patterns[piece.ordinal()] = pattern;
            any |= pattern != null;
        }
        state.any = any;
    }

    /**
     * @return false if the item is no longer a piece of this pattern
     */
    private boolean applyColor(ItemStack item, ArmorPiece piece, ColorPattern pattern, Color color) {
        if (item == null || item.getType() != piece.material()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof LeatherArmorMeta leather) || !pattern.name().equals(plugin.patternName(meta))) {
            return false;
        }
        if (!color.equals(leather.getColor())) {
            leather.setColor(color);
            item.setItemMeta(leather);
        }
        return true;
    }

    private void spawnParticles(Player player, Color color, ParticleSettings settings) {
        if (settings.count() <= 0) {
            return;
        }
        if (settings.hideForSpectators() && player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        if (settings.hideWhenInvisible() && player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            return;
        }
        Location location = player.getLocation();
        Particle.DustOptions dust = new Particle.DustOptions(color, settings.size());
        double maxDistanceSquared = settings.viewDistance() * settings.viewDistance();
        double offset = settings.offset();
        for (Player viewer : player.getWorld().getPlayers()) {
            // canSee respects vanish plugins, so vanished staff don't give themselves away.
            if (viewer != player && !viewer.canSee(player)) {
                continue;
            }
            if (viewer.getLocation().distanceSquared(location) > maxDistanceSquared) {
                continue;
            }
            viewer.spawnParticle(Particle.DUST, location, settings.count(), offset, offset, offset, 0, dust);
        }
    }

    private static final class WornPatterns {
        private final ColorPattern[] patterns = new ColorPattern[ArmorPiece.VALUES.length];
        private boolean any;
        private long nextScan;
    }
}
