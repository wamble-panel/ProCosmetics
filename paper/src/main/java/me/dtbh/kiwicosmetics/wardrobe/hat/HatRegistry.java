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

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import me.dtbh.kiwicosmetics.wardrobe.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Loads hats.yml, builds hat items and recognizes hat items, including the ones handed out by ExecutableItems.
 * <p>
 * Hats get the vanilla "equippable" component, so the game itself handles wearing them (right-click,
 * shift-click, dragging into the head slot). No equip code, no listeners running per tick.
 */
public final class HatRegistry {
    /** Namespaces ExecutableItems (and its SCore library) store their data under. */
    private static final Set<String> LEGACY_NAMESPACES = Set.of("executableitems", "score");
    public static final List<String> DEFAULT_LORE = List.of(
            "<#8A8A8A>✦ <#CDB4DB>ᴄᴏꜱᴍᴇᴛɪᴄ ʜᴀᴛ",
            "",
            "<#A2D2FF>➜ <#FFD8BE>ʀɪɢʜᴛ-ᴄʟɪᴄᴋ ᴛᴏ ᴡᴇᴀʀ"
    );

    private final NamespacedKey hatKey;
    private final Logger logger;
    private Map<String, Hat> hats = Map.of();
    private Map<String, Hat> byName = Map.of();
    private Set<Material> materials = EnumSet.noneOf(Material.class);
    private boolean matchByName = true;

    public HatRegistry(NamespacedKey hatKey, Logger logger) {
        this.hatKey = hatKey;
        this.logger = logger;
    }

    public void load(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        matchByName = config.getBoolean("settings.match-by-name", true);

        Map<String, Hat> loaded = new LinkedHashMap<>();
        Map<String, Hat> names = new HashMap<>();
        Set<Material> types = EnumSet.noneOf(Material.class);
        ConfigurationSection section = config.getConfigurationSection("hats");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                ConfigurationSection hatSection = section.getConfigurationSection(key);
                if (hatSection == null) {
                    continue;
                }
                String id = key.toLowerCase(Locale.ROOT);
                String materialName = hatSection.getString("material", "");
                Material material = Material.matchMaterial(materialName);
                if (material == null || !material.isItem() || material.isAir()) {
                    logger.warning("Hat '" + id + "' has an unknown material '" + materialName + "', skipping it.");
                    continue;
                }
                Hat hat = new Hat(
                        id,
                        material,
                        hatSection.getString("name", id),
                        hatSection.getStringList("lore"),
                        hatSection.getBoolean("glow", false),
                        hatSection.getString("head-texture", null),
                        hatSection.getBoolean("wearable", true),
                        hatSection.getStringList("commands")
                );
                loaded.put(id, hat);
                names.putIfAbsent(nameKey(material, Text.plain(Text.item(hat.name()))), hat);
                types.add(material);
            }
        }
        hats = loaded;
        byName = names;
        materials = types;
        logger.info("Loaded " + loaded.size() + " hat(s).");
    }

    public Collection<String> ids() {
        return hats.keySet();
    }

    public Hat get(String id) {
        return hats.get(id.toLowerCase(Locale.ROOT));
    }

    /** Cheap pre-check: only items of a hat material can be hats. */
    public boolean couldBeHat(ItemStack item) {
        return item != null && materials.contains(item.getType());
    }

    /**
     * @return the hat this item is, or null. Cheap for anything that isn't a hat material.
     */
    public Hat match(ItemStack item) {
        if (!couldBeHat(item) || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();

        String id = data.get(hatKey, PersistentDataType.STRING);
        if (id != null) {
            return hats.get(id);
        }

        // Hats given out by ExecutableItems: their tags stay on the item after it's uninstalled.
        boolean fromExecutableItems = false;
        for (NamespacedKey key : data.getKeys()) {
            if (!LEGACY_NAMESPACES.contains(key.getNamespace())) {
                continue;
            }
            fromExecutableItems = true;
            if (data.has(key, PersistentDataType.STRING)) {
                String value = data.get(key, PersistentDataType.STRING);
                Hat hat = value == null ? null : hats.get(value.toLowerCase(Locale.ROOT));
                if (hat != null && hat.material() == item.getType()) {
                    return hat;
                }
            }
        }

        // Name fallback only for real ExecutableItems items: players can't add that data themselves,
        // so renaming a plain item in an anvil never turns it into a hat.
        if (matchByName && fromExecutableItems) {
            String name = plainName(meta);
            if (name != null) {
                return byName.get(nameKey(item.getType(), name));
            }
        }
        return null;
    }

    /** Old hats (ExecutableItems, or made before hats were equippable) get rebuilt once. */
    public boolean needsUpgrade(ItemStack item, Hat hat) {
        return hat.wearable() && !item.hasData(DataComponentTypes.EQUIPPABLE);
    }

    public ItemStack create(Hat hat, int amount) {
        ItemStack item = new ItemStack(hat.material(), amount);
        item.editMeta(meta -> {
            meta.displayName(Text.item(hat.name()));
            meta.lore(hat.lore().stream().map(Text::item).toList());
            if (hat.glow()) {
                meta.setEnchantmentGlintOverride(true);
            }
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            if (hat.headTexture() != null && meta instanceof SkullMeta skull) {
                // Same texture -> same profile id, so heads of the same hat stack.
                UUID uuid = UUID.nameUUIDFromBytes(hat.headTexture().getBytes(StandardCharsets.UTF_8));
                PlayerProfile profile = Bukkit.createProfile(uuid);
                profile.setProperty(new ProfileProperty("textures", hat.headTexture()));
                skull.setPlayerProfile(profile);
            }
            meta.getPersistentDataContainer().set(hatKey, PersistentDataType.STRING, hat.id());
        });
        if (hat.wearable()) {
            // Let the game put it on the head, and make sure food hats (apple, potato...) can't be eaten.
            item.setData(DataComponentTypes.EQUIPPABLE, Equippable.equippable(EquipmentSlot.HEAD).build());
            item.unsetData(DataComponentTypes.CONSUMABLE);
        }
        return item;
    }

    /** Adds a hat to hats.yml. @return the new hat's id */
    public String add(File file, Material material, String name) throws IOException {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String base = material.name().toLowerCase(Locale.ROOT);
        String id = base;
        for (int i = 2; config.contains("hats." + id); i++) {
            id = base + "_" + i;
        }
        config.set("hats." + id + ".material", material.name());
        config.set("hats." + id + ".name", name);
        config.set("hats." + id + ".lore", DEFAULT_LORE);
        config.save(file);
        load(file);
        return id;
    }

    /** @return false if there was no such hat */
    public boolean remove(File file, String id) throws IOException {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String path = "hats." + id.toLowerCase(Locale.ROOT);
        if (!config.contains(path)) {
            return false;
        }
        config.set(path, null);
        config.save(file);
        load(file);
        return true;
    }

    public static String plainName(ItemMeta meta) {
        Component name = meta.hasDisplayName() ? meta.displayName() : meta.hasItemName() ? meta.itemName() : null;
        return name == null ? null : Text.plain(name);
    }

    private static String nameKey(Material material, String plainName) {
        return material.name() + '\0' + plainName;
    }
}
