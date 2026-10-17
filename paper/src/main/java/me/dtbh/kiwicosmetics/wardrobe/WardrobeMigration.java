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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Plain Java (no Paper classes) so it can run on any server before the configs are exported.
 * <p>
 * The old KiwiCosmetics plugin kept its rainbow patterns and messages in config.yml, which is now
 * the main cosmetics config. Move it to wardrobe.yml before a new config.yml is written.
 */
public final class WardrobeMigration {

    private WardrobeMigration() {
    }

    public static void migrate(File dataFolder, Logger logger) {
        File wardrobe = new File(dataFolder, Wardrobe.CONFIG_FILE);

        if (wardrobe.exists()) {
            return;
        }
        File config = new File(dataFolder, "config.yml");

        try {
            if (config.isFile() && isOldKiwiConfig(config)) {
                Files.move(config.toPath(), wardrobe.toPath(), StandardCopyOption.ATOMIC_MOVE);
                logger.info("Moved the old KiwiCosmetics config.yml (rainbow patterns, messages) to " + wardrobe.getName());
                return;
            }
            File kiwiRainbow = new File(dataFolder.getParentFile(), "Kiwi-Rainbow/config.yml");

            if (kiwiRainbow.isFile()) {
                Files.createDirectories(dataFolder.toPath());
                Files.copy(kiwiRainbow.toPath(), wardrobe.toPath());
                logger.info("Copied your old Kiwi-Rainbow config.yml into " + wardrobe.getPath());
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not migrate the old rainbow armor config", e);
        }
    }

    private static boolean isOldKiwiConfig(File file) throws IOException {
        boolean patterns = false;

        for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
            if (line.startsWith("config_version:")) {
                return false;
            }
            if (line.startsWith("patterns:")) {
                patterns = true;
            }
        }
        return patterns;
    }
}
