package com.werewolf.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;

public class RoleSelectionGUI {

    private record RoleEntry(String name, int id, String configScore, Material material, NamedTextColor nameColor) {}

    private static final RoleEntry[] VILLAGE_ROLES = {
        new RoleEntry("村人", 1, null, Material.LIME_CONCRETE, NamedTextColor.GREEN),
        new RoleEntry("占い師", 6, "#fortune_teller_count", Material.LIME_CONCRETE, NamedTextColor.AQUA),
        new RoleEntry("エリクサー", 7, "#elixir_count", Material.LIME_CONCRETE, NamedTextColor.GREEN),
        new RoleEntry("ゲッサー", 8, "#guesser_count", Material.LIME_CONCRETE, NamedTextColor.GOLD),
        new RoleEntry("ギャンブラー", 9, "#gambler_count", Material.LIME_CONCRETE, NamedTextColor.YELLOW),
        new RoleEntry("スペランカー", 15, "#spelunker_count", Material.LIME_CONCRETE, NamedTextColor.LIGHT_PURPLE),
    };

    private static final RoleEntry[] WOLF_ROLES = {
        new RoleEntry("人狼", 2, "#werewolf_count", Material.RED_CONCRETE, NamedTextColor.DARK_RED),
        new RoleEntry("共犯者", 3, "#accomplice_count", Material.RED_CONCRETE, NamedTextColor.DARK_PURPLE),
        new RoleEntry("狼憑き", 5, "#wolfpossessed_count", Material.RED_CONCRETE, NamedTextColor.DARK_GREEN),
        new RoleEntry("イビルエリクサー", 10, "#evil_elixir_count", Material.RED_CONCRETE, NamedTextColor.DARK_RED),
        new RoleEntry("イビルゲッサー", 11, "#evil_guesser_count", Material.RED_CONCRETE, NamedTextColor.DARK_RED),
        new RoleEntry("トラッパー", 12, "#trapper_count", Material.RED_CONCRETE, NamedTextColor.DARK_RED),
        new RoleEntry("カメレオン", 13, "#chameleon_count", Material.RED_CONCRETE, NamedTextColor.DARK_RED),
    };

    private static final RoleEntry[] THIRD_ROLES = {
        new RoleEntry("吸血鬼", 4, "#vampire_count", Material.YELLOW_CONCRETE, NamedTextColor.RED),
        new RoleEntry("ゾンビ", 14, "#zombie_count", Material.YELLOW_CONCRETE, NamedTextColor.DARK_GREEN),
    };

    public static void open(Player player) {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective config = sb.getObjective("ww_config");

        boolean hasThird = false;
        if (config != null) {
            for (RoleEntry role : THIRD_ROLES) {
                if (role.configScore != null && config.getScore(role.configScore).getScore() >= 1) {
                    hasThird = true;
                    break;
                }
            }
        }

        int rows = hasThird ? 4 : 3;
        int size = rows * 9;

        Inventory gui = Bukkit.createInventory(
            new RoleGUIHolder(),
            size,
            Component.text("===== 役職を選択 =====").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
        );

        // Row 0: Village team
        gui.setItem(0, createHeader("【村人陣営】", Material.GREEN_STAINED_GLASS_PANE, NamedTextColor.GREEN));
        int slot = 1;
        for (RoleEntry role : VILLAGE_ROLES) {
            if (isRoleEnabled(config, role)) {
                gui.setItem(slot++, createRoleItem(role));
            }
        }

        // Row 1: Wolf team
        gui.setItem(9, createHeader("【人狼陣営】", Material.RED_STAINED_GLASS_PANE, NamedTextColor.DARK_RED));
        slot = 10;
        for (RoleEntry role : WOLF_ROLES) {
            if (isRoleEnabled(config, role)) {
                gui.setItem(slot++, createRoleItem(role));
            }
        }

        // Row 2: Third team (if enabled)
        if (hasThird) {
            gui.setItem(18, createHeader("【第三陣営】", Material.YELLOW_STAINED_GLASS_PANE, NamedTextColor.YELLOW));
            slot = 19;
            for (RoleEntry role : THIRD_ROLES) {
                if (isRoleEnabled(config, role)) {
                    gui.setItem(slot++, createRoleItem(role));
                }
            }
        }

        // Cancel button
        ItemStack cancel = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.displayName(Component.text("キャンセル").color(NamedTextColor.RED));
        cancel.setItemMeta(cancelMeta);
        gui.setItem(size - 5, cancel);

        player.openInventory(gui);
    }

    private static boolean isRoleEnabled(Objective config, RoleEntry role) {
        if (role.configScore == null) return true;
        if (config == null) return false;
        return config.getScore(role.configScore).getScore() >= 1;
    }

    private static ItemStack createHeader(String text, Material material, NamedTextColor color) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(text).color(color).decorate(TextDecoration.BOLD));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createRoleItem(RoleEntry role) {
        ItemStack item = new ItemStack(role.material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(role.name).color(role.nameColor));
        meta.lore(List.of(
            Component.text("クリックして選択").color(NamedTextColor.GRAY)
        ));
        meta.setCustomModelData(role.id);
        item.setItemMeta(meta);
        return item;
    }
}
