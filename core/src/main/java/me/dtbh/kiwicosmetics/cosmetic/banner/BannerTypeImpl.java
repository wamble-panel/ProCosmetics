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
package me.dtbh.kiwicosmetics.cosmetic.banner;

import org.bukkit.inventory.ItemStack;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.api.config.Config;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticRarity;
import me.dtbh.kiwicosmetics.api.cosmetic.banner.Banner;
import me.dtbh.kiwicosmetics.api.cosmetic.banner.BannerBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.banner.BannerType;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CosmeticCategory;
import me.dtbh.kiwicosmetics.api.user.User;
import me.dtbh.kiwicosmetics.api.util.AnimationFrame;
import me.dtbh.kiwicosmetics.cosmetic.CosmeticTypeImpl;
import me.dtbh.kiwicosmetics.util.item.ItemBuilderImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class BannerTypeImpl extends CosmeticTypeImpl<BannerType, BannerBehavior> implements BannerType {

    private final long tickInterval;
    private final List<AnimationFrame> frames;

    public BannerTypeImpl(String key,
                          CosmeticCategory<BannerType, BannerBehavior, ?> category,
                          Supplier<BannerBehavior> behaviorFactory,
                          boolean enabled,
                          CosmeticRarity rarity,
                          ItemStack itemStack,
                          long tickInterval,
                          List<AnimationFrame> frames) {
        super(key, category, behaviorFactory, enabled, rarity, itemStack);
        this.tickInterval = tickInterval;
        this.frames = frames;
    }

    @Override
    protected Banner createInstance(KiwiCosmeticsPlugin plugin, User user, BannerBehavior behavior) {
        return new BannerImpl(plugin, user, this, behavior);
    }

    @Override
    public long getTickInterval() {
        return tickInterval;
    }

    @Override
    public List<AnimationFrame> getFrames() {
        return frames;
    }

    public static class BuilderImpl extends CosmeticTypeImpl.BuilderImpl<BannerType, BannerBehavior, BannerType.Builder> implements BannerType.Builder {

        private long tickInterval = -1L;
        private List<AnimationFrame> frames = new ArrayList<>();

        public BuilderImpl(String key, CosmeticCategory<BannerType, BannerBehavior, ?> category) {
            super(key, category);
        }

        @Override
        protected BannerType.Builder self() {
            return this;
        }

        @Override
        public BannerType.Builder readFromConfig() {
            super.readFromConfig();

            frames.clear();

            Config config = category.getConfig();
            String path = getPath();

            tickInterval = config.getInt(path + "tick_interval");

            if (config.hasKey(path + "frames")) {
                List<Map<?, ?>> animationList = config.getMapList(path + "frames");

                for (Map<?, ?> animation : animationList) {
                    String item = (String) animation.get("item");
                    int duration = (int) animation.get("duration");

                    if (item != null && duration > 0) {
                        addFrame(AnimationFrame.of(new ItemBuilderImpl(item).getItemStack(), duration));
                    }
                }
            }
            return this;
        }

        @Override
        public BannerType.Builder tickInterval(long tickInterval) {
            this.tickInterval = tickInterval;
            return this;
        }

        @Override
        public BannerType.Builder frames(List<AnimationFrame> frames) {
            this.frames = new ArrayList<>(frames);
            return this;
        }

        @Override
        public BannerType.Builder addFrame(AnimationFrame frame) {
            frames.add(frame);
            return this;
        }

        @Override
        public BannerType build() {
            return new BannerTypeImpl(key,
                    category,
                    factory,
                    enabled,
                    rarity,
                    itemStack,
                    tickInterval,
                    frames
            );
        }
    }
}
