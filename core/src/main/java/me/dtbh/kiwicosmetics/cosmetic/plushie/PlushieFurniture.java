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
package me.dtbh.kiwicosmetics.cosmetic.plushie;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import me.dtbh.kiwicosmetics.KiwiCosmeticsPlugin;
import me.dtbh.kiwicosmetics.api.config.Config;
import me.dtbh.kiwicosmetics.api.cosmetic.plushie.PlushieBehavior;
import me.dtbh.kiwicosmetics.api.cosmetic.plushie.PlushieType;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CosmeticCategory;
import me.dtbh.kiwicosmetics.api.user.User;
import me.dtbh.kiwicosmetics.menu.MenuStyle;
import me.dtbh.kiwicosmetics.util.item.ItemBuilderImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Placed plushies: a skinned mannequin riding an invisible item display, which is what makes
 * the client draw it sitting. Both are real entities, so they are saved with the world like
 * any other decoration. Right-click a plushie to hug it, hit it or sneak + right-click to pick it back up.
 */
public class PlushieFurniture implements Listener {

    public static final String PERMISSION_UNLIMITED = "kiwicosmetics.plushies.unlimited";
    public static final String PERMISSION_ADMIN = "kiwicosmetics.plushies.admin";
    private static final int DEFAULT_MAX_PLACED = 10;
    private static final String ENTRY_SEPARATOR = ";";
    // How tall a sitting plushie is at scale 1, and how far from a wall it sits when a side is clicked
    private static final double SEATED_HEIGHT = 1.2d;
    private static final double WALL_DISTANCE = 0.2d;

    private final KiwiCosmeticsPlugin plugin;
    // On plushie items, placed mannequins and their seats: the plushie key
    private final NamespacedKey typeKey;
    // On items and mannequins: the skin to use instead of the one in plushies.yml
    private final NamespacedKey skinKey;
    private final NamespacedKey ownerKey;
    private final NamespacedKey ownerNameKey;
    // On players: the plushies they placed, as "uuid@world@chunkX@chunkZ" entries
    private final NamespacedKey placedKey;

    public PlushieFurniture(KiwiCosmeticsPlugin plugin) {
        this.plugin = plugin;
        this.typeKey = new NamespacedKey(plugin, "plushie");
        this.skinKey = new NamespacedKey(plugin, "plushie_skin");
        this.ownerKey = new NamespacedKey(plugin, "plushie_owner");
        this.ownerNameKey = new NamespacedKey(plugin, "plushie_owner_name");
        this.placedKey = new NamespacedKey(plugin, "plushies_placed");
    }

    // ---- items

    /**
     * Gives the player the item of a plushie. "self" plushies remember the skin of the player
     * they were given to, so they keep looking like them when given away.
     */
    public void giveItem(User user, Player player, PlushieType type) {
        if (hasItem(player, type)) {
            user.sendMessage(user.translate("plushies.already_have", Placeholder.unparsed("cosmetic", type.getName(user))));
            return;
        }
        if (player.getInventory().firstEmpty() == -1) {
            user.sendMessage(user.translate("plushies.inventory_full"));
            return;
        }
        String skin = PlushieSkins.SELF.equalsIgnoreCase(type.getSkin()) ? PlushieSkins.skinOf(player) : null;
        player.getInventory().addItem(createItem(user, type, skin));
        player.playSound(player, Sound.ENTITY_ITEM_PICKUP, 0.6f, 1.2f);
        user.sendMessage(user.translate("plushies.received", Placeholder.unparsed("cosmetic", type.getName(user))));
    }

    private ItemStack createItem(User user, PlushieType type, @Nullable String skin) {
        ItemBuilderImpl builder = new ItemBuilderImpl(type.getItemStack())
                .setDisplayName(user.translate("item.plushie.name", Placeholder.unparsed("name", MenuStyle.smallCaps(type.getName(user)))))
                .setLore(user.translateList("item.plushie.desc"))
                .setCustomData(typeKey, PersistentDataType.STRING, type.getKey());

        if (skin != null) {
            builder.setCustomData(skinKey, PersistentDataType.STRING, skin);
            PlayerProfile profile = PlushieSkins.textureProfile(plugin, skin);

            if (profile != null) {
                builder.modifyItemMeta(meta -> {
                    if (meta instanceof SkullMeta skullMeta) {
                        skullMeta.setOwnerProfile(profile);
                    }
                });
            }
        }
        return builder.hide().getItemStack();
    }

