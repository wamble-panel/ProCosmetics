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
package me.dtbh.kiwicosmetics.cosmetic.deatheffect;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.api.cosmetic.deatheffect.DeathEffect;
import me.dtbh.kiwicosmetics.api.cosmetic.deatheffect.DeathEffectBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.deatheffect.DeathEffectType;
import me.dtbh.kiwicosmetics.api.user.User;
import me.dtbh.kiwicosmetics.cosmetic.CosmeticImpl;

public class DeathEffectImpl extends CosmeticImpl<DeathEffectType, DeathEffectBehavior> implements DeathEffect {

    private final boolean killEffects;

    public DeathEffectImpl(KiwiCosmeticsPlugin plugin, User user, DeathEffectType type, DeathEffectBehavior behavior) {
        super(plugin, user, type, behavior);
        killEffects = type.getCategory().getConfig().getBoolean("kill_effects");
    }

    @Override
    protected void onEquip() {
    }

    @Override
    protected void onUpdate() {
    }

    @Override
    protected void onUnequip() {
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (killEffects) {
            Player vitim = event.getEntity();

            if (vitim.getKiller() == player) {
                behavior.playEffect(this, vitim.getLocation());
            }
        } else {
            if (event.getEntity() == player) {
                behavior.playEffect(this, player.getLocation());
            }
        }
    }
}
