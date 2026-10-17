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
package me.dtbh.kiwicosmetics;

import com.xxmicloxx.NoteBlockAPI.NoteBlockAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import me.dtbh.kiwicosmetics.api.KiwiCosmetics;
import me.dtbh.kiwicosmetics.api.KiwiCosmeticsProvider;
import me.dtbh.kiwicosmetics.api.config.Config;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CategoryRegistries;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CosmeticRarityRegistry;
import me.dtbh.kiwicosmetics.api.platform.PlatformAdapter;
import me.dtbh.kiwicosmetics.api.user.User;
import me.dtbh.kiwicosmetics.command.CommandBase;
import me.dtbh.kiwicosmetics.command.SimpleCommand;
import me.dtbh.kiwicosmetics.command.commands.*;
import me.dtbh.kiwicosmetics.config.ConfigManagerImpl;
import me.dtbh.kiwicosmetics.cosmetic.registry.CategoryRegistriesImpl;
import me.dtbh.kiwicosmetics.cosmetic.registry.CosmeticRarityRegistryImpl;
import me.dtbh.kiwicosmetics.listener.*;
import me.dtbh.kiwicosmetics.listener.hook.cmi.CMIVanishListener;
import me.dtbh.kiwicosmetics.listener.hook.essentials.EssentialsVanishListener;
import me.dtbh.kiwicosmetics.listener.hook.premiumvanish.PremiumVanishListener;
import me.dtbh.kiwicosmetics.locale.LanguageManagerImpl;
import me.dtbh.kiwicosmetics.menu.MenuManagerImpl;
import me.dtbh.kiwicosmetics.nms.NMSManagerImpl;
import me.dtbh.kiwicosmetics.placeholder.PlaceholderManager;
import me.dtbh.kiwicosmetics.platform.PaperAdapter;
import me.dtbh.kiwicosmetics.platform.SpigotAdapter;
import me.dtbh.kiwicosmetics.user.UserManagerImpl;
import me.dtbh.kiwicosmetics.util.LogUtil;
import me.dtbh.kiwicosmetics.util.ResourceExporter;
import me.dtbh.kiwicosmetics.util.block.FakeBlockManager;
import me.dtbh.kiwicosmetics.util.version.VersionUtil;
import me.dtbh.kiwicosmetics.wardrobe.Wardrobe;
import me.dtbh.kiwicosmetics.wardrobe.WardrobeMigration;
import me.dtbh.kiwicosmetics.worldguard.WorldGuardManager;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

public class KiwiCosmeticsPlugin extends JavaPlugin implements KiwiCosmetics {

    private static KiwiCosmeticsPlugin plugin;

    private Logger logger;
    private Executor syncExecutor;
    private Executor ayncExecutor;
    private ConfigManagerImpl configManager;
    private LanguageManagerImpl languageManager;
    private NMSManagerImpl nmsManager;
    private CosmeticRarityRegistryImpl cosmeticRarityRegistry;
    private CategoryRegistriesImpl categoryRegistries;
    private UserManagerImpl userManager;
    private MenuManagerImpl menuManager;
    private FakeBlockManager fakeBlockManager;
    private PlaceholderManager placeholderManager;
    private CommandBase commandBase;
    private PlatformAdapter platformAdapter;
    private WorldGuardManager worldGuardManager;
    private Wardrobe wardrobe;

    @Override
    public void onLoad() {
        KiwiCosmeticsPlugin.plugin = this;
        logger = getLogger();
        syncExecutor = runnable -> getServer().getScheduler().runTask(this, runnable);
        ayncExecutor = runnable -> getServer().getScheduler().runTaskAsynchronously(this, runnable);

        if (!VersionUtil.isSupported()) {
            LogUtil.printUnsupported();
            return;
        }
        logger.info("Initializing...");

        // Must run before config.yml is exported, the old KiwiCosmetics plugin used that file name
        WardrobeMigration.migrate(getDataFolder(), logger);
        ResourceExporter.export(this);
        initializePlatformAdapter();

        configManager = new ConfigManagerImpl(this);
        languageManager = new LanguageManagerImpl(this);
        nmsManager = new NMSManagerImpl();
        cosmeticRarityRegistry = new CosmeticRarityRegistryImpl(this);

        // Hats and rainbow armor use Paper's item component API
        if (platformAdapter instanceof PaperAdapter) {
            wardrobe = new Wardrobe(this);
            wardrobe.load();
        } else {
            wardrobe = null;
            logger.warning("Hats and rainbow armor need Paper, they are disabled on this server.");
        }
        categoryRegistries = new CategoryRegistriesImpl(this);
        userManager = new UserManagerImpl(this);
        menuManager = new MenuManagerImpl(this);
        fakeBlockManager = new FakeBlockManager(this);
        placeholderManager = new PlaceholderManager(this);
        commandBase = new CommandBase(this);

        preHookPlugins();
    }

