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
package me.dtbh.kiwicosmetics.wardrobe;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import me.dtbh.kiwicosmetics.wardrobe.command.HatsCmd;
import me.dtbh.kiwicosmetics.wardrobe.command.RainbowCmd;
import me.dtbh.kiwicosmetics.wardrobe.hat.HatListener;
import me.dtbh.kiwicosmetics.wardrobe.hat.HatRegistry;
import me.dtbh.kiwicosmetics.wardrobe.rainbow.ArmorPiece;
import me.dtbh.kiwicosmetics.wardrobe.rainbow.ColorGradient;
import me.dtbh.kiwicosmetics.wardrobe.rainbow.ColorPattern;
import me.dtbh.kiwicosmetics.wardrobe.rainbow.ParticleSettings;
import me.dtbh.kiwicosmetics.wardrobe.rainbow.RainbowTask;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hats (hats.yml) and animated rainbow leather armor (wardrobe.yml), formerly the separate
 * KiwiCosmetics plugin from the Improvements repository. Uses Paper's data component API,
 * so it is only enabled on Paper servers.
 */
public final class Wardrobe {

    public static final String CONFIG_FILE = "wardrobe.yml";

    private final JavaPlugin plugin;
    private final NamespacedKey rainbowKey;
    private final HatRegistry hats;
    private final RainbowTask task;
    private YamlConfiguration config = new YamlConfiguration();
    private Map<String, ColorPattern> patterns = Collections.emptyMap();
    private BukkitTask bukkitTask;
    private long scanInterval = 10;
    private ParticleSettings particleSettings;

    public Wardrobe(JavaPlugin plugin) {
        this.plugin = plugin;
        // Written out in full: rainbow armor and hats that were already handed out keep these keys
        this.rainbowKey = new NamespacedKey("kiwi-rainbow", "rainbow_armor");
        this.hats = new HatRegistry(new NamespacedKey("kiwicosmetics", "hat"), plugin.getLogger());
        this.task = new RainbowTask(this);
    }

    /** Writes the default files and loads hats and patterns. */
    public void load() {
        if (!new File(plugin.getDataFolder(), CONFIG_FILE).exists()) {
            plugin.saveResource(CONFIG_FILE, false);
        }
        if (!hatsFile().exists()) {
            plugin.saveResource("hats.yml", false);
        }
        reloadAll();
    }

    /** Starts the rainbow task and registers the /hats and /rainbow commands and listeners. */
    public void enable() {
        bukkitTask = plugin.getServer().getScheduler().runTaskTimer(plugin, task, 1L, 1L);

        register("rainbow", new RainbowCmd(this));
        register("hats", new HatsCmd(this));
        plugin.getServer().getPluginManager().registerEvents(new AnvilListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new HatListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new QuitListener(task), plugin);

        for (String old : List.of("Kiwi-Rainbow", "ExecutableItems")) {
            if (plugin.getServer().getPluginManager().getPlugin(old) != null) {
                getLogger().warning(old + " is still installed. Remove it, KiwiCosmetics replaces it"
                        + (old.equals("ExecutableItems") ? " (otherwise hat clicks are handled twice)." : "."));
            }
        }
    }

    public void disable() {
        if (bukkitTask != null) {
            bukkitTask.cancel();
            bukkitTask = null;
        }
    }

    private <T extends CommandExecutor & TabCompleter> void register(String name, T handler) {
        PluginCommand command = plugin.getCommand(name);
        if (command != null) {
            command.setExecutor(handler);
            command.setTabCompleter(handler);
        }
    }

    public void reloadAll() {
        loadConfig();
        loadColors();
        hats.load(hatsFile());
    }

