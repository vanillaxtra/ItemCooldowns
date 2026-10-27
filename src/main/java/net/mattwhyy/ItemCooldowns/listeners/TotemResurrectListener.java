package net.mattwhyy.itemcooldowns.listeners;

import net.mattwhyy.itemcooldowns.ItemCooldownsPlugin;
import net.mattwhyy.itemcooldowns.features.cooldown.ClickSide;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;

public final class TotemResurrectListener implements Listener {
    private final ItemCooldownsPlugin plugin;

    public TotemResurrectListener(ItemCooldownsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onResurrect(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        ItemStack totem = hand(player.getInventory().getItemInMainHand(), player.getInventory().getItemInOffHand());
        if (!plugin.cooldowns().use(player, totem, ClickSide.RIGHT)) {
            event.setCancelled(true);
        }
    }

    private static ItemStack hand(ItemStack main, ItemStack off) {
        if (main.getType() == Material.TOTEM_OF_UNDYING) {
            return main;
        }
        if (off.getType() == Material.TOTEM_OF_UNDYING) {
            return off;
        }
        return new ItemStack(Material.TOTEM_OF_UNDYING);
    }
}
