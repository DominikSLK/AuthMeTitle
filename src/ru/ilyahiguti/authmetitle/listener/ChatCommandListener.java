package ru.ilyahiguti.authmetitle.listener;

import fr.xephi.authme.events.LoginEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.ilyahiguti.authmetitle.AuthMeTitle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ChatCommandListener implements Listener {
    private final AuthMeTitle plugin;
    private List<Player> unAuthPlayers = new ArrayList<>();
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    public ChatCommandListener(AuthMeTitle plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        lock.readLock().lock();
        boolean isAuthed = !unAuthPlayers.contains(event.getPlayer());
        lock.readLock().unlock();

        if (isAuthed) return;

        event.setCancelled(true);
        Bukkit.getScheduler().runTask(plugin, () -> event.getPlayer().performCommand((plugin.getAuthMeApi().isRegistered(event.getPlayer().getName()) ? "login " : "register ") + event.getMessage()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        lock.writeLock().lock();
        unAuthPlayers.add(event.getPlayer());
        lock.writeLock().unlock();
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        lock.writeLock().lock();
        unAuthPlayers.remove(event.getPlayer());
        lock.writeLock().unlock();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lock.writeLock().lock();
        unAuthPlayers.remove(event.getPlayer());
        lock.writeLock().unlock();
    }
}
