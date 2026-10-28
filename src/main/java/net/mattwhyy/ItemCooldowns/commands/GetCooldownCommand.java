package net.mattwhyy.itemcooldowns.commands;

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

public final class GetCooldownCommand implements CommandExecutor, TabCompleter {
    private final ItemCooldownsPlugin plugin;

    public GetCooldownCommand(ItemCooldownsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("itemcooldowns.get")) {
            sender.sendMessage(Messages.text(Messages.NO_PERMISSION));
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(Messages.text(Messages.GET_USAGE));
            return true;
        }
        Material material = CooldownRule.materialOf(args[0]);
        if (material == null) {
            sender.sendMessage(Messages.text(Messages.BAD_ITEM));
            return true;
        }
        CooldownRule found = null;
        for (CooldownRule rule : plugin.rules().rules()) {
            if (rule.material() == material && !rule.specific()) {
                found = rule;
                break;
            }
        }
        if (found == null) {
            sender.sendMessage(Messages.text(Messages.GET_NONE, "item", material.name().toLowerCase(Locale.ROOT)));
            return true;
        }
        sender.sendMessage(Messages.text(
                Messages.GET_OK,
                "item", material.name().toLowerCase(Locale.ROOT),
                "seconds", String.valueOf(found.seconds())));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("itemcooldowns.get") || args.length != 1) {
            return List.of();
        }
        return SetCooldownCommand.materialHints(args[0]);
    }
}
