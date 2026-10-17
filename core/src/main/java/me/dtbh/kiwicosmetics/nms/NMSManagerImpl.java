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
package me.dtbh.kiwicosmetics.nms;

import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.api.nms.EntityTracker;
import me.dtbh.kiwicosmetics.api.nms.NMSManager;
import me.dtbh.kiwicosmetics.nms.entitytype.CachedEntityType;
import me.dtbh.kiwicosmetics.nms.entitytype.EntityTypeRegistry;
import me.dtbh.kiwicosmetics.util.ReflectionUtil;
import me.dtbh.kiwicosmetics.util.version.VersionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class NMSManagerImpl implements NMSManager {

    private final Constructor<?> nmsEntityConstructor;
    private final Constructor<?> nmsFallingBlockConstructor;
    private final Constructor<?> realEntityConstructor;
    private final Constructor<?> nmsEquipmentConstructor;
    private final NMSUtilImpl nmsUtil;

    public NMSManagerImpl() {
        String path = KiwiCosmeticsPlugin.class.getPackage().getName() + "." + VersionUtil.VERSION.toString() + ".";
        Class<?> packetEntityClass = ReflectionUtil.getClass(path + "NMSEntity");
        Class<?> equipmentClass = ReflectionUtil.getClass(path + "NMSEquipment");

        nmsEntityConstructor = ReflectionUtil.getConstructor(packetEntityClass, World.class, CachedEntityType.class, EntityTracker.class);
        nmsFallingBlockConstructor = ReflectionUtil.getConstructor(packetEntityClass, World.class, BlockData.class, EntityTracker.class);
        realEntityConstructor = ReflectionUtil.getConstructor(packetEntityClass, Entity.class);
        nmsEquipmentConstructor = ReflectionUtil.getConstructor(equipmentClass, Player.class, boolean.class);

        try {
            nmsUtil = (NMSUtilImpl) ReflectionUtil.getClass(path + "NMSUtil").getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new IllegalStateException("Failed to instantiate NMSUtil.", e);
        }

        if (nmsEntityConstructor == null
                || nmsFallingBlockConstructor == null
                || realEntityConstructor == null
                || nmsEquipmentConstructor == null) {
            throw new IllegalStateException("Could not find constructors for NMS classes.");
        }
    }

    @Override
    public NMSEntityImpl<?> createEntity(World world, EntityType entityType) {
        return createEntity(world, entityType, null);
    }

    @Override
    public NMSEntityImpl<?> createEntity(World world, EntityType entityType, EntityTracker entityTracker) {
        if (entityTracker == null) {
            entityTracker = new EntityTrackerImpl();
        }
        CachedEntityType nmsEntityType = EntityTypeRegistry.getCachedEntityType(entityType);

        try {
            return (NMSEntityImpl<?>) nmsEntityConstructor.newInstance(world, nmsEntityType, entityTracker);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to create NMS entity for type " + entityType.name(), e);
        }
    }

    @Override
    public NMSEntityImpl<?> createFallingBlock(World world, BlockData blockData, EntityTracker entityTracker) {
        try {
            return (NMSEntityImpl<?>) nmsFallingBlockConstructor.newInstance(world, blockData, entityTracker);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to create NMS falling block entity.", e);
        }
    }

    @Override
    public NMSEntityImpl<?> entityToNMSEntity(Entity entity) {
        try {
            return (NMSEntityImpl<?>) realEntityConstructor.newInstance(entity);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to convert entity " + entity.getType().name() + " to NMS entity.", e);
        }
    }

    public AbstractNMSEquipment<?> createEquipment(Player player, boolean tracker) {
        try {
            return (AbstractNMSEquipment<?>) nmsEquipmentConstructor.newInstance(player, tracker);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to setup NMS equipment for player " + player.getName() + ".", e);
        }
    }

    @Override
    public NMSUtilImpl getNMSUtil() {
        return nmsUtil;
    }
}
