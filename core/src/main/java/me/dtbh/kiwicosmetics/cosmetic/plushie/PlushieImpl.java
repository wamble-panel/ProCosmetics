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
package me.dtbh.kiwicosmetics.cosmetic.plushie;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mannequin;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.api.cosmetic.plushie.Plushie;
import me.dtbh.kiwicosmetics.api.cosmetic.plushie.PlushieBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.plushie.PlushieType;
import me.dtbh.kiwicosmetics.api.nms.EntityTracker;
import me.dtbh.kiwicosmetics.api.nms.NMSEntity;
import me.dtbh.kiwicosmetics.api.user.User;
import me.dtbh.kiwicosmetics.cosmetic.CosmeticImpl;
import me.dtbh.kiwicosmetics.nms.EntityTrackerImpl;
import me.dtbh.kiwicosmetics.util.FastMathUtil;

/**
 * A skinned mannequin riding an invisible item display. Riding is what makes the client
 * draw it in the sitting pose, the seat floats next to the player like a miniature.
 */
public class PlushieImpl extends CosmeticImpl<PlushieType, PlushieBehavior> implements Plushie {

    private static final double MAX_DISTANCE_SQUARED_BEFORE_TELEPORT = 256.0d;
    private static final double DISTANCE_SQUARED_BEFORE_MOVE_TOWARDS_PLAYER = 3.5d;
    private static final double ACCELERATION = 0.15d;
    private static final double SEAT_HEIGHT = 0.9d;
    // A riding player model hangs this far below its seat, before scaling
    private static final double PLAYER_VEHICLE_ATTACHMENT = 0.6d;

    private final EntityTracker entityTracker = new EntityTrackerImpl();
    private NMSEntity seat;
    private NMSEntity entity;
    private int tick;
    private double currentSpeedX;
    private double currentSpeedZ;
    private double sitOffset;

    private final Location playerLocation = new Location(null, 0.0d, 0.0d, 0.0d);
    private final Location seatLocation = new Location(null, 0.0d, 0.0d, 0.0d);

    public PlushieImpl(KiwiCosmeticsPlugin plugin, User user, PlushieType type, PlushieBehavior behavior) {
        super(plugin, user, type, behavior);
    }

    @Override
    protected void onEquip() {
        spawnEntities();
        runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public void onUpdate() {
        player.getLocation(playerLocation);
        seat.getPreviousLocation(seatLocation);

        if (seatLocation.getWorld() != playerLocation.getWorld()) {
            return;
        }
        double distanceSquared = playerLocation.distanceSquared(seatLocation);

        if (distanceSquared > MAX_DISTANCE_SQUARED_BEFORE_TELEPORT) {
            moveTo(getSpawnLocation());
            return;
        }
        double yOffset = 0.15d * FastMathUtil.cos(FastMathUtil.toRadians(tick * 4));
        seatLocation.setY(playerLocation.getY() + SEAT_HEIGHT + yOffset);

        // Look at the player's face
        seatLocation.setDirection(playerLocation.add(0.0d, 1.5d, 0.0d)
                .subtract(seatLocation.getX(), seatLocation.getY() + sitOffset, seatLocation.getZ())
                .toVector()
                .normalize());

        if (distanceSquared > DISTANCE_SQUARED_BEFORE_MOVE_TOWARDS_PLAYER) {
            float angle = FastMathUtil.toRadians(seatLocation.getYaw());
            double targetSpeed = distanceSquared * 0.02d;
            double targetX = -FastMathUtil.sin(angle) * targetSpeed;
            double targetZ = FastMathUtil.cos(angle) * targetSpeed;

            currentSpeedX += (targetX - currentSpeedX) * ACCELERATION;
            currentSpeedZ += (targetZ - currentSpeedZ) * ACCELERATION;
        } else {
            currentSpeedX *= (1.0d - ACCELERATION);
            currentSpeedZ *= (1.0d - ACCELERATION);
        }
        seatLocation.add(currentSpeedX, 0.0d, currentSpeedZ);
        moveTo(seatLocation);

        if (++tick >= 360) {
            tick = 0;
        }
        behavior.onUpdate(this, entity, tick);
    }

    private void moveTo(Location location) {
        seat.sendPositionRotationPacket(location);
        // The client places a passenger on its seat by itself, this packet only carries the rotation
        entity.sendPositionRotationPacket(location.clone().subtract(0.0d, sitOffset, 0.0d));
    }

    @Override
    protected void onUnequip() {
        entityTracker.destroy();
        seat = null;
        entity = null;
    }

    private void spawnEntities() {
        Location location = getSpawnLocation();
        sitOffset = PLAYER_VEHICLE_ATTACHMENT * cosmeticType.getScale();

        // The seat must be added to the tracker first so it exists when the passenger spawns
        seat = plugin.getNMSManager().createEntity(player.getWorld(), EntityType.ITEM_DISPLAY, entityTracker);
        seat.setPositionRotation(location);

        if (seat.getBukkitEntity() instanceof Display display) {
            display.setTeleportDuration(2);
        }

        NMSEntity plushie = plugin.getNMSManager().createEntity(player.getWorld(), EntityType.MANNEQUIN, entityTracker);
        plushie.setPositionRotation(location.clone().subtract(0.0d, sitOffset, 0.0d));
        entity = plushie;

        if (plushie.getBukkitEntity() instanceof Mannequin mannequin) {
            AttributeInstance attribute = mannequin.getAttribute(Attribute.SCALE);

            if (attribute != null) {
                attribute.setBaseValue(cosmeticType.getScale());
            }
            plugin.getPlatformAdapter().hideMannequinDescription(mannequin);
            seat.getBukkitEntity().addPassenger(mannequin);

            PlushieSkins.resolve(plugin, player, cosmeticType.getSkin(), profile -> {
                // Name lookups finish later, the plushie may be gone by then
                if (entity != plushie) {
                    return;
                }
                plugin.getPlatformAdapter().setMannequinProfile(mannequin, profile);
                plushie.sendEntityMetadataPacket();
            });
        }
        behavior.setupEntity(this, entity);
        entityTracker.setOwner(player);
        entityTracker.startTracking();
    }

    private Location getSpawnLocation() {
        Location location = player.getLocation();
        location.add(0.0d, SEAT_HEIGHT, 0.0d).add(location.getDirection().setY(0.0d).normalize().multiply(1.5d));
        location.setDirection(player.getLocation()
                .subtract(location.getX(), location.getY(), location.getZ())
                .toVector()
                .normalize());
        return location;
    }

    @Override
    public NMSEntity getNMSEntity() {
        return entity;
    }

    @Override
    public NMSEntity getSeat() {
        return seat;
    }
}
