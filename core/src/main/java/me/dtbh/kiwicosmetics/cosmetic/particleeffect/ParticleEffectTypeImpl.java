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
package me.dtbh.kiwicosmetics.cosmetic.particleeffect;

import org.bukkit.inventory.ItemStack;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticRarity;
import me.dtbh.kiwicosmetics.api.cosmetic.particleeffect.ParticleEffect;
import me.dtbh.kiwicosmetics.api.cosmetic.particleeffect.ParticleEffectBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.particleeffect.ParticleEffectType;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CosmeticCategory;
import me.dtbh.kiwicosmetics.api.user.User;
import me.dtbh.kiwicosmetics.cosmetic.CosmeticTypeImpl;

import java.util.List;
import java.util.function.Supplier;

public class ParticleEffectTypeImpl extends CosmeticTypeImpl<ParticleEffectType, ParticleEffectBehavior> implements ParticleEffectType {

    private final int repeatDelay;

    public ParticleEffectTypeImpl(String key,
                                  CosmeticCategory<ParticleEffectType, ParticleEffectBehavior, ?> category,
                                  Supplier<ParticleEffectBehavior> behaviorFactory,
                                  boolean enabled,
                                  CosmeticRarity rarity,
                                  ItemStack itemStack,
                                  int repeatDelay) {
        super(key, category, behaviorFactory, enabled, rarity, itemStack);
        this.repeatDelay = repeatDelay;
    }

    @Override
    protected ParticleEffect createInstance(KiwiCosmeticsPlugin plugin, User user, ParticleEffectBehavior behavior) {
        return new ParticleEffectImpl(plugin, user, this, behavior);
    }

    @Override
    public int getRepeatDelay() {
        return repeatDelay;
    }

    public static class BuilderImpl extends CosmeticTypeImpl.BuilderImpl<ParticleEffectType, ParticleEffectBehavior, ParticleEffectType.Builder> implements ParticleEffectType.Builder {

        private int repeatDelay = 1;

        public BuilderImpl(String key, CosmeticCategory<ParticleEffectType, ParticleEffectBehavior, ?> category) {
            super(key, category);
        }

        @Override
        protected ParticleEffectType.Builder self() {
            return this;
        }

        @Override
        public ParticleEffectType.Builder repeatDelay(int repeatDelay) {
            this.repeatDelay = repeatDelay;
            return this;
        }

        @Override
        public ParticleEffectType build() {
            return new ParticleEffectTypeImpl(key,
                    category,
                    factory,
                    enabled,
                    rarity,
                    itemStack,
                    repeatDelay
            );
        }
    }
}
