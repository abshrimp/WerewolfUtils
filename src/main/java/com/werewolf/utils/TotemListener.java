package com.werewolf.utils;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class TotemListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        double health = player.getHealth();
        double damage = event.getFinalDamage();

        if (health - damage > 0) return;

        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();

        // Check knight prayer first (same priority as datapack's try_kill)
        Objective prayerObj = sb.getObjective("ww_knight_prayer");
        if (prayerObj != null) {
            int prayer = prayerObj.getScore(player).getScore();
            if (prayer >= 1) {
                event.setCancelled(true);
                prayerObj.getScore(player).setScore(0);
                player.setHealth(player.getMaxHealth());
                player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
                player.sendMessage(
                    Component.text("[騎士の祈り]", NamedTextColor.GOLD)
                        .append(Component.text(" 即死ダメージを防ぎました！",
                            NamedTextColor.YELLOW).decoration(TextDecoration.BOLD, true))
                );
                return;
            }
        }

        // Check knight blessing
        Objective blessingObj = sb.getObjective("ww_knight_blessing");
        if (blessingObj != null) {
            int blessing = blessingObj.getScore(player).getScore();
            if (blessing >= 1) {
                event.setCancelled(true);
                blessingObj.getScore(player).setScore(0);
                player.setHealth(player.getMaxHealth());
                player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.0f);
                player.sendMessage(
                    Component.text("[騎士の加護]", NamedTextColor.GOLD)
                        .append(Component.text(" 騎士の加護の効果が発動しました！",
                            NamedTextColor.YELLOW).decoration(TextDecoration.BOLD, true))
                );
                return;
            }
        }
    }
}
