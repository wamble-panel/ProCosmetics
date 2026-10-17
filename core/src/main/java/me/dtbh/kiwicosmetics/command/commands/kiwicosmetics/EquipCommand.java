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
package me.dtbh.kiwicosmetics.command.commands.kiwicosmetics;


import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.api.cosmetic.Cosmetic;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticType;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CosmeticCategory;
import me.dtbh.kiwicosmetics.api.locale.Translator;
import me.dtbh.kiwicosmetics.api.user.User;
import me.dtbh.kiwicosmetics.command.SubCommand;

import java.util.stream.Collectors;

public class EquipCommand extends SubCommand<CommandSender> {

    public EquipCommand(KiwiCosmeticsPlugin plugin) {
        super(plugin, "kiwicosmetics.command.equip.other", true);
        addFlat("equip");
        addArgument(Player.class, "target", sender -> plugin.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
        addArgument(String.class, "category", sender -> plugin.getCategoryRegistries().getCategories().stream().filter(CosmeticCategory::isEnabled).map(CosmeticCategory::getKey).collect(Collectors.toList()));
        addArgument(String.class, "cosmetic");
    }

    @Override
    public void onExecute(CommandSender sender, String[] args) {
        Translator translator = translator(sender);
        Player target = parseArgument(args, 1);

        if (target == null) {
            audience(sender).sendMessage(translator.translate("generic.player_offline"));
            return;
        }
        User user = plugin.getUserManager().getConnected(target);

        if (user == null) {
            audience(sender).sendMessage(translator.translate("generic.error.player_data.target"));
            return;
        }
        CosmeticCategory<?, ?, ?> category = plugin.getCategoryRegistries().getCategoryRaw(parseArgument(args, 2));

        if (category == null) {
            audience(sender).sendMessage(translator.translate("category.not_found"));
            return;
        }
        if (!category.isEnabled()) {
            audience(sender).sendMessage(translator.translate("category.disabled"));
            return;
        }
        CosmeticType<?, ?> cosmeticType = category.getCosmeticRegistry().getEnabledType(parseArgument(args, 3));

        if (cosmeticType == null) {
            audience(sender).sendMessage(translator.translate("cosmetic.not_found"));
            return;
        }
        Cosmetic<?, ?> cosmetic = user.getCosmetic(category);

        if (cosmetic != null && cosmetic.getType().equals(cosmeticType)) {
            audience(sender).sendMessage(translator.translate(
                    "command.equipplayer.already_equipped",
                    Placeholder.unparsed("player", target.getName())
            ));
            return;
        }
        cosmeticType.equip(user, false, true);
        audience(sender).sendMessage(translator.translate(
                "command.equipplayer.success",
                Placeholder.unparsed("player", target.getName())
        ));
    }
}
