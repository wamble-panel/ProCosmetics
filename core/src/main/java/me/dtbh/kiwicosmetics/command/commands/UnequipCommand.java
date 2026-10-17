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
package me.dtbh.kiwicosmetics.command.commands;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CosmeticCategory;
import me.dtbh.kiwicosmetics.api.user.User;
import me.dtbh.kiwicosmetics.command.SimpleCommand;
import me.dtbh.kiwicosmetics.command.SubCommand;

import java.util.stream.Collectors;

public class UnequipCommand extends SimpleCommand<CommandSender> {

    public UnequipCommand(KiwiCosmeticsPlugin plugin) {
        super(plugin, "unequip", "kiwicosmetics.command.unequip", false);
        setSubCommands(new ArgumentSubCommand(plugin));
    }

    @Override
    public void onExecute(CommandSender sender, String[] args) {
        // NOTE: Don't translate this, when we switch to brigadier command lib it solves itself
        audience(sender).sendMessage(Component.text("/unequip <type>", NamedTextColor.RED));
    }

    private static class ArgumentSubCommand extends SubCommand<CommandSender> {

        public ArgumentSubCommand(KiwiCosmeticsPlugin plugin) {
            super(plugin, "kiwicosmetics.command.unequip", false);
            addArgument(String.class, "category", sender -> plugin.getCategoryRegistries().getCategories().stream().filter(CosmeticCategory::isEnabled).map(CosmeticCategory::getKey).collect(Collectors.toList()));
        }

        @Override
        public void onExecute(CommandSender sender, String[] args) {
            User user = plugin.getUserManager().getConnected((Player) sender);

            if (user == null) {
                audience(sender).sendMessage(translator(sender).translate("generic.error.player_data"));
                return;
            }
            String type = parseArgument(args, 0);
            CosmeticCategory<?, ?, ?> category = plugin.getCategoryRegistries().getCategory(type);

            // Don't expose disabled categories
            if (category == null || !category.isEnabled()) {
                user.sendMessage(user.translate("category.not_found"));
                return;
            }

            if (user.hasCosmetic(category)) {
                user.removeCosmetic(category, false, true);
            } else {
                user.sendMessage(user.translate("cosmetic.not_equipped"));
            }
        }
    }
}
