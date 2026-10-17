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
package me.dtbh.kiwicosmetics.api.event;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import me.dtbh.kiwicosmetics.api.KiwiCosmetics;

/**
 * Called after the plugin has finished reloading.
 */
public class PluginReloadEvent extends KiwiCosmeticsEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * Constructs a new PluginReloadEvent.
     *
     * @param plugin the KiwiCosmetics plugin instance
     */
    public PluginReloadEvent(KiwiCosmetics plugin) {
        super(plugin);
    }

    /**
     * Gets the handler list for this event.
     *
     * @return the {@link HandlerList} instance
     */
    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    /**
     * Gets the handler list for this event.
     *
     * @return the {@link HandlerList} instance
     */
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
