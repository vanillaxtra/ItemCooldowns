package net.mattwhyy.itemcooldowns.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.mattwhyy.itemcooldowns.ItemCooldownsPlugin;
import net.mattwhyy.itemcooldowns.Messages;
import net.mattwhyy.itemcooldowns.features.gui.RuleListMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class ItemCooldownsCommand implements CommandExecutor, TabCompleter {
    private static final String ADMIN = "itemcooldowns.admin";

    private final ItemCooldownsPlugin plugin;

    public ItemCooldownsCommand(ItemCooldownsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(ADMIN)) {
            sender.sendMessage(Messages.text(Messages.NO_PERMISSION));
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadSettings();
            sender.sendMessage(Messages.text(Messages.RELOADED));
            return true;
        }
        if (args.length > 0) {
            sender.sendMessage(Messages.text(Messages.USAGE));
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Messages.text(Messages.PLAYERS_ONLY));
            return true;
        }
        RuleListMenu.open(plugin, player, 0, true);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(ADMIN) || args.length != 1) {
            return List.of();
        }
        String prefix = args[0].toLowerCase(Locale.ROOT);
        List<String> options = new ArrayList<>();
        if ("reload".startsWith(prefix)) {
            options.add("reload");
        }
        return options;
    }
}
