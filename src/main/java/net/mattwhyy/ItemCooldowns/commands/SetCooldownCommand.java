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

public final class SetCooldownCommand implements CommandExecutor, TabCompleter {
    private final ItemCooldownsPlugin plugin;

    public SetCooldownCommand(ItemCooldownsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("itemcooldowns.set")) {
            sender.sendMessage(Messages.text(Messages.NO_PERMISSION));
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(Messages.text(Messages.SET_USAGE));
            return true;
        }
        Material material = CooldownRule.materialOf(args[0]);
        int seconds;
        try {
            seconds = Integer.parseInt(args[1]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(Messages.text(Messages.SET_USAGE));
            return true;
        }
        if (material == null || seconds < 0) {
            sender.sendMessage(Messages.text(Messages.BAD_ITEM));
            return true;
        }
        CooldownRule rule = new CooldownRule(
                null,
                material,
                null,
                null,
                null,
                CooldownRule.leftOf(material, seconds),
                CooldownRule.rightOf(material, seconds));
        int existing = plugin.rules().indexOfFilter(rule.filter());
        if (existing >= 0) {
            CooldownRule old = plugin.rules().get(existing);
            plugin.rules().replace(existing, new CooldownRule(
                    old.id(),
                    material,
                    null,
                    null,
                    null,
                    CooldownRule.leftOf(material, seconds),
                    CooldownRule.rightOf(material, seconds)));
        } else {
            plugin.rules().add(rule);
        }
        sender.sendMessage(Messages.text(Messages.SET_OK, "item", material.name().toLowerCase(Locale.ROOT), "seconds", String.valueOf(seconds)));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("itemcooldowns.set")) {
            return List.of();
        }
        if (args.length == 1) {
            return materialHints(args[0]);
        }
        return List.of();
    }

    static List<String> materialHints(String prefix) {
        String needle = prefix.toLowerCase(Locale.ROOT);
        List<String> options = new ArrayList<>();
        for (Material material : Material.values()) {
            if (!material.isItem() || material.isLegacy()) {
                continue;
            }
            String name = material.name().toLowerCase(Locale.ROOT);
            if (name.startsWith(needle)) {
                options.add(name);
            }
            if (options.size() >= 20) {
                break;
            }
        }
        return options;
    }
}
