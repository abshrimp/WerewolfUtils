package com.werewolf.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class WerewolfUtilsPlugin extends JavaPlugin {

    private NametagManager nametagManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        ensureObjectives();
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        configManager.load();
        configManager.applyToScoreboard();
        Bukkit.getPluginManager().registerEvents(new GUIListener(this), this);
        Bukkit.getPluginManager().registerEvents(new TotemListener(), this);
        Bukkit.getPluginManager().registerEvents(new DropListener(), this);
        Bukkit.getPluginManager().registerEvents(new InteractionListener(this), this);
        Bukkit.getScheduler().runTaskTimer(this, new ScoreboardWatcher(this), 1L, 1L);
        nametagManager = new NametagManager(this);
        Bukkit.getScheduler().runTaskTimer(this, nametagManager, 1L, 1L);
        if (getCommand("wwconfig") != null) {
            getCommand("wwconfig").setExecutor(new ConfigCommand(this));
        }
        GameCommand gameCommand = new GameCommand(this);
        if (getCommand("wwstart") != null) {
            getCommand("wwstart").setExecutor(gameCommand);
        }
        if (getCommand("wwend") != null) {
            getCommand("wwend").setExecutor(gameCommand);
        }
        getLogger().info("WerewolfUtils enabled");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public void onDisable() {
        if (nametagManager != null) {
            nametagManager.cleanup();
        }
    }

    private void ensureObjectives() {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        for (SelectionType type : SelectionType.values()) {
            String name = type.getTriggerObjective();
            if (sb.getObjective(name) == null) {
                sb.registerNewObjective(name, "dummy", name);
            }
        }
        // Guesser role selection GUI trigger
        if (sb.getObjective("ww_gui_guesser_role") == null) {
            sb.registerNewObjective("ww_gui_guesser_role", "dummy", "ww_gui_guesser_role");
        }
        // 設定再適用トリガー
        if (sb.getObjective("ww_gui_config") == null) {
            sb.registerNewObjective("ww_gui_config", "dummy", "ww_gui_config");
        }
    }
}
