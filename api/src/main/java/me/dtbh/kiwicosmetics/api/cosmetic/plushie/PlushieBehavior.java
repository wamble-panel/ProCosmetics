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
package me.dtbh.kiwicosmetics.api.cosmetic.plushie;

import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticContext;
import me.dtbh.kiwicosmetics.api.nms.NMSEntity;

/**
 * Defines the behavior for plushie cosmetics.
 *
 * @see CosmeticBehavior
 * @see PlushieType
 */
public interface PlushieBehavior extends CosmeticBehavior<PlushieType> {

    /**
     * Called once the plushie entity has been created, before it is shown to anyone.
     *
     * @param context   the context containing information about the plushie cosmetic
     * @param nmsEntity the mannequin entity of the plushie
     */
    void setupEntity(CosmeticContext<PlushieType> context, NMSEntity nmsEntity);

    /**
     * Called on every update tick for the plushie.
     *
     * @param context   the context containing information about the plushie cosmetic
     * @param nmsEntity the mannequin entity of the plushie
     * @param tick      the current tick count
     */
    void onUpdate(CosmeticContext<PlushieType> context, NMSEntity nmsEntity, int tick);
}
