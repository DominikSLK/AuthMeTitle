package ru.ilyahiguti.authmetitle.vanish;

import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.ilyahiguti.authmetitle.AuthMeTitle;

import java.util.HashSet;
import java.util.Set;

public final class VanishManager {
    private static IVanish vanish;

    public static void init(AuthMeTitle plugin, boolean enabled, boolean showAfterLogIn, long delayBeforeShow) {
        if (enabled) {
            if (showAfterLogIn) {
                vanish = new TempVanish(plugin, delayBeforeShow);
            } else {
                vanish = new SimpleVanish();
            }
        } else {
            vanish = null;
        }
    }

    private VanishManager(AuthMeTitle plugin) {
        AuthMeApi authMeApi = AuthMeApi.getInstance();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (authMeApi.isAuthenticated(player)) {
                markAsLoggedIn(player);
            } else {
                markAsUnlogged(player);
            }
        }

        Bukkit.getPluginManager().registerEvents(new QuitListener(), plugin);
    }

    public static void markAsUnlogged(Player player) {
        if (vanish != null) {
            vanish.markAsUnlogged(player);
        }
    }

    public static void markAsLoggedIn(Player player) {
        if (vanish != null) {
            vanish.markAsLoggedIn(player);
        }
    }

    private static void onQuit(Player player) {
        if (vanish != null) {
            vanish.onQuit(player);
        }
    }

    public static class QuitListener implements Listener {
        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            onQuit(event.getPlayer());
        }
    }
}
