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
 */
package me.dtbh.kiwicosmetics.menu.menus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import me.dtbh.kiwicosmetics.api.KiwiCosmetics;
import me.dtbh.kiwicosmetics.api.config.Config;
import me.dtbh.kiwicosmetics.api.cosmetic.Cosmetic;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CosmeticCategory;
import me.dtbh.kiwicosmetics.api.menu.Menu;
import me.dtbh.kiwicosmetics.api.user.User;
import me.dtbh.kiwicosmetics.locale.LanguageManagerImpl;
import me.dtbh.kiwicosmetics.menu.MenuImpl;
import me.dtbh.kiwicosmetics.menu.MenuStyle;
import me.dtbh.kiwicosmetics.util.item.ItemBuilderImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * The menu /cosmetics opens: a header with the player's progress, one tile per category,
 * links to other plugins' menus (pets, tags, glow) and buttons to unequip everything or close the menu.
 */
public class MainMenu extends MenuImpl {

    private static final String PATH = "menu.main.";

    public MainMenu(KiwiCosmetics plugin, User user) {
        super(plugin, user, user.translate("menu.main.title"),
                plugin.getConfigManager().getMainConfig().getInt(PATH + "rows")
        );
    }

    @Override
    protected void addItems() {
        Config config = plugin.getConfigManager().getMainConfig();
        int unlockedTotal = 0;
        int total = 0;

        for (CosmeticCategory<?, ?, ?> category : plugin.getCategoryRegistries().getCategories()) {
            String path = PATH + "categories." + category.getKey();

            if (!category.isEnabled() || !config.hasKey(path + ".slot")) {
                continue;
            }
            int unlocked = category.getUnlockedCosmetics(player);
            int amount = category.getCosmeticRegistry().getEnabledTypes().size();
            unlockedTotal += unlocked;
            total += amount;

            setItem(config.getInt(path + ".slot"), getCategoryItem(category, unlocked, amount), event -> {
                if (event.isRightClick()) {
                    if (user.hasCosmetic(category)) {
                        user.removeCosmetic(category, false, true);
                        player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.5f, 0.8f);
                        refresh();
                    } else {
                        playDenySound();
                    }
                } else {
                    Menu menu = category.createMenu(plugin, user);
                    menu.setPreviousMenu(this);
                    menu.open();
                    playClickSound();
                }
            });
        }

        addLinks(config);

        // Header with the overall progress
        TagResolver progress = progressResolvers(unlockedTotal, total);
        ItemBuilderImpl header = new ItemBuilderImpl(config, PATH + "header");
        header.setSkullOwner(player);
        header.setDisplayName(user.translate("menu.main.header.name", Placeholder.unparsed("player", player.getName())));
        header.setLore(user.translateList("menu.main.header.desc", progress));
        setItem(header.getSlot(), header.getItemStack(), event -> {
        });

