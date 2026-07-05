package com.werewolf.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 役職固有アイテム(感染確認/ゲッサーの書/賢者の盾/サンプラー)の見た目制御。
 *
 * アイテムの実体はデータパック側で cooked_beef (ステーキ) として配布されるため、
 * 他のプレイヤーからは Java版・統合版(Geyser) を問わず常にステーキに見える。
 * このクラスは「所持者本人」のクライアントにだけ、インベントリスロット同期パケット
 * (ClientboundContainerSetSlotPacket) を送り、本来のアイテム(紙・本・盾・アメジストの欠片)
 * の見た目で表示させる。スロット同期はGeyserがインベントリ補正で常用する経路のため、
 * 統合版の所持者にも反映される。
 *
 * 以前の実装(他人向けに sendEquipmentChange で偽装)は、統合版クライアントが
 * リモートプレイヤーの装備上書きパケットを反映しないため廃止した。
 *
 * 注意: runTaskTimer のタスクは run() から例外が漏れると恒久的にキャンセルされるため、
 * プレイヤー単位で例外を握りつぶし、発生時は1分に1回だけ警告ログを出す。
 */
public class ItemDisguiseManager implements Runnable {

    /** custom_data 内 ww.id → 所持者に見せる本来のアイテム。
     *  盾(SHIELD)は不可: クライアントが「構える」動作をしてしまい右クリック使用が発動しないため、
     *  賢者の盾は使用動作を持たないオウムガイの殻で表示する。 */
    private static final Map<String, Material> TRUE_LOOKS = Map.of(
        "infection_check", Material.PAPER,
        "guesser_book", Material.BOOK,
        "sage_shield", Material.NAUTILUS_SHELL,
        "sampler", Material.AMETHYST_SHARD
    );

    private static final long LOG_INTERVAL_MS = 60_000L;
    /** 変化がなくても再送するハートビート間隔 (tick) */
    private static final int HEARTBEAT_TICKS = 20;

    private final JavaPlugin plugin;
    /** プレイヤーごとの前回送信内容のフィンガープリント (0=未送信/要再送) */
    private final Map<UUID, Integer> lastSent = new HashMap<>();
    private long lastErrorLogAt = 0L;
    private int tick = 0;

