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

import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.api.cosmetic.plushie.Plushie;
import me.dtbh.kiwicosmetics.api.cosmetic.plushie.PlushieBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.plushie.PlushieType;
import me.dtbh.kiwicosmetics.api.user.User;
import me.dtbh.kiwicosmetics.cosmetic.CosmeticImpl;

/**
 * Plushies are never worn: choosing one hands out a plushie item, which {@link PlushieFurniture}
 * turns into a placed plushie. The permission, world and region checks still run first.
 */
public class PlushieImpl extends CosmeticImpl<PlushieType, PlushieBehavior> implements Plushie {

    public PlushieImpl(KiwiCosmeticsPlugin plugin, User user, PlushieType type, PlushieBehavior behavior) {
        super(plugin, user, type, behavior);
    }

    @Override
    protected boolean canEquip() {
        plugin.getPlushieFurniture().giveItem(user, player, cosmeticType);
        // Nothing to wear, so the plushie never counts as equipped
        return false;
    }

    @Override
    protected void onEquip() {
    }

    @Override
    public void onUpdate() {
    }

    @Override
    protected void onUnequip() {
    }
}
