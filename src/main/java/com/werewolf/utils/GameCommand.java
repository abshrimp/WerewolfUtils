package com.werewolf.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * OP向けのゲーム操作コマンド。データパックの werewolf:game/* 関数を実行する。
 *  - /wwstart : ゲーム開始
 *  - /wwend   : ゲーム終了
 */
public class GameCommand implements CommandExecutor {

    private final WerewolfUtilsPlugin plugin;

    public GameCommand(WerewolfUtilsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player && !player.isOp() && !player.hasPermission("werewolfutils.game")) {
            player.sendMessage(Component.text("このコマンドを使う権限がありません").color(NamedTextColor.RED));
            return true;
        }

        String name = command.getName().toLowerCase();
        switch (name) {
            case "wwstart" -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "function werewolf:game/start");
                sender.sendMessage(Component.text("[WerewolfUtils] ").color(NamedTextColor.GOLD)
                    .append(Component.text("ゲームを開始しました").color(NamedTextColor.YELLOW)));
            }
            case "wwend" -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "function werewolf:game/end");
                sender.sendMessage(Component.text("[WerewolfUtils] ").color(NamedTextColor.GOLD)
                    .append(Component.text("ゲームを終了しました").color(NamedTextColor.YELLOW)));
            }
            default -> {
                return false;
            }
        }
        return true;
    }
}
