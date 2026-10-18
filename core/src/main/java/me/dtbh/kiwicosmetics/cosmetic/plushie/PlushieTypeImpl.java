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

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.api.config.Config;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticRarity;
import me.dtbh.kiwicosmetics.api.cosmetic.plushie.Plushie;
import me.dtbh.kiwicosmetics.api.cosmetic.plushie.PlushieBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.plushie.PlushieType;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CosmeticCategory;
import me.dtbh.kiwicosmetics.api.locale.Translator;
import me.dtbh.kiwicosmetics.api.user.User;
import me.dtbh.kiwicosmetics.cosmetic.CosmeticTypeImpl;

import java.util.function.Supplier;

public class PlushieTypeImpl extends CosmeticTypeImpl<PlushieType, PlushieBehavior> implements PlushieType {

    private final String skin;
    private final double scale;
    @Nullable
    private final String name;

    public PlushieTypeImpl(String key,
                           CosmeticCategory<PlushieType, PlushieBehavior, ?> category,
                           Supplier<PlushieBehavior> behaviorFactory,
                           boolean enabled,
                           CosmeticRarity rarity,
                           ItemStack itemStack,
                           String skin,
                           double scale,
                           @Nullable String name) {
        super(key, category, behaviorFactory, enabled, rarity, itemStack);
        this.skin = skin;
        this.scale = scale;
        this.name = name;
    }

    @Override
    protected Plushie createInstance(KiwiCosmeticsPlugin plugin, User user, PlushieBehavior behavior) {
        return new PlushieImpl(plugin, user, this, behavior);
    }

    @Override
    public String getName(Translator translator) {
        // Plushies added to plushies.yml can carry their own name instead of a translation
        if (name != null && !name.isEmpty()) {
            return name;
        }
        return super.getName(translator);
    }

    @Override
    public String getSkin() {
        return skin;
    }

    @Override
    public double getScale() {
        return scale;
    }

    public static class BuilderImpl extends CosmeticTypeImpl.BuilderImpl<PlushieType, PlushieBehavior, PlushieType.Builder> implements PlushieType.Builder {

        private String skin = PlushieSkins.SELF;
        private double scale = 0.5d;
        private String name;

        public BuilderImpl(String key, CosmeticCategory<PlushieType, PlushieBehavior, ?> category) {
            super(key, category);
        }

        @Override
        protected PlushieType.Builder self() {
            return this;
        }

        @Override
        public PlushieType.Builder readFromConfig() {
            super.readFromConfig();

            Config config = category.getConfig();
            String path = getPath();

            if (config.hasKey(path + "skin")) {
                skin = config.getString(path + "skin");
            }
            if (config.hasKey(path + "scale")) {
                scale = config.getDouble(path + "scale");
            }
            name = config.hasKey(path + "name") ? config.getString(path + "name") : null;

            return this;
        }

        @Override
        public PlushieType.Builder skin(String skin) {
            this.skin = skin;
            return this;
        }

        @Override
        public PlushieType.Builder scale(double scale) {
            this.scale = scale;
            return this;
        }

        @Override
        public PlushieType build() {
            return new PlushieTypeImpl(key,
                    category,
                    factory,
                    enabled,
                    rarity,
                    itemStack,
                    skin,
                    scale,
                    name
            );
        }
    }
}
