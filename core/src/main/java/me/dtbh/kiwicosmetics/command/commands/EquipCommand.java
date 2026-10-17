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
import me.dtbh.kiwicosmetics.api.cosmetic.Cosmetic;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticType;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CosmeticCategory;
import me.dtbh.kiwicosmetics.api.user.User;
import me.dtbh.kiwicosmetics.command.SimpleCommand;
import me.dtbh.kiwicosmetics.command.SubCommand;

import java.util.stream.Collectors;

public class EquipCommand extends SimpleCommand<CommandSender> {

    public EquipCommand(KiwiCosmeticsPlugin plugin) {
        super(plugin, "equip", "kiwicosmetics.command.equip", false);
        setSubCommands(new ArgumentSubCommand(plugin));
    }

    @Override
    public void onExecute(CommandSender sender, String[] args) {
        // NOTE: Don't translate this, when we switch to brigadier command lib it solves itself
        audience(sender).sendMessage(Component.text("/equip <category> <cosmetic>", NamedTextColor.RED));
    }

    private static class ArgumentSubCommand extends SubCommand<CommandSender> {

        public ArgumentSubCommand(KiwiCosmeticsPlugin plugin) {
            super(plugin, "kiwicosmetics.command.equip", false);
            addArgument(String.class, "category", sender -> plugin.getCategoryRegistries().getCategories().stream().filter(CosmeticCategory::isEnabled).map(CosmeticCategory::getKey).collect(Collectors.toList()));
            addArgument(String.class, "cosmetic");
        }

        @Override
        public void onExecute(CommandSender sender, String[] args) {
            User user = plugin.getUserManager().getConnected((Player) sender);

            if (user == null) {
                audience(sender).sendMessage(translator(sender).translate("generic.error.player_data"));
                return;
            }
            String type = parseArgument(args, 0);
            CosmeticCategory<?, ?, ?> category = CosmeticsCommand.findCategory(plugin, type);

            // Don't expose disabled categories
            if (category == null) {
                user.sendMessage(user.translate("category.not_found"));
                return;
            }
            String cosmeticName = parseArgument(args, 1);
            CosmeticType<?, ?> cosmeticType = category.getCosmeticRegistry().getEnabledType(cosmeticName);

            if (cosmeticType == null) {
                user.sendMessage(user.translate("cosmetic.not_found"));
                return;
            }
            Cosmetic<?, ?> cosmetic = user.getCosmetic(category);

            if (cosmetic != null && cosmetic.isEquipped() && cosmetic.getType().equals(cosmeticType)) {
                user.sendMessage(user.translate("cosmetic.already_equipped"));
                return;
            }
            cosmeticType.equip(user, false, true);
        }
    }
}
