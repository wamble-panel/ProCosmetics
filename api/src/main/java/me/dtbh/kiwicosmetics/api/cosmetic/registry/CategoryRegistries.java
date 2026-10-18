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
package me.dtbh.kiwicosmetics.api.cosmetic.registry;

import org.jetbrains.annotations.Nullable;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticType;
import me.dtbh.kiwicosmetics.api.cosmetic.arroweffect.ArrowEffectBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.arroweffect.ArrowEffectType;
import me.dtbh.kiwicosmetics.api.cosmetic.balloon.BalloonBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.balloon.BalloonType;
import me.dtbh.kiwicosmetics.api.cosmetic.banner.BannerBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.banner.BannerType;
import me.dtbh.kiwicosmetics.api.cosmetic.deatheffect.DeathEffectBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.deatheffect.DeathEffectType;
import me.dtbh.kiwicosmetics.api.cosmetic.gadget.GadgetBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.gadget.GadgetType;
import me.dtbh.kiwicosmetics.api.cosmetic.miniature.MiniatureBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.miniature.MiniatureType;
import me.dtbh.kiwicosmetics.api.cosmetic.music.MusicBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.music.MusicType;
import me.dtbh.kiwicosmetics.api.cosmetic.particleeffect.ParticleEffectBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.particleeffect.ParticleEffectType;
import me.dtbh.kiwicosmetics.api.cosmetic.plushie.PlushieBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.plushie.PlushieType;

import java.util.Collection;

/**
 * Represents the central registry that holds all cosmetic categories.
 *
 * @see CosmeticCategory
 * @see CosmeticRegistry
 * @see CosmeticType
 */
public interface CategoryRegistries {

    /**
     * Registers a new cosmetic category into the registry.
     *
     * @param registry the category to register
     * @param <T>      the cosmetic type contained within the category
     * @param <B>      the behavior type associated with the cosmetic
     * @param <U>      the builder type used to construct the cosmetic type
     */
    <T extends CosmeticType<T, B>, B extends CosmeticBehavior<T>, U extends CosmeticType.Builder<T, B, U>>
    void register(CosmeticCategory<T, B, U> registry);

    /**
     * Retrieves a cosmetic category by its unique key.
     *
     * @param key the category key
     * @param <T> the cosmetic type contained within the category
     * @param <B> the behavior type associated with the cosmetic
     * @param <U> the builder type used to construct the cosmetic type
     * @return the matching {@link CosmeticCategory}, or {@code null} if not found
     */
    @Nullable
    @SuppressWarnings("unchecked")
    default <T extends CosmeticType<T, B>, B extends CosmeticBehavior<T>, U extends CosmeticType.Builder<T, B, U>> CosmeticCategory<T, B, U> getCategory(String key) {
        return (CosmeticCategory<T, B, U>) getCategoryRaw(key);
    }

    /**
     * Checks whether a category with the given key exists in the registry.
     *
     * @param key the category key
     * @return {@code true} if a category with the key exists, otherwise {@code false}
     */
    default boolean hasRegistry(String key) {
        return getCategoryRaw(key) != null;
    }

    /**
     * Retrieves a cosmetic category without generic type safety.
     *
     * @param key the category key
     * @return the category, or {@code null} if not found
     */
    @Nullable
    CosmeticCategory<? extends CosmeticType<?, ?>,
            ? extends CosmeticBehavior<?>,
            ? extends CosmeticType.Builder<?, ?, ?>> getCategoryRaw(String key);

    /**
     * Returns all registered cosmetic categories.
     *
     * @return a collection of all registered {@link CosmeticCategory} instances
     */
    Collection<CosmeticCategory<? extends CosmeticType<?, ?>,
            ? extends CosmeticBehavior<?>,
            ? extends CosmeticType.Builder<?, ?, ?>>> getCategories();

    // --- Built-in category accessors ---

    /**
     * Gets the arrow effects cosmetic category.
     *
     * @return the built-in {@code arrow_effects} category, or {@code null} if not registered
     */
    default CosmeticCategory<ArrowEffectType, ArrowEffectBehavior, ArrowEffectType.Builder> arrowEffects() {
        return getCategory(BuiltIn.ARROW_EFFECTS);
    }

