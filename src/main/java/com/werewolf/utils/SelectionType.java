package com.werewolf.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public enum SelectionType {
    FORTUNE(
        "ww_gui_fortune",
        "ww_fortune_target",
        Component.text("===== 占いの対象を選択 =====").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD),
        "ww_fortune_self",
        true
    ),
    BLESSING(
        "ww_gui_blessing",
        "ww_blessing_target",
        Component.text("===== 騎士の加護の対象を選択 =====").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD),
        "ww_blessing_self",
        false
    ),
    GUESSER(
        "ww_gui_guesser",
        "ww_guesser_target",
        Component.text("===== 推理の対象を選択 =====").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD),
        "ww_guesser_self",
        true
    ),
    ZOMBIE(
        "ww_gui_zombie",
        "ww_zombie_target",
        Component.text("===== 最初の感染源を選択 =====").color(NamedTextColor.DARK_GREEN).decorate(TextDecoration.BOLD),
        "ww_zombie_self",
        false
    );

    private final String triggerObjective;
    private final String targetObjective;
    private final Component title;
    private final String selfTag;
    private final boolean includeDead;

    SelectionType(String triggerObjective, String targetObjective, Component title, String selfTag, boolean includeDead) {
        this.triggerObjective = triggerObjective;
        this.targetObjective = targetObjective;
        this.title = title;
        this.selfTag = selfTag;
        this.includeDead = includeDead;
    }

    public String getTriggerObjective() { return triggerObjective; }
    public String getTargetObjective() { return targetObjective; }
    public Component getTitle() { return title; }
    public String getSelfTag() { return selfTag; }
    public boolean includeDead() { return includeDead; }
}
