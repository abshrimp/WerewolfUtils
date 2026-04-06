package com.werewolf.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardWatcher implements Runnable {

    private final WerewolfUtilsPlugin plugin;

    public ScoreboardWatcher(WerewolfUtilsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();

        for (SelectionType type : SelectionType.values()) {
            Objective obj = sb.getObjective(type.getTriggerObjective());
            if (obj == null) continue;

            for (Player player : Bukkit.getOnlinePlayers()) {
                int score = obj.getScore(player).getScore();
                if (score == 1) {
                    obj.getScore(player).setScore(0);
                    PlayerSelectionGUI.open(player, type);
                }
            }
        }

        // Guesser role selection GUI
        Objective roleObj = sb.getObjective("ww_gui_guesser_role");
        if (roleObj != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                int score = roleObj.getScore(player).getScore();
                if (score == 1) {
                    roleObj.getScore(player).setScore(0);
                    RoleSelectionGUI.open(player);
                }
            }
        }
    }
}