    private boolean hasItem(Player player, PlushieType type) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (type.getKey().equals(readKey(item))) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private String readKey(@Nullable ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
    }

    @Nullable
    private PlushieType getType(@Nullable String key) {
        CosmeticCategory<PlushieType, PlushieBehavior, PlushieType.Builder> category = plugin.getCategoryRegistries().plushies();

        if (key == null || category == null) {
            return null;
        }
        return category.getCosmeticRegistry().getType(key);
    }

    // ---- placing

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlace(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() == null) {
            return;
        }
        ItemStack item = event.getItem();
        String key = readKey(item);

        if (key == null) {
            return;
        }
        Player player = event.getPlayer();
        Block clicked = event.getClickedBlock();

        // Holding a plushie should not stop you from opening chests and doors, just like holding a block
        if (clicked == null || !player.isSneaking() && clicked.getType().isInteractable()) {
            return;
        }
        // The plushie item is a player head, it must never be placed as one
        event.setCancelled(true);

        User user = plugin.getUserManager().getConnected(player);
        PlushieType type = getType(key);

        if (user == null || type == null || !type.isEnabled() || !type.getCategory().isEnabled()) {
            return;
        }
        if (!type.hasPermission(player)) {
            user.sendMessage(user.translate("cosmetic.locked", Placeholder.unparsed("cosmetic", type.getName(user))));
            return;
        }
        Location location = findSpot(player, clicked, event.getBlockFace());

        if (location == null) {
            user.sendMessage(user.translate("plushies.no_ground"));
            return;
        }
        World world = location.getWorld();
        double height = SEATED_HEIGHT * type.getScale();

        if (!isOpen(world, location.getX(), location.getY() + 0.05d, location.getZ())
                || !isOpen(world, location.getX(), location.getY() + height, location.getZ())) {
            user.sendMessage(user.translate("plushies.no_space"));
            return;
        }
        // Only the exact same spot is taken, plushies can sit right next to each other
        if (!world.getNearbyEntities(location, 0.1d, height, 0.1d, this::isPlushie).isEmpty()) {
            user.sendMessage(user.translate("plushies.occupied"));
            return;
        }
        float yaw = Math.round((player.getLocation().getYaw() + 180.0f) / 22.5f) * 22.5f;
        location.setYaw(yaw);
        Block target = world.getBlockAt(location.getBlockX(), Location.locToBlock(location.getY() + 0.05d), location.getBlockZ());

        List<String> placed = getPlaced(player);
        int max = getMaxPlaced();

        if (max >= 0 && placed.size() >= max && !player.hasPermission(PERMISSION_UNLIMITED)) {
            user.sendMessage(user.translate("plushies.limit", Placeholder.unparsed("max", String.valueOf(max))));
            return;
        }
        // Let protection plugins (claims, regions) decide as if a block was placed here
        BlockPlaceEvent placeEvent = new BlockPlaceEvent(target, target.getState(), clicked, item, player, true, event.getHand());
        plugin.getServer().getPluginManager().callEvent(placeEvent);

        if (placeEvent.isCancelled() || !placeEvent.canBuild()) {
            return;
        }
        String skin = item.getItemMeta().getPersistentDataContainer().get(skinKey, PersistentDataType.STRING);
        Mannequin mannequin = spawn(location, type, skin, headOf(item), player);

        placed.add(mannequin.getUniqueId() + "@" + target.getWorld().getName() + "@" + (target.getX() >> 4) + "@" + (target.getZ() >> 4));
        setPlaced(player, placed);

