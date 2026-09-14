/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabCompleter
 *  org.bukkit.entity.Player
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package pl.laina.plots.command;

import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.laina.plots.LainaPlotsPlugin;
import pl.laina.plots.gui.PlotsMenuController;
import pl.laina.plots.message.Messages;

public final class PlotsCommand
implements CommandExecutor,
TabCompleter {
    private final LainaPlotsPlugin plugin;
    private final PlotsMenuController menus;
    private final Messages messages;

    public PlotsCommand(LainaPlotsPlugin plugin, PlotsMenuController menus, Messages messages) {
        this.plugin = plugin;
        this.menus = menus;
        this.messages = messages;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("lainaplots.reload")) {
                sender.sendMessage(this.messages.component("no-permission"));
                return true;
            }
            this.plugin.reloadSettings();
            sender.sendMessage(this.messages.component("reloaded"));
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(this.messages.component("players-only"));
            return true;
        }
        Player player = (Player)sender;
        if (!player.hasPermission("lainaplots.use")) {
            this.messages.send(player, "no-permission");
            return true;
        }
        this.menus.open(player);
        return true;
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("lainaplots.reload") && "reload".startsWith(args[0].toLowerCase())) {
            return List.of("reload");
        }
        return List.of();
    }
}

