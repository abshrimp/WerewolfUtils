package com.werewolf.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /wwconfig コマンド。OP（または werewolfutils.config 権限）に設定GUIを開く。 */
public class ConfigCommand implements CommandExecutor {

    private final WerewolfUtilsPlugin plugin;

    public ConfigCommand(WerewolfUtilsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ使用できます");
            return true;
        }
        if (!player.isOp() && !player.hasPermission("werewolfutils.config")) {
            player.sendMessage(Component.text("この設定GUIを開く権限がありません").color(NamedTextColor.RED));
            return true;
        }
        ConfigGUI.open(player, plugin, 0);
        return true;
    }
}
