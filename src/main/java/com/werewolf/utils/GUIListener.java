package com.werewolf.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class GUIListener implements Listener {

    private final WerewolfUtilsPlugin plugin;

    public GUIListener(WerewolfUtilsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof RoleGUIHolder) {
            handleRoleClick(event);
            return;
        }

        if (!(event.getInventory().getHolder() instanceof GUIHolder holder)) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        if (clicked.getType() == Material.BARRIER) {
            ((Player) event.getWhoClicked()).closeInventory();
            return;
        }

        if (!(clicked.getItemMeta() instanceof SkullMeta meta)) return;

        int targetId = meta.getCustomModelData();
        if (targetId <= 0) return;

        Player player = (Player) event.getWhoClicked();
        SelectionType type = holder.getType();

        Bukkit.dispatchCommand(
            Bukkit.getConsoleSender(),
            "scoreboard players set " + player.getName() + " " + type.getTargetObjective() + " " + targetId
        );

        player.closeInventory();
    }

    private void handleRoleClick(InventoryClickEvent event) {
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        if (clicked.getType() == Material.BARRIER) {
            ((Player) event.getWhoClicked()).closeInventory();
            return;
        }

        // Skip glass pane headers
        if (clicked.getType().name().endsWith("STAINED_GLASS_PANE")) return;

        if (!clicked.getItemMeta().hasCustomModelData()) return;

        int roleId = clicked.getItemMeta().getCustomModelData();
        if (roleId <= 0) return;

        Player player = (Player) event.getWhoClicked();

        Bukkit.dispatchCommand(
            Bukkit.getConsoleSender(),
            "scoreboard players set " + player.getName() + " ww_guesser_role " + roleId
        );

        player.closeInventory();
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof GUIHolder
                || event.getInventory().getHolder() instanceof RoleGUIHolder) {
            event.setCancelled(true);
        }
    }
}
