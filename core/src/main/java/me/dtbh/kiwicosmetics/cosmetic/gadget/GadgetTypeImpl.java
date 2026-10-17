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
package me.dtbh.kiwicosmetics.cosmetic.gadget;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.api.config.Config;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticRarity;
import me.dtbh.kiwicosmetics.api.cosmetic.gadget.Gadget;
import me.dtbh.kiwicosmetics.api.cosmetic.gadget.GadgetBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.gadget.GadgetType;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CosmeticCategory;
import me.dtbh.kiwicosmetics.api.user.User;
import me.dtbh.kiwicosmetics.api.util.structure.StructureData;
import me.dtbh.kiwicosmetics.cosmetic.CosmeticTypeImpl;
import me.dtbh.kiwicosmetics.util.structure.StructureReader;

import java.util.function.Supplier;

public class GadgetTypeImpl extends CosmeticTypeImpl<GadgetType, GadgetBehavior> implements GadgetType {

    private final double cooldown;
    private final double duration;
    private final StructureData structure;

    public GadgetTypeImpl(String key,
                          CosmeticCategory<GadgetType, GadgetBehavior, ?> category,
                          Supplier<GadgetBehavior> behaviourFactory,
                          boolean enabled,
                          CosmeticRarity rarity,
                          ItemStack itemStack,
                          double cooldown,
                          double duration,
                          StructureData structure) {
        super(key, category, behaviourFactory, enabled, rarity, itemStack);
        this.cooldown = cooldown;
        this.duration = duration;
        this.structure = structure;
    }

    @Override
    protected Gadget createInstance(KiwiCosmeticsPlugin plugin, User user, GadgetBehavior behaviour) {
        return new GadgetImpl(plugin, user, this, behaviour);
    }

    @Override
    public double getCooldown() {
        return cooldown;
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public @Nullable StructureData getStructure() {
        return structure;
    }

    public static class BuilderImpl extends CosmeticTypeImpl.BuilderImpl<GadgetType, GadgetBehavior, GadgetType.Builder> implements GadgetType.Builder {

        private double cooldown;
        private double duration;
        private StructureData structure;

        public BuilderImpl(String key, CosmeticCategory<GadgetType, GadgetBehavior, ?> category) {
            super(key, category);
        }

        @Override
        protected GadgetType.Builder self() {
            return this;
        }

        @Override
        public GadgetType.Builder readFromConfig() {
            super.readFromConfig();

            Config config = category.getConfig();
            String path = getPath();

            cooldown = Math.max(0.1d, config.getDouble(path + "cooldown"));
            duration = Math.min(cooldown, config.getDouble(path + "duration"));
            structure = StructureReader.loadStructure(key);
            return self();
        }

        @Override
        public GadgetType.Builder cooldown(double cooldown) {
            this.cooldown = cooldown;
            return this;
        }

        @Override
        public GadgetType.Builder duration(double duration) {
            this.duration = duration;
            return this;
        }

        @Override
        public GadgetType.Builder structure(StructureData structure) {
            this.structure = structure;
            return this;
        }

        @Override
        public GadgetType build() {
            return new GadgetTypeImpl(key, category, factory, enabled, rarity, itemStack, cooldown, duration, structure);
        }
    }
}
