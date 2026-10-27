package net.mattwhyy.itemcooldowns.features.gui;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public final class MenuSounds {
    private MenuSounds() {
    }

    public static void open(Player player) {
        play(player, Sound.BLOCK_BARREL_OPEN);
    }

    public static void close(Player player) {
        play(player, Sound.BLOCK_BARREL_CLOSE);
    }

    public static void click(Player player) {
        play(player, Sound.UI_BUTTON_CLICK);
    }

    public static void page(Player player) {
        play(player, Sound.ITEM_BOOK_PAGE_TURN);
    }

    public static void save(Player player) {
        play(player, Sound.BLOCK_SMITHING_TABLE_USE);
    }

    public static void delete(Player player) {
        play(player, Sound.ENTITY_ITEM_BREAK);
    }

    public static void confirm(Player player) {
        play(player, Sound.BLOCK_CHEST_LOCKED);
    }

    public static void toggleOn(Player player) {
        play(player, Sound.BLOCK_STONE_BUTTON_CLICK_ON);
    }

    public static void toggleOff(Player player) {
        play(player, Sound.BLOCK_STONE_BUTTON_CLICK_OFF);
    }

    public static void place(Player player) {
        play(player, Sound.ENTITY_ITEM_FRAME_ADD_ITEM);
    }

    public static void clear(Player player) {
        play(player, Sound.BLOCK_GRINDSTONE_USE);
    }

    public static void deny(Player player) {
        play(player, Sound.ENTITY_VILLAGER_NO);
    }

    private static void play(Player player, Sound sound) {
        player.playSound(player.getLocation(), sound, 1f, 1f);
    }
}
