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

import me.dtbh.kiwicosmetics.api.cosmetic.Cosmetic;
import me.dtbh.kiwicosmetics.api.nms.NMSEntity;

/**
 * Represents a plushie cosmetic instance associated with a user.
 * Plushies are small skinned player figures that sit on an invisible seat next to the player.
 */
public interface Plushie extends Cosmetic<PlushieType, PlushieBehavior> {

    /**
     * Gets the NMS entity of the skinned figure.
     *
     * @return the mannequin entity of this plushie
     */
    NMSEntity getNMSEntity();

    /**
     * Gets the invisible entity the plushie sits on.
     *
     * @return the seat entity of this plushie
     */
    NMSEntity getSeat();
}
