package ru.ilyahiguti.authmetitle;

import fr.xephi.authme.events.LoginEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class ExperienceListener implements Listener {
    private final int timeout;
    private AuthMeTitle plugin;
    private Map<Player, PlayerExp> expMap;
    private Map<Player, BukkitTask> taskMap = new HashMap<>();

    ExperienceListener(AuthMeTitle plugin, int timeout, boolean getBackExp) {
        this.plugin = plugin;
        if (getBackExp) {
            expMap = new HashMap<>();
        }
        this.timeout = timeout;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (expMap != null) {
            expMap.put(player, new PlayerExp(player));
        }
        taskMap.put(player, new BukkitRunnable() {
            final float timer = ((float) timeout);
            int remainingTime = timeout;

            @Override
            public void run() {
                if (timeout < 0) {
                    cancel();
                }
                float exp = (timer - (timer - remainingTime)) / timer;
                player.setLevel(remainingTime--);
                player.setExp(exp);
            }
        }.runTaskTimer(plugin, 0, 20));
    }

    @EventHandler
    public void onAuth(LoginEvent event) {
        restoreExp(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        restoreExp(event.getPlayer());
    }

    private void restoreExp(Player player) {
        taskMap.remove(player).cancel();
        if (expMap != null) {
            PlayerExp playerExp = expMap.get(player);
            if (playerExp != null) {
                playerExp.getBack();
                expMap.remove(player);
            }
        } else {
            player.setLevel(0);
            player.setExp(0);
        }
    }
}