        if (player.getGameMode() != GameMode.CREATIVE) {
            ItemStack inHand = player.getInventory().getItem(event.getHand());
            inHand.setAmount(inHand.getAmount() - 1);
            player.getInventory().setItem(event.getHand(), inHand.getAmount() > 0 ? inHand : null);
        }
        world.playSound(location, Sound.BLOCK_WOOL_PLACE, SoundCategory.BLOCKS, 1.0f, 1.2f);
        world.spawnParticle(Particle.HEART, location.clone().add(0.0d, 0.8d * type.getScale() / 0.5d, 0.0d), 1);
        user.sendMessage(user.translate("plushies.placed",
                Placeholder.unparsed("cosmetic", type.getName(user)),
                Placeholder.unparsed("count", String.valueOf(placed.size())),
                Placeholder.unparsed("max", max >= 0 && !player.hasPermission(PERMISSION_UNLIMITED) ? String.valueOf(max) : "∞")));
    }

    /**
     * A plain copy of the plushie item for the plushie to wear: same head, but without the
     * plushie data, name and lore, so it is never a second plushie.
     */
    private ItemStack headOf(ItemStack item) {
        return new ItemBuilderImpl(item)
                .setAmount(1)
                .modifyItemMeta(meta -> {
                    meta.setDisplayName(null);
                    meta.setLore(null);
                    meta.getPersistentDataContainer().remove(typeKey);
                    meta.getPersistentDataContainer().remove(skinKey);
                })
                .getItemStack();
    }

    /**
     * Where the plushie goes: the exact point the player looks at on top of a block, or the
     * floor right in front of a wall when its side is clicked. Snapped to the 1/16 pixel grid.
     *
     * @return the spot, or null when there is nothing to sit on
     */
    @Nullable
    private Location findSpot(Player player, Block clicked, BlockFace clickedFace) {
        World world = player.getWorld();
        Location eye = player.getEyeLocation();
        // Passable blocks (grass, flowers) are looked through, the plushie sits on the ground below
        RayTraceResult hit = world.rayTraceBlocks(eye, eye.getDirection(), 8.0d, FluidCollisionMode.NEVER, true);
        Vector point;
        BlockFace face;

        if (hit != null && hit.getHitBlock() != null && hit.getHitBlockFace() != null) {
            point = hit.getHitPosition();
            face = hit.getHitBlockFace();
        } else {
            point = clicked.getLocation().toVector().add(new Vector(0.5d, 0.5d, 0.5d)).add(clickedFace.getDirection().multiply(0.5d));
            face = clickedFace;
        }

        if (face == BlockFace.DOWN) {
            return null;
        }
        if (face != BlockFace.UP) {
            point = point.clone().add(face.getDirection().multiply(WALL_DISTANCE));
            Block column = world.getBlockAt(point.getBlockX(), point.getBlockY(), point.getBlockZ());
            Block support = column.isPassable() ? column.getRelative(BlockFace.DOWN) : column;
            double top = support.getBoundingBox().getMaxY();

            if (!support.getType().isSolid() || top <= support.getY()) {
                return null;
            }
            point.setY(top);
        }
        return new Location(world, snap(point.getX()), point.getY(), snap(point.getZ()));
    }

    private static double snap(double value) {
        return Math.round(value * 16.0d) / 16.0d;
    }

    /**
     * Whether a point is not inside a block's collision, so slabs, stairs and carpets count
     * by their real shape.
     */
    private static boolean isOpen(World world, double x, double y, double z) {
        Block block = world.getBlockAt(Location.locToBlock(x), Location.locToBlock(y), Location.locToBlock(z));

        if (block.getType() == Material.LAVA) {
            return false;
        }
        if (block.isPassable()) {
            return true;
        }
        double rx = x - block.getX();
        double ry = y - block.getY();
        double rz = z - block.getZ();
        return !block.getCollisionShape().overlaps(new BoundingBox(rx - 0.01d, ry - 0.01d, rz - 0.01d, rx + 0.01d, ry + 0.01d, rz + 0.01d));
    }

    private Mannequin spawn(Location location, PlushieType type, @Nullable String skin, ItemStack head, Player owner) {
        World world = location.getWorld();
        ItemDisplay seat = world.spawn(location, ItemDisplay.class, display ->
                display.getPersistentDataContainer().set(typeKey, PersistentDataType.STRING, type.getKey()));

        Mannequin mannequin = world.spawn(location, Mannequin.class, spawned -> {
            AttributeInstance scale = spawned.getAttribute(Attribute.SCALE);

            if (scale != null) {
                scale.setBaseValue(type.getScale());
            }
            // Not invulnerable: that would hide survival players' hits, damage is cancelled in onDamage
            spawned.setSilent(true);
            spawned.setCollidable(false);
            spawned.setRemoveWhenFarAway(false);
            spawned.setPersistent(true);
            plugin.getPlatformAdapter().hideMannequinDescription(spawned);

            EntityEquipment equipment = spawned.getEquipment();
            if (equipment != null) {
                equipment.setHelmet(head);
            }

            PersistentDataContainer data = spawned.getPersistentDataContainer();
            data.set(typeKey, PersistentDataType.STRING, type.getKey());
            data.set(ownerKey, PersistentDataType.STRING, owner.getUniqueId().toString());
            data.set(ownerNameKey, PersistentDataType.STRING, owner.getName());

            if (skin != null) {
                data.set(skinKey, PersistentDataType.STRING, skin);
            }
        });
        // Riding is what makes the client draw the mannequin sitting
        seat.addPassenger(mannequin);

        PlushieSkins.resolve(plugin, owner, skin != null ? skin : type.getSkin(), profile -> {
            // Name lookups finish later, the plushie may have been picked up by then
            if (mannequin.isValid()) {
                plugin.getPlatformAdapter().setMannequinProfile(mannequin, profile);
                wearSkinIfBlank(mannequin, profile);
            }
        });
        return mannequin;
    }

    /**
     * A head without a skin (a self plushie whose skin was unknown when it was handed out)
     * gets the skin the plushie ended up with.
     */
    private void wearSkinIfBlank(Mannequin mannequin, PlayerProfile profile) {
        EntityEquipment equipment = mannequin.getEquipment();
        ItemStack helmet = equipment != null ? equipment.getHelmet() : null;

        if (helmet == null || !(helmet.getItemMeta() instanceof SkullMeta skullMeta) || skullMeta.getOwnerProfile() != null) {
            return;
        }
        skullMeta.setOwnerProfile(profile);
        helmet.setItemMeta(skullMeta);
        equipment.setHelmet(helmet);
    }

    // ---- placed plushies

    @EventHandler(priority = EventPriority.HIGH)
    public void onHug(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();

        if (!isPlushie(entity)) {
            return;
        }
        // No name tags, leads or armor on plushies
        event.setCancelled(true);

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getPlayer().isSneaking()) {
            pickUp(event.getPlayer(), (Mannequin) entity);
            return;
        }
        Location location = entity.getLocation();
        double height = entity instanceof Mannequin mannequin ? mannequin.getHeight() : 0.9d;
        entity.getWorld().spawnParticle(Particle.HEART, location.add(0.0d, height * 0.8d, 0.0d), 2, 0.15d, 0.1d, 0.15d, 0.0d);
        entity.getWorld().playSound(location, Sound.BLOCK_WOOL_HIT, SoundCategory.NEUTRAL, 0.8f, 1.5f);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteractAt(PlayerInteractAtEntityEvent event) {
        if (isPlushie(event.getRightClicked())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Mannequin mannequin) || !isPlushie(mannequin)) {
            return;
        }
        event.setCancelled(true);

        if (event instanceof EntityDamageByEntityEvent byEntity && byEntity.getDamager() instanceof Player player) {
            pickUp(player, mannequin);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        // Only possible with commands, the worn head must not drop as a free item
        if (isPlushie(event.getEntity())) {
            event.getDrops().clear();
        }
    }

    private void pickUp(Player player, Mannequin mannequin) {
        User user = plugin.getUserManager().getConnected(player);

        if (user == null) {
            return;
        }
        PersistentDataContainer data = mannequin.getPersistentDataContainer();
        String owner = data.get(ownerKey, PersistentDataType.STRING);

        if (!player.getUniqueId().toString().equals(owner) && !player.hasPermission(PERMISSION_ADMIN)) {
            String ownerName = data.get(ownerNameKey, PersistentDataType.STRING);
            user.sendMessage(user.translate("plushies.not_owner", Placeholder.unparsed("owner", ownerName != null ? ownerName : "?")));
            return;
        }
        Location location = mannequin.getLocation();
        PlushieType type = getType(data.get(typeKey, PersistentDataType.STRING));

        if (type != null) {
            ItemStack item = createItem(user, type, data.get(skinKey, PersistentDataType.STRING));
            player.getInventory().addItem(item).values().forEach(left -> location.getWorld().dropItem(location, left));
        }
        Entity seat = mannequin.getVehicle();
        mannequin.remove();

        if (seat != null && seat.getPersistentDataContainer().has(typeKey, PersistentDataType.STRING)) {
            seat.remove();
        }

        if (owner != null) {
            Player ownerPlayer = Bukkit.getPlayer(UUID.fromString(owner));

            if (ownerPlayer != null) {
                List<String> placed = getPlaced(ownerPlayer);
                placed.removeIf(entry -> entry.startsWith(mannequin.getUniqueId().toString()));
                setPlaced(ownerPlayer, placed);
            }
        }
        location.getWorld().playSound(location, Sound.BLOCK_WOOL_BREAK, SoundCategory.BLOCKS, 1.0f, 1.2f);
        user.sendMessage(user.translate("plushies.picked_up", Placeholder.unparsed("cosmetic", type != null ? type.getName(user) : "?")));
    }

    private boolean isPlushie(Entity entity) {
        return entity instanceof Mannequin && entity.getPersistentDataContainer().has(typeKey, PersistentDataType.STRING);
    }

    // ---- placed counts

    private int getMaxPlaced() {
        CosmeticCategory<PlushieType, PlushieBehavior, PlushieType.Builder> category = plugin.getCategoryRegistries().plushies();

        if (category == null) {
            return DEFAULT_MAX_PLACED;
        }
        Config config = category.getConfig();
        return config.hasKey("max_placed_per_player") ? config.getInt("max_placed_per_player") : DEFAULT_MAX_PLACED;
    }

    /**
     * The plushies a player placed. Entries whose plushie is gone (killed by a command, world
     * edited) are dropped as soon as their chunk is loaded, so the limit can't get stuck.
     */
    private List<String> getPlaced(Player player) {
        String stored = player.getPersistentDataContainer().get(placedKey, PersistentDataType.STRING);
        List<String> placed = new ArrayList<>();

        if (stored == null || stored.isEmpty()) {
            return placed;
        }
        for (String entry : stored.split(ENTRY_SEPARATOR)) {
            String[] parts = entry.split("@");

            if (parts.length != 4) {
                continue;
            }
            try {
                World world = Bukkit.getWorld(parts[1]);

                if (world != null) {
                    int chunkX = Integer.parseInt(parts[2]);
                    int chunkZ = Integer.parseInt(parts[3]);

                    if (world.isChunkLoaded(chunkX, chunkZ) && world.getChunkAt(chunkX, chunkZ).isEntitiesLoaded()
                            && Bukkit.getEntity(UUID.fromString(parts[0])) == null) {
                        continue;
                    }
                }
                placed.add(entry);
            } catch (IllegalArgumentException ignored) {
                // Broken entry, drop it
            }
        }
        return placed;
    }

    private void setPlaced(Player player, List<String> placed) {
        if (placed.isEmpty()) {
            player.getPersistentDataContainer().remove(placedKey);
        } else {
            player.getPersistentDataContainer().set(placedKey, PersistentDataType.STRING, String.join(ENTRY_SEPARATOR, placed));
        }
    }
}
