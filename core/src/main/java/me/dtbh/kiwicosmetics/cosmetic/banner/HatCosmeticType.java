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
 */package me.dtbh.kiwicosmetics.cosmetic.banner;

import org.bukkit.inventory.ItemStack;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticRarity;
import me.dtbh.kiwicosmetics.api.cosmetic.banner.BannerBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.banner.BannerType;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CosmeticCategory;
import me.dtbh.kiwicosmetics.api.locale.Translator;
import me.dtbh.kiwicosmetics.cosmetic.banner.type.DefaultBanner;

import java.util.List;

/**
 * A hat from hats.yml in the Hats category. It is shown on the head with the same fake
 * equipment as banners, so the player's real helmet is never touched.
 */
public class HatCosmeticType extends BannerTypeImpl {

    private final String name;

    public HatCosmeticType(String key,
                           CosmeticCategory<BannerType, BannerBehavior, ?> category,
                           CosmeticRarity rarity,
                           ItemStack itemStack,
                           String name) {
        super(key, category, DefaultBanner::new, true, rarity, itemStack, -1L, List.of());
        this.name = name;
    }

    @Override
    public String getName(Translator translator) {
        return name;
    }
}