    /**
     * Gets the balloons cosmetic category.
     *
     * @return the built-in {@code balloons} category, or {@code null} if not registered
     */
    default CosmeticCategory<BalloonType, BalloonBehavior, BalloonType.Builder> balloons() {
        return getCategory(BuiltIn.BALLOONS);
    }

    /**
     * Gets the banners cosmetic category.
     *
     * @return the built-in {@code banners} category, or {@code null} if not registered
     */
    default CosmeticCategory<BannerType, BannerBehavior, BannerType.Builder> banners() {
        return getCategory(BuiltIn.BANNERS);
    }

    /**
     * Gets the hats cosmetic category (hats from hats.yml, shown on the head like banners).
     *
     * @return the built-in {@code hats} category, or {@code null} if not registered (not running on Paper)
     */
    default CosmeticCategory<BannerType, BannerBehavior, BannerType.Builder> hats() {
        return getCategory(BuiltIn.HATS);
    }

    /**
     * Gets the death effects cosmetic category.
     *
     * @return the built-in {@code death_effects} category, or {@code null} if not registered
     */
    default CosmeticCategory<DeathEffectType, DeathEffectBehavior, DeathEffectType.Builder> deathEffects() {
        return getCategory(BuiltIn.DEATH_EFFECTS);
    }

    /**
     * Gets the gadgets cosmetic category.
     *
     * @return the built-in {@code gadgets} category, or {@code null} if not registered
     */
    default CosmeticCategory<GadgetType, GadgetBehavior, GadgetType.Builder> gadgets() {
        return getCategory(BuiltIn.GADGETS);
    }

    /**
     * Gets the miniatures cosmetic category.
     *
     * @return the built-in {@code miniatures} category, or {@code null} if not registered
     */
    default CosmeticCategory<MiniatureType, MiniatureBehavior, MiniatureType.Builder> miniatures() {
        return getCategory(BuiltIn.MINIATURES);
    }

    /**
     * Gets the music cosmetic category.
     *
     * @return the built-in {@code music} category, or {@code null} if not registered
     */
    default CosmeticCategory<MusicType, MusicBehavior, MusicType.Builder> music() {
        return getCategory(BuiltIn.MUSIC);
    }

    /**
     * Gets the particle effects cosmetic category.
     *
     * @return the built-in {@code particle_effects} category, or {@code null} if not registered
     */
    default CosmeticCategory<ParticleEffectType, ParticleEffectBehavior, ParticleEffectType.Builder> particleEffects() {
        return getCategory(BuiltIn.PARTICLE_EFFECTS);
    }

    /**
     * Gets the trails cosmetic category. Trails use the particle effect types and behaviors.
     *
     * @return the built-in {@code trails} category, or {@code null} if not registered
     */
    default CosmeticCategory<ParticleEffectType, ParticleEffectBehavior, ParticleEffectType.Builder> trails() {
        return getCategory(BuiltIn.TRAILS);
    }

    /**
     * Gets the plushies cosmetic category.
     *
     * @return the built-in {@code plushies} category, or {@code null} if not registered
     */
    default CosmeticCategory<PlushieType, PlushieBehavior, PlushieType.Builder> plushies() {
        return getCategory(BuiltIn.PLUSHIES);
    }

    /**
     * Defines constant keys for all built-in cosmetic categories.
     */
    interface BuiltIn {

        /**
         * Built-in key for arrow effects.
         */
        String ARROW_EFFECTS = "arrow_effects";

        /**
         * Built-in key for balloons.
         */
        String BALLOONS = "balloons";

        /**
         * Built-in key for banners.
         */
        String BANNERS = "banners";

        /**
         * Built-in key for death effects.
         */
        String DEATH_EFFECTS = "death_effects";

        /**
         * Built-in key for gadgets.
         */
        String GADGETS = "gadgets";

        /**
         * Built-in key for miniatures.
         */
        String MINIATURES = "miniatures";

        /**
         * Built-in key for music cosmetics.
         */
        String MUSIC = "music";

        /**
         * Built-in key for particle effects.
         */
        String PARTICLE_EFFECTS = "particle_effects";

        /**
         * Built-in key for trails.
         */
        String TRAILS = "trails";

        /**
         * Built-in key for plushies.
         */
        String PLUSHIES = "plushies";

        /**
         * Built-in key for hats (from hats.yml, Paper only).
         */
        String HATS = "hats";

    }
}
