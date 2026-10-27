package net.mattwhyy.itemcooldowns.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.mattwhyy.itemcooldowns.ItemCooldownsPlugin;
import net.mattwhyy.itemcooldowns.Messages;
import net.mattwhyy.itemcooldowns.features.cooldown.CooldownRule;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public final class ResetCooldownCommand implements CommandExecutor, TabCompleter {
    private final ItemCooldownsPlugin plugin;

    public ResetCooldownCommand(ItemCooldownsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("itemcooldowns.reset")) {
            sender.sendMessage(Messages.text(Messages.NO_PERMISSION));
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(Messages.text(Messages.RESET_USAGE));
            return true;
        }
        if (args[0].equalsIgnoreCase("all")) {
            plugin.rules().clear();
            sender.sendMessage(Messages.text(Messages.RESET_ALL));
            return true;
        }
        Material material = CooldownRule.materialOf(args[0]);
        if (material == null) {
            sender.sendMessage(Messages.text(Messages.BAD_ITEM));
            return true;
        }
        List<CooldownRule> snapshot = plugin.rules().rules();
        boolean removed = false;
        for (int i = snapshot.size() - 1; i >= 0; i--) {
            if (snapshot.get(i).material() == material) {
                plugin.rules().delete(i);
                removed = true;
            }
        }
        if (removed) {
            sender.sendMessage(Messages.text(Messages.RESET_ONE, "item", material.name().toLowerCase(Locale.ROOT)));
        } else {
            sender.sendMessage(Messages.text(Messages.GET_NONE, "item", material.name().toLowerCase(Locale.ROOT)));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("itemcooldowns.reset") || args.length != 1) {
            return List.of();
        }
        List<String> options = new ArrayList<>();
        if ("all".startsWith(args[0].toLowerCase(Locale.ROOT))) {
            options.add("all");
        }
        options.addAll(SetCooldownCommand.materialHints(args[0]));
        return options;
    }
}
