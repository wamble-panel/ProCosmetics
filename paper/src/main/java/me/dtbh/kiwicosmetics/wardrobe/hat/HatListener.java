/*
 * This file is part of KiwiCosmetics, a fork of ProCosmetics - https://github.com/FilleDev/ProCosmetics
 * Copyright (C) 2026 FilleDev, dtbh and contributors
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
package me.dtbh.kiwicosmetics.wardrobe.hat;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import me.dtbh.kiwicosmetics.wardrobe.Wardrobe;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Hats are equippable items, so shift-click / drag in the inventory work on their own. Right-clicks are
 * handled here instead of by the game: a click at a block would try to <em>place</em> the hat, and the
 * game's own equip copies the item in creative mode, so the plugin does the swap itself and always
 * moves the hat (one on the head, the old helmet back in the hand).
 * <p>
 * Safety: handlers here only cancel or deny vanilla actions, they never allow anything, so WorldGuard,
 * claim plugins etc. keep full control over chests, doors and everything else.
 */
public class HatListener implements Listener {
    /** Blocks that would take or use up the hat when right-clicked with it. */
    private static final Set<Material> ITEM_EATING_BLOCKS = EnumSet.noneOf(Material.class);
    /** One click can reach the server as two packets (block use + item use); ignore the second. */
    private static final int EQUIP_COOLDOWN_TICKS = 4;

    static {
        for (Material material : Material.values()) {
            String name = material.name();
            if (name.startsWith("LEGACY_")) {
                continue;
            }
            if (name.equals("COMPOSTER") || name.equals("FLOWER_POT") || name.equals("DECORATED_POT")
                    || name.equals("CAKE") || name.equals("SHELF") || name.endsWith("CAMPFIRE") || name.endsWith("_SHELF")) {
                ITEM_EATING_BLOCKS.add(material);
            }
        }
    }

    private final Wardrobe plugin;
    private final Map<UUID, Integer> lastEquipTick = new HashMap<>();

    public HatListener(Wardrobe plugin) {
        this.plugin = plugin;
    }

    // Not ignoreCancelled: right-clicks into the air arrive already "cancelled".
    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = event.getItem();
        if (!plugin.hats().couldBeHat(item) || event.getHand() == null) {
            return;
        }
        Hat hat = plugin.hats().match(item);
        if (hat == null) {
            return;
        }
        Player player = event.getPlayer();

        if (plugin.hats().needsUpgrade(item, hat)) {
            event.setCancelled(true);
            player.getInventory().setItem(event.getHand(), plugin.hats().create(hat, item.getAmount()));
            plugin.send(player, "hat-upgraded");
            return;
        }

        // Never let the game use the item itself: no placing it, and no creative-mode copy on equip.
        event.setUseItemInHand(Event.Result.DENY);
        Block block = event.getClickedBlock();
        if (action == Action.RIGHT_CLICK_BLOCK && block != null) {
            if (ITEM_EATING_BLOCKS.contains(block.getType())) {
                event.setUseInteractedBlock(Event.Result.DENY);
            } else if (block.getType().isInteractable() && !player.isSneaking()) {
                return; // let the chest / door / button do its thing (protection plugins decide)
            }
        }
        if (!hat.wearable()) {
            return;
        }

        int now = Bukkit.getCurrentTick();
        Integer last = lastEquipTick.get(player.getUniqueId());
        if (last != null && now - last < EQUIP_COOLDOWN_TICKS) {
            return;
        }
        if (equip(player, event.getHand())) {
            lastEquipTick.put(player.getUniqueId(), now);
        }
    }

    /**
     * One hat goes on the head, the old helmet comes back to the hand (or the inventory when holding
     * a stack). Nothing is created or destroyed, in creative mode too.
     */
    private boolean equip(Player player, EquipmentSlot hand) {
        PlayerInventory inventory = player.getInventory();
        ItemStack held = inventory.getItem(hand);
        if (held == null || held.getType().isAir()) {
            return false;
        }
        ItemStack current = inventory.getHelmet();
        boolean wearing = current != null && !current.getType().isAir();
        boolean creative = player.getGameMode() == GameMode.CREATIVE;
        if (wearing && (current.isSimilar(held) || (!creative && current.containsEnchantment(Enchantment.BINDING_CURSE)))) {
            return false;
        }

        ItemStack worn = held.clone();
        worn.setAmount(1);
        if (held.getAmount() > 1) {
            held.setAmount(held.getAmount() - 1);
            inventory.setItem(hand, held);
            if (wearing) {
                inventory.addItem(current).values()
                        .forEach(left -> player.getWorld().dropItem(player.getLocation(), left));
            }
        } else {
            inventory.setItem(hand, wearing ? current : null);
        }
        inventory.setHelmet(worn);
        return true;
    }

    /** No feeding hats to animals, taming wolves with the bone hat, giving them to allays... */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof LivingEntity) || event.getRightClicked() instanceof ArmorStand
                || event.getRightClicked() instanceof Player) {
            return;
        }
        ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
        if (plugin.hats().couldBeHat(item) && plugin.hats().match(item) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (plugin.hats().couldBeHat(event.getItemInHand()) && plugin.hats().match(event.getItemInHand()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        if (plugin.hats().couldBeHat(event.getItem()) && plugin.hats().match(event.getItem()) != null) {
            event.setCancelled(true);
        }
    }

    /** Hats are not crafting ingredients (no turning a slime hat into slimeballs). */
    @EventHandler(priority = EventPriority.LOW)
    public void onCraft(PrepareItemCraftEvent event) {
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (plugin.hats().couldBeHat(item) && plugin.hats().match(item) != null) {
                event.getInventory().setResult(null);
                return;
            }
        }
    }

    /** Upgrade old hats in the inventory once, so they can be worn right away. */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PlayerInventory inventory = event.getPlayer().getInventory();
        ItemStack[] contents = inventory.getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (!plugin.hats().couldBeHat(item)) {
                continue;
            }
            Hat hat = plugin.hats().match(item);
            if (hat != null && plugin.hats().needsUpgrade(item, hat)) {
                inventory.setItem(slot, plugin.hats().create(hat, item.getAmount()));
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastEquipTick.remove(event.getPlayer().getUniqueId());
    }

    /** Runs a hat's commands when it is put on. */
    @EventHandler
    public void onArmorChange(PlayerArmorChangeEvent event) {
        if (event.getSlot() != EquipmentSlot.HEAD || !plugin.hats().couldBeHat(event.getNewItem())) {
            return;
        }
        Player player = event.getPlayer();
        // Armor also "changes" when a player joins or respawns; only count real equips.
        if (player.getTicksLived() < 40) {
            return;
        }
        Hat hat = plugin.hats().match(event.getNewItem());
        if (hat == null || hat.commands().isEmpty() || hat == plugin.hats().match(event.getOldItem())) {
            return;
        }
        for (String command : hat.commands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
        }
    }
}
