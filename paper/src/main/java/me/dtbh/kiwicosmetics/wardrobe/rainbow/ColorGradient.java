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
package me.dtbh.kiwicosmetics.wardrobe.rainbow;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds smooth color cycles by interpolating between color stops in HSL space.
 * Colors are plain 0xRRGGBB ints so this class has no server dependency.
 */
public final class ColorGradient {
    private ColorGradient() {
    }

    /**
     * @param stops      the configured colors, as 0xRRGGBB
     * @param steps      how many colors each transition between two stops produces
     * @param smoothLoop whether the last stop fades back into the first one
     */
    public static List<Integer> build(List<Integer> stops, int steps, boolean smoothLoop) {
        List<Integer> result = new ArrayList<>();
        if (stops.isEmpty()) {
            return result;
        }
        if (stops.size() == 1) {
            result.add(stops.get(0));
            return result;
        }
        steps = Math.max(1, steps);
        int segments = smoothLoop ? stops.size() : stops.size() - 1;
        for (int i = 0; i < segments; i++) {
            float[] from = toHsl(stops.get(i));
            float[] to = toHsl(stops.get((i + 1) % stops.size()));
            // The end point is left out because it is the start of the next segment.
            for (int step = 0; step < steps; step++) {
                result.add(fromHsl(lerp(from, to, (float) step / steps)));
            }
        }
        if (!smoothLoop) {
            result.add(stops.get(stops.size() - 1));
        }
        return result;
    }

    static float[] lerp(float[] from, float[] to, float t) {
        float h1 = from[0];
        float h2 = to[0];
        // Black, white and greys have no meaningful hue: borrow the other color's hue
        // so e.g. orange -> black darkens the orange instead of sweeping through red.
        if (from[1] == 0f) {
            h1 = h2;
        } else if (to[1] == 0f) {
            h2 = h1;
        }
        // Take the shortest way around the color wheel.
        float delta = ((h2 - h1) % 360f + 540f) % 360f - 180f;
        float h = ((h1 + delta * t) % 360f + 360f) % 360f;
        float s = from[1] + (to[1] - from[1]) * t;
        float l = from[2] + (to[2] - from[2]) * t;
        return new float[]{h, s, l};
    }

    /**
     * @return {hue 0-360, saturation 0-1, lightness 0-1}
     */
    public static float[] toHsl(int rgb) {
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8) & 0xFF) / 255f;
        float b = (rgb & 0xFF) / 255f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float l = (max + min) / 2f;
        if (max == min) {
            return new float[]{0f, 0f, l};
        }
        float d = max - min;
        float s = l > 0.5f ? d / (2f - max - min) : d / (max + min);
        float h;
        if (max == r) {
            h = (g - b) / d + (g < b ? 6f : 0f);
        } else if (max == g) {
            h = (b - r) / d + 2f;
        } else {
            h = (r - g) / d + 4f;
        }
        return new float[]{h * 60f, s, l};
    }

    public static int fromHsl(float[] hsl) {
        float h = hsl[0] / 360f;
        float s = clamp(hsl[1]);
        float l = clamp(hsl[2]);
        float r;
        float g;
        float b;
        if (s == 0f) {
            r = g = b = l;
        } else {
            float q = l < 0.5f ? l * (1f + s) : l + s - l * s;
            float p = 2f * l - q;
            r = hueToRgb(p, q, h + 1f / 3f);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1f / 3f);
        }
        return (channel(r) << 16) | (channel(g) << 8) | channel(b);
    }

    private static float hueToRgb(float p, float q, float t) {
        if (t < 0f) {
            t += 1f;
        }
        if (t > 1f) {
            t -= 1f;
        }
        if (t < 1f / 6f) {
            return p + (q - p) * 6f * t;
        }
        if (t < 1f / 2f) {
            return q;
        }
        if (t < 2f / 3f) {
            return p + (q - p) * (2f / 3f - t) * 6f;
        }
        return p;
    }

    private static float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    private static int channel(float value) {
        return Math.round(clamp(value) * 255f);
    }

    /**
     * Parses "#ff7b00", "ff7b00" or "0xff7b00".
     *
     * @throws NumberFormatException if the value is not a 6 digit hex color
     */
    public static int parseHex(String value) {
        String hex = value.trim();
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        } else if (hex.startsWith("0x") || hex.startsWith("0X")) {
            hex = hex.substring(2);
        }
        if (hex.length() != 6) {
            throw new NumberFormatException("Expected 6 hex digits");
        }
        return Integer.parseInt(hex, 16);
    }
}
