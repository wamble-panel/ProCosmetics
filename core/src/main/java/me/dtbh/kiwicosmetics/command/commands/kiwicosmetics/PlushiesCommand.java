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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticType;
import me.dtbh.kiwicosmetics.api.cosmetic.plushie.PlushieBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.plushie.PlushieType;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CosmeticCategory;
import me.dtbh.kiwicosmetics.api.locale.Translator;
import me.dtbh.kiwicosmetics.command.SubCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * /kc plushies reset &lt;player&gt; &lt;plushie|all&gt;: lets a player claim plushies again, for
 * example after losing the item. Their old items of those plushies stop working.
 */
public class PlushiesCommand extends SubCommand<CommandSender> {

    private static final String ALL = "all";

    public PlushiesCommand(KiwiCosmeticsPlugin plugin) {
        super(plugin, "kiwicosmetics.command.plushies", true);
        addFlat("plushies");
        addFlat("reset");
        addArgument(String.class, "player", sender -> plugin.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
        addArgument(String.class, "plushie", sender -> {
            List<String> keys = new ArrayList<>();
            keys.add(ALL);
            CosmeticCategory<PlushieType, PlushieBehavior, PlushieType.Builder> category = plugin.getCategoryRegistries().plushies();

            if (category != null) {
                category.getCosmeticRegistry().getTypes().stream().map(CosmeticType::getKey).forEach(keys::add);
            }
            return keys;
        });
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onExecute(CommandSender sender, String[] args) {
        Translator translator = translator(sender);
        String name = parseArgument(args, 2).toString();
        OfflinePlayer target = plugin.getServer().getPlayerExact(name);

        if (target == null) {
            // Name lookup of a player who isn't online; fine for an admin command
            target = plugin.getServer().getOfflinePlayer(name);

            if (!target.hasPlayedBefore()) {
                audience(sender).sendMessage(translator.translate("generic.player_not_found"));
                return;
            }
        }
        String plushie = parseArgument(args, 3).toString().toLowerCase();
        CosmeticCategory<PlushieType, PlushieBehavior, PlushieType.Builder> category = plugin.getCategoryRegistries().plushies();

        if (!plushie.equals(ALL) && (category == null || category.getCosmeticRegistry().getType(plushie) == null)) {
            audience(sender).sendMessage(translator.translate("cosmetic.not_found"));
            return;
        }
        int removed = plugin.getPlushieFurniture().getClaims().reset(target.getUniqueId(), plushie.equals(ALL) ? null : plushie);
        audience(sender).sendMessage(translator.translate("command.plushies.reset",
                Placeholder.unparsed("player", target.getName() != null ? target.getName() : name),
                Placeholder.unparsed("count", String.valueOf(removed))));
    }
}
