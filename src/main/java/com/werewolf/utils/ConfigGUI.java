package com.werewolf.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

/**
 * OP向けのゲーム設定GUI。ww_config スコアボードの値を直接編集する。
 * 3ページ構成（基本設定 / 役職人数 / 闇鍋・第三陣営）。
 */
public class ConfigGUI {

    public static final int PAGE_COUNT = 3;
    public static final String NAV_KEY = "config_nav";
    public static final String SETTING_KEY = "config_setting";

    private static final Component[] TITLES = {
        Component.text("⚙ ゲーム設定 - 基本設定").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD),
        Component.text("⚙ ゲーム設定 - 役職人数").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD),
        Component.text("⚙ ゲーム設定 - 闇鍋/第三陣営").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD),
    };

    public static void open(Player player, WerewolfUtilsPlugin plugin, int page) {
        page = Math.max(0, Math.min(PAGE_COUNT - 1, page));
        Inventory gui = Bukkit.createInventory(new ConfigGUIHolder(page), 54, TITLES[page]);
        populate(gui, page, plugin);
        player.openInventory(gui);
    }

    /** GUIの中身を構築する。値変更後の再描画にも使用する。 */
    public static void populate(Inventory gui, int page, WerewolfUtilsPlugin plugin) {
        gui.clear();
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective config = sb.getObjective("ww_config");

        if (page == 1) {
            gui.setItem(0, header("【村人陣営】", Material.GREEN_STAINED_GLASS_PANE, NamedTextColor.GREEN));
            gui.setItem(9, header("【人狼陣営】", Material.RED_STAINED_GLASS_PANE, NamedTextColor.RED));
        } else if (page == 2) {
            gui.setItem(18, header("【第三陣営】", Material.YELLOW_STAINED_GLASS_PANE, NamedTextColor.GOLD));
        }

        for (ConfigSetting s : ConfigSetting.values()) {
            if (s.getPage() != page) continue;
            int value = config != null ? config.getScore(s.getKey()).getScore() : 0;
            gui.setItem(s.getSlot(), buildItem(s, value, plugin));
        }

        // 最下段: ナビゲーション
        ItemStack filler = header(" ", Material.GRAY_STAINED_GLASS_PANE, NamedTextColor.GRAY);
        for (int i = 45; i < 54; i++) {
            gui.setItem(i, filler);
        }
        if (page > 0) {
            gui.setItem(45, navItem("prev", Material.ARROW, "← 前のページ", NamedTextColor.YELLOW, plugin, null));
        }
        if (page < PAGE_COUNT - 1) {
            gui.setItem(53, navItem("next", Material.ARROW, "次のページ →", NamedTextColor.YELLOW, plugin, null));
        }
        gui.setItem(47, pageIndicator(page));
        gui.setItem(49, navItem("close", Material.BARRIER, "閉じる", NamedTextColor.RED, plugin, null));
        gui.setItem(51, navItem("reset", Material.TNT, "全設定をリセット", NamedTextColor.RED, plugin,
            List.of("デフォルト設定に戻します", "GUIで変更した内容は失われます")));
    }

    private static ItemStack buildItem(ConfigSetting s, int value, WerewolfUtilsPlugin plugin) {
        ItemStack item = new ItemStack(s.getMaterial());
        ItemMeta meta = item.getItemMeta();

        NamedTextColor nameColor = switch (s.getMaterial()) {
            case LIME_CONCRETE -> NamedTextColor.GREEN;
            case RED_CONCRETE -> NamedTextColor.RED;
            case YELLOW_CONCRETE -> NamedTextColor.GOLD;
            default -> NamedTextColor.YELLOW;
        };
        meta.displayName(Component.text(s.getDisplay()).color(nameColor)
            .decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(s.getDescription()).color(NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());

        if (s.getType() == ConfigSetting.Type.TOGGLE) {
            boolean on = value != 0;
            lore.add(Component.text("現在: ").color(NamedTextColor.WHITE)
                .append(Component.text(on ? "ON" : "OFF").color(on ? NamedTextColor.GREEN : NamedTextColor.RED))
                .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("クリックで切り替え").color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        } else {
            lore.add(Component.text("現在の値: ").color(NamedTextColor.WHITE)
                .append(Component.text(value).color(NamedTextColor.AQUA))
                .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("左クリック: +" + s.getStep()).color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("右クリック: -" + s.getStep()).color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Shift+クリック: ±" + s.getShiftStep()).color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);
        meta.getPersistentDataContainer().set(
            new NamespacedKey(plugin, SETTING_KEY), PersistentDataType.STRING, s.name());
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack navItem(String action, Material material, String name,
                                     NamedTextColor color, WerewolfUtilsPlugin plugin, List<String> loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name).color(color).decoration(TextDecoration.ITALIC, false));
        if (loreLines != null) {
            List<Component> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(Component.text(line).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(lore);
        }
        meta.getPersistentDataContainer().set(
            new NamespacedKey(plugin, NAV_KEY), PersistentDataType.STRING, action);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack pageIndicator(int page) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("ページ " + (page + 1) + " / " + PAGE_COUNT)
            .color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack header(String text, Material material, NamedTextColor color) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(text).color(color)
            .decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));
        item.setItemMeta(meta);
        return item;
    }
}
