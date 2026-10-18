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
package me.dtbh.kiwicosmetics.cosmetic.plushie.type;

import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticContext;
import me.dtbh.kiwicosmetics.api.cosmetic.plushie.PlushieBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.plushie.PlushieType;
import me.dtbh.kiwicosmetics.api.nms.NMSEntity;

public class DefaultPlushie implements PlushieBehavior {

    @Override
    public void onEquip(CosmeticContext<PlushieType> context) {
    }

    @Override
    public void setupEntity(CosmeticContext<PlushieType> context, NMSEntity nmsEntity) {
    }

    @Override
    public void onUpdate(CosmeticContext<PlushieType> context, NMSEntity nmsEntity, int tick) {
    }

    @Override
    public void onUnequip(CosmeticContext<PlushieType> context) {
    }
}