    @Override
    public void onEnable() {
        if (!VersionUtil.isSupported()) {
            LogUtil.printUnsupported();
            commandBase = new CommandBase(this);
            registerCommands(new UnsupportedCommand(this));
            return;
        }
        registerListeners();
        registerCommands();
        hookPlugins();

        if (wardrobe != null) {
            wardrobe.enable();
        }

        NoteBlockAPI.init(this);

        userManager.loadOnlinePlayers();
        KiwiCosmeticsProvider.register(this);

        logger.info("Initialized!");
    }

    private void registerListeners() {
        userManager.registerListeners();
        menuManager.registerListeners();

        registerListeners(new BlockListener(),
                new WorldListener(this),
                new CosmeticListener(this),
                new CreatureSpawnListener(),
                new EntityListener(),
                new FallDamageListener(this),
                new InventoryListener(),
                new PlayerListener(this)
        );
    }

    @Override
    public void onDisable() {
        if (!VersionUtil.isSupported()) {
            LogUtil.printUnsupported();
            return;
        }
        for (User user : userManager.getAllConnected()) {
            user.clearAllCosmetics(true, false);
        }
        if (wardrobe != null) {
            wardrobe.disable();
        }
        fakeBlockManager.shutdown();
        HandlerList.unregisterAll(this);
        getServer().getScheduler().cancelTasks(this);

        NoteBlockAPI.getAPI().shutdown();
    }

    private void initializePlatformAdapter() {
        boolean paper = false;

        try {
            Class.forName("com.destroystokyo.paper.ParticleBuilder");
            paper = true;
        } catch (ClassNotFoundException _) {
        }
        platformAdapter = paper ? new PaperAdapter() : new SpigotAdapter();
    }

    private void registerCommands() {
        commandBase.getCommands().clear();

        registerCommands(new CosmeticsCommand(this),
                new EquipCommand(this),
                new UnequipCommand(this),
                new UnequipAllCommand(this),
                new KiwiCosmeticsCommand(this)
        );
    }

    private void preHookPlugins() {
        PluginManager pluginManager = getServer().getPluginManager();

        if (pluginManager.getPlugin("WorldGuard") != null && configManager.getMainConfig().getBoolean("world_guard.enabled")) {
            logger.info("Hooking into WorldGuard...");
            this.worldGuardManager = new WorldGuardManager(this);
        }
    }

    private void hookPlugins() {
        PluginManager pluginManager = getServer().getPluginManager();

        if (pluginManager.getPlugin("Essentials") != null) {
            logger.info("Hooking into Essentials...");
            registerListeners(new EssentialsVanishListener(this));
        }
        if (pluginManager.getPlugin("CMI") != null) {
            logger.info("Hooking into CMI...");
            registerListeners(new CMIVanishListener(this));
        }
        if (pluginManager.getPlugin("PremiumVanish") != null) {
            logger.info("Hooking into PremiumVanish...");
            registerListeners(new PremiumVanishListener(this));
        }
        if (pluginManager.getPlugin("SuperVanish") != null) {
            logger.info("Hooking into SuperVanish...");
            registerListeners(new PremiumVanishListener(this));
        }

        if (worldGuardManager != null) {
            worldGuardManager.register(this);
        }
    }

    private void registerListeners(Listener... listeners) {
        PluginManager pluginManager = getServer().getPluginManager();

        for (Listener listener : listeners) {
            if (listener != null) {
                pluginManager.registerEvents(listener, this);
            }
        }
    }

    @SafeVarargs
    private void registerCommands(SimpleCommand<CommandSender>... commands) {
        for (SimpleCommand<CommandSender> command : commands) {
            commandBase.registerCommand(command);
        }
    }

    public static KiwiCosmeticsPlugin getPlugin() {
        return plugin;
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Override
    public Executor getSyncExecutor() {
        return syncExecutor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return ayncExecutor;
    }

    @Override
    public ConfigManagerImpl getConfigManager() {
        return configManager;
    }

    @Override
    public LanguageManagerImpl getLanguageManager() {
        return languageManager;
    }

    @Override
    public NMSManagerImpl getNMSManager() {
        return nmsManager;
    }

    @Override
    public CosmeticRarityRegistry getCosmeticRarityRegistry() {
        return cosmeticRarityRegistry;
    }

    @Override
    public CategoryRegistries getCategoryRegistries() {
        return categoryRegistries;
    }

    @Override
    public UserManagerImpl getUserManager() {
        return userManager;
    }

    @Override
    public MenuManagerImpl getMenuManager() {
        return menuManager;
    }

    public FakeBlockManager getFakeBlockManager() {
        return fakeBlockManager;
    }

    @Override
    public PlatformAdapter getPlatformAdapter() {
        return platformAdapter;
    }

    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    public CommandBase getCommandBase() {
        return commandBase;
    }

    /**
     * @return hats and rainbow armor, or null when not running on Paper
     */
    public Wardrobe getWardrobe() {
        return wardrobe;
    }

    public WorldGuardManager getWorldGuardManager() {
        return worldGuardManager;
    }
}
