package net.mattwhyy.itemcooldowns.features.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

public interface MenuHolder extends InventoryHolder {
    void onClick(Player player, InventoryClickEvent event);

    void onDrag(Player player, InventoryDragEvent event);

    void markSilent();

    boolean consumeSilent();
}
