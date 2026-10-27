package net.mattwhyy.itemcooldowns.listeners;

import net.mattwhyy.itemcooldowns.ItemCooldownsPlugin;
import net.mattwhyy.itemcooldowns.features.cooldown.ClickSide;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.inventory.ItemStack;

public final class PlayerRiptideListener implements Listener {
    private final ItemCooldownsPlugin plugin;

    public PlayerRiptideListener(ItemCooldownsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRiptide(PlayerRiptideEvent event) {
        Player player = event.getPlayer();
        ItemStack trident = player.getInventory().getItemInMainHand();
        if (trident.getType() != Material.TRIDENT) {
            trident = player.getInventory().getItemInOffHand();
        }
        plugin.cooldowns().use(player, trident, ClickSide.RIGHT);
    }
}
