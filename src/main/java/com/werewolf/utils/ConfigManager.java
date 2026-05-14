package com.werewolf.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.io.IOException;

/**
 * GUIで変更した設定を config.yml に永続化し、データパック再読み込み時に再適用する。
 * config/default.mcfunction で設定されるデフォルト値の「上書き」として動作する。
 */
public class ConfigManager {

    private final WerewolfUtilsPlugin plugin;
    private File file;
    private FileConfiguration cfg;

    public ConfigManager(WerewolfUtilsPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("config.yml の作成に失敗しました: " + e.getMessage());
            }
        }
        cfg = YamlConfiguration.loadConfiguration(file);
    }

    /** GUIで変更された設定値を上書きとして保存する。scoreKey は "#day_duration" 形式。 */
    public void setOverride(String scoreKey, int value) {
        cfg.set("overrides." + scoreKey.replace("#", ""), value);
        save();
    }

    /** すべての上書きを削除する（デフォルトに戻す）。 */
    public void clearAll() {
        cfg.set("overrides", null);
        save();
    }

    /** 保存済みの上書き値を ww_config スコアボードに適用する。 */
    public void applyToScoreboard() {
        ConfigurationSection sec = cfg.getConfigurationSection("overrides");
        if (sec == null) return;
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective config = sb.getObjective("ww_config");
        if (config == null) return;
        for (String k : sec.getKeys(false)) {
            config.getScore("#" + k).setScore(sec.getInt(k));
        }
    }

    private void save() {
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("config.yml の保存に失敗しました: " + e.getMessage());
        }
    }
}
