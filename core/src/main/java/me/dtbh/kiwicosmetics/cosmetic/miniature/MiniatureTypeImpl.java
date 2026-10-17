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
package me.dtbh.kiwicosmetics.cosmetic.miniature;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.api.config.Config;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticRarity;
import me.dtbh.kiwicosmetics.api.cosmetic.miniature.Miniature;
import me.dtbh.kiwicosmetics.api.cosmetic.miniature.MiniatureBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.miniature.MiniatureType;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CosmeticCategory;
import me.dtbh.kiwicosmetics.api.user.User;
import me.dtbh.kiwicosmetics.cosmetic.CosmeticTypeImpl;

import java.util.List;
import java.util.function.Supplier;

public class MiniatureTypeImpl extends CosmeticTypeImpl<MiniatureType, MiniatureBehavior> implements MiniatureType {

    private final EntityType entityType;
    private final boolean baby;
    private final double scale;

    public MiniatureTypeImpl(String key,
                             CosmeticCategory<MiniatureType, MiniatureBehavior, ?> category,
                             Supplier<MiniatureBehavior> behaviorFactory,
                             boolean enabled,
                             CosmeticRarity rarity,
                             ItemStack itemStack,
                             EntityType entityType,
                             boolean baby,
                             double scale) {
        super(key, category, behaviorFactory, enabled, rarity, itemStack);
        this.entityType = entityType;
        this.baby = baby;
        this.scale = scale;
    }

    @Override
    protected Miniature createInstance(KiwiCosmeticsPlugin plugin, User user, MiniatureBehavior behavior) {
        return new MiniatureImpl(plugin, user, this, behavior);
    }

    @Override
    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public boolean isBaby() {
        return baby;
    }

    @Override
    public double getScale() {
        return scale;
    }

    public static class BuilderImpl extends CosmeticTypeImpl.BuilderImpl<MiniatureType, MiniatureBehavior, MiniatureType.Builder> implements MiniatureType.Builder {

        private EntityType entityType;
        private boolean baby;
        private double scale = 1.0d;

        public BuilderImpl(String key, CosmeticCategory<MiniatureType, MiniatureBehavior, ?> category) {
            super(key, category);
        }

        @Override
        protected MiniatureType.Builder self() {
            return this;
        }

        @Override
        public MiniatureType.Builder readFromConfig() {
            super.readFromConfig();

            Config config = category.getConfig();
            String path = getPath();

            entityType = parseEntity(path + "entity");
            baby = config.getBoolean(path + "baby");
            scale = config.getDouble(path + "scale");

            return this;
        }

        @Override
        public MiniatureType.Builder entityType(EntityType entityType) {
            this.entityType = entityType;
            return this;
        }

        @Override
        public MiniatureType.Builder baby(boolean baby) {
            this.baby = baby;
            return this;
        }

        @Override
        public MiniatureType.Builder scale(double scale) {
            this.scale = scale;
            return this;
        }

        @Override
        public MiniatureType build() {
            return new MiniatureTypeImpl(key,
                    category,
                    factory,
                    enabled,
                    rarity,
                    itemStack,
                    entityType,
                    baby,
                    scale
            );
        }
    }
}
