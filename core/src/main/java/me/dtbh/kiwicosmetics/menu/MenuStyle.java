/*
 * This file is part of KiwiCosmetics, a fork of ProCosmetics - https://github.com/FilleDev/ProCosmetics
 * Copyright (C) 2026 FilleDev and contributors
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
package me.dtbh.kiwicosmetics.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

import java.util.Locale;

/**
 * Shared look of the menus: the pastel palette, small caps and progress bars.
 */
public final class MenuStyle {

    public static final TextColor PINK = TextColor.color(0xFFC8DD);
    public static final TextColor PEACH = TextColor.color(0xFFD8BE);
    public static final TextColor LIGHT_PINK = TextColor.color(0xFFE5EC);
    public static final TextColor MINT = TextColor.color(0xB5EAD7);
    public static final TextColor SOFT_RED = TextColor.color(0xFF9AA2);
    public static final TextColor LILAC = TextColor.color(0xCDB4DB);
    public static final TextColor SKY = TextColor.color(0xA2D2FF);
    public static final TextColor GREY = TextColor.color(0x8A8A8A);

    private static final String SMALL_CAPS = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢ";
    private static final int BAR_LENGTH = 10;

    private MenuStyle() {
    }

    /**
     * "Diamond Block" -> "ᴅɪᴀᴍᴏɴᴅ ʙʟᴏᴄᴋ"
     */
    public static String smallCaps(String text) {
        StringBuilder result = new StringBuilder(text.length());

        for (char c : text.toLowerCase(Locale.ROOT).toCharArray()) {
            result.append(c >= 'a' && c <= 'z' ? SMALL_CAPS.charAt(c - 'a') : c);
        }
        return result.toString();
    }

    /**
     * A bar like ■■■■■□□□□□, mint for the unlocked part and grey for the rest.
     */
    public static Component progressBar(int value, int max) {
        int filled = max <= 0 ? 0 : (int) Math.round((double) value / max * BAR_LENGTH);
        filled = Math.max(0, Math.min(BAR_LENGTH, filled));

        TextComponent.Builder builder = Component.text();
        builder.append(Component.text("■".repeat(filled), MINT));
        builder.append(Component.text("■".repeat(BAR_LENGTH - filled), GREY));
        return builder.build();
    }

    public static String percent(int value, int max) {
        return max <= 0 ? "0%" : Math.round(value * 100.0d / max) + "%";
    }
}
