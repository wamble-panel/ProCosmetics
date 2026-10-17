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
package me.dtbh.kiwicosmetics.api;

import org.bukkit.plugin.java.JavaPlugin;
import me.dtbh.kiwicosmetics.api.config.ConfigManager;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CategoryRegistries;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CosmeticRarityRegistry;
import me.dtbh.kiwicosmetics.api.locale.LanguageManager;
import me.dtbh.kiwicosmetics.api.menu.MenuManager;
import me.dtbh.kiwicosmetics.api.nms.NMSManager;
import me.dtbh.kiwicosmetics.api.platform.PlatformAdapter;
import me.dtbh.kiwicosmetics.api.user.UserManager;

import java.util.concurrent.Executor;

/**
 * Main API interface for KiwiCosmetics.
 * This interface provides access to all major subsystems including configuration,
 * localization, user management, database operations, cosmetic registries, and more.
 *
 * <p>Access this instance via {@link KiwiCosmeticsProvider#get()}.</p>
 *
 * <p>Example usage:
 * <pre>{@code
 * KiwiCosmetics api = KiwiCosmeticsProvider.get();
 * UserManager userManager = api.getUserManager();
 * User user = userManager.getConnected(player);
 * }</pre>
 */
public interface KiwiCosmetics {

    /**
     * Gets the configuration manager for accessing plugin configs.
     *
     * @return the configuration manager
     */
    ConfigManager getConfigManager();

    /**
     * Gets the language manager for translations and localization.
     *
     * @return the language manager
     */
    LanguageManager getLanguageManager();

    /**
     * Gets the user manager for retrieving and managing users.
     *
     * @return the user manager
     */
    UserManager getUserManager();

    /**
     * Gets the menu manager for GUI operations.
     *
     * @return the menu manager
     */
    MenuManager getMenuManager();

    /**
     * Gets the NMS manager for version-specific entity operations.
     *
     * @return the nMS manager
     */
    NMSManager getNMSManager();

    /**
     * Gets the cosmetic rarity registry.
     *
     * @return the cosmetic rarity registry
     */
    CosmeticRarityRegistry getCosmeticRarityRegistry();

    /**
     * Gets the category registries containing all cosmetic categories.
     *
     * @return the category registries
     */
    CategoryRegistries getCategoryRegistries();

    /**
     * Gets the platform adapter for handling platform-specific operations.
     *
     * @return the platform adapter
     */
    PlatformAdapter getPlatformAdapter();

    /**
     * Gets the underlying Bukkit JavaPlugin instance.
     *
     * @return the javaPlugin instance
     */
    JavaPlugin getJavaPlugin();

    /**
     * Gets an executor that runs tasks on the main server thread.
     *
     * @return the synchronous executor
     */
    Executor getSyncExecutor();

    /**
     * Gets an executor that runs tasks asynchronously off the main thread.
     *
     * @return the asynchronous executor
     */
    Executor getAsyncExecutor();
}