    public ItemDisguiseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        if (!SlotPacket.AVAILABLE) {
            plugin.getLogger().warning("ItemDisguiseManager: NMSパケット初期化に失敗したため、所持者向けの見た目復元を無効化します: " + SlotPacket.INIT_ERROR);
        }
    }

    @Override
    public void run() {
        if (!SlotPacket.AVAILABLE) return;
        tick++;
        boolean heartbeat = tick % HEARTBEAT_TICKS == 0;
        lastSent.keySet().removeIf(id -> Bukkit.getPlayer(id) == null);
        for (Player p : Bukkit.getOnlinePlayers()) {
            try {
                revealToHolder(p, heartbeat);
            } catch (Exception e) {
                logThrottled(e);
            }
        }
    }

    private void revealToHolder(Player p, boolean heartbeat) throws Exception {
        // チェスト・取引などのGUIを開いている間はスロット番号が変わるため送らない。
        // フィンガープリントを消しておき、閉じた直後に必ず再送させる。
        if (p.getOpenInventory().getType() != InventoryType.CRAFTING) {
            lastSent.remove(p.getUniqueId());
            return;
        }

        // アイテム使用中(食べる・構える等)はスロット再送がクライアントの使用状態を
        // 中断してしまうため送らない (使用終了後のtickで再送される)
        if (p.isHandRaised()) return;

        PlayerInventory inv = p.getInventory();
        int fingerprint = 1;
        // 対象スロットを収集 (メインインベントリ 0-35 とオフハンド)
        Material[] looks = new Material[37];
        for (int slot = 0; slot <= 35; slot++) {
            Material look = trueLook(inv.getItem(slot));
            looks[slot] = look;
            if (look != null) {
                ItemStack item = inv.getItem(slot);
                fingerprint = fingerprint * 31 + slot;
                fingerprint = fingerprint * 31 + look.ordinal();
                fingerprint = fingerprint * 31 + item.getAmount();
            }
        }
        Material offLook = trueLook(inv.getItemInOffHand());
        looks[36] = offLook;
        if (offLook != null) {
            fingerprint = fingerprint * 31 + 36;
            fingerprint = fingerprint * 31 + offLook.ordinal();
            fingerprint = fingerprint * 31 + inv.getItemInOffHand().getAmount();
        }

        if (fingerprint == 1) {
            lastSent.remove(p.getUniqueId());
            return;
        }
        Integer prev = lastSent.get(p.getUniqueId());
        if (!heartbeat && prev != null && prev == fingerprint) return;
        lastSent.put(p.getUniqueId(), fingerprint);

        for (int slot = 0; slot <= 35; slot++) {
            if (looks[slot] == null) continue;
            // network slot: ホットバー(0-8)→36-44、メイン(9-35)→そのまま
            int netSlot = slot < 9 ? 36 + slot : slot;
            SlotPacket.send(p, netSlot, inv.getItem(slot).withType(looks[slot]));
        }
        if (offLook != null) {
            SlotPacket.send(p, 45, inv.getItemInOffHand().withType(offLook));
        }
    }

    private static Material trueLook(ItemStack item) {
        if (item == null || item.getType() != Material.COOKED_BEEF || !item.hasItemMeta()) return null;
        String snbt = item.getItemMeta().getAsString();
        if (!snbt.contains("custom_data")) return null;
        for (Map.Entry<String, Material> e : TRUE_LOOKS.entrySet()) {
            String id = e.getKey();
            // SNBTのクォート形式差 (id:"x" / id:'x' / id:x) に依存しないよう部分一致で判定
            if (snbt.contains("id:\"" + id + "\"") || snbt.contains("id:'" + id + "'") || snbt.contains("id:" + id)) {
                return e.getValue();
            }
        }
        return null;
    }

    private void logThrottled(Exception e) {
        long now = System.currentTimeMillis();
        if (now - lastErrorLogAt < LOG_INTERVAL_MS) return;
        lastErrorLogAt = now;
        plugin.getLogger().warning("ItemDisguiseManager: スロット偽装パケット送信に失敗 (継続します): " + e);
    }

    /**
     * ClientboundContainerSetSlotPacket をリフレクションで送信するヘルパー。
     * paper-api のみに依存してビルドするため、NMS(Mojangマッピング)へは実行時に
     * リフレクションでアクセスする (Paper 1.20.5+ はランタイムがMojangマッピング)。
     */
    private static final class SlotPacket {
        static final boolean AVAILABLE;
        static final String INIT_ERROR;

        private static Method getHandle;        // CraftPlayer#getHandle -> ServerPlayer
        private static Field connectionField;   // ServerPlayer.connection
        private static Method sendMethod;       // ServerCommonPacketListenerImpl#send(Packet)
        private static Field inventoryMenuField;// ServerPlayer.inventoryMenu
        private static Field containerIdField;  // AbstractContainerMenu.containerId
        private static Method getStateId;       // AbstractContainerMenu#getStateId()
        private static Constructor<?> packetCtor; // ClientboundContainerSetSlotPacket(int,int,int,ItemStack)
        private static Method asNmsCopy;        // CraftItemStack.asNMSCopy(ItemStack)

        static {
            boolean ok = false;
            String err = "";
            try {
                Class<?> serverPlayer = Class.forName("net.minecraft.server.level.ServerPlayer");
                Class<?> menuClass = Class.forName("net.minecraft.world.inventory.AbstractContainerMenu");
                Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket");
                Class<?> packetBase = Class.forName("net.minecraft.network.protocol.Packet");
                Class<?> nmsItem = Class.forName("net.minecraft.world.item.ItemStack");

                connectionField = serverPlayer.getField("connection");
                inventoryMenuField = serverPlayer.getField("inventoryMenu");
                containerIdField = menuClass.getField("containerId");
                getStateId = menuClass.getMethod("getStateId");
                packetCtor = packetClass.getConstructor(int.class, int.class, int.class, nmsItem);
                sendMethod = connectionField.getType().getMethod("send", packetBase);

                // org.bukkit.craftbukkit(.vX_XX_RX)?.inventory.CraftItemStack
                String craftPkg = Bukkit.getServer().getClass().getPackage().getName();
                Class<?> craftItemStack = Class.forName(craftPkg + ".inventory.CraftItemStack");
                asNmsCopy = craftItemStack.getMethod("asNMSCopy", ItemStack.class);

                ok = true;
            } catch (Exception e) {
                err = e.toString();
            }
            AVAILABLE = ok;
            INIT_ERROR = err;
        }

        static void send(Player player, int netSlot, ItemStack fakeItem) throws Exception {
            if (getHandle == null) {
                getHandle = player.getClass().getMethod("getHandle");
            }
            Object handle = getHandle.invoke(player);
            Object menu = inventoryMenuField.get(handle);
            int containerId = containerIdField.getInt(menu);
            int stateId = (int) getStateId.invoke(menu);
            Object nms = asNmsCopy.invoke(null, fakeItem);
            Object packet = packetCtor.newInstance(containerId, stateId, netSlot, nms);
            sendMethod.invoke(connectionField.get(handle), packet);
        }
    }
}
