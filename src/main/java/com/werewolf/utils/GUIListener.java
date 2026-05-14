package com.werewolf.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

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

        if (event.getInventory().getHolder() instanceof ConfigGUIHolder configHolder) {
            handleConfigClick(event, configHolder);
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

    private void handleConfigClick(InventoryClickEvent event, ConfigGUIHolder holder) {
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        Player player = (Player) event.getWhoClicked();
        PersistentDataContainer pdc = clicked.getItemMeta().getPersistentDataContainer();

        String nav = pdc.get(new NamespacedKey(plugin, ConfigGUI.NAV_KEY), PersistentDataType.STRING);
        if (nav != null) {
            switch (nav) {
                case "prev" -> ConfigGUI.open(player, plugin, holder.getPage() - 1);
                case "next" -> ConfigGUI.open(player, plugin, holder.getPage() + 1);
                case "close" -> player.closeInventory();
                case "reset" -> {
                    plugin.getConfigManager().clearAll();
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "function werewolf:config/default");
                    ConfigGUI.populate(event.getInventory(), holder.getPage(), plugin);
                    player.sendMessage("§a[WerewolfUtils] §f設定をデフォルトに戻しました");
                }
            }
            return;
        }

        String settingName = pdc.get(new NamespacedKey(plugin, ConfigGUI.SETTING_KEY), PersistentDataType.STRING);
        if (settingName == null) return;

        ConfigSetting setting;
        try {
            setting = ConfigSetting.valueOf(settingName);
        } catch (IllegalArgumentException e) {
            return;
        }

        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective config = sb.getObjective("ww_config");
        if (config == null) return;

        int current = config.getScore(setting.getKey()).getScore();
        int next;
        if (setting.getType() == ConfigSetting.Type.TOGGLE) {
            next = current != 0 ? 0 : 1;
        } else {
            int step = event.isShiftClick() ? setting.getShiftStep() : setting.getStep();
            next = event.isRightClick() ? current - step : current + step;
            next = Math.max(setting.getMin(), Math.min(setting.getMax(), next));
        }

        if (next != current) {
            config.getScore(setting.getKey()).setScore(next);
            plugin.getConfigManager().setOverride(setting.getKey(), next);
        }
        ConfigGUI.populate(event.getInventory(), holder.getPage(), plugin);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof GUIHolder
                || event.getInventory().getHolder() instanceof RoleGUIHolder
                || event.getInventory().getHolder() instanceof ConfigGUIHolder) {
            event.setCancelled(true);
        }
    }
}
