package com.werewolf.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
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
                runStartInSenderOrConfiguredWorld(sender);
                sender.sendMessage(Component.text("[WerewolfUtils] ").color(NamedTextColor.GOLD)
                    .append(Component.text("ゲームを開始しました").color(NamedTextColor.YELLOW)));
            }
            case "wwend" -> {
                if (hasGameManager()) {
                    Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(),
                        "execute as @e[type=minecraft:marker,tag=ww_game_manager,limit=1] at @s run function werewolf:game/end"
                    );
                } else {
                    World world = resolveConfiguredWorld();
                    if (world != null) {
                        Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            "execute in " + world.getKey().asString() + " run function werewolf:game/end"
                        );
                    } else {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "function werewolf:game/end");
                    }
                }
                sender.sendMessage(Component.text("[WerewolfUtils] ").color(NamedTextColor.GOLD)
                    .append(Component.text("ゲームを終了しました").color(NamedTextColor.YELLOW)));
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    private void runStartInSenderOrConfiguredWorld(CommandSender sender) {
        if (sender instanceof Player player) {
            Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                "execute as " + player.getName() + " at @s run function werewolf:game/start"
            );
            return;
        }

        World world = resolveConfiguredWorld();
        if (world != null) {
            Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                "execute in " + world.getKey().asString() + " run function werewolf:game/start"
            );
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "function werewolf:game/start");
        }
    }

    private boolean hasGameManager() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity.getScoreboardTags().contains("ww_game_manager")) {
                    return true;
                }
            }
        }
        return false;
    }

    private World resolveConfiguredWorld() {
        String configuredWorld = plugin.getConfig().getString("game-world", "world");
        World world = Bukkit.getWorld(configuredWorld);
        if (world != null) {
            return world;
        }
        if (!Bukkit.getWorlds().isEmpty()) {
            return Bukkit.getWorlds().get(0);
        }
        return null;
    }
}
