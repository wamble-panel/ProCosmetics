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
 */package me.dtbh.kiwicosmetics.command.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CosmeticCategory;
import me.dtbh.kiwicosmetics.api.user.User;
import me.dtbh.kiwicosmetics.command.SimpleCommand;
import me.dtbh.kiwicosmetics.command.SubCommand;
import me.dtbh.kiwicosmetics.menu.menus.MainMenu;

import java.util.Locale;
import java.util.stream.Collectors;

/**
 * /cosmetics opens the cosmetics menu, /cosmetics <category> opens a category directly
 * (e.g. /cosmetics particles, /cosmetics trails, /cosmetics hats).
 */
public class CosmeticsCommand extends SimpleCommand<CommandSender> {

    public CosmeticsCommand(KiwiCosmeticsPlugin plugin) {
        super(plugin, "cosmetics", "kiwicosmetics.command.cosmetics", false);
        setSubCommands(new CategorySubCommand(plugin));
    }

    @Override
    public void onExecute(CommandSender sender, String[] args) {
        User user = plugin.getUserManager().getConnected((Player) sender);

        if (user == null) {
            audience(sender).sendMessage(translator(sender).translate("generic.error.player_data"));
            return;
        }
        new MainMenu(plugin, user).open();
    }

    /**
     * Finds a category by key ("particle_effects") or a short name ("particles", "arrows", "deaths").
     */
    public static CosmeticCategory<?, ?, ?> findCategory(KiwiCosmeticsPlugin plugin, String name) {
        String key = name.toLowerCase(Locale.ROOT).replace('-', '_');
        key = switch (key) {
            case "particles", "particle", "effects" -> "particle_effects";
            case "trail" -> "trails";
            case "gadget" -> "gadgets";
            case "hat" -> "hats";
            case "balloon" -> "balloons";
            case "banner" -> "banners";
            case "miniature", "minis", "mini" -> "miniatures";
            case "arrows", "arrow" -> "arrow_effects";
            case "deaths", "death" -> "death_effects";
            case "songs", "song" -> "music";
            default -> key;
        };
        CosmeticCategory<?, ?, ?> category = plugin.getCategoryRegistries().getCategoryRaw(key);
        return category != null && category.isEnabled() ? category : null;
    }

    private static class CategorySubCommand extends SubCommand<CommandSender> {

        public CategorySubCommand(KiwiCosmeticsPlugin plugin) {
            super(plugin, "kiwicosmetics.command.cosmetics", false);
            addArgument(String.class, "category", sender -> plugin.getCategoryRegistries().getCategories().stream()
                    .filter(CosmeticCategory::isEnabled)
                    .map(CosmeticCategory::getKey)
                    .collect(Collectors.toList()));
        }

        @Override
        public void onExecute(CommandSender sender, String[] args) {
            User user = plugin.getUserManager().getConnected((Player) sender);

            if (user == null) {
                audience(sender).sendMessage(translator(sender).translate("generic.error.player_data"));
                return;
            }
            CosmeticCategory<?, ?, ?> category = findCategory(plugin, parseArgument(args, 0));

            if (category == null) {
                user.sendMessage(user.translate("category.not_found"));
                return;
            }
            category.createMenu(plugin, user).open();
        }
    }
}
