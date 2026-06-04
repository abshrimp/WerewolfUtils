package com.werewolf.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.EnumSet;
import java.util.Set;

/**
 * ゲーム中(#game_state ww_state >= 1)、樽・チェスト・かまどなどの機能ブロックや
 * 額縁の操作を禁止する。許可/禁止は config.yml の allow-interaction で切り替える。
 */
public class InteractionListener implements Listener {

    private final WerewolfUtilsPlugin plugin;

    /** 操作を禁止する機能ブロック。 */
    private static final Set<Material> BLOCKED = EnumSet.of(
        Material.BARREL,
        Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST,
        Material.FURNACE, Material.BLAST_FURNACE, Material.SMOKER,
        Material.CRAFTING_TABLE, Material.ENCHANTING_TABLE, Material.BREWING_STAND,
        Material.GRINDSTONE, Material.CARTOGRAPHY_TABLE, Material.SMITHING_TABLE,
        Material.STONECUTTER, Material.LOOM,
        Material.ANVIL, Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL,
        Material.HOPPER, Material.DISPENSER, Material.DROPPER
    );

    public InteractionListener(WerewolfUtilsPlugin plugin) {
        this.plugin = plugin;
    }

    /** 樽・チェスト・かまどなどの右クリック操作。 */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        Material type = block.getType();
        if (!BLOCKED.contains(type) && !Tag.SHULKER_BOXES.isTagged(type)) return;
        block(event, event.getPlayer());
    }

    /** 額縁へのアイテム設置・回転。 */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!isItemFrame(event.getRightClicked().getType())) return;
        block(event, event.getPlayer());
    }

    /** 額縁からのアイテム取り出し・破壊(プレイヤーによる攻撃)。 */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamageEntity(EntityDamageByEntityEvent event) {
        if (!isItemFrame(event.getEntity().getType())) return;
        if (!(event.getDamager() instanceof Player player)) return;
        block(event, player);
    }

    /** 額縁の破壊(プレイヤー起因)。 */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (!isItemFrame(event.getEntity().getType())) return;
        if (!(event.getRemover() instanceof Player player)) return;
        block(event, player);
    }

    private void block(Cancellable event, Player player) {
        if (player.hasPermission("werewolfutils.bypass.interact")) return;
        if (plugin.getConfig().getBoolean("allow-interaction", false)) return;
        if (!isGameRunning()) return;
        event.setCancelled(true);
    }

    private static boolean isItemFrame(EntityType type) {
        return type == EntityType.ITEM_FRAME || type == EntityType.GLOW_ITEM_FRAME;
    }

    private static boolean isGameRunning() {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective state = sb.getObjective("ww_state");
        return state != null && state.getScore("#game_state").getScore() >= 1;
    }
}
