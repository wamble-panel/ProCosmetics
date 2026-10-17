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


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticType;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CosmeticCategory;
import me.dtbh.kiwicosmetics.api.locale.Translator;
import me.dtbh.kiwicosmetics.command.SubCommand;

import java.util.Comparator;
import java.util.stream.Collectors;

public class PermissionCommand extends SubCommand<CommandSender> {

    public PermissionCommand(KiwiCosmeticsPlugin plugin) {
        super(plugin, "kiwicosmetics.command.permission", true);
        addFlat("permission");
        addArgument(String.class, "category", sender -> plugin.getCategoryRegistries().getCategories().stream().map(CosmeticCategory::getKey).collect(Collectors.toList()));
    }

    @Override
    public void onExecute(CommandSender sender, String[] args) {
        Translator translator = translator(sender);
        CosmeticCategory<? extends CosmeticType<?, ?>, ?, ?> category = plugin.getCategoryRegistries().getCategoryRaw(parseArgument(args, 1));

        if (category == null) {
            audience(sender).sendMessage(translator.translate("category.not_found"));
            return;
        }
        TextComponent.Builder builder = Component.text().append(
                Component.text(category.getKey() + ":", NamedTextColor.YELLOW)
        );
        category.getCosmeticRegistry().getTypes().stream()
                .sorted(Comparator.comparing(CosmeticType::getPermission))
                .forEach(cosmeticType -> {
                    builder.append(Component.newline(),
                            Component.text(cosmeticType.getPermission())
                                    .clickEvent(ClickEvent.copyToClipboard(cosmeticType.getPermission()))
                                    .hoverEvent(HoverEvent.showText(
                                            Component.text("Click to copy!", NamedTextColor.YELLOW)
                                    ))
                    );
                });
        audience(sender).sendMessage(builder.build());
    }
}
