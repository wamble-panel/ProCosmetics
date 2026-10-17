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
package me.dtbh.kiwicosmetics.wardrobe;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.regex.Pattern;

/**
 * Parses config text. MiniMessage ({@code <#FFC8DD>}, {@code <bold>}, {@code <gradient:...>}) is used
 * when the text contains tags; otherwise the legacy formats that ended up in the old configs:
 * {@code &a}, {@code §a}, {@code &#ff7b00}, ExecutableItems' bare {@code #ff7b00} and {@code §x§f§f§7§b§0§0}.
 */
public final class Text {
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build();
    private static final Pattern MINI_TAG = Pattern.compile("<[#/!a-zA-Z][^<>]*>");
    private static final Pattern SPIGOT_HEX = Pattern.compile("&x((?:&[0-9a-fA-F]){6})");
    private static final Pattern BARE_HEX = Pattern.compile("(?<!&)#([0-9a-fA-F]{6})");

    private Text() {
    }

    public static Component chat(String text) {
        if (MINI_TAG.matcher(text).find()) {
            return MINI.deserialize(text);
        }
        return LEGACY.deserialize(normalize(text));
    }

    /** Item names and lore: same as chat text, but not italic unless the config asks for it. */
    public static Component item(String text) {
        return chat(text).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    /** Makes player input safe to insert into a MiniMessage string. */
    public static String escape(String text) {
        return MINI.escapeTags(text);
    }

    private static final String SMALL_CAPS = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢ";

    /** "Diamond Block" -> "ᴅɪᴀᴍᴏɴᴅ ʙʟᴏᴄᴋ" */
    public static String smallCaps(String text) {
        StringBuilder result = new StringBuilder(text.length());
        for (char c : text.toLowerCase(java.util.Locale.ROOT).toCharArray()) {
            result.append(c >= 'a' && c <= 'z' ? SMALL_CAPS.charAt(c - 'a') : c);
        }
        return result.toString();
    }

    static String normalize(String text) {
        String result = text.replace('§', '&');
        result = SPIGOT_HEX.matcher(result).replaceAll(match -> "&#" + match.group(1).replace("&", ""));
        return BARE_HEX.matcher(result).replaceAll("&#$1");
    }
}
