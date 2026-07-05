package com.werewolf.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;
import java.util.Set;

/**
 * 役職固有アイテム(感染確認/ゲッサーの書/賢者の盾/サンプラー)を、
 * 手に持っても所持者以外のプレイヤーにはステーキ(cooked_beef)に見せる偽装処理。
 *
 * 毎tick、対象アイテムを手に持つプレイヤーについて、同ワールドの他プレイヤーへ
 * 偽の装備パケット(sendEquipmentChange)を送信して見た目を上書きする。
 * サーバーが実際の装備を再送信(持ち替え・視界に入った直後など)しても、
 * 次のtickで再び偽装されるため、見えるのは最大1tick(50ms)だけで実用上気付かれない。
 * 所持者自身の見た目はクライアント側のインベントリ情報で描画されるため影響を受けない。
 * GeyserMC経由の統合版クライアントにも SetEquipment → MobEquipmentPacket として翻訳される。
 *
 * 注意: runTaskTimer のタスクは run() から例外が漏れると恒久的にキャンセルされ、
 * 以降ゲームが終わるまで誰も偽装されなくなる (切断処理中のプレイヤーへの送信などで
 * 稀に例外が起き得る)。そのためプレイヤー単位・送信単位で例外を握りつぶし、
 * 発生時は1分に1回だけ警告ログを出す。
 */
public class ItemDisguiseManager implements Runnable {

    /** 偽装対象アイテムの custom_data 内 ww.id */
    private static final String[] DISGUISED_IDS = {
        "infection_check",
        "guesser_book",
        "sage_shield",
        "sampler",
    };

    /** 偽装対象になり得るアイテム素材 (早期リターン用) */
    private static final Set<Material> DISGUISED_MATERIALS =
        EnumSet.of(Material.PAPER, Material.BOOK, Material.SHIELD, Material.AMETHYST_SHARD);

    private static final long LOG_INTERVAL_MS = 60_000L;

    private final JavaPlugin plugin;
    private long lastErrorLogAt = 0L;

    public ItemDisguiseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player holder : Bukkit.getOnlinePlayers()) {
            try {
                disguiseHolder(holder);
            } catch (Exception e) {
                logThrottled(e);
            }
        }
    }

    private void disguiseHolder(Player holder) {
        if (!holder.isOnline()) return;
        ItemStack main = holder.getInventory().getItemInMainHand();
        ItemStack off = holder.getInventory().getItemInOffHand();
        boolean disguiseMain = isDisguised(main);
        boolean disguiseOff = isDisguised(off);
        if (!disguiseMain && !disguiseOff) return;

        ItemStack fakeMain = disguiseMain ? fake(main) : null;
        ItemStack fakeOff = disguiseOff ? fake(off) : null;
        for (Player viewer : holder.getWorld().getPlayers()) {
            if (viewer.equals(holder)) continue;
            try {
                if (disguiseMain) viewer.sendEquipmentChange(holder, EquipmentSlot.HAND, fakeMain);
                if (disguiseOff) viewer.sendEquipmentChange(holder, EquipmentSlot.OFF_HAND, fakeOff);
            } catch (Exception e) {
                // 切断直後のviewerなどで失敗しても、他のviewerへの偽装は続行する
                logThrottled(e);
            }
        }
    }

    private boolean isDisguised(ItemStack item) {
        if (item == null || !DISGUISED_MATERIALS.contains(item.getType()) || !item.hasItemMeta()) return false;
        String snbt = item.getItemMeta().getAsString();
        if (!snbt.contains("custom_data")) return false;
        for (String id : DISGUISED_IDS) {
            // SNBTのクォート形式差 (id:"x" / id:'x' / id:x) に依存しないよう部分一致で判定
            if (snbt.contains("id:\"" + id + "\"") || snbt.contains("id:'" + id + "'") || snbt.contains("id:" + id)) {
                return true;
            }
        }
        return false;
    }

    private static ItemStack fake(ItemStack real) {
        return new ItemStack(Material.COOKED_BEEF, Math.max(1, real.getAmount()));
    }

    private void logThrottled(Exception e) {
        long now = System.currentTimeMillis();
        if (now - lastErrorLogAt < LOG_INTERVAL_MS) return;
        lastErrorLogAt = now;
        plugin.getLogger().warning("ItemDisguiseManager: 偽装パケット送信に失敗 (継続します): " + e);
    }
}
