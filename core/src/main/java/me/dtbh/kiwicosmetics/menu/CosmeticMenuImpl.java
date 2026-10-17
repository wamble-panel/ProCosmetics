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
 */package me.dtbh.kiwicosmetics.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import me.dtbh.kiwicosmetics.api.KiwiCosmetics;
import me.dtbh.kiwicosmetics.api.config.Config;
import me.dtbh.kiwicosmetics.api.cosmetic.Cosmetic;
import me.dtbh.kiwicosmetics.api.cosmetic.CosmeticType;
import me.dtbh.kiwicosmetics.api.cosmetic.registry.CosmeticCategory;
import me.dtbh.kiwicosmetics.api.menu.ClickableItem;
import me.dtbh.kiwicosmetics.api.menu.CosmeticMenu;
import me.dtbh.kiwicosmetics.api.user.User;
import me.dtbh.kiwicosmetics.locale.LanguageManagerImpl;
import me.dtbh.kiwicosmetics.util.item.ItemBuilderImpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * One page per category: a header, the cosmetics in the middle and a bottom bar with
 * back, page, unequip and sort buttons. The layout is shared by all categories (config.yml menu.category).
 */
public class CosmeticMenuImpl<T extends CosmeticType<T, ?>> extends PaginatedMenu<CosmeticMenuImpl<T>.CosmeticPaginatedItem> implements CosmeticMenu<T> {

    private static final String PATH = "menu.category.";

    protected final CosmeticCategory<T, ?, ?> category;
    private final Config config;
    private final String categoryName;
    private Sorting sorting = Sorting.UNLOCKED;

    public CosmeticMenuImpl(KiwiCosmetics plugin, User user, CosmeticCategory<T, ?, ?> category) {
        super(plugin, user,
                user.translate("menu.category.title", Placeholder.unparsed("name", user.translateRaw("category." + category.getKey() + ".name"))),
                plugin.getConfigManager().getMainConfig().getInt(PATH + "rows"),
                1
        );
        this.category = category;
        this.config = plugin.getConfigManager().getMainConfig();
        this.categoryName = user.translateRaw("category." + category.getKey() + ".name");

        ItemBuilderImpl pageItem = new ItemBuilderImpl(config, PATH + "next_page");
        setNextPageSlot(pageItem.getSlot());
        setNextPageItemStack(pageInfo -> pageItem
                .setDisplayName(user.translate("menu.category.next_page.name"))
                .setLore(user.translateList("menu.category.next_page.desc", pageResolvers(pageInfo.getCurrentPage() + 1, pageInfo.getPageCount())))
                .getItemStack());

        ItemBuilderImpl previousItem = new ItemBuilderImpl(config, PATH + "previous_page");
        setPreviousPageSlot(previousItem.getSlot());
        setPreviousPageItemStack(pageInfo -> previousItem
                .setDisplayName(user.translate("menu.category.previous_page.name"))
                .setLore(user.translateList("menu.category.previous_page.desc", pageResolvers(pageInfo.getCurrentPage() - 1, pageInfo.getPageCount())))
                .getItemStack());

        populateCosmetics();
        setSorting(sorting);
    }

    private static TagResolver pageResolvers(int page, int pages) {
        return TagResolver.resolver(
                Placeholder.unparsed("page", String.valueOf(page)),
                Placeholder.unparsed("pages", String.valueOf(pages))
        );
    }

    private void populateCosmetics() {
        @SuppressWarnings("unchecked")
        Cosmetic<T, ?> equippedCosmetic = (Cosmetic<T, ?>) user.getCosmetic(category);
        T equippedType = equippedCosmetic != null && equippedCosmetic.isEquipped() ? equippedCosmetic.getType() : null;

        for (T cosmeticType : category.getCosmeticRegistry().getEnabledTypes()) {
            boolean locked = !cosmeticType.hasPermission(player);
            addPaginatedItem(new CosmeticPaginatedItem(cosmeticType, locked, cosmeticType == equippedType));
        }
    }

