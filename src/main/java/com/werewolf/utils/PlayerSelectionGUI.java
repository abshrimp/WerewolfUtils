package com.werewolf.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlayerSelectionGUI {

    public static final String GUI_TAG = "ww_gui_open";

    public static void open(Player opener, SelectionType type) {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective idObj = sb.getObjective("ww_id");
        if (idObj == null) return;

        List<Player> candidates = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.equals(opener)) continue;
            if (type.includeDead()) {
                if (!p.getScoreboardTags().contains("ww_alive") && !p.getScoreboardTags().contains("ww_dead")) continue;
            } else {
                if (!p.getScoreboardTags().contains("ww_alive")) continue;
            }
            candidates.add(p);
        }

        if (candidates.isEmpty()) return;

        candidates.sort(Comparator.comparing(p -> p.getName().toLowerCase()));

        int contentRows = (candidates.size() - 1) / 9 + 1;
        int size = Math.min(54, (contentRows + 1) * 9);
        Inventory gui = Bukkit.createInventory(new GUIHolder(type), size, type.getTitle());

        for (int i = 0; i < candidates.size() && i < 54; i++) {
            Player target = candidates.get(i);
            int targetId = idObj.getScore(target).getScore();

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(target);
            meta.displayName(Component.text(target.getName()).color(NamedTextColor.YELLOW));
            meta.lore(List.of(
                Component.text("クリックして選択").color(NamedTextColor.GRAY)
            ));
            meta.setCustomModelData(targetId);
            head.setItemMeta(meta);

            gui.setItem(i, head);
        }

        ItemStack cancel = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.displayName(Component.text("キャンセル").color(NamedTextColor.RED));
        cancel.setItemMeta(cancelMeta);
        gui.setItem(size - 5, cancel);

        opener.openInventory(gui);
    }
}