    private void loadConfig() {
        YamlConfiguration loaded = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), CONFIG_FILE));

        // Missing keys fall back to the bundled file, so older configs still get new messages
        try (InputStream stream = plugin.getResource(CONFIG_FILE)) {
            if (stream != null) {
                loaded.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8)));
            }
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Could not read the bundled " + CONFIG_FILE, e);
        }
        config = loaded;
    }

    public Logger getLogger() {
        return plugin.getLogger();
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public File hatsFile() {
        return new File(plugin.getDataFolder(), "hats.yml");
    }

    public HatRegistry hats() {
        return hats;
    }

    public RainbowTask task() {
        return task;
    }

    public long scanInterval() {
        return scanInterval;
    }

    public ParticleSettings particleSettings() {
        return particleSettings;
    }

    public Collection<String> patternList() {
        return patterns.keySet();
    }

    public String formattedPatternList() {
        return String.join(", ", patterns.keySet());
    }

    public ColorPattern pattern(String name) {
        return patterns.get(name.toLowerCase(Locale.ROOT));
    }

    public boolean isRainbow(ItemStack item) {
        return item != null && !item.getType().isAir() && item.hasItemMeta() && patternName(item.getItemMeta()) != null;
    }

    public ColorPattern pattern(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        String name = patternName(item.getItemMeta());
        return name == null ? null : patterns.get(name);
    }

    public String patternName(ItemMeta meta) {
        return meta == null ? null : meta.getPersistentDataContainer().get(rainbowKey, PersistentDataType.STRING);
    }

    private void loadColors() {
        scanInterval = Math.max(1, config.getInt("settings.scan-interval", 10));
        particleSettings = ParticleSettings.load(config.getConfigurationSection("particles"));

        Map<String, ColorPattern> loaded = new LinkedHashMap<>();
        ConfigurationSection patternsSection = config.getConfigurationSection("patterns");
        if (patternsSection != null) {
            for (String key : patternsSection.getKeys(false)) {
                ConfigurationSection section = patternsSection.getConfigurationSection(key);
                if (section == null) {
                    continue;
                }
                String name = key.toLowerCase(Locale.ROOT);
                loaded.put(name, loadPattern(name, section));
            }
        }
        patterns = loaded;
        task.reset();
        getLogger().info("Loaded " + loaded.size() + " rainbow pattern(s): " + formattedPatternList());
    }

    private ColorPattern loadPattern(String name, ConfigurationSection section) {
        List<Integer> stops = new ArrayList<>();
        for (String hex : section.getStringList("colors")) {
            try {
                stops.add(ColorGradient.parseHex(hex));
            } catch (NumberFormatException e) {
                getLogger().warning("Pattern '" + name + "' has an invalid color '" + hex + "' (expected something like #ff7b00), skipping it.");
            }
        }
        if (stops.isEmpty()) {
            getLogger().warning("Pattern '" + name + "' has no valid colors, it will stay white.");
        }

        int steps = section.getInt("amount-of-colors", 7);
        long interval = section.getLong("interval", 10L);
        if (interval < 1) {
            getLogger().warning("Pattern '" + name + "' has interval " + interval + ", using 1 instead.");
        }
        boolean smoothLoop = section.getBoolean("smooth-loop", true);

        Color[] colors = ColorGradient.build(stops, steps, smoothLoop).stream()
                .map(Color::fromRGB)
                .toArray(Color[]::new);

        ItemStack[] armorSet = new ItemStack[ArmorPiece.VALUES.length];
        for (ArmorPiece piece : ArmorPiece.VALUES) {
            armorSet[piece.ordinal()] = item(piece, name, section.getConfigurationSection("items." + piece.key()));
        }
        return new ColorPattern(name, colors, interval, armorSet);
    }

    private ItemStack item(ArmorPiece piece, String pattern, ConfigurationSection itemSection) {
        if (itemSection == null) {
            return null;
        }
        String name = itemSection.getString("name", "Rainbow item");
        List<String> lore = itemSection.getStringList("lore");
        ItemStack item = new ItemStack(piece.material());
        item.editMeta(meta -> {
            meta.displayName(Text.item(name));
            meta.lore(lore.stream().map(Text::item).toList());
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_DYE);
            meta.getPersistentDataContainer().set(rainbowKey, PersistentDataType.STRING, pattern);
        });
        return item;
    }

    /**
     * A message from wardrobe.yml. {@code vars} are name/value pairs replacing {name}; values are
     * escaped so player input can't inject formatting, except names starting with '!' (pre-formatted).
     */
    public Component msg(String key, String... vars) {
        String message = config.getString("messages." + key);
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        String prefix = config.getString("messages.prefix");
        message = message.replace("{prefix}", prefix == null ? "" : prefix);
        for (int i = 0; i + 1 < vars.length; i += 2) {
            boolean raw = vars[i].startsWith("!");
            String name = raw ? vars[i].substring(1) : vars[i];
            message = message.replace("{" + name + "}", raw ? vars[i + 1] : Text.escape(vars[i + 1]));
        }
        return Text.chat(message);
    }

    public void send(CommandSender to, String key, String... vars) {
        Component message = msg(key, vars);
        if (!message.equals(Component.empty())) {
            to.sendMessage(message);
        }
    }

    public void sendLines(CommandSender to, String key) {
        for (String line : config.getStringList("messages." + key)) {
            to.sendMessage(Text.chat(line));
        }
    }

    /** "a • b • c" in the palette, for {!hats} / {!patterns}. */
    public static String list(Collection<String> items) {
        if (items.isEmpty()) {
            return "<#8A8A8A>-";
        }
        StringBuilder result = new StringBuilder();
        for (String item : items) {
            if (!result.isEmpty()) {
                result.append(" <#8A8A8A>• ");
            }
            result.append("<#FFC8DD>").append(Text.escape(item));
        }
        return result.toString();
    }
}
