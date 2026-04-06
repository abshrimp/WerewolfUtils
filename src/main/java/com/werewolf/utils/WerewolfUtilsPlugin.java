package com.werewolf.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class WerewolfUtilsPlugin extends JavaPlugin {

    private NametagManager nametagManager;

    @Override
    public void onEnable() {
        ensureObjectives();
        Bukkit.getPluginManager().registerEvents(new GUIListener(this), this);
        Bukkit.getPluginManager().registerEvents(new TotemListener(), this);
        Bukkit.getPluginManager().registerEvents(new DropListener(), this);
        Bukkit.getScheduler().runTaskTimer(this, new ScoreboardWatcher(this), 1L, 1L);
        nametagManager = new NametagManager(this);
        Bukkit.getScheduler().runTaskTimer(this, nametagManager, 1L, 1L);
        getLogger().info("WerewolfUtils enabled");
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
    }
}
