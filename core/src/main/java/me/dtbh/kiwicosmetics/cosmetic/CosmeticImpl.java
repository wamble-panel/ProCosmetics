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
package me.dtbh.kiwicosmetics.cosmetic;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.api.KiwiCosmetics;
import me.dtbh.kiwicosmetics.api.config.Config;
import me.dtbh.kiwicosmetics.api.cosmetic.Cosmetic;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticContext;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticType;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CosmeticCategory;
import me.dtbh.kiwicosmetics.api.event.PlayerEquipCosmeticEvent;
import me.dtbh.kiwicosmetics.api.event.PlayerPreEquipCosmeticEvent;
import me.dtbh.kiwicosmetics.api.event.PlayerUnequipCosmeticEvent;
import me.dtbh.kiwicosmetics.api.user.User;
import me.dtbh.kiwicosmetics.config.ConfigManagerImpl;
import me.dtbh.kiwicosmetics.util.AbstractRunnable;

public abstract class CosmeticImpl<T extends CosmeticType<T, B>,
        B extends CosmeticBehavior<T>> extends AbstractRunnable implements Cosmetic<T, B>, Listener, CosmeticContext<T> {

    protected final KiwiCosmeticsPlugin plugin;
    protected final User user;
    protected final T cosmeticType;
    protected final B behavior;
    protected Player player;
    private boolean equipped;

    public CosmeticImpl(KiwiCosmeticsPlugin plugin, User user, T cosmeticType, B behavior) {
        this.plugin = plugin;
        this.user = user;
        this.cosmeticType = cosmeticType;
        this.behavior = behavior;
    }

    @Override
    public void equip(boolean silent, boolean saveToDatabase) {
        player = plugin.getServer().getPlayer(user.getUniqueId());

        if (player == null || !player.isOnline()) {
            return;
        }
        Server server = plugin.getServer();
        PluginManager pluginManager = server.getPluginManager();
        PlayerPreEquipCosmeticEvent event = new PlayerPreEquipCosmeticEvent(plugin, user, player, cosmeticType);
        pluginManager.callEvent(event);

        if (event.isCancelled()) {
            return;
        }
        player.closeInventory();

        if (!cosmeticType.hasPermission(player)) {
            if (!silent) {
                user.sendMessage(user.translate("cosmetic.locked", Placeholder.unparsed("cosmetic", cosmeticType.getName(user))));
            }
            return;
        }
        ConfigManagerImpl configManager = plugin.getConfigManager();

        Config config = configManager.getMainConfig();
        boolean blacklistedWorlds = config.getBoolean("multi_world.blacklisted_worlds");
        boolean inWorld = config.getStringList("multi_world.worlds").contains(player.getWorld().getName());

        if (blacklistedWorlds && inWorld || !blacklistedWorlds && !inWorld) {
            if (!silent) {
                user.sendMessage(user.translate("cosmetic.equip.deny.world"));
            }
            return;
        }
        CosmeticCategory<T, B, ?> category = cosmeticType.getCategory();

        if (plugin.getWorldGuardManager() != null && !plugin.getWorldGuardManager().isCosmeticAllow(player, category)) {
            if (!silent) {
                user.sendMessage(user.translate("cosmetic.equip.deny.region"));
            }
            return;
        }
        if (!canEquip()) {
            return;
        }
        pluginManager.registerEvents(this, plugin);

        if (user.hasCosmetic(category) && user.getCosmetic(category).isEquipped()) {
            user.removeCosmetic(category, true, false);
        }
        user.setCosmetic(category, this);

        onEquip();
        behavior.onEquip(this);

        if (behavior instanceof Listener behaviorListener) {
            pluginManager.registerEvents(behaviorListener, plugin);
        }

        if (!silent) {
            server.getScheduler().runTask(plugin, () -> user.sendMessage(user.translate(
                    "cosmetic." + cosmeticType.getCategory().getKey() + ".equip",
                    Placeholder.unparsed("cosmetic", cosmeticType.getName(user))
            )));
        }
        equipped = true;
        pluginManager.callEvent(new PlayerEquipCosmeticEvent(plugin, user, player, cosmeticType));

        if (saveToDatabase) {
            plugin.getUserManager().getStorage().saveEquipped(player, cosmeticType);
        }
    }

    @Override
    public void run() {
        if (player == null || !player.isOnline() || !equipped) {
            cancel();
            return;
        }
        onUpdate();
    }

    @Override
    public void unequip(boolean silent, boolean saveToDatabase) {
        if (!equipped) {
            return;
        }
        cancel();
        HandlerList.unregisterAll(this);
        behavior.onUnequip(this);

        if (behavior instanceof Listener behaviorListener) {
            HandlerList.unregisterAll(behaviorListener);
        }
        onUnequip();

        if (!silent) {
            user.sendMessage(user.translate(
                    "cosmetic." + cosmeticType.getCategory().getKey() + ".unequip",
                    Placeholder.unparsed("cosmetic", cosmeticType.getName(user))
            ));
        }
        plugin.getServer().getPluginManager().callEvent(new PlayerUnequipCosmeticEvent(plugin, player, cosmeticType));
        equipped = false;

        if (saveToDatabase && player.isOnline()) {
            plugin.getUserManager().getStorage().removeEquipped(player, cosmeticType.getCategory());
        }
    }

    protected boolean canEquip() {
        return true;
    }

    protected abstract void onEquip();

    protected abstract void onUpdate();

    protected abstract void onUnequip();

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public T getType() {
        return cosmeticType;
    }

    @Override
    public B getBehavior() {
        return behavior;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean isEquipped() {
        return equipped;
    }

    @Override
    public KiwiCosmetics getPlugin() {
        return plugin;
    }
}
