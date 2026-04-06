package com.werewolf.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Set;

public class DropListener implements Listener {

    private static final Set<String> ROLE_ITEM_NAMES = Set.of(
        "占い師の心（占い師用）", "推理の魂", "地雷", "カメレオンの仮面", "感染確認"
    );

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!player.getScoreboardTags().contains("ww_alive")) return;

        ItemStack item = event.getItemDrop().getItemStack();

        if (isRoleItem(item)) {
            event.setCancelled(true);
            return;
        }

        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective config = sb.getObjective("ww_config");
        if (config != null && config.getScore("#no_drop").getScore() >= 1) {
            event.setCancelled(true);
        }
    }

    private static boolean isRoleItem(ItemStack item) {
        if (!item.hasItemMeta()) return false;
        Component name = item.getItemMeta().customName();
        if (name == null) return false;
        String plainName = PlainTextComponentSerializer.plainText().serialize(name);
        return ROLE_ITEM_NAMES.contains(plainName);
    }
}
