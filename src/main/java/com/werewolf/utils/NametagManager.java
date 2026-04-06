package com.werewolf.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class NametagManager implements Runnable {

    // 表示距離（ブロック）
    private static final double VISIBLE_RANGE = 10.0;
    private static final double VISIBLE_RANGE_SQ = VISIBLE_RANGE * VISIBLE_RANGE;
    // プレイヤーの頭上に表示するオフセット（ブロック）
    private static final double DISPLAY_OFFSET_Y = 2.1;

    private final WerewolfUtilsPlugin plugin;
    private final Map<UUID, TextDisplay> displays = new HashMap<>();
    private int tick = 0;

    public NametagManager(WerewolfUtilsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        tick++;

        // ww_players チームの在線プレイヤーを収集
        Set<UUID> inGameIds = new HashSet<>();
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam("ww_players");
        if (team != null) {
            for (String entry : team.getEntries()) {
                Player p = Bukkit.getPlayerExact(entry);
                if (p != null && p.isOnline()) {
                    inGameIds.add(p.getUniqueId());
                }
            }
        }

        // ゲームから抜けたプレイヤーの TextDisplay を削除
        Set<UUID> toRemove = new HashSet<>();
        for (UUID id : displays.keySet()) {
            if (!inGameIds.contains(id)) {
                TextDisplay d = displays.get(id);
                if (!d.isDead()) d.remove();
                toRemove.add(id);
            }
        }
        toRemove.forEach(displays::remove);

        // ゲーム中プレイヤーの TextDisplay を生成または追従移動
        for (UUID id : inGameIds) {
            Player target = Bukkit.getPlayer(id);
            if (target == null) continue;

            Location loc = target.getLocation().clone().add(0, DISPLAY_OFFSET_Y, 0);
            TextDisplay display = displays.get(id);

            if (display == null || display.isDead()) {
                display = spawnDisplay(target, loc);
                displays.put(id, display);
                // 生成直後は全員から非表示にする
                final TextDisplay fd = display;
                for (Player observer : Bukkit.getOnlinePlayers()) {
                    observer.hideEntity(plugin, fd);
                }
            } else {
                display.teleport(loc);
            }
        }

        // 4 tick ごとに距離チェックして表示/非表示を切り替え
        if (tick % 4 == 0) {
            updateVisibility(inGameIds);
        }
    }

    private TextDisplay spawnDisplay(Player player, Location loc) {
        return loc.getWorld().spawn(loc, TextDisplay.class, d -> {
            d.text(Component.text(player.getName()).color(NamedTextColor.WHITE));
            d.setBillboard(Billboard.CENTER);
            d.setShadowed(true);
            d.setPersistent(false);
        });
    }

    private void updateVisibility(Set<UUID> inGameIds) {
        for (Player observer : Bukkit.getOnlinePlayers()) {
            for (UUID targetId : inGameIds) {
                // 自分自身のネームタグは表示しない
                if (targetId.equals(observer.getUniqueId())) continue;

                Player target = Bukkit.getPlayer(targetId);
                if (target == null) continue;

                TextDisplay display = displays.get(targetId);
                if (display == null) continue;

                // 距離、状態、および「壁越しでないか（視線が通っているか）」を判定
                boolean inRange = !target.isSneaking()
                    && !target.isInvisible()
                    && target.getGameMode() != GameMode.SPECTATOR
                    && target.getWorld().equals(observer.getWorld())
                    && target.getLocation().distanceSquared(observer.getLocation()) <= VISIBLE_RANGE_SQ
                    && observer.hasLineOfSight(target);

                if (inRange) {
                    observer.showEntity(plugin, display);
                } else {
                    observer.hideEntity(plugin, display);
                }
            }
        }
    }

    public void cleanup() {
        for (TextDisplay display : displays.values()) {
            if (!display.isDead()) display.remove();
        }
        displays.clear();
    }
}