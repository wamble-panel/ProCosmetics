/*
 * This file is part of ProCosmetics - https://github.com/FilleDev/ProCosmetics
 * Copyright (C) 2025-2026 FilleDev and contributors
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
package se.filledev.procosmetics.command.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import se.filledev.procosmetics.ProCosmeticsPlugin;
import se.filledev.procosmetics.command.SimpleCommand;
import se.filledev.procosmetics.command.argument.Argument;
import se.filledev.procosmetics.command.commands.procosmetics.*;
import se.filledev.procosmetics.command.commands.procosmetics.EquipCommand;
import se.filledev.procosmetics.command.commands.procosmetics.UnequipCommand;

public class ProCosmeticsCommand extends SimpleCommand<CommandSender> {

    public ProCosmeticsCommand(ProCosmeticsPlugin plugin) {
        super(plugin, "procosmetics", "procosmetics.command");
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
        TextComponent.Builder builder = Component.text();
        builder.append(Component.text(plugin.getDescription().getName() + " Commands", NamedTextColor.AQUA));

        getSubCommands().forEach(subCommand -> {
            builder.append(Component.newline(),
                    Component.text("/pc ", NamedTextColor.YELLOW)
            );

            for (Argument<?> argument : subCommand.getArguments()) {
                builder.append(Component.text(argument.getType() == null ? argument.getArgument() + " " : "<" + argument.getArgument() + "> ", NamedTextColor.YELLOW));
            }
        });
        audience(sender).sendMessage(builder);
    }
}
