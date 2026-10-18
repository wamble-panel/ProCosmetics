# KiwiCosmetics

Cosmetics for Minecraft servers (Spigot & Paper **26.1+**, Java 25): particles, trails, gadgets, hats,
rainbow armor, balloons, banners, miniatures, arrow & death effects and music, all in one plugin with a
pastel, small-caps menu.

KiwiCosmetics is a fork of [ProCosmetics](https://github.com/FilleDev/ProCosmetics) by FilleDev (GPLv3),
merged with the hats and rainbow armor of the old KiwiCosmetics plugin.

## Highlights

- **Permission only.** No coins, no shop, no treasure chests: a cosmetic is unlocked when the player has its permission.
- **/cosmetics** opens the menu, **/cosmetics &lt;category&gt;** opens a category directly (`particles`, `trails`, `hats`, ...).
- **Block trails**: the ground you walk on turns into other blocks for a few seconds (rainbow wool, ice, gold,
  cinnabar, sulfur, cherry, sculk...). The blocks are fake, only shown to players; the world never changes.
- **New particle shapes** using the 26.2/26.3 particles (poplar leaves, geysers, sulfur bubbles, noxious gas,
  fireflies, copper fire, trails). On older versions they fall back to similar particles.
- **Hype Train** gadget, like on Hypixel: ride the locomotive, other players right-click the train to hop on,
  carriages follow your exact path and a boss bar shows the hype.
- **Hats & rainbow armor** (Paper): `/hats` and `/rainbow` work exactly like before, and every hat can also be worn
  from the menu (visual only, the real helmet stays put).
- No database: equipped cosmetics are remembered on the player (persistent data container).

Removed compared to ProCosmetics: pets, morphs, mounts, emotes, statuses, coins/economy, gadget ammo, treasure chests,
SQL/Mongo/Redis storage and the hotbar menu item.

## Permissions

| Permission | Default | What |
|---|---|---|
| `kiwicosmetics.cosmetic.*` | op | every cosmetic |
| `kiwicosmetics.cosmetic.<category>.*` | - | a whole category, e.g. `kiwicosmetics.cosmetic.trails.*` |
| `kiwicosmetics.cosmetic.<category>.<cosmetic>` | - | one cosmetic, e.g. `kiwicosmetics.cosmetic.particle_effects.angel_wings` |
| `kiwicosmetics.command.cosmetics` / `equip` / `unequip` / `unequipall` | everyone | player commands |
| `kiwicosmetics.admin` | op | all `/kiwicosmetics` (`/kc`) commands, plus `kiwi.*` |
| `kiwi.hats.*`, `kiwi.rainbow.*` | op | `/hats` and `/rainbow` (same nodes as before) |

`/kc permission <category>` lists every permission of a category (click to copy).

Categories: `particle_effects`, `trails`, `gadgets`, `hats`, `balloons`, `banners`, `miniatures`, `arrow_effects`, `death_effects`, `music`.

## Updating from the old KiwiCosmetics (hats + rainbow armor)

Replace the old jar with this one. On first start the old `config.yml` (rainbow patterns + messages) is moved to
`wardrobe.yml`, `hats.yml` stays as it is, and handed-out hats and rainbow armor keep working.

## Files

| File | What |
|---|---|
| `config.yml` | worlds, WorldGuard, menu layout |
| `<category>.yml` | enable/disable cosmetics, their menu item and rarity |
| `hats.yml`, `wardrobe.yml` | hats, rainbow patterns, hat/rainbow messages |
| `hat_cosmetics.yml` | the Hats category of the menu |
| `rarities.yml` | rarity names and colors |
| `lang/en_US.json` | every message and menu text (MiniMessage) |

Palette: peach `#FFD8BE` text, pink `#FFC8DD` names, light pink `#FFE5EC` values, mint `#B5EAD7` success,
soft red `#FF9AA2` errors, lilac `#CDB4DB` accents, sky `#A2D2FF` hints, grey `#8A8A8A` details.

## Building

1. Install the Spigot dependencies with `bash install.sh` (needs Java 25), or use [BuildTools](https://www.spigotmc.org/wiki/buildtools/)
   with `--remapped` for 26.1.2, 26.2 and 26.3.
2. Run `./gradlew clean build`. The jar is `build/libs/KiwiCosmetics-<version>.jar`.

Every push also builds the jar on GitHub Actions (download it from the run's *Artifacts*).

## License

GNU General Public License v3.0, see [LICENSE](LICENSE). Based on ProCosmetics, Copyright (C) FilleDev and contributors.