    @Override
    protected void addNavigationItems() {
        super.addNavigationItems();

        // Header: category name and how many cosmetics are unlocked
        int unlocked = category.getUnlockedCosmetics(player);
        int total = category.getCosmeticRegistry().getEnabledTypes().size();
        ItemBuilderImpl header = ItemBuilderImpl.of(category.getMenuItem().getItemStack());
        header.setSlot(config.getInt(PATH + "header_slot"));
        header.setDisplayName(user.translate("menu.main.category.name", Placeholder.unparsed("name", categoryName)));
        List<Component> headerLore = new ArrayList<>(user.translateList("category." + category.getKey() + ".desc"));
        headerLore.addAll(user.translateList("menu.category.header.desc",
                Placeholder.unparsed("unlocked", String.valueOf(unlocked)),
                Placeholder.unparsed("total", String.valueOf(total)),
                Placeholder.unparsed("percent", MenuStyle.percent(unlocked, total)),
                Placeholder.component("bar", MenuStyle.progressBar(unlocked, total))
        ));
        header.setLore(headerLore);
        setItem(header.getSlot(), header.hide().getItemStack(), event -> {
        });

        // Back to the main menu, or close when opened directly
        ItemBuilderImpl back = new ItemBuilderImpl(config, PATH + "back");
        boolean hasPrevious = getPreviousMenu() != null;
        back.setDisplayName(user.translate(hasPrevious ? "menu.category.back.name" : "menu.close.name"));
        back.setLore(user.translateList(hasPrevious ? "menu.category.back.desc" : "menu.close.desc"));
        setItem(back.getSlot(), back.getItemStack(), event -> {
            playClickSound();

            if (getPreviousMenu() != null) {
                getPreviousMenu().open();
            } else {
                player.closeInventory();
            }
        });

        // Unequip the current cosmetic of this category
        Cosmetic<?, ?> equipped = user.getCosmetic(category);

        if (equipped != null && equipped.isEquipped()) {
            ItemBuilderImpl unequip = new ItemBuilderImpl(config, PATH + "unequip");
            unequip.setDisplayName(user.translate("menu.category.unequip.name"));
            unequip.setLore(user.translateList("menu.category.unequip.desc",
                    Placeholder.unparsed("cosmetic", MenuStyle.smallCaps(equipped.getType().getName(user)))));
            setItem(unequip.getSlot(), unequip.getItemStack(), event -> {
                user.removeCosmetic(category, false, true);
                player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.5f, 0.8f);
                player.closeInventory();
            });
        }

        // Sorting
        ItemBuilderImpl sort = new ItemBuilderImpl(config, PATH + "sort");
        sort.setDisplayName(user.translate("menu.category.sort.name"));
        List<Component> sortLore = new ArrayList<>();

        for (Sorting s : Sorting.VALUES) {
            sortLore.add(user.translate(s == sorting ? "menu.category.sort.selected" : "menu.category.sort.option",
                    Placeholder.unparsed("option", user.translateRaw("menu.category.sorting." + s.getKey()))));
        }
        sortLore.addAll(user.translateList("menu.category.sort.desc"));
        sort.setLore(sortLore);
        setItem(sort.getSlot(), sort.getItemStack(), event -> {
            if (event.getAction() != InventoryAction.NOTHING) {
                setSorting(event.isRightClick() ? sorting.previous() : sorting.next());
                playClickSound();
            }
        });

