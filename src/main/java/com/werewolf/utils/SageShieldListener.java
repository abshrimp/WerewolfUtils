package com.werewolf.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

/**
 * 賢者の盾 (ww_sage_shield が 1 以上のプレイヤー) への物理攻撃を無効化する。
 *
 * データパック側だけでは着弾ダメージ(特に一撃弓の矢)を打ち消せないため、
 * プレイヤー起因のダメージ (直接攻撃・プレイヤーが撃った矢など) をここでキャンセルし、
 * 攻撃者に ww_sg_reflect、賢者に ww_sg_blocked のタグを付ける。
 * 実際の返り討ち処理と通知は次tickにデータパック (tick_main →
 * roles/sage_reflect_pending, roles/sage_block_notify) が行う。
 *
 * スケルトンなどMob起因の攻撃はキャンセルしない (仕様: スケルトンの攻撃は防げない)。
 * ダメージ0の被弾 (スタングレネードの雪玉など) も反射しない。
 *
 * また、賢者の盾アイテムの右クリック発動もここで検知する。
 * consumable + using_item 進捗による発動はクライアントの使用予測に依存し、
 * 見た目偽装 (ItemDisguiseManager) と組み合わせると特に統合版で発動しないことが
 * あるため、PlayerInteractEvent から直接データパックの発動関数を呼ぶ。
 * (進捗経由でも同tickに発動した場合、sage_shield_use 側のガードで二重発動はしない)
 */
public class SageShieldListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        if (!player.getScoreboardTags().contains("ww_alive")) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.COOKED_BEEF || !item.hasItemMeta()) return;
        String snbt = item.getItemMeta().getAsString();
        if (!snbt.contains("custom_data") || !snbt.contains("sage_shield")) return;

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
            "execute as " + player.getUniqueId() + " at @s run function werewolf:roles/sage_shield_use");
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!victim.getScoreboardTags().contains("ww_alive")) return;
        if (event.getDamage() <= 0) return;

        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective shield = sb.getObjective("ww_sage_shield");
        if (shield == null) return;
        if (shield.getScore(victim.getName()).getScore() < 1) return;

        Player attacker = resolvePlayerAttacker(event.getDamager());
        if (attacker == null || attacker.equals(victim)) return;

        event.setCancelled(true);
        if (event.getDamager() instanceof Projectile projectile) {
            projectile.remove();
        }
        attacker.addScoreboardTag("ww_sg_reflect");
        victim.addScoreboardTag("ww_sg_blocked");
    }

    /** 直接攻撃したプレイヤー、またはプレイヤーが放った飛び道具の射手を返す (それ以外は null) */
    private static Player resolvePlayerAttacker(Entity damager) {
        if (damager instanceof Player player) return player;
        if (damager instanceof Projectile projectile
                && projectile.getShooter() instanceof Player shooter) {
            return shooter;
        }
        return null;
    }
}
