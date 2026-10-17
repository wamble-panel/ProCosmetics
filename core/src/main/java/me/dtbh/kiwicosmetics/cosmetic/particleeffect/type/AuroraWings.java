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
package me.dtbh.kiwicosmetics.cosmetic.particleeffect.type;

import me.dtbh.kiwicosmetics.cosmetic.particleeffect.shape.ShapeParticleEffectBehavior;
import org.bukkit.Color;

public class AuroraWings extends ShapeParticleEffectBehavior {

    // Pastel palette, from the wing tips (1) to the center (5)
    private static final Color[] COLORS = new Color[]{
            null,
            Color.fromRGB(0xFFB5A7),
            Color.fromRGB(0xFFC8DD),
            Color.fromRGB(0xCDB4DB),
            Color.fromRGB(0xA2D2FF),
            Color.fromRGB(0xB5EAD7)
    };

    private static final int[][] MASK = new int[][]{
            {0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0},
            {1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1},
            {0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0}
    };

    private static final int[][] SHAPE = colorByDistance(MASK);

    public AuroraWings() {
        super(settings()
                .shape(SHAPE)
                .colorProvider(value -> COLORS[value])
                .spacing(0.18d)
                .heightOffset(0.2d)
                .distanceBehind(0.3d)
                .movingParticleCount(4)
                .movingParticleSpread(0.1d)
                .shouldRotate(false)
                .rotationSpeed(0.0f)
                .positionMode(PositionMode.BEHIND_PLAYER)
        );
    }

    /**
     * Turns the wing mask into color values: the further a point is from the spine, the warmer its color.
     */
    private static int[][] colorByDistance(int[][] mask) {
        int[][] shape = new int[mask.length][];
        int center = mask[0].length / 2;

        for (int row = 0; row < mask.length; row++) {
            shape[row] = new int[mask[row].length];

            for (int col = 0; col < mask[row].length; col++) {
                if (mask[row][col] == 0) {
                    continue;
                }
                int distance = Math.abs(col - center);
                shape[row][col] = distance >= 8 ? 1 : distance >= 6 ? 2 : distance >= 4 ? 3 : distance >= 2 ? 4 : 5;
            }
        }
        return shape;
    }
}