        drawBorder();
    }

    private void drawBorder() {
        ItemStack border = new ItemBuilderImpl(config, "menu.border").getItemStack();

        for (int slot = 0; slot < getSize(); slot++) {
            int row = slot / 9;
            int column = slot % 9;
            boolean edge = row == 0 || row == rows - 1 || column == 0 || column == 8;

            if (edge && inventory.getItem(slot) == null) {
                inventory.setItem(slot, border);
            }
        }
    }

    public void setSorting(Sorting sorting) {
        this.sorting = sorting;
        super.setSorting(sorting.getComparator());

        if (isValid()) {
            refresh();
        }
    }

    @Override
    public void open() {
        this.page = 1;
        super.open();
    }

    @Override
    public ItemStack getFillEmptySlotsItem() {
        if (!config.hasKey("menu.filler.item")) {
            return null;
        }
        return new ItemBuilderImpl(config, "menu.filler").getItemStack();
    }

    public CosmeticCategory<T, ?, ?> getCategory() {
        return category;
    }

    private boolean hasTranslation(String key) {
        return ((LanguageManagerImpl) plugin.getLanguageManager()).has(key, user.getLocale());
    }

    public class CosmeticPaginatedItem extends PaginatedItem {

        private final T cosmeticType;
        private final boolean locked;
        private final boolean equipped;

        public CosmeticPaginatedItem(T cosmeticType, boolean locked, boolean equipped) {
            super(null, null);
            this.cosmeticType = cosmeticType;
            this.locked = locked;
            this.equipped = equipped;
        }

        @Override
        public ItemStack getItemStack() {
            TagResolver resolvers = TagResolver.resolver(
                    cosmeticType.getResolvers(user),
                    Placeholder.unparsed("name", MenuStyle.smallCaps(cosmeticType.getName(user)))
            );
            String descKey = "menu." + category.getKey() + "." + cosmeticType.getKey() + ".desc";

            if (!hasTranslation(descKey)) {
                descKey = "menu." + category.getKey() + ".default.desc";
            }
            List<Component> lore = new ArrayList<>();

            if (hasTranslation(descKey)) {
                lore.addAll(user.translateList(descKey, resolvers));
            }
            lore.addAll(user.translateList("menu.cosmetic.info", resolvers));
            String state = equipped ? "equipped" : locked ? "locked" : "unlocked";
            lore.addAll(user.translateList("menu.cosmetic." + state, resolvers));

            ItemBuilderImpl itemBuilder = ItemBuilderImpl.of(cosmeticType.getItemStack());

            if (locked && config.hasKey(PATH + "locked.item")) {
                itemBuilder = new ItemBuilderImpl(config, PATH + "locked");
            }
            itemBuilder.setDisplayName(user.translate("menu.cosmetic." + state + ".name", resolvers))
                    .setLore(lore)
                    .setGlintOverride(equipped);
            return itemBuilder.hide().getItemStack();
        }

        @Override
        public ClickableItem getClickHandler() {
            if (equipped) {
                return event -> {
                    user.removeCosmetic(category, false, true);
                    player.closeInventory();
                    player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.5f, 0.8f);
                };
            } else if (!locked) {
                return event -> {
                    cosmeticType.equip(user, false, true);
                    player.playSound(player, Sound.ITEM_ARMOR_EQUIP_LEATHER, 0.5f, 0.8f);
                };
            }
            return event -> {
                playDenySound();
                user.sendMessage(user.translate("cosmetic.locked", Placeholder.unparsed("cosmetic", MenuStyle.smallCaps(cosmeticType.getName(user)))));
            };
        }

        public T getCosmeticType() {
            return cosmeticType;
        }

        public boolean isLocked() {
            return locked;
        }

        public String getName() {
            return cosmeticType.getName(user);
        }
    }

    public enum Sorting {
        UNLOCKED("unlocked"),
        RARITY("rarity"),
        ALPHABET("alphabetical");

        public static final List<Sorting> VALUES = List.of(values());

        private final String key;

        Sorting(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public <T extends CosmeticType<T, ?>> Comparator<CosmeticMenuImpl<T>.CosmeticPaginatedItem> getComparator() {
            return switch (this) {
                case ALPHABET -> Comparator.comparing(
                        (CosmeticMenuImpl<T>.CosmeticPaginatedItem item) -> item.getName(),
                        String.CASE_INSENSITIVE_ORDER
                );
                case UNLOCKED -> Comparator
                        .comparing((CosmeticMenuImpl<T>.CosmeticPaginatedItem item) -> item.isLocked())
                        .thenComparing(item -> item.getName(), String.CASE_INSENSITIVE_ORDER);
                case RARITY -> Comparator
                        .comparing((CosmeticMenuImpl<T>.CosmeticPaginatedItem item) -> item.getCosmeticType().getRarity())
                        .reversed()
                        .thenComparing(item -> item.getName(), String.CASE_INSENSITIVE_ORDER);
            };
        }

        public Sorting next() {
            return VALUES.get((ordinal() + 1) % VALUES.size());
        }

        public Sorting previous() {
            return VALUES.get((VALUES.size() + ordinal() - 1) % VALUES.size());
        }
    }
}
