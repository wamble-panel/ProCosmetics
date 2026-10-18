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
package me.dtbh.kiwicosmetics.api.cosmetic.plushie;

import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticType;

/**
 * Represents a type of plushie cosmetic.
 */
public interface PlushieType extends CosmeticType<PlushieType, PlushieBehavior> {

    /**
     * Gets the skin of this plushie: {@code self} for the owner's own skin, a player name,
     * a skin texture URL or a base64 {@code textures} value.
     *
     * @return the skin of this plushie
     */
    String getSkin();

    /**
     * Gets the scale multiplier for this plushie's size.
     * A scale of 1.0 is the size of a player.
     *
     * @return the scale multiplier for the plushie
     */
    double getScale();

    /**
     * Builder interface for constructing plushie type instances.
     */
    interface Builder extends CosmeticType.Builder<PlushieType, PlushieBehavior, Builder> {

        /**
         * Sets the skin of this plushie.
         *
         * @param skin {@code self}, a player name, a skin texture URL or a base64 {@code textures} value
         * @return this builder for method chaining
         */
        PlushieType.Builder skin(String skin);

        /**
         * Sets the scale multiplier for this plushie's size.
         *
         * @param scale the scale multiplier (1.0 = player size)
         * @return this builder for method chaining
         */
        PlushieType.Builder scale(double scale);

        /**
         * Builds and returns the configured plushie type instance.
         *
         * @return the built plushie type
         */
        @Override
        PlushieType build();
    }
}
