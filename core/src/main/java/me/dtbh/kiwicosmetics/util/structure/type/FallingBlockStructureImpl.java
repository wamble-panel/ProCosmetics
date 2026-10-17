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
package me.dtbh.kiwicosmetics.util.structure.type;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.api.nms.EntityTracker;
import me.dtbh.kiwicosmetics.api.nms.NMSEntity;
import me.dtbh.kiwicosmetics.api.util.structure.StructureData;
import me.dtbh.kiwicosmetics.api.util.structure.type.FallingBlockStructure;
import me.dtbh.kiwicosmetics.nms.EntityTrackerImpl;
import me.dtbh.kiwicosmetics.util.MathUtil;
import me.dtbh.kiwicosmetics.util.structure.StructureImpl;

import java.util.Map;

public class FallingBlockStructureImpl extends StructureImpl<NMSEntity> implements FallingBlockStructure {

    private static final KiwiCosmeticsPlugin PLUGIN = KiwiCosmeticsPlugin.getPlugin();
    private final EntityTracker tracker = new EntityTrackerImpl();

    public FallingBlockStructureImpl(StructureData data) {
        super(data, block -> block.isPassable() && !block.isLiquid());
    }

    @Override
    public double spawn(Location location) {
        double angle = calculateAngle(location);

        for (Map.Entry<Vector, BlockData> entry : data.getPlacement().entrySet()) {
            Vector vector = MathUtil.rotateAroundAxisY(entry.getKey().clone(), angle);
            BlockData blockData = entry.getValue().clone();

            rotate(blockData, location.getYaw());

            NMSEntity nmsFallingBlock = PLUGIN.getNMSManager().createFallingBlock(location.getWorld(), blockData, tracker);
            if (nmsFallingBlock.getBukkitEntity() instanceof FallingBlock fallingBlock) {
                fallingBlock.setGravity(false);
            }
            location.add(vector);
            nmsFallingBlock.setPositionRotation(location);
            location.subtract(vector);

            placedEntries.add(nmsFallingBlock);
        }
        tracker.startTracking();
        return angle;
    }

    @Override
    public void remove() {
        tracker.destroy();
        placedEntries.clear();
    }
}
