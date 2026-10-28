package net.mattwhyy.itemcooldowns.features.cooldown;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.mattwhyy.itemcooldowns.ItemCooldownsPlugin;
import net.mattwhyy.itemcooldowns.Messages;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class CooldownService {
    private final ItemCooldownsPlugin plugin;
    private final Map<UUID, Map<String, Long>> readyAt = new HashMap<>();

    public CooldownService(ItemCooldownsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean allow(Player player, ItemStack stack, ClickSide side) {
        CooldownRule rule = plugin.rules().best(stack);
        if (rule == null || rule.seconds(side) <= 0) {
            return true;
        }
        long left = remainingMillis(player, rule, side);
        if (left <= 0) {
            return true;
        }
        tell(player, left);
        return false;
    }

    public void start(Player player, ItemStack stack, ClickSide side) {
        CooldownRule rule = plugin.rules().best(stack);
        if (rule == null) {
            return;
        }
        int seconds = rule.seconds(side);
        if (seconds <= 0) {
            return;
        }
        long until = System.currentTimeMillis() + seconds * 1000L;
        readyAt.computeIfAbsent(player.getUniqueId(), ignored -> new HashMap<>()).put(key(rule, side), until);
        boolean vanilla = !rule.specific() && oneSided(rule);
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            if (rule.group() == CooldownRule.Group.SPEAR) {
                for (String key : rule.enabled()) {
                    Material material = CooldownRule.materialOf(key);
                    if (material != null) {
                        player.setCooldown(material, seconds * 20);
                    }
                }
                return;
            }
            if (vanilla) {
                player.setCooldown(rule.material(), seconds * 20);
            }
        });
    }

    public boolean use(Player player, ItemStack stack, ClickSide side) {
        if (!allow(player, stack, side)) {
            return false;
        }
        start(player, stack, side);
        return true;
    }

    public void forget(UUID playerId) {
        readyAt.remove(playerId);
    }

    private boolean oneSided(CooldownRule rule) {
        return rule.leftSeconds() == 0 || rule.rightSeconds() == 0;
    }

    private long remainingMillis(Player player, CooldownRule rule, ClickSide side) {
        Map<String, Long> times = readyAt.get(player.getUniqueId());
        if (times == null) {
            return 0L;
        }
        Long until = times.get(key(rule, side));
        if (until == null) {
            return 0L;
        }
        return until - System.currentTimeMillis();
    }

    private static String key(CooldownRule rule, ClickSide side) {
        return rule.id() + ":" + side.name();
    }

    private static void tell(Player player, long remainingMillis) {
        double seconds = Math.max(0.1d, remainingMillis / 1000d);
        String shown = seconds >= 10 ? String.valueOf((int) Math.ceil(seconds)) : String.format("%.1f", seconds);
        Component text = Messages.text(Messages.ON_COOLDOWN, "seconds", shown);
        player.sendActionBar(text);
    }
}
