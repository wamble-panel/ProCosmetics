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

import me.dtbh.kiwicosmetics.wardrobe.Wardrobe;
import me.dtbh.kiwicosmetics.wardrobe.hat.Hat;
import me.dtbh.kiwicosmetics.wardrobe.hat.HatRegistry;
import me.dtbh.kiwicosmetics.wardrobe.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public class HatsCmd implements CommandExecutor, TabCompleter {
    private static final List<String> SUBCOMMANDS = List.of("give", "create", "delete", "list", "inspect", "reload");

    private final Wardrobe plugin;

    public HatsCmd(Wardrobe plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String sub = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        if (!SUBCOMMANDS.contains(sub)) {
            plugin.sendLines(sender, "help-hats");
            return true;
        }
        if (!sender.hasPermission("kiwi.hats." + sub)) {
            plugin.send(sender, "no-permission");
            return true;
        }
        switch (sub) {
            case "give" -> give(sender, args);
            case "create" -> create(sender, args);
            case "delete" -> delete(sender, args);
            case "list" -> plugin.send(sender, "hat-list",
                    "!hats", Wardrobe.list(plugin.hats().ids()), "count", String.valueOf(plugin.hats().ids().size()));
            case "inspect" -> inspect(sender);
            case "reload" -> {
                plugin.reloadAll();
                plugin.send(sender, "reloaded");
            }
            default -> {
            }
        }
        return true;
    }

    /** /hats give <hat> [player] [amount] */
    private void give(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.send(sender, "usage", "usage", "/hats give <hat> [player] [amount]");
            return;
        }
        Hat hat = plugin.hats().get(args[1]);
        if (hat == null) {
            plugin.send(sender, "invalid-hat");
            return;
        }
        Player target = args.length >= 3 ? Bukkit.getPlayerExact(args[2]) : sender instanceof Player self ? self : null;
        if (target == null) {
            plugin.send(sender, args.length >= 3 ? "invalid-player" : "players-only");
            return;
        }
        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Math.max(1, Math.min(64, Integer.parseInt(args[3])));
            } catch (NumberFormatException e) {
                plugin.send(sender, "usage", "usage", "/hats give <hat> [player] [amount]");
                return;
            }
        }
        giveItem(target, plugin.hats().create(hat, amount));
        if (sender != target) {
            plugin.send(sender, "hat-given", "hat", hat.id(), "player", target.getName());
        }
        plugin.send(target, "hat-received", "hat", hat.id());
    }

    /** /hats create <material> [name...] - saves a new hat to hats.yml and gives it to you. */
    private void create(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.send(sender, "players-only");
            return;
        }
        if (args.length < 2) {
            plugin.send(sender, "usage", "usage", "/hats create <material> [name]");
            return;
        }
        Material material = Material.matchMaterial(args[1]);
        if (material == null || !material.isItem() || material.isAir()) {
            plugin.send(sender, "invalid-material", "material", args[1]);
            return;
        }
        String name = args.length > 2
                ? String.join(" ", Arrays.copyOfRange(args, 2, args.length))
                : "<gradient:#FFB5A7:#FFC8DD>" + Text.smallCaps(pretty(material)) + " ʜᴀᴛ</gradient>";
        try {
            String id = plugin.hats().add(plugin.hatsFile(), material, name);
            giveItem(player, plugin.hats().create(plugin.hats().get(id), 1));
            plugin.send(sender, "hat-created", "hat", id);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not save hats.yml", e);
            plugin.send(sender, "save-failed");
        }
    }

    /** /hats delete <hat> */
    private void delete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.send(sender, "usage", "usage", "/hats delete <hat>");
            return;
        }
        try {
            if (plugin.hats().remove(plugin.hatsFile(), args[1])) {
                plugin.send(sender, "hat-deleted", "hat", args[1].toLowerCase(Locale.ROOT));
            } else {
                plugin.send(sender, "invalid-hat");
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not save hats.yml", e);
            plugin.send(sender, "save-failed");
        }
    }

    /** Shows how a held item is tagged, to check old ExecutableItems hats are recognized. */
    private void inspect(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            plugin.send(sender, "players-only");
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        Hat hat = plugin.hats().match(item);
        line(sender, "ᴛʏᴘᴇ", item.getType().name().toLowerCase(Locale.ROOT));
        line(sender, "ʜᴀᴛ", hat == null ? "-" : hat.id());
        if (!item.hasItemMeta()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        String name = HatRegistry.plainName(meta);
        line(sender, "ɴᴀᴍᴇ", name == null ? "-" : name);
        PersistentDataContainer data = meta.getPersistentDataContainer();
        for (NamespacedKey key : data.getKeys()) {
            String value = data.has(key, PersistentDataType.STRING) ? data.get(key, PersistentDataType.STRING) : "(not text)";
            line(sender, "ᴛᴀɢ " + key.asString(), String.valueOf(value));
        }
    }

    private void line(CommandSender sender, String label, String value) {
        sender.sendMessage(Text.chat("<#8A8A8A>• <#F2F2F2>" + Text.escape(label) + " <#8A8A8A>— ")
                .append(Component.text(value, TextColor.color(0xFFE5EC))));
    }

    private static void giveItem(Player player, ItemStack item) {
        player.getInventory().addItem(item).values()
                .forEach(left -> player.getWorld().dropItem(player.getLocation(), left));
    }

    private static String pretty(Material material) {
        return material.name().toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 0) {
            return List.of();
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        String typed = args[args.length - 1];
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            for (String option : SUBCOMMANDS) {
                if (sender.hasPermission("kiwi.hats." + option)) {
                    options.add(option);
                }
            }
        } else if (!sender.hasPermission("kiwi.hats." + sub)) {
            return List.of();
        } else if (args.length == 2 && (sub.equals("give") || sub.equals("delete"))) {
            options.addAll(plugin.hats().ids());
        } else if (args.length == 2 && sub.equals("create")) {
            // Every item in the game, built on demand so nothing is kept in memory.
            String prefix = typed.toLowerCase(Locale.ROOT);
            for (Material material : Material.values()) {
                if (material.isItem() && !material.isAir() && !material.name().startsWith("LEGACY_")) {
                    String name = material.name().toLowerCase(Locale.ROOT);
                    if (name.startsWith(prefix)) {
                        options.add(name);
                    }
                }
            }
            return options;
        } else if (args.length == 3 && sub.equals("give")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!(sender instanceof Player self) || self.canSee(player)) {
                    options.add(player.getName());
                }
            }
        }
        return StringUtil.copyPartialMatches(typed, options, new ArrayList<>());
    }
}
