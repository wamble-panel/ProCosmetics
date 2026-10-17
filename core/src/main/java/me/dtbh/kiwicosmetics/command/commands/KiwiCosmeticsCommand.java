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
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.command.SimpleCommand;
import me.dtbh.kiwicosmetics.command.argument.Argument;
import me.dtbh.kiwicosmetics.menu.MenuStyle;
import me.dtbh.kiwicosmetics.command.commands.kiwicosmetics.*;
import me.dtbh.kiwicosmetics.command.commands.kiwicosmetics.EquipCommand;
import me.dtbh.kiwicosmetics.command.commands.kiwicosmetics.UnequipCommand;

public class KiwiCosmeticsCommand extends SimpleCommand<CommandSender> {

    public KiwiCosmeticsCommand(KiwiCosmeticsPlugin plugin) {
        super(plugin, "kiwicosmetics", "kiwicosmetics.command");
        setSubCommands(
                new EquipCommand(plugin),
                new ItemCommand(plugin),
                new MenuCommand(plugin),
                new PermissionCommand(plugin),
                new ReloadCommand(plugin),
                new StructureCommand(plugin),
                new UnequipAllPlayersCommand(plugin),
                new UnequipCommand(plugin)
        );
    }

    @Override
    public void onExecute(CommandSender sender, String[] args) {
        MiniMessage mini = MiniMessage.miniMessage();
        TextComponent.Builder builder = Component.text();
        builder.append(mini.deserialize("<#8A8A8A><st>━━━━━━━━━━━━━━━━━━━━━━━━</st>"), Component.newline(),
                mini.deserialize("<gradient:#FFB5A7:#FFC8DD><bold>✦ ᴋɪᴡɪᴄᴏꜱᴍᴇᴛɪᴄꜱ</bold></gradient> <#CDB4DB>ʜᴇʟᴘ"));

        getSubCommands().forEach(subCommand -> {
            StringBuilder usage = new StringBuilder("/kc");

            for (Argument<?> argument : subCommand.getArguments()) {
                usage.append(' ').append(argument.getType() == null ? argument.getArgument() : "<" + argument.getArgument() + ">");
            }
            builder.append(Component.newline(), Component.text("• ", MenuStyle.GREY), Component.text(usage.toString(), MenuStyle.PINK));
        });
        builder.append(Component.newline(), Component.text("• ", MenuStyle.GREY), Component.text("/hats", MenuStyle.PINK),
                Component.text(" , ", MenuStyle.GREY), Component.text("/rainbow", MenuStyle.PINK),
                Component.text(" — ", MenuStyle.GREY), Component.text(MenuStyle.smallCaps("hats and rainbow armor"), MenuStyle.PEACH));
        builder.append(Component.newline(), mini.deserialize("<#8A8A8A><st>━━━━━━━━━━━━━━━━━━━━━━━━</st>"));
        audience(sender).sendMessage(builder);
    }
}
