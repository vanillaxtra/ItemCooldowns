package net.mattwhyy.itemcooldowns.features.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

public interface MenuHolder extends InventoryHolder {
    void onClick(Player player, InventoryClickEvent event);

    void onDrag(Player player, InventoryDragEvent event);

    static void freePlayerInv(InventoryClickEvent event) {
        int top = event.getView().getTopInventory().getSize();
        if (event.getRawSlot() >= top && event.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            event.setCancelled(false);
        }
    }

    static void freePlayerDrag(InventoryDragEvent event) {
        int top = event.getView().getTopInventory().getSize();
        for (int slot : event.getRawSlots()) {
            if (slot < top) {
                return;
            }
        }
        event.setCancelled(false);
    }

    default void onClose(Player player) {
    }

    void markSilent();

    boolean consumeSilent();
}
