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
package me.dtbh.kiwicosmetics.cosmetic.gadget.type;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.api.config.Config;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticContext;
import me.dtbh.kiwicosmetics.api.cosmetic.gadget.GadgetBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.gadget.GadgetType;
import me.dtbh.kiwicosmetics.api.nms.NMSEntity;
import me.dtbh.kiwicosmetics.api.user.User;
import me.dtbh.kiwicosmetics.util.MathUtil;
import me.dtbh.kiwicosmetics.util.MetadataUtil;
import me.dtbh.kiwicosmetics.util.material.Materials;
import me.dtbh.kiwicosmetics.worldguard.WorldGuardManager;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Hypixel-style Hype Train. The owner drives a locomotive by looking around, other players
 * right-click the train to hop on and get their own carriage at the back. Carriages follow the
 * exact path of the locomotive. The ride ends after the gadget duration, when the driver gets off,
 * or when the gadget is unequipped; everything is removed in {@link #end}.
 */
public class HypeTrain implements GadgetBehavior, Listener {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.SECTION_CHAR)
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static final float CAR_SCALE = 0.7f;
    private static final double CAR_HEIGHT = CAR_SCALE; // The block sits below the seat
    private static final double STEP_HEIGHT = 1.01d;
    private static final double FALL_SPEED = 0.35d;
    private static final double ACCELERATION = 0.02d;
    private static final double JOIN_DISTANCE_SQUARED = 8.0d * 8.0d;
    private static final double PATH_POINT_SPACING = 0.05d;

    private static final double DEFAULT_SPEED = 0.3d;
    private static final int DEFAULT_MAX_PASSENGERS = 10;
    private static final double DEFAULT_SPACING = 1.15d;
    private static final String DEFAULT_LOCOMOTIVE_BLOCK = "minecraft:diamond_block";

    private final List<Car> cars = new ArrayList<>();
    // Locomotive path, newest first: {x, baseY, z}
    private final ArrayDeque<double[]> path = new ArrayDeque<>();
    private final double[] point = new double[3];
    private final double[] ahead = new double[3];
    private final double[] behind = new double[3];
    private final Location location = new Location(null, 0.0d, 0.0d, 0.0d);

    private CosmeticContext<GadgetType> context;
    private BossBar bossBar;
    private World world;
    private double headX;
    private double headBaseY;
    private double headZ;
    private double velocity;
    private int rideTicks;
    private int bumpCooldown;
    private boolean ending;

    // Config
    private double speed = DEFAULT_SPEED;
    private int maxPassengers = DEFAULT_MAX_PASSENGERS;
    private double spacing = DEFAULT_SPACING;
    private BlockData locomotiveBlock;

    @Override
    public void onEquip(CosmeticContext<GadgetType> context) {
        this.context = context;
        Config config = context.getType().getCategory().getConfig();
        String prefix = "cosmetics." + context.getType().getKey() + ".";

        double configSpeed = config.getDouble(prefix + "speed");
        speed = configSpeed > 0.0d ? Math.min(configSpeed, 1.0d) : DEFAULT_SPEED;

        int configMax = config.getInt(prefix + "max_passengers");
        maxPassengers = configMax > 0 ? configMax : DEFAULT_MAX_PASSENGERS;

        double configSpacing = config.getDouble(prefix + "spacing");
        spacing = configSpacing > 0.0d ? configSpacing : DEFAULT_SPACING;

        String block = config.hasKey(prefix + "locomotive_block") ? config.getString(prefix + "locomotive_block") : DEFAULT_LOCOMOTIVE_BLOCK;
        try {
            locomotiveBlock = Bukkit.createBlockData(block);
        } catch (IllegalArgumentException e) {
            locomotiveBlock = Material.DIAMOND_BLOCK.createBlockData();
        }
    }

    @Override
    public InteractionResult onInteract(CosmeticContext<GadgetType> context, Action action, @Nullable Block clickedBlock, @Nullable Vector clickedPosition) {
        end(false);

        Player driver = context.getPlayer();
        Location start = driver.getLocation();
        world = start.getWorld();
        headX = start.getX();
        headBaseY = start.getY();
        headZ = start.getZ();
        velocity = 0.0d;
        rideTicks = 0;
        bumpCooldown = 0;

        // Lay down a straight path behind the locomotive so new carriages always have a spot
        Vector back = start.getDirection().setY(0.0d);
        if (back.lengthSquared() < 1.0E-4d) {
            back.setX(0.0d).setZ(1.0d);
        }
        back.normalize().multiply(-1.0d);
        double length = (maxPassengers + 1) * spacing + 1.0d;

        path.clear();
        for (double d = PATH_POINT_SPACING; d <= length; d += PATH_POINT_SPACING) {
            path.addLast(new double[]{headX + back.getX() * d, headBaseY, headZ + back.getZ() * d});
        }

        Car locomotive = spawnCar(driver, locomotiveBlock, start.getYaw(), 0.0d);
        cars.add(locomotive);

        bossBar = Bukkit.createBossBar("", BarColor.PINK, BarStyle.SEGMENTED_10);
        bossBar.addPlayer(driver);
        updateBossBar();

        context.getUser().sendMessage(context.getUser().translate("cosmetic.gadgets.hype_train.start"));
        world.playSound(start, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 1.2f);
        world.playSound(start, Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.5f);
        return InteractionResult.success();
    }

    private Car spawnCar(Player rider, BlockData blockData, float yaw, double distance) {
        pointAt(distance, point);
        location.setWorld(world);
        location.setX(point[0]);
        location.setY(point[1] + CAR_HEIGHT);
        location.setZ(point[2]);
        location.setYaw(yaw);
        location.setPitch(0.0f);

        BlockDisplay seat = world.spawn(location, BlockDisplay.class, entity -> {
            entity.setBlock(blockData);
            entity.setTeleportDuration(2);
            entity.setTransformationMatrix(new Matrix4f()
                    .scale(CAR_SCALE)
                    .translate(-0.5f, -1.0f, -0.5f));
            MetadataUtil.setCustomEntity(entity);
        });
        // The hitbox rides along, so it moves with the seat and covers the rider
        Interaction hitbox = world.spawn(location, Interaction.class, entity -> {
            entity.setInteractionWidth(1.0f);
            entity.setInteractionHeight(1.9f);
            entity.setResponsive(true);
            MetadataUtil.setCustomEntity(entity);
        });
        seat.addPassenger(rider);
        seat.addPassenger(hitbox);

        NMSEntity nmsSeat = context.getPlugin().getNMSManager().entityToNMSEntity(seat);
        return new Car(seat, nmsSeat, hitbox, rider);
    }

    @Override
    public void onUpdate(CosmeticContext<GadgetType> context) {
        if (cars.isEmpty() || ending) {
            return;
        }
        Car locomotive = cars.getFirst();
        Player driver = locomotive.rider();

        if (!driver.isOnline() || driver.isDead() || driver.getVehicle() != locomotive.seat()
                || ++rideTicks >= context.getType().getDurationTicks()) {
            end(true);
            return;
        }
        removeLeftPassengers();
        moveLocomotive(driver);

        if (cars.isEmpty()) {
            return;
        }
        positionCars();
        effects();

        if (rideTicks % 5 == 0) {
            updateBossBar();
        }
    }

    private void moveLocomotive(Player driver) {
        if (velocity < speed) {
            velocity = Math.min(speed, velocity + ACCELERATION);
        }
        double yaw = Math.toRadians(driver.getLocation().getYaw());
        double nextX = headX - Math.sin(yaw) * velocity;
        double nextZ = headZ + Math.cos(yaw) * velocity;
        double nextBaseY = findBaseY(nextX, headBaseY, nextZ);

        if (Double.isNaN(nextBaseY)) {
            // Hit a wall, stop until the driver turns
            velocity = 0.0d;

            if (bumpCooldown-- <= 0) {
                bumpCooldown = 20;
                location.setWorld(world);
                location.setX(headX);
                location.setY(headBaseY);
                location.setZ(headZ);
                world.playSound(location, Sound.BLOCK_ANVIL_LAND, 0.3f, 1.6f);
            }
            return;
        }

        if (nextBaseY < world.getMinHeight()) {
            end(true);
            return;
        }
        headX = nextX;
        headBaseY = nextBaseY;
        headZ = nextZ;
        recordPath();
    }

    private void recordPath() {
        double[] last = path.peekFirst();

        if (last == null || distance(last, headX, headBaseY, headZ) >= PATH_POINT_SPACING) {
            path.addFirst(new double[]{headX, headBaseY, headZ});
        }
        // Only keep as much path as the longest possible train needs
        double needed = (maxPassengers + 1) * spacing + 1.0d;
        double total = 0.0d;
        double prevX = headX, prevY = headBaseY, prevZ = headZ;
        Iterator<double[]> iterator = path.iterator();

        while (iterator.hasNext()) {
            double[] p = iterator.next();

            if (total > needed) {
                iterator.remove();
                continue;
            }
            total += distance(p, prevX, prevY, prevZ);
            prevX = p[0];
            prevY = p[1];
            prevZ = p[2];
        }
    }

    /**
     * Finds where the bottom of the train goes at x/z: steps up one block, falls down gently,
     * and glides over water. Returns NaN when blocked by a wall.
     */
    private double findBaseY(double x, double baseY, double z) {
        int blockX = (int) Math.floor(x);
        int blockZ = (int) Math.floor(z);
        Block at = world.getBlockAt(blockX, (int) Math.floor(baseY + 0.001d), blockZ);

        if (isFloor(at)) {
            double top = topOf(at);

            if (top - baseY > STEP_HEIGHT) {
                return Double.NaN;
            }
            int headroom = (int) Math.ceil(top - 0.001d);

            if (isFloor(world.getBlockAt(blockX, headroom, blockZ)) || isFloor(world.getBlockAt(blockX, headroom + 1, blockZ))) {
                return Double.NaN;
            }
            return Math.max(top, baseY);
        }

        if (isFloor(world.getBlockAt(blockX, (int) Math.floor(baseY + 0.001d) + 1, blockZ))) {
            return Double.NaN;
        }
        Block below = world.getBlockAt(blockX, (int) Math.floor(baseY - 0.001d), blockZ);

        if (isFloor(below)) {
            return Math.max(topOf(below), baseY - FALL_SPEED);
        }
        return baseY - FALL_SPEED;
    }

    private static boolean isFloor(Block block) {
        return !block.isPassable() || block.isLiquid();
    }

    private static double topOf(Block block) {
        if (block.isLiquid()) {
            return block.getY() + 0.9d;
        }
        BoundingBox box = block.getBoundingBox();
        return box.getVolume() > 0.0d ? box.getMaxY() : block.getY() + 1.0d;
    }

    private void positionCars() {
        for (int i = 0; i < cars.size(); i++) {
            Car car = cars.get(i);
            double distance = i * spacing;
            pointAt(distance, point);

            float yaw;
            if (i == 0) {
                yaw = car.rider().getLocation().getYaw();
            } else {
                pointAt(Math.max(0.0d, distance - 0.4d), ahead);
                pointAt(distance + 0.4d, behind);
                double dx = ahead[0] - behind[0];
                double dz = ahead[2] - behind[2];
                yaw = dx * dx + dz * dz > 1.0E-6d ? (float) Math.toDegrees(Math.atan2(-dx, dz)) : car.seat().getLocation().getYaw();
            }
            location.setWorld(world);
            location.setX(point[0]);
            location.setY(point[1] + CAR_HEIGHT);
            location.setZ(point[2]);
            location.setYaw(yaw);
            location.setPitch(0.0f);
            car.nmsSeat().setPositionRotation(location);
        }
    }

    /**
     * Point on the locomotive's path {@code distance} blocks behind it.
     */
    private void pointAt(double distance, double[] out) {
        double prevX = headX, prevY = headBaseY, prevZ = headZ;
        double travelled = 0.0d;

        for (double[] p : path) {
            double segment = distance(p, prevX, prevY, prevZ);

            if (travelled + segment >= distance && segment > 0.0d) {
                double t = (distance - travelled) / segment;
                out[0] = prevX + (p[0] - prevX) * t;
                out[1] = prevY + (p[1] - prevY) * t;
                out[2] = prevZ + (p[2] - prevZ) * t;
                return;
            }
            travelled += segment;
            prevX = p[0];
            prevY = p[1];
            prevZ = p[2];
        }
        out[0] = prevX;
        out[1] = prevY;
        out[2] = prevZ;
    }

    private static double distance(double[] p, double x, double y, double z) {
        double dx = p[0] - x, dy = p[1] - y, dz = p[2] - z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private void effects() {
        int passengers = cars.size() - 1;
        Location head = cars.getFirst().seat().getLocation(location);

        // Steam from the locomotive
        if (rideTicks % 3 == 0) {
            world.spawnParticle(Particle.CLOUD, head.getX(), head.getY() + 0.3d, head.getZ(), 1, 0.1d, 0.05d, 0.1d, 0.01d);
        }

        // Choo choo! More passengers, more hype
        int whistleInterval = Math.max(40, 120 - passengers * 8);
        int whistleTick = rideTicks % whistleInterval;
        if (whistleTick == 0) {
            world.playSound(head, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 1.2f);
        } else if (whistleTick == 5) {
            world.playSound(head, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 0.9f);
        }

        if (rideTicks % 4 != 0) {
            return;
        }
        for (int i = 0; i < cars.size(); i++) {
            Location car = cars.get(i).seat().getLocation(location);
            double x = car.getX(), y = car.getY() + 2.1d, z = car.getZ();

            // Note particles use the x offset as color when count is 0
            world.spawnParticle(Particle.NOTE, x, y, z, 0, MathUtil.THREAD_LOCAL_RANDOM.nextDouble(), 0.0d, 0.0d, 1.0d);

            if (passengers >= 3) {
                world.spawnParticle(Particle.FIREWORK, x, y - 1.0d, z, 1, 0.3d, 0.2d, 0.3d, 0.02d);
            }
            if (passengers >= 6) {
                world.spawnParticle(Particle.END_ROD, x, y - 0.5d, z, 1, 0.3d, 0.2d, 0.3d, 0.01d);
            }
        }
    }

    private void updateBossBar() {
        if (bossBar == null) {
            return;
        }
        long duration = Math.max(1L, context.getType().getDurationTicks());
        long remaining = Math.max(0L, duration - rideTicks);
        User user = context.getUser();

        bossBar.setTitle(LEGACY.serialize(user.translate(
                "cosmetic.gadgets.hype_train.bar",
                Placeholder.unparsed("passengers", String.valueOf(cars.size() - 1)),
                Placeholder.unparsed("max", String.valueOf(maxPassengers)),
                Placeholder.unparsed("seconds", String.valueOf((remaining + 19L) / 20L))
        )));
        bossBar.setProgress(Math.min(1.0d, Math.max(0.0d, (double) remaining / duration)));
    }

    private void removeLeftPassengers() {
        for (int i = cars.size() - 1; i >= 1; i--) {
            Car car = cars.get(i);
            Player rider = car.rider();

            if (rider.isOnline() && !rider.isDead() && rider.getVehicle() == car.seat()) {
                continue;
            }
            cars.remove(i);
            removeCar(car);
            bossBar.removePlayer(rider);
            protect(rider);
            announce("cosmetic.gadgets.hype_train.left", rider);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClickTrain(PlayerInteractEntityEvent event) {
        if (cars.isEmpty() || ending) {
            return;
        }
        Car clickedCar = findCar(event.getRightClicked());

        if (clickedCar == null) {
            return;
        }
        // Never let the click do anything else (feeding, other gadgets...)
        event.setCancelled(true);

        Player clicker = event.getPlayer();

        if (event.getHand() != EquipmentSlot.HAND || isOnTrain(clicker) || clicker.isInsideVehicle()
                || clicker.getGameMode() == GameMode.SPECTATOR
                || clicker.getWorld() != world
                || clicker.getLocation().distanceSquared(clickedCar.seat().getLocation()) > JOIN_DISTANCE_SQUARED) {
            return;
        }
        KiwiCosmeticsPlugin plugin = KiwiCosmeticsPlugin.getPlugin();
        User clickerUser = plugin.getUserManager().getConnected(clicker);

        if (cars.size() - 1 >= maxPassengers) {
            if (clickerUser != null) {
                clickerUser.sendMessage(clickerUser.translate("cosmetic.gadgets.hype_train.full"));
            }
            clicker.playSound(clicker, Sound.ENTITY_ITEM_BREAK, 0.5f, 1.0f);
            return;
        }
        WorldGuardManager worldGuard = plugin.getWorldGuardManager();

        if (worldGuard != null && !worldGuard.isCosmeticAllow(clicker, plugin.getCategoryRegistries().gadgets())) {
            return;
        }
        Car carriage = spawnCar(clicker, Materials.getRandomWool().createBlockData(),
                cars.getLast().seat().getLocation().getYaw(), cars.size() * spacing);
        cars.add(carriage);
        bossBar.addPlayer(clicker);
        updateBossBar();

        world.playSound(carriage.seat().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        world.playSound(carriage.seat().getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.5f);
        announce("cosmetic.gadgets.hype_train.joined", clicker);
    }

    @Nullable
    private Car findCar(Entity entity) {
        for (Car car : cars) {
            if (car.seat() == entity || car.hitbox() == entity || car.rider() == entity) {
                return car;
            }
        }
        return null;
    }

    private boolean isOnTrain(Player player) {
        for (Car car : cars) {
            if (car.rider() == player) {
                return true;
            }
        }
        return false;
    }

    private void announce(String key, Player subject) {
        KiwiCosmeticsPlugin plugin = KiwiCosmeticsPlugin.getPlugin();

        for (Car car : cars) {
            User user = plugin.getUserManager().getConnected(car.rider());

            if (user != null) {
                user.sendActionBar(user.translate(key, Placeholder.unparsed("player", subject.getName())));
            }
        }
    }

    private void protect(Player player) {
        User user = KiwiCosmeticsPlugin.getPlugin().getUserManager().getConnected(player);

        if (user != null) {
            user.setFallDamageProtection(5);
        }
    }

    private void removeCar(Car car) {
        car.seat().eject();

        if (car.hitbox().isValid()) {
            car.hitbox().remove();
        }
        if (car.seat().isValid()) {
            car.seat().remove();
        }
    }

    /**
     * Ends the ride: everyone gets off safely and every entity is removed.
     */
    private void end(boolean celebrate) {
        if (ending) {
            return;
        }
        ending = true;

        try {
            for (Car car : cars) {
                if (celebrate && car.seat().isValid()) {
                    Location carLocation = car.seat().getLocation(location);
                    world.spawnParticle(Particle.FIREWORK, carLocation.getX(), carLocation.getY() + 0.5d, carLocation.getZ(), 15, 0.3d, 0.3d, 0.3d, 0.1d);
                }
                removeCar(car);
                protect(car.rider());

                if (celebrate && car.rider().isOnline()) {
                    User user = KiwiCosmeticsPlugin.getPlugin().getUserManager().getConnected(car.rider());

                    if (user != null) {
                        user.sendMessage(user.translate("cosmetic.gadgets.hype_train.ended"));
                    }
                }
            }
            if (celebrate && !cars.isEmpty()) {
                Location head = cars.getFirst().seat().getLocation(location);
                world.playSound(head, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);
                world.playSound(head, Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
            }
            cars.clear();
            path.clear();

            if (bossBar != null) {
                bossBar.removeAll();
                bossBar = null;
            }
        } finally {
            ending = false;
        }
    }

    @Override
    public void onUnequip(CosmeticContext<GadgetType> context) {
        end(false);
    }

    @Override
    public boolean requiresGroundOnUse() {
        return true;
    }

    @Override
    public boolean isEnoughSpaceToUse(Location location) {
        Block feet = location.getBlock();
        return feet.isPassable() && feet.getRelative(0, 1, 0).isPassable() && feet.getRelative(0, 2, 0).isPassable();
    }

    @Override
    public boolean shouldUnequipOnTeleport() {
        return true;
    }

    private record Car(BlockDisplay seat, NMSEntity nmsSeat, Interaction hitbox, Player rider) {
    }
}
