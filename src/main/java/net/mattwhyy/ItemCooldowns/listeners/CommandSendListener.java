package net.mattwhyy.itemcooldowns.listeners;

import java.util.Locale;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

public final class CommandSendListener implements Listener {
    @EventHandler
    public void onSend(PlayerCommandSendEvent event) {
        event.getCommands().removeIf(name -> name.toLowerCase(Locale.ROOT).startsWith("itemcooldowns:"));
    }
}
