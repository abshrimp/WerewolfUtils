package com.werewolf.utils;

import org.bukkit.Material;

/**
 * WerewolfQuestデータパックの ww_config 設定項目を定義する列挙型。
 * GUIで編集できる各設定の表示名・編集範囲・配置を保持する。
 */
public enum ConfigSetting {
    // ===== ページ0: 基本設定 / 闇鍋モード =====
    FIRST_DAY_DURATION("#first_day_duration", "初日の昼の長さ", Type.NUMERIC, 0, 72000, 100, 1000, Material.WHITE_DYE, 0, 10, "初日の昼の長さ (tick)"),
    DAY_DURATION("#day_duration", "昼の長さ", Type.NUMERIC, 0, 72000, 100, 1000, Material.YELLOW_DYE, 0, 11, "通常の昼の長さ (tick)"),
    NIGHT_DURATION("#night_duration", "夜の長さ", Type.NUMERIC, 0, 72000, 100, 1000, Material.BLUE_DYE, 0, 12, "夜の長さ (tick)"),
    MAX_HEALTH("#max_health", "最大体力", Type.NUMERIC, 2, 200, 2, 10, Material.GOLDEN_APPLE, 0, 13, "プレイヤーの最大体力 (HP)"),
    SKEL_PER_PLAYER("#skel_per_player", "1人あたりのスケルトン数", Type.NUMERIC, 0, 200, 1, 10, Material.SKELETON_SKULL, 0, 19, "生存者1人あたりのスケルトン湧き数 (最低40体)"),
    SKEL_EMERALD_REWARD("#skel_emerald_reward", "討伐報酬エメラルド数", Type.NUMERIC, 0, 64, 1, 5, Material.EMERALD, 0, 20, "スケルトン討伐で得られるエメラルド数"),
    SKEL_EMERALD_CHANCE("#skel_emerald_chance", "エメラルド獲得確率", Type.NUMERIC, 0, 100, 5, 25, Material.EMERALD_BLOCK, 0, 21, "スケルトン討伐でエメラルドを得られる確率 (%)"),
    MISSION_ENABLED("#mission_enabled", "ミッションシステム", Type.TOGGLE, 0, 1, 1, 1, Material.FILLED_MAP, 0, 22, "2夜ごとにミッションが発生する"),
    YAMINABE("#yaminabe", "闇鍋モード", Type.TOGGLE, 0, 1, 1, 1, Material.CAULDRON, 0, 30, "闇鍋モードの有効/無効"),
    THIRD_FACTION_CHANCE("#third_faction_chance", "第三陣営出現確率", Type.NUMERIC, 0, 100, 5, 25, Material.NETHER_STAR, 0, 31, "闇鍋で第三陣営が1人でも出現する確率 (%)"),
    THIRD_FACTION_MAX("#third_faction_max", "第三陣営最大人数", Type.NUMERIC, 0, 50, 1, 5, Material.YELLOW_CONCRETE, 0, 32, "闇鍋で第三陣営の最大人数 (0=制限なし)"),

