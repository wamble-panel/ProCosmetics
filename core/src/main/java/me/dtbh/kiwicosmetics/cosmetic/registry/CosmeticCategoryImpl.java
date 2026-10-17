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
package me.dtbh.kiwicosmetics.cosmetic.registry;

import org.apache.commons.lang3.function.TriFunction;
import org.bukkit.entity.Player;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.api.KiwiCosmetics;
import me.dtbh.kiwicosmetics.api.config.Config;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticType;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CosmeticCategory;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CosmeticRegistry;
import me.dtbh.kiwicosmetics.api.menu.CosmeticMenu;
import me.dtbh.kiwicosmetics.api.user.User;
import me.dtbh.kiwicosmetics.api.util.item.ItemBuilder;
import me.dtbh.kiwicosmetics.cosmetic.CosmeticTypeImpl;
import me.dtbh.kiwicosmetics.menu.CosmeticMenuImpl;
import me.dtbh.kiwicosmetics.util.item.ItemBuilderImpl;

import java.util.Collection;
import java.util.function.BiFunction;

public class CosmeticCategoryImpl<T extends CosmeticType<T, B>,
        B extends CosmeticBehavior<T>,
        U extends CosmeticType.Builder<T, B, U>>
        implements CosmeticCategory<T, B, U> {

    private static final KiwiCosmeticsPlugin PLUGIN = KiwiCosmeticsPlugin.getPlugin();

    private final String key;
    private final CosmeticRegistry<T, B, U> registry;
    private final Config config;
    private final boolean enabled;
    private final String permission;
    private final ItemBuilderImpl menuItem;
    private final TriFunction<KiwiCosmetics, User, CosmeticCategory<T, B, U>, CosmeticMenuImpl<T>> menuFactory;

    public CosmeticCategoryImpl(String key,
                                BiFunction<String, CosmeticCategory<T, B, U>, U> builderFactory,
                                TriFunction<KiwiCosmetics, User, CosmeticCategory<T, B, U>, CosmeticMenuImpl<T>> menuFactory) {
        this(key, key, builderFactory, menuFactory);
    }

    /**
     * @param configName the config file (without .yml) to read, for categories whose key is taken by another file
     */
    public CosmeticCategoryImpl(String key,
                                String configName,
                                BiFunction<String, CosmeticCategory<T, B, U>, U> builderFactory,
                                TriFunction<KiwiCosmetics, User, CosmeticCategory<T, B, U>, CosmeticMenuImpl<T>> menuFactory) {
        this.key = key.toLowerCase();
        this.registry = new CosmeticRegistryImpl<>(this, builderFactory);
        this.config = PLUGIN.getConfigManager().register(configName);
        this.enabled = config.getBoolean("enabled");
        this.permission = "kiwicosmetics.cosmetic." + key + ".*";
        this.menuItem = new ItemBuilderImpl(PLUGIN.getConfigManager().getMainConfig(), "menu.main.categories." + key);
        this.menuFactory = menuFactory;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public CosmeticRegistry<T, B, U> getCosmeticRegistry() {
        return registry;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public ItemBuilder getMenuItem() {
        return menuItem;
    }

    @Override
    public CosmeticMenu<T> createMenu(KiwiCosmetics plugin, User user) {
        return menuFactory.apply(plugin, user, this);
    }

    @Override
    public int getUnlockedCosmetics(Player player) {
        Collection<? extends CosmeticType<?, ?>> cosmeticTypes = registry.getEnabledTypes();

        if (player.hasPermission(CosmeticTypeImpl.PERMISSION_ALL_COSMETICS) || player.hasPermission(permission)) {
            return cosmeticTypes.size();
        }
        int i = 0;

        for (CosmeticType<?, ?> cosmeticType : cosmeticTypes) {
            if (cosmeticType.hasPermission(player)) {
                i++;
            }
        }
        return i;
    }
}
