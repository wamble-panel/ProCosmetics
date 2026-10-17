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
package me.dtbh.kiwicosmetics.cosmetic.arroweffect;

import org.bukkit.inventory.ItemStack;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticRarity;
import me.dtbh.kiwicosmetics.api.cosmetic.arroweffect.ArrowEffect;
import me.dtbh.kiwicosmetics.api.cosmetic.arroweffect.ArrowEffectBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.arroweffect.ArrowEffectType;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CosmeticCategory;
import me.dtbh.kiwicosmetics.api.user.User;
import me.dtbh.kiwicosmetics.cosmetic.CosmeticTypeImpl;

import java.util.List;
import java.util.function.Supplier;

public class ArrowEffectTypeImpl extends CosmeticTypeImpl<ArrowEffectType, ArrowEffectBehavior> implements ArrowEffectType {

    public ArrowEffectTypeImpl(String key,
                               CosmeticCategory<ArrowEffectType, ArrowEffectBehavior, ?> category,
                               Supplier<ArrowEffectBehavior> behaviorFactory,
                               boolean enabled,
                               CosmeticRarity rarity,
                               ItemStack itemStack) {
        super(key, category, behaviorFactory, enabled, rarity, itemStack);
    }

    @Override
    protected ArrowEffect createInstance(KiwiCosmeticsPlugin plugin, User user, ArrowEffectBehavior behavior) {
        return new ArrowEffectImpl(plugin, user, this, behavior);
    }

    public static class BuilderImpl extends CosmeticTypeImpl.BuilderImpl<ArrowEffectType, ArrowEffectBehavior, ArrowEffectType.Builder> implements ArrowEffectType.Builder {

        public BuilderImpl(String key, CosmeticCategory<ArrowEffectType, ArrowEffectBehavior, ?> category) {
            super(key, category);
        }

        @Override
        protected ArrowEffectType.Builder self() {
            return this;
        }

        @Override
        public ArrowEffectType build() {
            return new ArrowEffectTypeImpl(key,
                    category,
                    factory,
                    enabled,
                    rarity,
                    itemStack
            );
        }
    }
}
