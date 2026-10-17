/*
 * This file is part of ProCosmetics - https://github.com/FilleDev/ProCosmetics
 * Copyright (C) 2025-2026 FilleDev and contributors
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
package se.filledev.procosmetics.api.cosmetic.gadget;

import org.jetbrains.annotations.Nullable;
import se.filledev.procosmetics.api.cosmetic.CosmeticRarity;
import se.filledev.procosmetics.api.cosmetic.CosmeticType;
import se.filledev.procosmetics.api.util.structure.StructureData;

import java.util.Map;

/**
 * Represents a type of gadget cosmetic.
 */
public interface GadgetType extends CosmeticType<GadgetType, GadgetBehavior> {

    /**
     * Gets the cooldown period for this gadget in seconds.
     *
     * @return the cooldown duration in seconds
     */
    double getCooldown();

    /**
     * Gets the cooldown period for this gadget in ticks.
     *
     * @return the cooldown duration in seconds
     */
    default long getCooldownTicks() {
        return (long) (getCooldown() * 20L);
    }

    /**
     * Gets the duration this gadget remains active in seconds.
     * This applies to gadgets with sustained effects or temporary spawns.
     *
     * @return the active duration in seconds
     */
    double getDuration();

    /**
     * Gets the duration this gadget remains active in ticks.
     *
     * @return the active duration in ticks
     */
    default long getDurationTicks() {
        return (long) (getDuration() * 20L);
    }

    /**
     * Gets the structure data for this gadget.
     * Some gadgets spawn structures when activated.
     *
     * @return the structure data, or null if this gadget doesn't use structures
     */
    @Nullable
    StructureData getStructure();

    /**
     * Builder interface for constructing gadget type instances.
     */
    interface Builder extends CosmeticType.Builder<GadgetType, GadgetBehavior, Builder> {

        /**
         * Sets the cooldown period for this gadget.
         *
         * @param cooldown the cooldown duration in seconds
         * @return this builder for method chaining
         */
        Builder cooldown(double cooldown);

        /**
         * Sets the duration this gadget remains active.
         *
         * @param duration the active duration in seconds
         * @return this builder for method chaining
         */
        Builder duration(double duration);

        /**
         * Sets the structure data for this gadget.
         *
         * @param structure the structure data, or null if not applicable
         * @return this builder for method chaining
         */
        Builder structure(StructureData structure);

        /**
         * Builds and returns the configured gadget type instance.
         *
         * @return the built gadget type
         */
        @Override
        GadgetType build();
    }
}
