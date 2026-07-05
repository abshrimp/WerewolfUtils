package com.werewolf.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

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
 */
public class ItemDisguiseManager implements Runnable {

    /** 偽装対象アイテムの custom_data 内 ww.id (SNBT部分一致で判定する) */
    private static final String[] DISGUISED_IDS = {
        "id:\"infection_check\"",
        "id:\"guesser_book\"",
        "id:\"sage_shield\"",
        "id:\"sampler\"",
    };

    /** 偽装対象になり得るアイテム素材 (早期リターン用) */
    private static final Set<Material> DISGUISED_MATERIALS =
        EnumSet.of(Material.PAPER, Material.BOOK, Material.SHIELD, Material.AMETHYST_SHARD);

    @Override
    public void run() {
        for (Player holder : Bukkit.getOnlinePlayers()) {
            ItemStack main = holder.getInventory().getItemInMainHand();
            ItemStack off = holder.getInventory().getItemInOffHand();
            boolean disguiseMain = isDisguised(main);
            boolean disguiseOff = isDisguised(off);
            if (!disguiseMain && !disguiseOff) continue;

            ItemStack fakeMain = disguiseMain ? fake(main) : null;
            ItemStack fakeOff = disguiseOff ? fake(off) : null;
            for (Player viewer : holder.getWorld().getPlayers()) {
                if (viewer.equals(holder)) continue;
                if (disguiseMain) viewer.sendEquipmentChange(holder, EquipmentSlot.HAND, fakeMain);
                if (disguiseOff) viewer.sendEquipmentChange(holder, EquipmentSlot.OFF_HAND, fakeOff);
            }
        }
    }

    private static boolean isDisguised(ItemStack item) {
        if (item == null || !DISGUISED_MATERIALS.contains(item.getType()) || !item.hasItemMeta()) return false;
        String snbt = item.getItemMeta().getAsString();
        if (!snbt.contains("custom_data")) return false;
        for (String id : DISGUISED_IDS) {
            if (snbt.contains(id)) return true;
        }
        return false;
    }

    private static ItemStack fake(ItemStack real) {
        return new ItemStack(Material.COOKED_BEEF, Math.max(1, real.getAmount()));
    }
}