    // ===== ページ1: 役職人数 (村人陣営: 行0-1 / 人狼陣営: 行2 / 第三陣営: 行3) =====
    WOLFPOSSESSED("#wolfpossessed_count", "狼憑き", Type.NUMERIC, 0, 50, 1, 5, Material.LIME_CONCRETE, 1, 1, "狼憑きの人数"),
    FORTUNE_TELLER("#fortune_teller_count", "占い師", Type.NUMERIC, 0, 50, 1, 5, Material.LIME_CONCRETE, 1, 2, "占い師の人数"),
    ELIXIR("#elixir_count", "エリクサー", Type.NUMERIC, 0, 50, 1, 5, Material.LIME_CONCRETE, 1, 3, "エリクサーの人数"),
    GUESSER("#guesser_count", "ゲッサー", Type.NUMERIC, 0, 50, 1, 5, Material.LIME_CONCRETE, 1, 4, "ゲッサーの人数"),
    GAMBLER("#gambler_count", "ギャンブラー", Type.NUMERIC, 0, 50, 1, 5, Material.LIME_CONCRETE, 1, 5, "ギャンブラーの人数"),
    BAKER("#baker_count", "パン屋", Type.NUMERIC, 0, 50, 1, 5, Material.LIME_CONCRETE, 1, 6, "パン屋の人数"),
    SEER("#seer_count", "シーア", Type.NUMERIC, 0, 50, 1, 5, Material.LIME_CONCRETE, 1, 7, "シーアの人数"),
    SAGE("#sage_count", "賢者", Type.NUMERIC, 0, 50, 1, 5, Material.LIME_CONCRETE, 1, 8, "賢者の人数"),
    RESEARCHER("#researcher_count", "研究者", Type.NUMERIC, 0, 50, 1, 5, Material.LIME_CONCRETE, 1, 10, "研究者の人数"),
    WEREWOLF("#werewolf_count", "人狼", Type.NUMERIC, 0, 50, 1, 5, Material.RED_CONCRETE, 1, 19, "人狼の人数 (闇鍋では人狼陣営の人数)"),
    ACCOMPLICE("#accomplice_count", "共犯者", Type.NUMERIC, 0, 50, 1, 5, Material.RED_CONCRETE, 1, 20, "共犯者の人数"),
    EVIL_ELIXIR("#evil_elixir_count", "イビルエリクサー", Type.NUMERIC, 0, 50, 1, 5, Material.RED_CONCRETE, 1, 21, "イビルエリクサーの人数"),
    EVIL_GUESSER("#evil_guesser_count", "イビルゲッサー", Type.NUMERIC, 0, 50, 1, 5, Material.RED_CONCRETE, 1, 22, "イビルゲッサーの人数"),
    TRAPPER("#trapper_count", "トラッパー", Type.NUMERIC, 0, 50, 1, 5, Material.RED_CONCRETE, 1, 23, "トラッパーの人数"),
    CHAMELEON("#chameleon_count", "カメレオン", Type.NUMERIC, 0, 50, 1, 5, Material.RED_CONCRETE, 1, 24, "カメレオンの人数"),
    BOMBER("#bomber_count", "爆弾魔", Type.NUMERIC, 0, 50, 1, 5, Material.RED_CONCRETE, 1, 25, "爆弾魔の人数"),
    ECHO("#echo_count", "エコー", Type.NUMERIC, 0, 50, 1, 5, Material.RED_CONCRETE, 1, 26, "エコーの人数"),
    VAMPIRE("#vampire_count", "吸血鬼", Type.NUMERIC, 0, 50, 1, 5, Material.YELLOW_CONCRETE, 1, 28, "吸血鬼の人数"),
    ZOMBIE("#zombie_count", "ゾンビ", Type.NUMERIC, 0, 50, 1, 5, Material.YELLOW_CONCRETE, 1, 29, "ゾンビの人数"),
    SPELUNKER("#spelunker_count", "スペランカー", Type.NUMERIC, 0, 50, 1, 5, Material.YELLOW_CONCRETE, 1, 30, "スペランカーの人数"),
    SOUL_EATER("#soul_eater_count", "ソウルイーター", Type.NUMERIC, 0, 50, 1, 5, Material.YELLOW_CONCRETE, 1, 31, "ソウルイーターの人数"),
    FORGETTER("#forgetter_count", "忘却者", Type.NUMERIC, 0, 50, 1, 5, Material.YELLOW_CONCRETE, 1, 32, "忘却者の人数");

    public enum Type { NUMERIC, TOGGLE }

    private final String key;
    private final String display;
    private final Type type;
    private final int min;
    private final int max;
    private final int step;
    private final int shiftStep;
    private final Material material;
    private final int page;
    private final int slot;
    private final String description;

    ConfigSetting(String key, String display, Type type, int min, int max, int step, int shiftStep,
                  Material material, int page, int slot, String description) {
        this.key = key;
        this.display = display;
        this.type = type;
        this.min = min;
        this.max = max;
        this.step = step;
        this.shiftStep = shiftStep;
        this.material = material;
        this.page = page;
        this.slot = slot;
        this.description = description;
    }

    public String getKey() { return key; }
    public String getDisplay() { return display; }
    public Type getType() { return type; }
    public int getMin() { return min; }
    public int getMax() { return max; }
    public int getStep() { return step; }
    public int getShiftStep() { return shiftStep; }
    public Material getMaterial() { return material; }
    public int getPage() { return page; }
    public int getSlot() { return slot; }
    public String getDescription() { return description; }
}