        ItemBuilderImpl unequipAll = new ItemBuilderImpl(config, PATH + "unequip_all");
        unequipAll.setDisplayName(user.translate("menu.main.unequip_all.name"));
        unequipAll.setLore(user.translateList("menu.main.unequip_all.desc"));
        setItem(unequipAll.getSlot(), unequipAll.getItemStack(), event -> {
            if (user.getCosmetics().isEmpty()) {
                playDenySound();
                return;
            }
            user.clearAllCosmetics(false, true);
            player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.5f, 0.8f);
            refresh();
        });

        ItemBuilderImpl close = new ItemBuilderImpl(config, PATH + "close");
        close.setDisplayName(user.translate("menu.close.name"));
        close.setLore(user.translateList("menu.close.desc"));
        setItem(close.getSlot(), close.getItemStack(), event -> {
            playClickSound();
            player.closeInventory();
        });

        drawBorder(config);
    }

    /**
     * Tiles that run a command, for cosmetics handled by other plugins (/pets, /tags, /eglow).
     * A link is hidden when its plugin isn't installed or the player lacks its permission.
     */
    private void addLinks(Config config) {
        for (String key : config.getSectionKeys(PATH + "links")) {
            String path = PATH + "links." + key;

            if (!config.hasKey(path + ".slot") || !config.hasKey(path + ".command")) {
                continue;
            }
            String requiredPlugin = config.hasKey(path + ".plugin") ? config.getString(path + ".plugin") : "";

            if (!requiredPlugin.isEmpty() && !isPluginEnabled(requiredPlugin)) {
                continue;
            }
            String permission = config.hasKey(path + ".permission") ? config.getString(path + ".permission") : "";

            if (!permission.isEmpty() && !player.hasPermission(permission)) {
                continue;
            }
            String nameKey = "menu.link." + key;
            String name = hasTranslation(nameKey) ? user.translateRaw(nameKey)
                    : config.hasKey(path + ".name") ? MenuStyle.smallCaps(config.getString(path + ".name"))
                    : MenuStyle.smallCaps(key.replace('_', ' '));

            List<Component> lore = new ArrayList<>();
            if (hasTranslation(nameKey + ".desc")) {
                lore.addAll(user.translateList(nameKey + ".desc"));
            }
            lore.addAll(user.translateList("menu.link.desc"));

            ItemBuilderImpl item = new ItemBuilderImpl(config, path);
            item.setDisplayName(user.translate("menu.link.name", Placeholder.unparsed("name", name)));
            item.setLore(lore);

            String command = config.getString(path + ".command");
            String finalCommand = command.startsWith("/") ? command.substring(1) : command;

            setItem(item.getSlot(), item.hide().getItemStack(), event -> {
                playClickSound();
                player.closeInventory();
                // Next tick, so the other plugin's menu doesn't open while this one is closing
                plugin.getSyncExecutor().execute(() -> player.performCommand(finalCommand.replace("%player%", player.getName())));
            });
        }
    }

    private boolean isPluginEnabled(String name) {
        for (Plugin installed : plugin.getJavaPlugin().getServer().getPluginManager().getPlugins()) {
            if (installed.getName().equalsIgnoreCase(name)) {
                return installed.isEnabled();
            }
        }
        return false;
    }

    private boolean hasTranslation(String key) {
        return ((LanguageManagerImpl) plugin.getLanguageManager()).has(key, user.getLocale());
    }

    private ItemStack getCategoryItem(CosmeticCategory<?, ?, ?> category, int unlocked, int amount) {
        Cosmetic<?, ?> equipped = user.getCosmetic(category);
        Component equippedName = equipped != null
                ? Component.text(MenuStyle.smallCaps(equipped.getType().getName(user)), MenuStyle.PINK)
                : user.translate("menu.main.category.nothing_equipped");

        TagResolver resolvers = TagResolver.resolver(
                progressResolvers(unlocked, amount),
                Placeholder.component("equipped", equippedName)
        );
        String key = category.getKey();
        List<Component> lore = new ArrayList<>(user.translateList("category." + key + ".desc"));
        lore.addAll(user.translateList("menu.main.category.desc", resolvers));

        ItemBuilderImpl itemBuilder = ItemBuilderImpl.of(category.getMenuItem().getItemStack());
        itemBuilder.setDisplayName(user.translate("menu.main.category.name",
                Placeholder.unparsed("name", user.translateRaw("category." + key + ".name"))));
        itemBuilder.setLore(lore);
        itemBuilder.setGlintOverride(equipped != null);
        return itemBuilder.hide().getItemStack();
    }

    private TagResolver progressResolvers(int unlocked, int total) {
        return TagResolver.resolver(
                Placeholder.unparsed("unlocked", String.valueOf(unlocked)),
                Placeholder.unparsed("total", String.valueOf(total)),
                Placeholder.unparsed("percent", MenuStyle.percent(unlocked, total)),
                Placeholder.component("bar", MenuStyle.progressBar(unlocked, total))
        );
    }

    private void drawBorder(Config config) {
        ItemStack border = new ItemBuilderImpl(config, "menu.border").getItemStack();

        for (int slot = 0; slot < getSize(); slot++) {
            int row = slot / 9;

            if ((row == 0 || row == rows - 1) && inventory.getItem(slot) == null) {
                inventory.setItem(slot, border);
            }
        }
    }

    public void refresh() {
        clear();
        addItems();
        fillEmptySlots(inventory, getFillEmptySlotsItem());
    }

    @Override
    public ItemStack getFillEmptySlotsItem() {
        Config config = plugin.getConfigManager().getMainConfig();

        if (!config.hasKey("menu.filler.item")) {
            return null;
        }
        return new ItemBuilderImpl(config, "menu.filler").getItemStack();
    }
}
