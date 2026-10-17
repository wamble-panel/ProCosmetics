/*
 * This file is part of KiwiCosmetics, a fork of ProCosmetics - https://github.com/FilleDev/ProCosmetics
 * Copyright (C) 2026 FilleDev, dtbh and contributors
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
package me.dtbh.kiwicosmetics.wardrobe.command;

import me.dtbh.kiwicosmetics.wardrobe.rainbow.ArmorPiece;
import me.dtbh.kiwicosmetics.wardrobe.rainbow.ColorPattern;
import me.dtbh.kiwicosmetics.wardrobe.Wardrobe;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RainbowCmd implements CommandExecutor, TabCompleter {
    private static final String GIVE_USAGE = "/rainbow give <pattern> [player] [helmet|chestplate|leggings|boots|all]";

    private final Wardrobe plugin;

    public RainbowCmd(Wardrobe plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String sub = args.length == 0 ? "" : args[0].toLowerCase();
        switch (sub) {
            case "reload" -> {
                if (checkPermission(sender, "kiwi.rainbow.reload")) {
                    plugin.reloadAll();
                    plugin.send(sender, "reloaded");
                }
            }
            case "list" -> {
                if (checkPermission(sender, "kiwi.rainbow.list")) {
                    plugin.send(sender, "pattern-list", "!patterns", Wardrobe.list(plugin.patternList()));
                }
            }
            case "give" -> {
                if (checkPermission(sender, "kiwi.rainbow.give")) {
                    give(sender, args);
                }
            }
            default -> plugin.sendLines(sender, "help-rainbow");
        }
        return true;
    }

    private void give(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.send(sender, "usage", "usage", GIVE_USAGE);
            return;
        }
        ColorPattern pattern = plugin.pattern(args[1]);
        if (pattern == null) {
            plugin.send(sender, "invalid-pattern", "!patterns", Wardrobe.list(plugin.patternList()));
            return;
        }

        Player target;
        if (args.length >= 3) {
            target = Bukkit.getPlayerExact(args[2]);
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            plugin.send(sender, "usage", "usage", GIVE_USAGE);
            return;
        }
        if (target == null) {
            plugin.send(sender, "invalid-player");
            return;
        }

        List<ArmorPiece> pieces;
        if (args.length < 4 || args[3].equalsIgnoreCase("all")) {
            pieces = List.of(ArmorPiece.VALUES);
        } else {
            ArmorPiece piece = ArmorPiece.fromKey(args[3]);
            if (piece == null) {
                plugin.send(sender, "invalid-piece");
                return;
            }
            pieces = List.of(piece);
        }

        long tick = plugin.task().tick();
        List<ItemStack> items = new ArrayList<>();
        for (ArmorPiece piece : pieces) {
            ItemStack item = pattern.createItem(piece, tick);
            if (item != null) {
                items.add(item);
            }
        }
        Map<Integer, ItemStack> leftovers = target.getInventory().addItem(items.toArray(new ItemStack[0]));
        Location location = target.getLocation();
        leftovers.values().forEach(item -> target.getWorld().dropItem(location, item));

        if (sender != target) {
            plugin.send(sender, "given", "pattern", pattern.name(), "player", target.getName());
        }
        plugin.send(target, "received", "pattern", pattern.name(), "player", target.getName());
    }

    private boolean checkPermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        }
        plugin.send(sender, "no-permission");
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 0) {
            return List.of();
        }
        List<String> options = new ArrayList<>();
        boolean canGive = sender.hasPermission("kiwi.rainbow.give");
        boolean giving = canGive && args[0].equalsIgnoreCase("give");
        switch (args.length) {
            case 1 -> {
                if (canGive) {
                    options.add("give");
                }
                if (sender.hasPermission("kiwi.rainbow.list")) {
                    options.add("list");
                }
                if (sender.hasPermission("kiwi.rainbow.reload")) {
                    options.add("reload");
                }
            }
            case 2 -> {
                if (giving) {
                    options.addAll(plugin.patternList());
                }
            }
            case 3 -> {
                if (giving) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (!(sender instanceof Player self) || self.canSee(player)) {
                            options.add(player.getName());
                        }
                    }
                }
            }
            case 4 -> {
                if (giving) {
                    for (ArmorPiece piece : ArmorPiece.VALUES) {
                        options.add(piece.key());
                    }
                    options.add("all");
                }
            }
            default -> {
            }
        }
        return StringUtil.copyPartialMatches(args[args.length - 1], options, new ArrayList<>());
    }
}
