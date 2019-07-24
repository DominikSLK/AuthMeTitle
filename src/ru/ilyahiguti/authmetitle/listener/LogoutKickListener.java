package ru.ilyahiguti.authmetitle.listener;

import fr.xephi.authme.events.LogoutEvent;
import fr.xephi.authme.events.UnregisterByPlayerEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class LogoutKickListener implements Listener {
    private final String message;

    public LogoutKickListener(String message) {
        this.message = message;
    }

    @EventHandler
    public void onLogout(LogoutEvent event) {
        event.getPlayer().kickPlayer(message);
    }

    @EventHandler
    public void onUnregister(UnregisterByPlayerEvent event) {
        event.getPlayer().kickPlayer(message);
    }
}
