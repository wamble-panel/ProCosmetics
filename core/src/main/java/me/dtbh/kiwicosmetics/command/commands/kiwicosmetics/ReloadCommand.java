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
import me.dtbh.kiwicosmetics.menu.MenuStyle;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.api.event.PluginReloadEvent;
import me.dtbh.kiwicosmetics.command.SubCommand;

public class ReloadCommand extends SubCommand<CommandSender> {

    public ReloadCommand(KiwiCosmeticsPlugin plugin) {
        super(plugin, "kiwicosmetics.command.reload", true);
        addFlat("reload");
    }

    @Override
    public void onExecute(CommandSender sender, String[] args) {
        long before = System.currentTimeMillis();
        audience(sender).sendMessage(Component.text().append(
                Component.text("✦ ", MenuStyle.PINK),
                Component.text(MenuStyle.smallCaps("Reloading KiwiCosmetics..."), MenuStyle.PEACH)
        ));
        plugin.onDisable();
        plugin.onLoad();
        plugin.onEnable();
        plugin.getServer().getPluginManager().callEvent(new PluginReloadEvent(plugin));

        long took = System.currentTimeMillis() - before;
        audience(sender).sendMessage(Component.text().append(
                Component.text("✔ ", MenuStyle.MINT),
                Component.text(MenuStyle.smallCaps("Reloaded in "), MenuStyle.PEACH),
                Component.text(took + "ms", MenuStyle.LIGHT_PINK),
                Component.text(MenuStyle.smallCaps(". Restart the server if a change doesn't show up."), MenuStyle.GREY)
        ));
    }
}
